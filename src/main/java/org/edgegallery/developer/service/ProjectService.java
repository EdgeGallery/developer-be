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

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.ReleaseConfigMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.mapper.capability.CapabilityGroupMapper;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.model.CapabilitiesDetail;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.model.ServiceDetail;
import org.edgegallery.developer.model.SshConnectInfo;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.model.atp.AtpResultInfo;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.model.deployyaml.PodEvents;
import org.edgegallery.developer.model.deployyaml.PodEventsRes;
import org.edgegallery.developer.model.deployyaml.PodStatusInfo;
import org.edgegallery.developer.model.deployyaml.PodStatusInfos;
import org.edgegallery.developer.model.lcm.UploadResponse;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.ApplicationProjectCapability;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigDeployStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapability;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfigStageStatus;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.capability.CapabilityService;
import org.edgegallery.developer.service.csar.NewCreateCsar;
import org.edgegallery.developer.service.dao.ProjectDao;
import org.edgegallery.developer.service.deploy.IConfigDeployStage;
import org.edgegallery.developer.service.virtual.VmService;
import org.edgegallery.developer.template.ChartFileCreator;
import org.edgegallery.developer.util.AppStoreUtil;
import org.edgegallery.developer.util.AtpUtil;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("projectService")
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private static CookieStore cookieStore = new BasicCookieStore();

    private static Gson gson = new Gson();

    ExecutorService threadPool = Executors.newSingleThreadExecutor();

    @Value("${imagelocation.domainname:}")
    private String imageDomainName;

    @Value("${imagelocation.project:}")
    private String imageProject;

    @Value("${security.oauth2.resource.jwt.key-uri:}")
    private String loginUrl;

    @Value("${client.client-id:}")
    private String clientId;

    @Value("${client.client-secret:}")
    private String clientPW;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private ReleaseConfigMapper configMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private CapabilityMapper capabilityMapper;

    @Autowired
    private CapabilityGroupMapper capabilityGroupMapper;

    @Autowired
    private ProjectDao projectDto;

    @Autowired
    private Map<String, IConfigDeployStage> deployServiceMap;

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private WebSshService webSshService;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Autowired
    private ProjectCapabilityMapper projectCapabilityMapper;

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectCapabilityService projectCapabilityService;

    @Autowired
    private EncryptedService encryptedService;

    @Autowired
    private CapabilityService capabilityService;

    public Page<ApplicationProject> getAllProjects(int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<ApplicationProject> pageInfo = new PageInfo<ApplicationProject>(projectMapper.getAllProject());
        LOGGER.info("get all projects success.");
        return new Page<ApplicationProject>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    public Page<ApplicationProject> getProjectByNameWithFuzzy(String userId, String projectName, int limit,
        int offset) {
        PageHelper.offsetPage(offset, limit);
        PageInfo<ApplicationProject> pageInfo = new PageInfo<ApplicationProject>(
            projectMapper.getProjectByNameWithFuzzy(userId, projectName));
        LOGGER.info("get all projects success.");
        return new Page<ApplicationProject>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    public Either<FormatRespDto, ApplicationProject> getProject(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        LOGGER.info("Get project information success");
        return Either.right(project);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Either<FormatRespDto, ApplicationProject> createProject(String userId, ApplicationProject project) {
        String newName = project.getName();
        String newVersion = project.getVersion();
        String newProvider = project.getProvider();
        // Check if the project with the same name, same version, and same provider
        // exists
        List<ApplicationProject> appList = projectMapper.getAllProjectNoCondtion();
        if (!CollectionUtils.isEmpty(appList)) {
            for (ApplicationProject appProject : appList) {
                String name = appProject.getName();
                String version = appProject.getVersion();
                String provider = appProject.getProvider();
                if (newName.equals(name) && newVersion.equals(version) && newProvider.equals(provider)) {
                    FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "the same project exists");
                    return Either.left(error);
                }
            }
        }
        project.setUserId(userId);
        String projectId = UUID.randomUUID().toString();
        String projectPath = getProjectPath(projectId);
        try {
            DeveloperFileUtils.deleteAndCreateDir(projectPath);
        } catch (IOException e1) {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Create project path failed.");
            return Either.left(error);
        }
        project.setId(projectId);

        // move icon file from temp dir to project dir
        String iconFileId = project.getIconFileId();
        if (iconFileId == null) {
            LOGGER.error("icon file is null");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "icon file is null");
            DeveloperFileUtils.deleteDir(projectPath);
            return Either.left(error);
        }
        try {
            moveFileToWorkSpaceById(iconFileId, projectId);
        } catch (IOException e) {
            LOGGER.error("move icon file failed {}", e.getMessage());
            FormatRespDto error = gson.fromJson(e.getMessage(), FormatRespDto.class);
            DeveloperFileUtils.deleteDir(projectPath);
            return Either.left(error);
        }

        // set default value
        project.setStatus(EnumProjectStatus.ONLINE);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        project.setCreateDate(time.format(new Date()));
        // save project to DB
        int res = projectMapper.save(project);
        if (res < 1) {
            LOGGER.error("Create project in db error.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "create project failed");
            DeveloperFileUtils.deleteDir(projectPath);
            return Either.left(error);
        }
        LOGGER.info("Create project success.");

        List<ApplicationProjectCapability> appProjectCapabilities = project.getApplicationProjectCapabilities();
        if (!CollectionUtils.isEmpty(appProjectCapabilities)) {
            for (ApplicationProjectCapability appProjectCapability : appProjectCapabilities) {
                Either<FormatRespDto, ApplicationProjectCapability> appProjectCapabilityResult
                    = projectCapabilityService.create(appProjectCapability);
                if (appProjectCapabilityResult.isLeft()) {
                    LOGGER.info("Insert ApplicationProjectCapability failed.");
                    throw new DeveloperException("Insert ApplicationProjectCapability failed.");
                }
            }
            LOGGER.info("Create project capability success.");
        }

        if (!capabilityService.updateSelectCountByIds(project.getCapabilityList())) {
            LOGGER.info("updateSelectCountByIds failed.");
            throw new DeveloperException("updateSelectCountByIds failed.");
        }
        // new release config
        ReleaseConfig releaseConfig = new ReleaseConfig();
        releaseConfig.setReleaseId(UUID.randomUUID().toString());
        releaseConfig.setProjectId(projectId);
        releaseConfig.setCreateTime(new Date());
        CapabilitiesDetail capabilitiesDetail = new CapabilitiesDetail();
        List<TrafficRule> appTrafficRule = new ArrayList<>();
        List<DnsRule> appDNSRule = new ArrayList<>();
        List<ServiceDetail> serviceDetails = new ArrayList<>();
        capabilitiesDetail.setAppTrafficRule(appTrafficRule);
        capabilitiesDetail.setAppDNSRule(appDNSRule);
        capabilitiesDetail.setServiceDetails(serviceDetails);
        releaseConfig.setCapabilitiesDetail(capabilitiesDetail);
        int saveRet = configMapper.saveConfig(releaseConfig);
        if (saveRet < 1) {
            LOGGER.error("save config data fail!");
            FormatRespDto dto = new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "save config data fail");
            return Either.left(dto);
        }
        return Either.right(project);
    }

    public String getProjectPath(String projectId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId
            + File.separator;
    }

    /**
     * moveFileToWorkSpaceById.
     */
    private void moveFileToWorkSpaceById(String srcId, String projectId) throws IOException {
        // at firstly, update the file status in DB, then this file should not be delete
        // by timer
        uploadedFileMapper.updateFileStatus(srcId, false);

        // to confirm, whether the status is updated
        UploadedFile file = uploadedFileMapper.getFileById(srcId);
        if (file == null || file.isTemp()) {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find file, please upload again.");

            uploadedFileMapper.updateFileStatus(srcId, true);
            throw new IOException(gson.toJson(error));
        }

        // get temp file
        String tempFilePath = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath() + srcId;
        File tempFile = new File(tempFilePath);
        if (!tempFile.exists() || tempFile.isDirectory()) {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find file, please upload again.");

            uploadedFileMapper.updateFileStatus(srcId, true);
            throw new IOException(gson.toJson(error));
        }

        // move file
        String projectPath = getProjectPath(projectId);
        File desFile = new File(projectPath + srcId);
        try {
            DeveloperFileUtils.moveFile(tempFile, desFile);
            String filePath = BusinessConfigUtil.getWorkspacePath() + projectId + File.separator + srcId;
            uploadedFileMapper.updateFilePath(srcId, filePath);
        } catch (IOException e) {
            LOGGER.error("move icon file failed {}", e.getMessage());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Move icon file failed.");
            uploadedFileMapper.updateFileStatus(srcId, true);
            throw new IOException(gson.toJson(error));
        }

    }

    /**
     * deleteProject.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteProject(String userId, String projectId, String token) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.warn("Can not find project by userId {} and projectId {}, do not need delete.", userId, projectId);
            return Either.right(true);
        }

        // delete capabilityGroup and CapabilityDetail
        projectCapabilityMapper.deleteByProjectId(projectId);
        // delete the project from db
        Either<FormatRespDto, Boolean> delResult = projectDto.deleteProject(userId, projectId);
        if (delResult.isLeft()) {
            return delResult;
        }
        // delete files of project
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteDir(projectPath);
        projectCapabilityService.deleteByProjectId(projectId);
        LOGGER.info("Delete project {} success.", projectId);
        EnumDeployPlatform platform = project.getDeployPlatform();
        EnumProjectStatus status = project.getStatus();
        // clean sandbox env
        if (platform.equals("KUBERNETES") && !status.equals("ONLINE")) {
            Either<FormatRespDto, Boolean> ret = cleanTestEnv(userId, projectId, token);
            if (ret == null || ret.isLeft()) {
                LOGGER.error("clean container project {} failed!", projectId);
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "clean container project env failed!");
                return Either.left(error);
            }
        }
        // clean vm env
        if (platform.equals("VIRTUALMACHINE") && !status.equals("ONLINE")) {
            Either<FormatRespDto, Boolean> vmRet = vmService.cleanVmDeploy(projectId, token);
            if (vmRet == null || vmRet.isLeft()) {
                LOGGER.error("clean vm project {} failed!", projectId);
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "clean vm project env failed!");
                return Either.left(error);
            }
        }
        return Either.right(true);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Either<FormatRespDto, ApplicationProject> modifyProject(String userId, String projectId,
        ApplicationProject newProject) {
        ApplicationProject oldProject = projectMapper.getProject(userId, projectId);
        if (oldProject == null) {
            LOGGER.error("Can not find project by userId {} and projectId {}.", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, Consts.RESPONSE_MESSAGE_CAN_NOT_FIND_PROJECT);
            return Either.left(error);
        }
        String status = oldProject.getStatus().name();
        if (!status.equals("ONLINE")) {
            LOGGER.error("Can not modify project {}.", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "cannot modify projects that are online");
            return Either.left(error);
        }
        String oldIconFileId = oldProject.getIconFileId();
        String newIconFileId = newProject.getIconFileId();
        if (!oldIconFileId.equals(newIconFileId)) {
            int updateRes = uploadedFileMapper.updateFileStatus(newIconFileId, false);
            if (updateRes < 1) {
                LOGGER.error("modify project {} icon file id to not temp failed!", projectId);
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "modify icon file status failed!");
                return Either.left(error);
            }
        }
        newProject.setId(projectId);
        newProject.setUserId(userId);

        projectCapabilityService.deleteByProjectId(projectId);
        List<ApplicationProjectCapability> applicationProjectCapabilities = newProject
            .getApplicationProjectCapabilities();
        Either<FormatRespDto, List<ApplicationProjectCapability>> prjCapaResult = projectCapabilityService
            .create(applicationProjectCapabilities);
        if (prjCapaResult.isLeft()) {
            LOGGER.error("Update ApplicationProjectCapability {} failed", newProject.getId());
            throw new DeveloperException("Update ApplicationProjectCapability [" + newProject.getId() + "] failed");
        }

        int res = projectMapper.updateProject(newProject);
        if (res < 1) {
            LOGGER.error("Update project {} failed", newProject.getId());
            throw new DeveloperException("Update project [" + newProject.getId() + "] failed");
        }
        LOGGER.info("Update project {} success.", projectId);
        return Either.right(projectMapper.getProject(userId, projectId));
    }

    /**
     * processDeploy. task job for scheduler
     *
     * @return
     */
    public void processDeploy() {
        // get deploying config list from db
        List<ProjectTestConfig> configList = projectMapper
            .getTestConfigByDeployStatus(EnumTestConfigDeployStatus.DEPLOYING.toString());
        if (CollectionUtils.isEmpty(configList)) {
            return;
        }
        configList.forEach(this::processConfig);
    }

    /**
     * processConfig.
     */
    public void processConfig(ProjectTestConfig config) {
        String nextStage = config.getNextStage();
        if (StringUtils.isBlank(nextStage)) {
            return;
        }
        try {
            IConfigDeployStage stageService = deployServiceMap.get(nextStage + "_service");
            stageService.execute(config);
        } catch (Exception e) {
            LOGGER.error("Deploy project config:{} failed on stage :{}, res:{}", config.getTestId(), nextStage,
                e.getMessage());
        }
    }

    /**
     * terminateProject.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> terminateProject(String userId, String projectId, String token) {
        List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(testConfigList)) {
            LOGGER.info("This project has not test config, do not terminate.");
            return Either.right(true);
        }
        ProjectTestConfig testConfig = testConfigList.get(0);
        if (!EnumTestConfigStatus.Success.equals(testConfig.getStageStatus().getInstantiateInfo())
            || testConfig.getWorkLoadId() == null) {
            LOGGER.error("Failed to terminate application when instantiateInfo not success.");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                "Failed to terminate application when instantiateInfo not success.");
            return Either.left(error);
        }
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
        MepHost host = hosts.get(0);
        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        boolean terminateResult = HttpClientUtil
            .terminateAppInstance(basePath, testConfig.getAppInstanceId(), userId, token);
        if (!terminateResult) {
            LOGGER.error("Failed to terminate application which userId is: {}, instanceId is {}", userId,
                testConfig.getAppInstanceId());
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to terminate application.");
            return Either.left(error);
        }
        // update config status
        testConfig.setErrorLog("The test has been completed and the application is terminated.");
        testConfig.setAccessUrl("");
        projectMapper.updateTestConfig(testConfig);
        return Either.right(true);
    }

    /**
     * deployProject.
     *
     * @return
     */
    public Either<FormatRespDto, ApplicationProject> deployProject(String userId, String projectId, String token) {
        // check config. hosts must be required.
        List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(testConfigList)) {
            LOGGER.error("Can not find test config by project id.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find project test config.");
            return Either.left(error);
        }
        // only one test-config for each project
        ProjectTestConfig testConfig = testConfigList.get(0);
        // check status
        if (testConfig.getDeployStatus() != null && !(EnumTestConfigDeployStatus.NOTDEPLOY)
            .equals(testConfig.getDeployStatus())) {
            // not ready
            LOGGER.error("The test config not ready with status:{}", testConfig.getDeployStatus());
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "The test config not ready.");
            return Either.left(error);
        }
        // update test-config status
        String appInstanceId = UUID.randomUUID().toString();
        testConfig.setDeployStatus(EnumTestConfigDeployStatus.DEPLOYING);
        ProjectTestConfigStageStatus stageStatus = new ProjectTestConfigStageStatus();
        testConfig.setStageStatus(stageStatus);
        testConfig.setAppInstanceId(appInstanceId);
        testConfig.setLcmToken(token);
        int tes = projectMapper.updateTestConfig(testConfig);
        if (tes < 1) {
            LOGGER.error("Update test-config {} failed.", testConfig.getTestId());
        }
        // update project status
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        project.setStatus(EnumProjectStatus.DEPLOYING);
        project.setLastTestId(testConfig.getTestId());
        int res = projectMapper.updateProject(project);
        if (res < 1) {
            LOGGER.error("Update project {} in db failed.", project.getId());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "update product in db failed.");
            return Either.left(error);
        }
        return Either.right(projectMapper.getProject(userId, projectId));
    }

    /**
     * createCsarPkg.
     */
    public File createCsarPkg(String userId, ApplicationProject project, ProjectTestConfig testConfig)
        throws IOException {
        String projectId = project.getId();
        List<Capability> capabilities = capabilityService.findByProjectId(project.getId());
        String projectPath = getProjectPath(projectId);
        String chartName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase() + testConfig
            .getAppInstanceId().substring(0, 8);
        String configMapName = "mepagent" + testConfig.getAppInstanceId().toString();
        String namespace = chartName;
        List<HelmTemplateYamlPo> yamlPoList = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
        File csarPkgDir;
        if (!CollectionUtils.isEmpty(yamlPoList)) {
            // create chart file
            ChartFileCreator chartFileCreator = new ChartFileCreator(chartName);
            chartFileCreator.setChartName(chartName);
            if (CollectionUtils.isEmpty(capabilities)) {
                chartFileCreator
                    .setChartValues("false", "true", namespace, configMapName, imageDomainName, imageProject);
            } else {
                chartFileCreator
                    .setChartValues("true", "true", namespace, configMapName, imageDomainName, imageProject);
            }
            // stop
            yamlPoList.forEach(helmTemplateYamlPo -> chartFileCreator
                .addTemplateYaml(helmTemplateYamlPo.getFileName(), helmTemplateYamlPo.getContent()));
            String tgzFilePath = chartFileCreator.build();

            // create csar file directory
            csarPkgDir = new NewCreateCsar().create(projectPath, testConfig, project, chartName, new File(tgzFilePath));
        } else {
            csarPkgDir = new NewCreateCsar().create(projectPath, testConfig, project, chartName, null);
        }
        encryptedService.encryptedFile(csarPkgDir.getCanonicalPath());
        encryptedService.encryptedCMS(csarPkgDir.getCanonicalPath());
        return CompressFileUtilsJava
            .compressToCsarAndDeleteSrc(csarPkgDir.getCanonicalPath(), projectPath, csarPkgDir.getName());
    }

    /**
     * deplay test config and csar package to appLcm.
     *
     * @return
     */
    public boolean deployTestConfigToAppLcm(File csar, ApplicationProject project, ProjectTestConfig testConfig,
        String userId, String token) {
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
        MepHost host = hosts.get(0);
        // Note(ch) only ip?
        testConfig.setAccessUrl(host.getLcmIp());
        // upload pkg
        LcmLog lcmLog = new LcmLog();
        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        String uploadRes = HttpClientUtil.uploadPkg(basePath, csar.getPath(), userId, token, lcmLog);
        LOGGER.info("upload res {}", uploadRes);
        if (org.springframework.util.StringUtils.isEmpty(uploadRes)) {
            testConfig.setErrorLog(lcmLog.getLog());
            return false;
        }
        UploadResponse uploadResponse = gson.fromJson(uploadRes, UploadResponse.class);
        String pkgId = uploadResponse.getPackageId();
        testConfig.setPackageId(pkgId);
        projectMapper.updateTestConfig(testConfig);
        // distribute pkg
        String distributeRes = HttpClientUtil.distributePkg(basePath, userId, token, pkgId, host.getMecHost(), lcmLog);
        LOGGER.info("distribute res {}", distributeRes);
        if (distributeRes == null) {
            testConfig.setErrorLog(lcmLog.getLog());
            return false;
        }
        String appInstanceId = testConfig.getAppInstanceId();
        // instantiate application
        Map<String, String> inputParams = null;
        boolean instantRes = HttpClientUtil
            .instantiateApplication(basePath, appInstanceId, userId, token, lcmLog, pkgId, host.getMecHost(),
                inputParams);
        LOGGER.info("after instant {}", instantRes);
        if (!instantRes) {
            testConfig.setErrorLog(lcmLog.getLog());
            return false;
        }
        return true;
    }

    /**
     * checkDependency.
     *
     * @param project project
     * @return
     */
    public boolean checkDependency(ApplicationProject project) {
        Optional<List<String>> groups = Optional.ofNullable(project.getCapabilityList());
        if (!groups.isPresent()) {
            LOGGER.error("the project being deployed does not have any capabilities selected ");
            return true;
        }
        Gson gsonGroup = new Gson();
        Type groupType = new TypeToken<List<OpenMepCapabilityGroup>>() { }.getType();
        List<OpenMepCapabilityGroup> capabilities = gsonGroup
            .fromJson(gsonGroup.toJson(project.getCapabilityList()), groupType);
        for (OpenMepCapabilityGroup group : capabilities) {
            List<OpenMepCapability> openMepCapabilityGroups = group.getCapabilityDetailList();
            Type openMepCapabilityType = new TypeToken<List<OpenMepCapability>>() { }.getType();
            List<OpenMepCapability> openMepCapabilityDetails = gsonGroup
                .fromJson(gsonGroup.toJson(openMepCapabilityGroups), openMepCapabilityType);
            for (OpenMepCapability detail : openMepCapabilityDetails) {
                if (!StringUtils.isEmpty(detail.getPackageId())) {
                    return true;
                }
            }
        }
        return true;

    }

    /**
     * createTestConfig.
     *
     * @return
     */
    public Either<FormatRespDto, ProjectTestConfig> createTestConfig(String userId, String projectId,
        ProjectTestConfig testConfig) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, Consts.RESPONSE_MESSAGE_CAN_NOT_FIND_PROJECT);
            return Either.left(error);
        }

        // validate mep host if privateHost is true
        if (testConfig.isPrivateHost()) {
            List<MepHost> privateHosts = hostMapper.getHostsByUserId(project.getUserId());
            testConfig.setHosts(privateHosts.subList(0, 1));
            if (testConfig.getHosts().size() != 1) {
                LOGGER.error("The mep host for project {} is required.", projectId);
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "The mep host for project is required.");
                return Either.left(error);
            }
            MepHost mepHost = testConfig.getHosts().get(0);
            if (!mepHost.getUserId().equals(userId)) {
                LOGGER.error("The mep host for project {} not private.", projectId);
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "The mep host for project not private.");
                return Either.left(error);
            }
        }

        testConfig.setProjectId(projectId);
        testConfig.setDeployStatus(EnumTestConfigDeployStatus.NOTDEPLOY);
        testConfig.setPlatform(EnumDeployPlatform.KUBERNETES);
        List<ProjectTestConfig> tests = projectMapper.getTestConfigByProjectId(projectId);

        int ret;
        if (!CollectionUtils.isEmpty(tests)) {
            ret = projectMapper.updateTestConfig(testConfig);
        } else {
            ret = projectMapper.saveTestConfig(testConfig);
        }
        if (ret > 0) {
            LOGGER.info("Create test {} config success.", testConfig.getTestId());
            return Either.right(projectMapper.getTestConfig(testConfig.getTestId()));
        }
        LOGGER.error("Create test config error.");
        FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Create test config error.");
        return Either.left(error);
    }

    private boolean checkUser(String projectId, String userId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.info("Have not permission to modify the project. userId {}, projectId {}.", userId, projectId);
            return false;
        }
        return true;
    }

    /**
     * modifyTestConfig.
     *
     * @return
     */
    public Either<FormatRespDto, ProjectTestConfig> modifyTestConfig(String projectId, ProjectTestConfig testConfig) {
        if (!checkUser(projectId, AccessUserUtil.getUserId())) {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not modify the project.");
            return Either.left(error);
        }
        List<ProjectTestConfig> tests = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(tests)) {
            LOGGER.error("Can not find test config by projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the test config.");
            return Either.left(error);
        }

        testConfig.setProjectId(projectId);
        testConfig.setTestId(tests.get(0).getTestId());
        testConfig.setPlatform(EnumDeployPlatform.KUBERNETES);
        int ret = projectMapper.modifyTestConfig(testConfig);
        if (ret > 0) {
            LOGGER.info("Update test config {} success.", testConfig.getTestId());
            return Either.right(projectMapper.getTestConfig(testConfig.getTestId()));
        }
        LOGGER.error("Update test config {} error.", testConfig.getTestId());
        FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Update test config error.");
        return Either.left(error);
    }

    /**
     * getTestConfig.
     *
     * @return
     */
    public Either<FormatRespDto, ProjectTestConfig> getTestConfig(String projectId) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.warn("Can not find the project projectId {}.", projectId);
            return Either.right(null);
        }
        List<ProjectTestConfig> tests = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(tests)) {
            LOGGER.warn("Can not find the test config by projectId {}.", projectId);
            return Either.right(null);
        } else {
            LOGGER.info("Get test config {} success.", tests.get(0).getTestId());
            tests.get(0).setLcmToken("");
            return Either.right(tests.get(0));
        }
    }

    /**
     * uploadToAppStore.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> uploadToAppStore(String userId, String projectId, String userName,
        String token) {
        // 0 check data. must be tested, and deployed status must be ok, can not be
        // error.
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not get project by inputs of userId and projectId.");
            String message = "Can not get project, userId or projectId is error.";
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, message));

        }
        ReleaseConfig releaseConfig = configMapper.getConfigByProjectId(projectId);
        if (releaseConfig == null) {
            LOGGER.error("Can not find ReleaseConfig by project id.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "can not get release config");
            return Either.left(error);
        }
        if (releaseConfig.getAtpTest() == null || !releaseConfig.getAtpTest().getStatus().equals("success")) {
            LOGGER.error("Can not upload appstore because apt test fail.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "can not upload appstore");
            return Either.left(error);
        }
        Either<FormatRespDto, JsonObject> resCsar = getCsarAndUpload(projectId, project, releaseConfig, userId,
            userName, token);
        LOGGER.warn("upload result true or false:{}", resCsar.isRight() ? resCsar.getRight() : resCsar.getLeft());
        if (resCsar.isLeft()) {
            return Either.left(resCsar.getLeft());
        }
        JsonObject jsonObject = resCsar.getRight();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOGGER.error("sleep fail! {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        Either<FormatRespDto, Boolean> pubRes = publishApp(jsonObject, token);
        LOGGER.warn("publish result true or false:{}", pubRes.isRight() ? pubRes.getRight() : pubRes.getLeft());
        if (pubRes.isLeft()) {
            return Either.left(pubRes.getLeft());
        }
        CapabilitiesDetail capabilitiesDetail = releaseConfig.getCapabilitiesDetail();
        if (capabilitiesDetail.getServiceDetails() != null && !capabilitiesDetail.getServiceDetails().isEmpty()) {
            // save db to openmepcapabilitydetail
            List<String> openCapabilityIds = new ArrayList<>();
            for (ServiceDetail serviceDetail : capabilitiesDetail.getServiceDetails()) {
                Capability detail = new Capability();
                CapabilityGroup group = new CapabilityGroup();
                String groupId = UUID.randomUUID().toString();
                fillCapabilityGroup(serviceDetail, groupId, group);
                fillCapabilityDetail(serviceDetail, detail, jsonObject, userId, groupId);
                Either<FormatRespDto, Boolean> resDb = doSomeDbOperation(group, detail, serviceDetail,
                    openCapabilityIds);
                if (resDb.isLeft()) {
                    return Either.left(resDb.getLeft());
                }
            }
            project.setOpenCapabilityId(openCapabilityIds.toString());
            project.setStatus(EnumProjectStatus.RELEASED);
            int updRes = projectMapper.updateProject(project);
            if (updRes < 1) {
                FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "set openCapabilityId fail!");
                return Either.left(error);
            }
        }
        project.setStatus(EnumProjectStatus.RELEASED);
        int updRes = projectMapper.updateProject(project);
        if (updRes < 1) {
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "set project status fail!");
            return Either.left(error);
        }
        LOGGER.warn("no application service publishing configuration!");
        return Either.right(true);
    }

    private Either<FormatRespDto, Boolean> doSomeDbOperation(CapabilityGroup group, Capability detail,
        ServiceDetail serviceDetail, List<String> openCapabilityIds) {
        int resGroup = capabilityGroupMapper.insert(group);
        if (resGroup < 1) {
            LOGGER.error("store db to tbl_capability_group fail!");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save capability group db fail!");
            return Either.left(error);
        }
        int res = capabilityMapper.insert(detail);
        if (res < 1) {
            LOGGER.error("store db to tbl_capability fail!");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save capability db fail!");
            return Either.left(error);
        }
        // set open_capability_id
        openCapabilityIds.add(detail.getId());
        // update file status
        int apiRes = uploadedFileMapper.updateFileStatus(serviceDetail.getApiJson(), false);
        if (apiRes < 1) {
            LOGGER.error("after publish,update api file {} status fail!", serviceDetail.getApiJson());
        }
        int mdRes = uploadedFileMapper.updateFileStatus(serviceDetail.getApiMd(), false);
        if (mdRes < 1) {
            LOGGER.error("after publish,update md file {} status fail!", serviceDetail.getApiMd());
        }
        LOGGER.warn("save db success! ");
        return Either.right(true);
    }

    private Either<FormatRespDto, Boolean> publishApp(JsonObject jsonObject, String token) {
        // publish app to appstore
        JsonElement appId = jsonObject.get("appId");
        JsonElement packageId = jsonObject.get("packageId");
        if (appId != null && packageId != null) {
            ResponseEntity<String> publishRes = AppStoreUtil
                .publishToAppStore(appId.getAsString(), packageId.getAsString(), token);
            if (publishRes == null) {
                LOGGER.error("publish app to appstore fail!");
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "publish app to appstore fail!");
                return Either.left(error);
            }
            LOGGER.info("publish over! {}", publishRes.getBody());
        }
        return Either.right(true);
    }

    private Either<FormatRespDto, JsonObject> getCsarAndUpload(String projectId, ApplicationProject project,
        ReleaseConfig releaseConfig, String userId, String userName, String token) {
        // 1 get CSAR package
        String fileName = getFileName(projectId);
        if (StringUtils.isEmpty(fileName)) {
            LOGGER.error("Can not find appInstanceId!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find appInstanceId!");
            return Either.left(error);
        }
        File csar = new File(getProjectPath(projectId) + fileName + ".csar");
        if (!csar.exists()) {
            LOGGER.error("Can not find csar package");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find csar package");
            return Either.left(error);
        }
        String iconFileId = project.getIconFileId();
        UploadedFile iconFile = uploadedFileMapper.getFileById(iconFileId);
        String iconPath = getProjectPath(projectId) + project.getIconFileId();
        File icon = new File(iconPath);
        File desIcon = new File(iconPath + File.separator + iconFile.getFileName());
        LOGGER.info("icon path:{}", iconFile);

        try {
            DeveloperFileUtils.deleteAndCreateFile(desIcon);
            DeveloperFileUtils.copyFile(icon, desIcon);
        } catch (IOException e) {
            // logger
            LOGGER.error("Create app icon file failed {}", e.getMessage());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Create app icon file failed");
            return Either.left(error);
        }
        // 2 upload to app store
        Map<String, Object> map = new HashMap<>();
        map.put("file", new FileSystemResource(csar));
        map.put("icon", new FileSystemResource(desIcon));
        map.put("type", project.getType());
        map.put("shortDesc", project.getDescription());
        map.put("affinity", StringUtils.join(project.getPlatform().toArray(), ","));
        map.put("industry", StringUtils.join(project.getIndustry().toArray(), ","));
        // add Field testTaskId
        map.put("testTaskId", releaseConfig.getAtpTest().getId());
        ResponseEntity<String> uploadReslut = AppStoreUtil.storeToAppStore(map, userId, userName, token);
        if (uploadReslut == null) {
            LOGGER.error("upload app to appstore fail!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "upload app to appstore fail!");
            return Either.left(error);
        }
        LOGGER.info("upload appstore result:{}", uploadReslut);
        JsonObject jsonObject = new JsonParser().parse(uploadReslut.getBody()).getAsJsonObject();
        LOGGER.info("upload over! {}", uploadReslut.getBody());
        return Either.right(jsonObject);
    }

    private void fillCapabilityDetail(ServiceDetail serviceDetail, Capability detail, JsonObject obj, String userId,
        String groupId) {
        detail.setId(UUID.randomUUID().toString());
        detail.setName(serviceDetail.getServiceName());
        detail.setNameEn(serviceDetail.getServiceName());
        detail.setVersion(serviceDetail.getVersion());
        detail.setDescription("");
        detail.setDescriptionEn("");
        JsonElement provider = obj.get("provider");
        if (provider != null) {
            detail.setProvider(provider.getAsString());
        } else {
            detail.setProvider("");
        }
        detail.setApiFileId(serviceDetail.getApiJson());
        detail.setGuideFileId(serviceDetail.getApiMd());
        detail.setGuideFileIdEn(serviceDetail.getApiMd());
        detail.setUploadTime(new Date().getTime());
        detail.setPort(serviceDetail.getInternalPort());
        detail.setHost(serviceDetail.getServiceName());
        detail.setProtocol(serviceDetail.getProtocol());
        detail.setAppId(obj.get("appId").getAsString());
        detail.setPackageId(obj.get("packageId").getAsString());
        detail.setUserId(userId);
        detail.setSelectCount(0);
        detail.setIconFileId(serviceDetail.getIconFileId());
        detail.setAuthor(serviceDetail.getAuthor());
        detail.setExperienceUrl(serviceDetail.getExperienceUrl());
    }

    private void fillCapabilityGroup(ServiceDetail serviceDetail, String groupId, CapabilityGroup group) {
        group.setId(groupId);
        group.setName(serviceDetail.getOneLevelName());
        group.setNameEn(serviceDetail.getOneLevelNameEn());
        group.setDescription(serviceDetail.getDescription());
        group.setDescriptionEn(serviceDetail.getDescription());
        group.setType(EnumOpenMepType.OPENMEP.toString());
        group.setIconFileId(serviceDetail.getIconFileId());
        group.setCreateTime(new Date().getTime());
        group.setUpdateTime(new Date().getTime());
        group.setAuthor(serviceDetail.getAuthor());
    }

    /**
     * cleanTestEnv.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> cleanTestEnv(String userId, String projectId, String token) {
        Map<String, Object> sshMap = webSshService.getSshMap();
        Map<String, String> userIdMap = webSshService.getUserIdMap();
        String uuid = "";
        SshConnectInfo sshConnectInfo = null;
        if (userIdMap != null && !userIdMap.isEmpty()) {
            uuid = userIdMap.get(userId);
        }
        if (sshMap != null && !sshMap.isEmpty()) {
            sshConnectInfo = (SshConnectInfo) sshMap.get(uuid);
        }
        if (sshConnectInfo != null) {
            // Disconnect
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            // mapRemove
            sshMap.remove(uuid);
        }
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find project by userId and projectId");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "can not find project");
            return Either.left(error);
        }
        ProjectTestConfig testConfig = projectMapper.getTestConfig(project.getLastTestId());
        if (testConfig == null) {
            LOGGER.info("This project has no config, do not need to clean env.");
            return Either.right(true);
        }

        deleteDeployApp(testConfig, project.getUserId(), token);

        // init project and config
        testConfig.initialConfig();
        project.initialProject();
        int res = projectMapper.updateProject(project);
        if (res < 1) {
            LOGGER.error("Update project status failed");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "update project failed");
            return Either.left(error);
        }
        LOGGER.info("Update project status to TESTED success");

        int tes = projectMapper.updateTestConfig(testConfig);
        if (tes < 1) {
            LOGGER.error("Update test config {} failed", testConfig.getTestId());
        }
        LOGGER.info("Update test config {} status to Deleted success", testConfig.getTestId());
        return Either.right(true);
    }

    private Boolean modifyHostStatus(ProjectTestConfig testConfig, ApplicationProject project, String operation) {
        if (!CollectionUtils.isEmpty(testConfig.getHosts())) {
            Type type = new TypeToken<List<MepHost>>() { }.getType();
            List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
            MepHost host = hosts.get(0);
            // save host logs
            MepHostLog mepHostLog = new MepHostLog();
            mepHostLog.setAppInstancesId(testConfig.getAppInstanceId());
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            mepHostLog.setDeployTime(time.format(new Date()));
            mepHostLog.setHostId(host.getHostId());
            mepHostLog.setHostIp(host.getLcmIp());
            mepHostLog.setLogId(UUID.randomUUID().toString());
            mepHostLog.setUserId(project.getUserId());
            mepHostLog.setProjectId(project.getId());
            mepHostLog.setProjectName(project.getName());
            mepHostLog.setAppInstancesId(testConfig.getAppInstanceId());
            mepHostLog.setStatus(host.getStatus());
            mepHostLog.setOperation(operation);
            int res = hostLogMapper.insert(mepHostLog);
            if (res < 1) {
                LOGGER.error("save host logs error");
                return false;
            }
        }
        return true;
    }

    /**
     * updateDeployResult.
     *
     * @return
     */
    @Transactional
    public void updateDeployResult(ProjectTestConfig testConfig, ApplicationProject project, String stage,
        EnumTestConfigStatus stageStatus) {
        LOGGER.info("Update deploy test on stage:{} status: {}", stage, stageStatus);
        // update test config always && update product if necessary
        switch (stage) {
            case "csar":
                testConfig.getStageStatus().setCsar(stageStatus);
                break;
            case "hostInfo":
                testConfig.getStageStatus().setHostInfo(stageStatus);
                break;
            case "instantiateInfo":
                testConfig.getStageStatus().setInstantiateInfo(stageStatus);
                break;
            case "workStatus":
                testConfig.getStageStatus().setWorkStatus(stageStatus);
                break;
            default:
                testConfig.setStageStatus(new ProjectTestConfigStageStatus());
                break;
        }
        boolean productUpdate = false;
        LOGGER.info("get workStatus status:{}, stage:{}", stageStatus, stage);
        if (EnumTestConfigStatus.Success.equals(stageStatus) && "hostInfo".equalsIgnoreCase(stage)) {
            // modify host status save host logs
            modifyHostStatus(testConfig, project, "instantiate");
        }
        if (EnumTestConfigStatus.Success.equals(stageStatus) && "workStatus".equalsIgnoreCase(stage)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED);
            testConfig.setErrorLog("");
            testConfig.setDeployStatus(EnumTestConfigDeployStatus.SUCCESS);
        } else if (EnumTestConfigStatus.Failed.equals(stageStatus)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED_FAILED);
            testConfig.setDeployStatus(EnumTestConfigDeployStatus.FAILED);
        }
        // update status if necessary
        if (productUpdate) {
            int res = projectMapper.updateProject(project);
            if (res < 1) {
                LOGGER.error("Update project {} error.", project.getId());
            }
        }

        int tes = projectMapper.updateTestConfig(testConfig);
        if (tes < 1) {
            LOGGER.error("Update test-config {} error.", testConfig.getTestId());
        }
        // delete resource after deploying failed
        if (EnumTestConfigStatus.Failed.equals(stageStatus) && testConfig.getWorkLoadId() != null) {
            deleteDeployApp(testConfig, project.getUserId(), testConfig.getLcmToken());
            LOGGER.warn("Deploy failed, delete deploy app.");
        }
    }

    /**
     * createAtpTestTask.
     */
    public Either<FormatRespDto, Boolean> createAtpTestTask(String projectId, String token, String userId) {
        String path = AtpUtil.getProjectPath(projectId);
        String fileName = getFileName(projectId);
        if (StringUtils.isEmpty(fileName)) {
            String msg = "get file name is null";
            LOGGER.error(msg);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, msg);
            return Either.left(error);
        }

        File csar = new File(path.concat(fileName).concat(".csar"));
        ResponseEntity<String> response = AtpUtil.sendCreatTask2Atp(csar.getPath(), token);
        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
        LOGGER.info("atp test result:{}", jsonObject);

        if (null == jsonObject) {
            String msg = "response from atp is null.";
            LOGGER.error(msg);
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, msg);
            return Either.left(error);
        }

        AtpResultInfo atpResultInfo = new AtpResultInfo();
        JsonElement id = jsonObject.get("id");
        JsonElement appName = jsonObject.get("appName");
        JsonElement status = jsonObject.get("status");
        JsonElement createTime = jsonObject.get("createTime");
        if (null != id) {
            atpResultInfo.setId(id.getAsString());
            atpResultInfo.setAppName(null != appName ? appName.getAsString() : null);
            atpResultInfo.setStatus(null != status ? status.getAsString() : null);
            atpResultInfo.setCreateTime(null != createTime ? createTime.getAsString() : null);
        }
        LOGGER.info("atp status:{}", atpResultInfo.getStatus());

        // save to db
        ReleaseConfig config = new ReleaseConfig();
        config.setProjectId(projectId);
        config.setAtpTest(atpResultInfo);
        LOGGER.info("update release config:{}", config);
        configMapper.updateAtpStatus(config);
        return Either.right(true);
    }

    private String getFileName(String projectId) {
        ApplicationProject applicationProject = projectMapper.getProjectById(projectId);
        if (applicationProject.getDeployPlatform() == EnumDeployPlatform.KUBERNETES) {
            List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
            if (CollectionUtils.isEmpty(testConfigList)) {
                LOGGER.info("This project has not test config, do not terminate.");
                return null;
            }
            ProjectTestConfig testConfig = testConfigList.get(0);
            return null != testConfig ? testConfig.getAppInstanceId() : null;
        } else {
            VmPackageConfig vmCreateConfig = vmConfigMapper.getVmPackageConfig(projectId);
            if (vmCreateConfig == null) {
                LOGGER.info("This project has not vm config, do not terminate.");
                return null;
            }
            return vmCreateConfig.getAppInstanceId();
        }

    }

    /**
     * getWorkStatus.
     *
     * @param projectId projectId
     * @param token token
     * @return
     */
    public Either<FormatRespDto, Boolean> getWorkStatus(String projectId, String token) {
        ApplicationProject project = projectMapper.getProjectById(projectId);

        if (project == null) {
            LOGGER.warn("Can not find the project projectId {}.", projectId);
            return Either.right(false);
        }
        List<ProjectTestConfig> configs = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(configs)) {
            LOGGER.warn("Can not find the test config by projectId {}.", projectId);
            return Either.right(false);
        }
        String userId = project.getUserId();
        ProjectTestConfig config = configs.get(0);
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(config.getHosts()), type);
        MepHost host = hosts.get(0);
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(), userId,
                token);
        LOGGER.info("pod workStatus: {}", workStatus);
        String workEvents = HttpClientUtil
            .getWorkloadEvents(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(), userId,
                token);
        LOGGER.info("pod workEvents: {}", workEvents);
        if (workStatus == null || workEvents == null) {
            LOGGER.error("get pod workStatus {} error.", config.getTestId());
            return Either.right(false);
        }
        String pods = mergeStatusAndEvents(workStatus, workEvents);
        config.setPods(pods);
        int tes = projectMapper.updateTestConfig(config);
        if (tes < 1) {
            LOGGER.error("Update test-config {} error.", config.getTestId());
            return Either.right(false);
        }
        return Either.right(true);
    }

    private String mergeStatusAndEvents(String workStatus, String workEvents) {
        Gson gson = new Gson();
        Type type = new TypeToken<PodStatusInfos>() { }.getType();
        PodStatusInfos status = gson.fromJson(workStatus, type);

        Type typeEvents = new TypeToken<PodEventsRes>() { }.getType();
        PodEventsRes events = gson.fromJson(workEvents, typeEvents);
        String pods = "";
        if (!CollectionUtils.isEmpty(status.getPods()) && !CollectionUtils.isEmpty(events.getPods())) {
            List<PodStatusInfo> statusInfos = status.getPods();
            List<PodEvents> eventsInfos = events.getPods();
            for (int i = 0; i < statusInfos.size(); i++) {
                for (int j = 0; j < eventsInfos.size(); j++) {
                    if (statusInfos.get(i).getPodname().equals(eventsInfos.get(i).getPodName())) {
                        statusInfos.get(i).setPodEventsInfo(eventsInfos.get(i).getPodEventsInfo());
                    }
                }
            }
            pods = gson.toJson(status);
        }
        return pods;
    }

    /**
     * deleteDeployApp.
     *
     * @return
     */
    private boolean deleteDeployApp(ProjectTestConfig testConfig, String userId, String token) {
        String appInsId = testConfig.getAppInstanceId();

        if (!CollectionUtils.isEmpty(testConfig.getHosts()) && StringUtils.isNotEmpty(appInsId)) {
            Type type = new TypeToken<List<MepHost>>() { }.getType();
            List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
            MepHost host = hosts.get(0);
            String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
            if (StringUtils.isNotEmpty(testConfig.getAppInstanceId())) {
                HttpClientUtil.terminateAppInstance(basePath, appInsId, userId, token);
            }
            if (StringUtils.isNotEmpty(testConfig.getPackageId())) {
                // delete hosts
                boolean deleteHostRes = HttpClientUtil
                    .deleteHost(basePath, userId, token, testConfig.getPackageId(), host.getLcmIp());

                // delete pkg
                boolean deletePkgRes = HttpClientUtil.deletePkg(basePath, userId, token, testConfig.getPackageId());
                if (!deleteHostRes || !deletePkgRes) {
                    return false;
                }
            }

        }

        return true;
    }

    /**
     * cleanUnreleasedEnv.
     */
    public void cleanUnreleasedEnv() {
        // Get all items
        List<ApplicationProject> projects = projectMapper.getAllProjectNoCondtion();
        if (CollectionUtils.isEmpty(projects)) {
            LOGGER.warn("DB have no record of app project!");
            return;
        }
        // log inuser-mgmt
        // Call by service nameuser-mgmtLogin interface
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
            URL url = new URL(loginUrl);
            String userLoginUrl = url.getProtocol() + "://" + url.getAuthority() + "/login";
            LOGGER.warn("user login url: {}", userLoginUrl);
            HttpPost httpPost = new HttpPost(userLoginUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("username", clientId + ":" + new Date().getTime());
            builder.addTextBody("password", clientPW);
            httpPost.setEntity(builder.build());
            // first call login interface
            client.execute(httpPost);
            String xsrf = getXsrf();
            httpPost.setHeader("X-XSRF-TOKEN", xsrf);
            // secode call login interface
            client.execute(httpPost);
            String xsrfToken = getXsrf();
            // third call auth login-info interface
            String getTokenUrl = url.getProtocol() + "://" + url.getHost() + ":30092/auth/login-info";
            LOGGER.warn("user login-info url: {}", getTokenUrl);
            HttpGet httpGet = new HttpGet(getTokenUrl);
            httpGet.setHeader("X-XSRF-TOKEN", xsrfToken);
            CloseableHttpResponse res = client.execute(httpGet);
            InputStream inputStream = res.getEntity().getContent();
            byte[] bytes = new byte[inputStream.available()];
            int byteNums = inputStream.read(bytes);
            if (byteNums <= 0) {
                LOGGER.warn("not get any response from login-info interface");
                return;
            }
            String authResult = new String(bytes, StandardCharsets.UTF_8);
            LOGGER.info("response token length: {}", authResult.length());
            // 获取accessToken
            String accessToken = "";
            if (StringUtils.isNotEmpty(authResult)) {
                if (authResult.contains("\"accessToken\":")) {
                    String[] authResults = authResult.split(",");
                    for (String authRes : authResults) {
                        if (authRes.contains("accessToken")) {
                            String[] tokenArr = authRes.split(":");
                            if (tokenArr != null && tokenArr.length > 1) {
                                accessToken = tokenArr[1].substring(1, tokenArr[1].length() - 1);
                            }
                        }
                    }
                } else {
                    cleanUnreleasedEnv();
                }
            }
            // Determine the status of the existing project as successful deployment or
            // deployment failure，And the project creation time has
            // exceeded24hour，transfercleanenvinterface
            for (ApplicationProject project : projects) {
                EnumProjectStatus status = project.getStatus();
                String createDate = project.getCreateDate();
                DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Instant dateOfProject = fmt.parse(createDate).toInstant();
                Instant now = Instant.now();
                Long timeDiff = Duration.between(dateOfProject, now).toHours();
                if (status.equals(EnumProjectStatus.DEPLOYED)
                    || status.equals(EnumProjectStatus.DEPLOYED_FAILED) && StringUtils.isNotEmpty(accessToken)
                    && timeDiff.intValue() >= 24) {
                    if (project.getDeployPlatform().equals("KUBERNETES")) {
                        cleanTestEnv(project.getUserId(), project.getId(), accessToken);
                    } else {
                        vmService.cleanVmDeploy(project.getId(), accessToken);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            if (e instanceof IOException && StringUtils.isEmpty(e.getMessage())) {
                cleanUnreleasedEnv();
            }
            LOGGER.error("call login or clean env interface occur error {}", e.getMessage());
            return;
        }

    }

    private static String getXsrf() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("XSRF-TOKEN")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private static CloseableHttpClient createIgnoreSslHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(cookieStore).setRedirectStrategy(new DefaultRedirectStrategy()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
