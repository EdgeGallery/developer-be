package org.edgegallery.developer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.spencerwi.either.Either;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.ReleaseConfigMapper;
import org.edgegallery.developer.model.AppConfigurationModel;
import org.edgegallery.developer.model.DnsRule;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.model.ServiceConfig;
import org.edgegallery.developer.model.ServiceDetail;
import org.edgegallery.developer.model.TrafficRule;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.CompressFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

@Service("releaseConfigService")
public class ReleaseConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseConfigService.class);

    @Autowired
    private ReleaseConfigMapper configMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    public Either<FormatRespDto, ReleaseConfig> saveConfig(String projectId, ReleaseConfig config) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        //update csar
        if (config.getCapabilitiesDetail() != null) {
            Either<FormatRespDto, Boolean> rebuildRes = rebuildCsar(projectId, config);
            if (rebuildRes.isLeft()) {
                return Either.left(rebuildRes.getLeft());
            }
        }
        String releaseId = UUID.randomUUID().toString();
        config.setReleaseId(releaseId);
        config.setProjectId(projectId);
        config.setCreateTime(new Date());
        int res = configMapper.saveConfig(config);
        if (res < 1) {
            LOGGER.error("save config data fail!");
            FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save config data fail");
            return Either.left(dto);
        }
        LOGGER.info("create release config success!");
        return Either.right(configMapper.getConfigByReleaseId(config.getReleaseId()));
    }

    public Either<FormatRespDto, ReleaseConfig> modifyConfig(String projectId, ReleaseConfig config) {
        if (StringUtils.isBlank(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        ReleaseConfig oldConfig = configMapper.getConfigByProjectId(projectId);
        if (oldConfig == null) {
            LOGGER.error("projectId error,can not find any project!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId error!");
            return Either.left(dto);
        }

        //update csar
        if (config.getCapabilitiesDetail() != null) {
            Either<FormatRespDto, Boolean> rebuildRes = rebuildCsar(projectId, config);
            if (rebuildRes.isLeft()) {
                return Either.left(rebuildRes.getLeft());
            }
        }

        config.setReleaseId(oldConfig.getReleaseId());
        config.setProjectId(projectId);
        config.setCreateTime(new Date());

        int res = configMapper.modifyReleaseConfig(config);
        if (res < 1) {
            LOGGER.error("create project {} release-config data fail!", projectId);
            FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save config data fail");
            return Either.left(dto);
        }
        LOGGER.info("modify release config success!");
        return Either.right(configMapper.getConfigByReleaseId(config.getReleaseId()));

    }

    public Either<FormatRespDto, ReleaseConfig> getConfigById(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        ReleaseConfig oldConfig = configMapper.getConfigByProjectId(projectId);
        LOGGER.info("get release config success!");
        return Either.right(oldConfig);
    }

    /**
     * rebuildCsar.
     *
     * @param projectId
     * @param releaseConfig
     */
    public Either<FormatRespDto, Boolean> rebuildCsar(String projectId, ReleaseConfig releaseConfig) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        List<ProjectTestConfig> testConfigs = projectMapper.getTestConfigByProjectId(projectId);
        if (testConfigs == null || testConfigs.size() == 0) {
            LOGGER.error("Project {} has not test config!", projectId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Project has not test config!");
            return Either.left(error);

        }
        ProjectTestConfig config = testConfigs.get(0);
        List<TrafficRule> trafficRules = releaseConfig.getCapabilitiesDetail().getAppTrafficRule();
        List<DnsRule> dnsRules = releaseConfig.getCapabilitiesDetail().getAppDNSRule();
        List<ServiceDetail> details = releaseConfig.getCapabilitiesDetail().getServiceDetails();
        // verify csar file
        String csarFilePath = projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId()
            + ".csar";
        File csar = new File(csarFilePath);
        if (!csar.exists()) {
            LOGGER.error("Cannot find csar file:{}.", csarFilePath);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                "Cannot find csar file: " + csarFilePath);
            return Either.left(error);
        }
        try {
            // decompress csar
            CompressFileUtils.decompress(csarFilePath, csar.getParent());
            // verify service template file
            String mainServiceTemplatePath = csar.getParent() + File.separator + config.getAppInstanceId()
                + File.separator + "APPD/Definition/MainServiceTemplate.yaml";
            File templateFile = new File(mainServiceTemplatePath);
            if (!templateFile.exists()) {
                LOGGER.error("Cannot find service template file.");
                FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                    "Cannot find service template file.");
                return Either.left(error);
            }
            // update node in template
            String yamlContent = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            yamlContent = yamlContent.replaceAll("\t", "");
            Yaml yaml = new Yaml();
            Map<String, Object> loaded = yaml.load(yamlContent);
            // get config node from template
            LinkedHashMap<String, Object> nodeMap = (LinkedHashMap<String, Object>) loaded.get("topology_template");
            LinkedHashMap<String, Object> templateNode = (LinkedHashMap<String, Object>) nodeMap.get("node_templates");
            AppConfigurationModel configModel = buildTemplateConfig(project, trafficRules, dnsRules, details);
            // write yaml
            templateNode.put("app_configuration", configModel);
            // write content to yaml
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.writeValue(templateFile, loaded);
            // update tgz in ~/Charts
            String chartsDir = csar.getParent() + File.separator + config.getAppInstanceId() + File.separator
                + "Artifacts/Deployment/Charts";
            File chartDirFile = new File(chartsDir);
            if (chartDirFile.exists()) {
                File[] tgzFiles = chartDirFile.listFiles();
                if (tgzFiles != null) {
                    for (File tgzFile : tgzFiles) {
                        if (!tgzFile.isFile() || !tgzFile.getName().endsWith(".tgz")) {
                            continue;
                        }
                        fillTemplateInTgzFile(tgzFile, details);
                    }
                }
            }
            // compress csar
            CompressFileUtils.compressToCsarAndDeleteSrc(csar.getParent() + File.separator + config.getAppInstanceId(),
                projectService.getProjectPath(projectId), config.getAppInstanceId());

        } catch (Exception e) {
            LOGGER.error("Update csar failed:{}.", e.getMessage());
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                "Update csar failed:" + e.getMessage());
            return Either.left(error);
        } finally {
            // delete csar dir if exists
            File csarDir = new File(csarFilePath.replace(".csar", ""));
            if (csarDir.exists()) {
                csarDir.delete();
            }
        }
        return Either.right(true);
    }

    /**
     * build appConfigurationConfig data
     *
     * @param project
     * @param trafficRules
     * @param dnsRules
     * @param details
     */
    private AppConfigurationModel buildTemplateConfig(ApplicationProject project, List<TrafficRule> trafficRules,
        List<DnsRule> dnsRules, List<ServiceDetail> details) {
        AppConfigurationModel configModel = new AppConfigurationModel();
        AppConfigurationModel.ConfigurationProperties properties = new AppConfigurationModel.ConfigurationProperties();
        properties.setAppName(project.getName());
        if (!CollectionUtils.isEmpty(details)) {
            properties.setAppServiceProduced(buildProducedServices(details));
        }

        if (!CollectionUtils.isEmpty(dnsRules)) {
            properties.setAppDNSRule(dnsRules);
        }

        if (!CollectionUtils.isEmpty(trafficRules)) {
            properties.setAppTrafficRule(trafficRules);
        }
        if (!CollectionUtils.isEmpty(project.getCapabilityList())) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<OpenMepCapabilityGroup>>() { }.getType();
            List<OpenMepCapabilityGroup> groups = gson.fromJson(gson.toJson(project.getCapabilityList()), type);
            List<AppConfigurationModel.ServiceRequired> requiredList = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            for (Object obj : groups) {
                LinkedTreeMap treeMap = (LinkedTreeMap) obj;
                OpenMepCapabilityGroup group = mapper.convertValue(treeMap, OpenMepCapabilityGroup.class);
                for (OpenMepCapabilityDetail capabilityDetail : group.getCapabilityDetailList()) {
                    AppConfigurationModel.ServiceRequired required = new AppConfigurationModel.ServiceRequired();
                    required.setSerName(capabilityDetail.getService());
                    required.setAppId(capabilityDetail.getAppId());
                    required.setPackageId(capabilityDetail.getPackageId());
                    required.setVersion(capabilityDetail.getVersion());
                    requiredList.add(required);
                }
            }
            properties.setAppServiceRequired(requiredList);
        }
        configModel.setProperties(properties);
        return configModel;
    }

    /**
     * get ServiceProduced list from service Details
     *
     * @param details
     */
    private List<AppConfigurationModel.ServiceProduced> buildProducedServices(List<ServiceDetail> details) {
        return details.stream().map(d -> {
            AppConfigurationModel.ServiceProduced produced = new AppConfigurationModel.ServiceProduced();
            produced.setDnsRuleIdList(d.getDnsRulesList());
            produced.setSerName(d.getServiceName());
            produced.setTrafficRuleIdList(d.getTrafficRulesList());
            produced.setVersion(d.getVersion());
            return produced;
        }).collect(Collectors.toList());
    }

    /**
     * fill value template with detailList
     *
     * @param tgzFile
     * @param detailList
     */
    private void fillTemplateInTgzFile(File tgzFile, List<ServiceDetail> detailList) {
        String fileName = tgzFile.getName().replace(".tgz", "");
        try {
            // decompress tgz
            CompressFileUtils.decompress(tgzFile.getAbsolutePath(), tgzFile.getParent());
            // get template node
            // load valueTemplate.yaml
            String valueTemplatePath = tgzFile.getAbsolutePath().replace(".tgz", "") + File.separator + "values.yaml";
            File valFile = new File(valueTemplatePath);
            if (!valFile.exists()) {
                return;
            }
            // load content from yaml
            String valueContent = FileUtils.readFileToString(valFile, StandardCharsets.UTF_8);
            valueContent = valueContent.replaceAll("\t", "");
            Yaml yaml = new Yaml();
            Map<String, Object> loaded = yaml.load(valueContent);
            // build node template
            // TODO(ch) use HTTP as default
            List<ServiceConfig> configs = detailList.stream()
                .map(t -> new ServiceConfig(t.getServiceName(), t.getInternalPort(), t.getVersion(), "HTTP"))
                .collect(Collectors.toList());
            // update node in template
            loaded.put("serviceconfig", configs);
            // write content to yaml
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.writeValue(valFile, loaded);
            // compress tgz
            CompressFileUtils
                .compressToTgzAndDeleteSrc(tgzFile.getAbsolutePath().replace(".tgz", ""), tgzFile.getParent(),
                    fileName);
        } catch (Exception e) {
            LOGGER.info("FillTemplateInTgzFile failed: {}", e.getMessage());
        } finally {
            // delete decompress dir if exists
            File tmpDir = new File(tgzFile.getParent() + File.separator + fileName);
            if (tmpDir.exists()) {
                tmpDir.delete();
            }
        }
    }

}
