/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.ReleaseConfigMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.AppConfigurationModel;
import org.edgegallery.developer.model.CapabilitiesDetail;
import org.edgegallery.developer.model.DnsRule;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.model.ServiceConfig;
import org.edgegallery.developer.model.ServiceDetail;
import org.edgegallery.developer.model.TrafficRule;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.CompressFileUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Service("releaseConfigService")
public class ReleaseConfigService {

    private static final String TEMPLATE_MD = "/Artifacts/Docs/template.md";

    private static final String CHARTS_TGZ = "/Artifacts/Deployment/Charts";

    private static final String APPD_PATH = "/APPD";

    private static final String MAIN_SERVICE_TEMPLATE = "/Definition/MainServiceTemplate.yaml";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseConfigService.class);

    @Autowired
    private ReleaseConfigMapper configMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    /**
     * saveConfig.
     */
    public Either<FormatRespDto, ReleaseConfig> saveConfig(String projectId, ReleaseConfig config) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }

        ReleaseConfig releaseConfig = configMapper.getConfigByProjectId(projectId);
        if (releaseConfig != null) {
            LOGGER.error("releaseConfig have exit!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "releaseConfig have exit!");
            return Either.left(dto);
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

        ApplicationProject applicationProject = projectMapper.getProjectById(projectId);
        if (!CollectionUtils.isEmpty(applicationProject.getCapabilityList()) || !CapabilitiesDetail
            .isEmpty(config.getCapabilitiesDetail()) || !StringUtils.isEmpty(config.getGuideFileId())) {
            if (applicationProject.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) {
                Either<FormatRespDto, Boolean> rebuildRes = rebuildCsar(projectId, config);
                if (rebuildRes.isLeft()) {
                    return Either.left(rebuildRes.getLeft());
                }
            } else {
                Either<FormatRespDto, Boolean> rebuildRes = rebuildVmCsar(projectId, config);
                if (rebuildRes.isLeft()) {
                    return Either.left(rebuildRes.getLeft());
                }
            }
        }

        LOGGER.info("create release config success!");
        return Either.right(configMapper.getConfigByReleaseId(config.getReleaseId()));
    }

    /**
     * modifyConfig.
     */
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

        config.setReleaseId(oldConfig.getReleaseId());
        config.setProjectId(projectId);
        config.setCreateTime(new Date());
        int res = configMapper.modifyReleaseConfig(config);
        if (res < 1) {
            LOGGER.error("create project {} release-config data fail!", projectId);
            FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "modify config data fail");
            return Either.left(dto);
        }

        ApplicationProject applicationProject = projectMapper.getProjectById(projectId);
        if (!CollectionUtils.isEmpty(applicationProject.getCapabilityList()) || !CapabilitiesDetail
            .isEmpty(config.getCapabilitiesDetail()) || !StringUtils.isEmpty(config.getGuideFileId())) {
            if (applicationProject.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) {
                Either<FormatRespDto, Boolean> rebuildRes = rebuildCsar(projectId, config);
                if (rebuildRes.isLeft()) {
                    return Either.left(rebuildRes.getLeft());
                }
            } else {
                Either<FormatRespDto, Boolean> rebuildRes = rebuildVmCsar(projectId, config);
                if (rebuildRes.isLeft()) {
                    return Either.left(rebuildRes.getLeft());
                }
            }
        }

        LOGGER.info("modify release config success!");
        return Either.right(configMapper.getConfigByReleaseId(config.getReleaseId()));

    }

    /**
     * getConfigById.
     */
    public Either<FormatRespDto, ReleaseConfig> getConfigById(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            LOGGER.error("req path miss project id!");
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("can not find the project by userId {} and projectId {}.", AccessUserUtil.getUserId(),
                projectId);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "projectId is null");
            return Either.left(dto);
        }
        ReleaseConfig oldConfig = configMapper.getConfigByProjectId(projectId);
        LOGGER.info("get release config success!");
        return Either.right(oldConfig);
    }

    /**
     * rebuildCsar.
     */
    public Either<FormatRespDto, Boolean> rebuildCsar(String projectId, ReleaseConfig releaseConfig) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        List<ProjectTestConfig> testConfigs = projectMapper.getTestConfigByProjectId(projectId);
        if (testConfigs == null || testConfigs.isEmpty()) {
            LOGGER.error("Project {} has not test config!", projectId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Project has not test config!");
            return Either.left(error);

        }
        ProjectTestConfig config = testConfigs.get(0);
        String projectPath = projectService.getProjectPath(config.getProjectId());
        String csarFilePath = projectPath + config.getAppInstanceId();
        // modify md file
        if (!StringUtils.isEmpty(releaseConfig.getGuideFileId())) {
            try {
                //verify md docs
                String readmePath = csarFilePath + TEMPLATE_MD;
                String readmeFileId = releaseConfig.getGuideFileId();
                if (readmeFileId != null && !readmeFileId.equals("")) {
                    UploadedFile path = uploadedFileMapper.getFileById(readmeFileId);
                    FileUtils.copyFile(new File(InitConfigUtil.getWorkSpaceBaseDir() + path.getFilePath()),
                        new File(readmePath));
                }
            } catch (IOException e) {
                String msg = "Update csar failed: occur IOException";
                LOGGER.error("Update csar failed: occur IOException {}.", e.getMessage());
                FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, msg + e.getMessage());
                return Either.left(error);
            }
        }

        if (!CapabilitiesDetail.isEmpty(releaseConfig.getCapabilitiesDetail()) || !CollectionUtils
            .isEmpty(project.getCapabilityList())) {
            // modify MainServiceTemplate zip
            Boolean res = rebuildAppd(project, csarFilePath, releaseConfig);
            if (!res) {
                LOGGER.error("Update MainServiceTemplate zip failed");
                FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                    "Update MainServiceTemplate zip failed");
                return Either.left(error);
            }
        }

        // update tgz in ~/Charts
        List<ServiceDetail> details = releaseConfig.getCapabilitiesDetail().getServiceDetails();
        if (!CollectionUtils.isEmpty(details)) {
            String chartsDir = csarFilePath + CHARTS_TGZ;
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
        }

        // compress csar
        try {
            CompressFileUtilsJava.compressToCsarAndDeleteSrc(csarFilePath, projectPath, config.getAppInstanceId());
        } catch (IOException e) {
            String msg = "Update csar failed: occur IOException";
            LOGGER.error("Update csar failed: occur IOException {}.", e.getMessage());
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, msg + e.getMessage());
            return Either.left(error);
        }

        return Either.right(true);
    }

    private Boolean rebuildAppd(ApplicationProject project, String csarFilePath, ReleaseConfig releaseConfig) {
        // decompress MainServiceTemplate.zip
        String appdDir = csarFilePath + APPD_PATH;
        File dirFile = new File(appdDir);
        if (dirFile.exists()) {
            File[] zipFiles = dirFile.listFiles();
            if (zipFiles != null) {
                for (File zipFile : zipFiles) {
                    if (!zipFile.isFile() || !zipFile.getName().endsWith(".zip")) {
                        continue;
                    }
                    fillTemplateInZipFile(project, appdDir, zipFile, releaseConfig);
                }
            }
        }
        return true;

    }

    private Boolean fillTemplateInZipFile(ApplicationProject project, String appdDir, File zipFile,
        ReleaseConfig releaseConfig) {
        List<TrafficRule> trafficRules = releaseConfig.getCapabilitiesDetail().getAppTrafficRule();
        List<DnsRule> dnsRules = releaseConfig.getCapabilitiesDetail().getAppDNSRule();
        List<ServiceDetail> details = releaseConfig.getCapabilitiesDetail().getServiceDetails();
        // verify csar file
        String appdFilePath = zipFile.getAbsolutePath().replace(".zip", "");

        try {
            // decompress tgz
            CompressFileUtils.decompress(zipFile.getAbsolutePath(), appdFilePath);
            // load valueTemplate.yaml
            String mainServiceTemplatePath = appdFilePath + MAIN_SERVICE_TEMPLATE;
            FileUtils.deleteQuietly(new File(zipFile.getAbsolutePath()));
            // verify service template file
            File templateFile = new File(mainServiceTemplatePath);
            if (!templateFile.exists()) {
                LOGGER.error("Cannot find service template file.");
                return false;
            }
            // update node in template
            String yamlContent = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
            yamlContent = yamlContent.replaceAll("\t", "");
            Yaml yaml = new Yaml(new SafeConstructor());
            Map<String, Object> loaded;
            try {
                loaded = yaml.load(yamlContent);
            } catch (DomainException e) {
                LOGGER.error("Yaml deserialization failed {}", e.getMessage());
                return false;
            }
            // get config node from template
            LinkedHashMap<String, Object> nodeMap = (LinkedHashMap<String, Object>) loaded.get("topology_template");
            LinkedHashMap<String, Object> templateNode = (LinkedHashMap<String, Object>) nodeMap.get("node_templates");
            AppConfigurationModel configModel = buildTemplateConfig(project, trafficRules, dnsRules, details);
            // write yaml
            templateNode.put("app_configuration", configModel);
            // write content to yaml
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.writeValue(templateFile, loaded);

            File templateFileModify = new File(mainServiceTemplatePath);
            String yamlContents = FileUtils.readFileToString(templateFileModify, StandardCharsets.UTF_8);
            yamlContents = yamlContents.replaceAll("\"", "");
            writeFile(templateFileModify, yamlContents);
            // compress to zip
            if (!org.springframework.util.StringUtils.isEmpty(appdFilePath)) {
                File dir = new File(appdFilePath);
                if (dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null && files.length > 0) {
                        List<File> subFiles = Arrays.asList(files);
                        if (!CollectionUtils.isEmpty(subFiles)) {
                            CompressFileUtilsJava
                                .zipFiles(subFiles, new File(appdDir + File.separator + "MainServiceTemplate.zip"));
                            for (File subFile : subFiles) {
                                FileUtils.deleteQuietly(subFile);
                            }
                        }
                    }
                }

            }
            //delete fileDir
            FileUtils.deleteDirectory(new File(appdFilePath));
        } catch (IOException e) {
            LOGGER.error("Update csar failed: occur IOException {}.", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * rebuildVmCsar.
     */
    public Either<FormatRespDto, Boolean> rebuildVmCsar(String projectId, ReleaseConfig releaseConfig) {
        VmPackageConfig vmPackageConfig = vmConfigMapper.getVmPackageConfig(projectId);
        if (vmPackageConfig == null) {
            LOGGER.error("Project {} has not vm package!", projectId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Project has not vm package!");
            return Either.left(error);
        }
        // verify csar file
        String csarFilePath = projectService.getProjectPath(projectId) + vmPackageConfig.getAppInstanceId();
        File csar = new File(csarFilePath);
        if (!csar.exists()) {
            LOGGER.error("Cannot find csar file:{}.", csarFilePath);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST,
                "Cannot find csar file: " + csarFilePath);
            return Either.left(error);
        }
        try {
            //verify md docs
            String readmePath = csarFilePath + TEMPLATE_MD;
            String readmeFileId = releaseConfig.getGuideFileId();
            if (readmeFileId != null && !readmeFileId.equals("")) {
                UploadedFile path = uploadedFileMapper.getFileById(readmeFileId);
                FileUtils.copyFile(new File(InitConfigUtil.getWorkSpaceBaseDir() + path.getFilePath()),
                    new File(readmePath));
            }
            // compress csar
            CompressFileUtilsJava.compressToCsarAndDeleteSrc(csarFilePath, projectService.getProjectPath(projectId),
                vmPackageConfig.getAppInstanceId());
        } catch (JsonGenerationException e) {
            String msg = "Update csar failed: occur JsonGenerationException";
            LOGGER.error("Update csar failed: occur JsonGenerationException {}.", e.getMessage());
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, msg + e.getMessage());
            return Either.left(error);
        } catch (JsonMappingException e) {
            String msg = "Update csar failed: occur JsonMappingException";
            LOGGER.error("Update csar failed: occur JsonMappingException {}.", e.getMessage());
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, msg + e.getMessage());
            return Either.left(error);
        } catch (IOException e) {
            String msg = "Update csar failed: occur IOException";
            LOGGER.error("Update csar failed: occur IOException {}.", e.getMessage());
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, msg + e.getMessage());
            return Either.left(error);
        }

        return Either.right(true);
    }

    private void writeFile(File file, String content) {
        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * build appConfigurationConfig data.
     */
    private AppConfigurationModel buildTemplateConfig(ApplicationProject project, List<TrafficRule> trafficRules,
        List<DnsRule> dnsRules, List<ServiceDetail> details) {
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
            List<AppConfigurationModel.ServiceRequired> requiredList = new ArrayList<>();
            Gson gson = new Gson();
            Type type = new TypeToken<List<OpenMepCapabilityGroup>>() { }.getType();
            List<OpenMepCapabilityGroup> capabilities = gson.fromJson(gson.toJson(project.getCapabilityList()), type);
            for (OpenMepCapabilityGroup obj : capabilities) {
                List<OpenMepCapabilityDetail> openMepCapabilityGroups = obj.getCapabilityDetailList();
                Type openMepCapabilityType = new TypeToken<List<OpenMepCapabilityDetail>>() { }.getType();
                List<OpenMepCapabilityDetail> openMepCapabilityDetails = gson
                    .fromJson(gson.toJson(openMepCapabilityGroups), openMepCapabilityType);

                for (OpenMepCapabilityDetail capabilityDetail : openMepCapabilityDetails) {
                    AppConfigurationModel.ServiceRequired required = new AppConfigurationModel.ServiceRequired();
                    required.setSerName(capabilityDetail.getHost());
                    required.setAppId(capabilityDetail.getAppId());
                    required.setPackageId(capabilityDetail.getPackageId());
                    required.setVersion(capabilityDetail.getVersion());
                    requiredList.add(required);
                }
            }
            properties.setAppServiceRequired(requiredList);
        }
        AppConfigurationModel configModel = new AppConfigurationModel();
        configModel.setProperties(properties);
        return configModel;
    }

    /**
     * get ServiceProduced list from service Details.
     *
     * @param details details
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
     * fill value template with detailList.
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
            Yaml yaml = new Yaml(new SafeConstructor());
            Map<String, Object> loaded;
            try {
                loaded = yaml.load(valueContent);
            } catch (DomainException e) {
                LOGGER.error("Yaml deserialization failed {}", e.getMessage());
                return;
            }
            // build node template
            List<ServiceConfig> configs = detailList.stream().map(
                t -> new ServiceConfig(t.getServiceName(), t.getInternalPort(), t.getVersion(), t.getProtocol(),
                    "default")).collect(Collectors.toList());
            // update node in template
            loaded.put("serviceconfig", configs);
            // write content to yaml
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            om.writeValue(valFile, loaded);
            // compress tgz
            CompressFileUtilsJava
                .compressToTgzAndDeleteSrc(tgzFile.getAbsolutePath().replace(".tgz", ""), tgzFile.getParent(),
                    fileName);
        } catch (JsonGenerationException e) {
            LOGGER.error("FillTemplateInTgzFile failed: occur JsonGenerationException {}", e.getMessage());
        } catch (JsonMappingException e) {
            LOGGER.error("FillTemplateInTgzFile failed:  occur JsonMappingException {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("FillTemplateInTgzFile failed: occur IOException {}", e.getMessage());
        }
        // delete decompress dir if exists
        File tmpDir = new File(tgzFile.getParent() + File.separator + fileName);
        if (tmpDir.exists()) {
            boolean isDelete = tmpDir.delete();
            if (!isDelete) {
                LOGGER.error("delete decompress dir failed");
            }
        }

    }

}
