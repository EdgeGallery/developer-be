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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
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
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
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
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectImageMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.ReleaseConfigMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.CapabilitiesDetail;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.model.ServiceDetail;
import org.edgegallery.developer.model.SshConnectInfo;
import org.edgegallery.developer.model.atp.AtpResultInfo;
import org.edgegallery.developer.model.deployyaml.PodEvents;
import org.edgegallery.developer.model.deployyaml.PodEventsRes;
import org.edgegallery.developer.model.deployyaml.PodStatusInfo;
import org.edgegallery.developer.model.deployyaml.PodStatusInfos;
import org.edgegallery.developer.model.lcm.UploadResponse;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigDeployStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.MepCreateHost;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfigStageStatus;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.csar.NewCreateCsar;
import org.edgegallery.developer.service.dao.ProjectDao;
import org.edgegallery.developer.service.deploy.IConfigDeployStage;
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
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private ProjectDao projectDto;

    @Autowired
    private Map<String, IConfigDeployStage> deployServiceMap;

    @Autowired
    private ProjectImageMapper projectImageMapper;

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private WebSshService webSshService;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    /**
     * getAllProjects.
     *
     * @return
     */
    public Either<FormatRespDto, List<ApplicationProject>> getAllProjects(String userId) {
        List<ApplicationProject> allProjects = projectMapper.getAllProject(userId);
        LOGGER.info("get all projects success.");
        return Either.right(allProjects);
    }

    /**
     * getProject.
     *
     * @return
     */
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

    /**
     * createProject.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, ApplicationProject> createProject(String userId, ApplicationProject project)
        throws IOException {
        project.setUserId(userId);
        String projectId = UUID.randomUUID().toString();
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteAndCreateDir(projectPath);
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
        return Either.right(projectMapper.getProject(userId, project.getId()));
    }

    public String getProjectPath(String projectId) {
        return InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getWorkspacePath() + projectId
            + File.separator;
    }

    /**
     * moveFileToWorkSpaceById.
     */
    private void moveFileToWorkSpaceById(String srcId, String projectId) throws IOException {

        // at firstly, update the file status in DB, then this file should not be delete by timer
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
    public Either<FormatRespDto, Boolean> deleteProject(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.warn("Can not find project by userId {} and projectId {}, do not need delete.", userId, projectId);
            return Either.right(true);
        }

        //delete capabilityGroup and CapabilityDetail
        String openCapabilityDetailId = project.getOpenCapabilityId();
        LOGGER.info("detailId: {} .", openCapabilityDetailId);
        String groupId = "";
        if (!StringUtils.isEmpty(openCapabilityDetailId)) {
            String[] ids = openCapabilityDetailId.substring(1, openCapabilityDetailId.length() - 1).split(",");
            for (String detailId : ids) {
                groupId = openMepCapabilityMapper.getGroupIdByDetailId(detailId);
                openMepCapabilityMapper.deleteCapability(detailId);
                if (!groupId.equals("")) {
                    LOGGER.info("groupId: {} .", groupId);
                    List<OpenMepCapabilityDetail> detailList = openMepCapabilityMapper.getDetailByGroupId(groupId);
                    if (detailList != null) {
                        LOGGER.info("detailList size: {} .", detailList.size());
                        if (detailList.isEmpty()) {
                            openMepCapabilityMapper.deleteGroup(groupId);
                        }
                    }
                }
            }
        }

        // delete the project from db
        Either<FormatRespDto, Boolean> delResult = projectDto.deleteProject(userId, projectId);
        if (delResult.isLeft()) {
            return delResult;
        }

        // delete files of project
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteDir(projectPath);

        LOGGER.info("Delete project {} success.", projectId);
        return Either.right(true);
    }

    /**
     * modifyProject.
     *
     * @return
     */
    public Either<FormatRespDto, ApplicationProject> modifyProject(String userId, String projectId,
        ApplicationProject newProject) {
        ApplicationProject oldProject = projectMapper.getProject(userId, projectId);
        if (oldProject == null) {
            LOGGER.error("Can not find project by userId {} and projectId {}.", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, Consts.RESPONSE_MESSAGE_CAN_NOT_FIND_PROJECT);
            return Either.left(error);
        }

        newProject.setId(projectId);
        newProject.setUserId(userId);

        int res = projectMapper.updateProject(newProject);
        if (res < 1) {
            LOGGER.error("Update project {} failed", newProject.getId());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Update project failed.");
            return Either.left(error);
        }
        LOGGER.info("Update project {} success.", projectId);
        return Either.right(projectMapper.getProject(userId, projectId));
    }

    /**
     * processDeploy.
     * task job for scheduler
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
        boolean terminateResult = HttpClientUtil
            .terminateAppInstance(host.getProtocol(), host.getLcmIp(), host.getPort(), testConfig.getAppInstanceId(),
                userId, token);
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
        List<OpenMepCapabilityGroup> mepCapability = project.getCapabilityList();
        String projectPath = getProjectPath(projectId);
        String projectName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();
        String configMapName = "mepagent" + UUID.randomUUID().toString();
//        String namespace = projectName + UUID.randomUUID().toString().substring(0, 8);
        List<HelmTemplateYamlPo> yamlPoList = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
        File csarPkgDir;
        if (!CollectionUtils.isEmpty(yamlPoList)) {
            // create chart file
            ChartFileCreator chartFileCreator = new ChartFileCreator(projectName);
            chartFileCreator.setChartName(projectName);
            if (mepCapability == null || mepCapability.isEmpty()) {
                chartFileCreator
                    .setChartValues("false", "false", "default", configMapName, imageDomainName, imageProject);
            } else {
                chartFileCreator
                    .setChartValues("true", "false", "default", configMapName, imageDomainName, imageProject);
            }
            //stop
            yamlPoList.forEach(helmTemplateYamlPo -> chartFileCreator
                .addTemplateYaml(helmTemplateYamlPo.getFileName(), helmTemplateYamlPo.getContent()));
            String tgzFilePath = chartFileCreator.build();

            // create csar file directory
            csarPkgDir = new NewCreateCsar().create(projectPath, testConfig, project, new File(tgzFilePath));
        } else {
            csarPkgDir = new NewCreateCsar().create(projectPath, testConfig, project, null);
        }
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
        String uploadRes = HttpClientUtil
            .uploadPkg(host.getProtocol(), host.getLcmIp(), host.getPort(), csar.getPath(), userId, token, lcmLog);
        if (org.springframework.util.StringUtils.isEmpty(uploadRes)) {
            testConfig.setErrorLog(lcmLog.getLog());
            return false;
        }
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<UploadResponse>() { }.getType();
        UploadResponse uploadResponse = gson.fromJson(uploadRes, typeEvents);
        String pkgId = uploadResponse.getPackageId();
        testConfig.setPackageId(pkgId);
        projectMapper.updateTestConfig(testConfig);
        // distribute pkg
        boolean distributeRes = HttpClientUtil
            .distributePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token, pkgId, host.getMecHost(),
                lcmLog);

        if (!distributeRes) {
            testConfig.setErrorLog(lcmLog.getLog());
            return false;
        }
        String appInstanceId = testConfig.getAppInstanceId();
        // instantiate application
        boolean instantRes = HttpClientUtil
            .instantiateApplication(host.getProtocol(), host.getLcmIp(), host.getPort(), appInstanceId, userId, token,
                lcmLog, pkgId, host.getMecHost());
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
        Optional<List<OpenMepCapabilityGroup>> groups = Optional.ofNullable(project.getCapabilityList());
        if (!groups.isPresent()) {
            LOGGER.error("the project being deployed does not have any capabilities selected ");
            return true;
        }
        Gson gsonGroup = new Gson();
        Type groupType = new TypeToken<List<OpenMepCapabilityGroup>>() { }.getType();
        List<OpenMepCapabilityGroup> capabilities = gsonGroup
            .fromJson(gsonGroup.toJson(project.getCapabilityList()), groupType);
        for (OpenMepCapabilityGroup group : capabilities) {
            List<OpenMepCapabilityDetail> openMepCapabilityGroups = group.getCapabilityDetailList();
            Type openMepCapabilityType = new TypeToken<List<OpenMepCapabilityDetail>>() { }.getType();
            List<OpenMepCapabilityDetail> openMepCapabilityDetails = gsonGroup
                .fromJson(gsonGroup.toJson(openMepCapabilityGroups), openMepCapabilityType);
            for (OpenMepCapabilityDetail detail : openMepCapabilityDetails) {
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
        // 0 check data. must be tested, and deployed status must be ok, can not be error.
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
            //save db to openmepcapabilitydetail
            List<String> openCapabilityIds = new ArrayList<>();
            for (ServiceDetail serviceDetail : capabilitiesDetail.getServiceDetails()) {
                OpenMepCapabilityDetail detail = new OpenMepCapabilityDetail();
                OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
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

    private Either<FormatRespDto, Boolean> doSomeDbOperation(OpenMepCapabilityGroup group,
        OpenMepCapabilityDetail detail, ServiceDetail serviceDetail, List<String> openCapabilityIds) {
        if (StringUtils.isEmpty(group.getDescriptionEn())) {
            group.setDescriptionEn(group.getDescription());
        }

        if (StringUtils.isEmpty(group.getOneLevelNameEn())) {
            group.setOneLevelNameEn(group.getOneLevelName());
        }
        if (StringUtils.isEmpty(group.getTwoLevelNameEn())) {
            group.setTwoLevelNameEn(group.getTwoLevelName());
        }
        if (StringUtils.isEmpty(detail.getServiceEn())) {
            detail.setServiceEn(detail.getService());
        }
        if (StringUtils.isEmpty(detail.getGuideFileIdEn())) {
            detail.setGuideFileIdEn(detail.getGuideFileId());
        }
        if (StringUtils.isEmpty(detail.getDescriptionEn())) {
            detail.setDescriptionEn(detail.getDescription());
        }
        int resGroup = openMepCapabilityMapper.saveGroup(group);
        if (resGroup < 1) {
            LOGGER.error("store db to openmepcapability fail!");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "save openmepcapability db fail!");
            return Either.left(error);
        }
        int res = openMepCapabilityMapper.saveCapability(detail);
        if (res < 1) {
            LOGGER.error("store db to openmepcapabilitydetail fail!");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                "save openmepcapabilitydetail db fail!");
            return Either.left(error);
        }
        //set open_capability_id
        openCapabilityIds.add(detail.getDetailId());
        //update file status
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
        //publish app to appstore
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
        File desIcon = new File(iconPath + iconFile.getFileName());

        try {
            DeveloperFileUtils.deleteAndCreateFile(desIcon);
            DeveloperFileUtils.copyFile(icon, desIcon);
        } catch (IOException e) {
            // logger
            LOGGER.error("Create app icon file failed ", e.getMessage());
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
        //add Field testTaskId
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

    private void fillCapabilityDetail(ServiceDetail serviceDetail, OpenMepCapabilityDetail detail, JsonObject obj,
        String userId, String groupId) {
        detail.setDetailId(UUID.randomUUID().toString());
        detail.setGroupId(groupId);
        JsonElement provider = obj.get("provider");
        if (provider != null) {
            detail.setProvider(provider.getAsString());
        }
        detail.setService(serviceDetail.getServiceName());
        detail.setVersion(serviceDetail.getVersion());
        detail.setDescription(serviceDetail.getDescription());
        detail.setApiFileId(serviceDetail.getApiJson());
        detail.setGuideFileId(serviceDetail.getApiMd());
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        detail.setUploadTime(time.format(new Date()));
        detail.setPort(serviceDetail.getInternalPort());
        detail.setHost(serviceDetail.getServiceName());
        detail.setProtocol(serviceDetail.getProtocol());
        detail.setAppId(obj.get("appId").getAsString());
        detail.setPackageId(obj.get("packageId").getAsString());
        detail.setUserId(userId);
    }

    private void fillCapabilityGroup(ServiceDetail serviceDetail, String groupId, OpenMepCapabilityGroup group) {
        group.setGroupId(groupId);
        group.setOneLevelName(serviceDetail.getOneLevelName());
        group.setTwoLevelName(serviceDetail.getTwoLevelName());
        group.setType(EnumOpenMepType.OPENMEP);
        group.setDescription(serviceDetail.getDescription());
    }

    /**
     * openToMecEco.
     *
     * @return
     */
    public Either<FormatRespDto, OpenMepCapabilityGroup> openToMecEco(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        // verify app project and test config
        if (project == null) {
            LOGGER.error("Can not get project by userId {} and projectId {}", userId, projectId);
            return Either
                .left(new FormatRespDto(Status.BAD_REQUEST, "Can not get project, userId or projectId is error."));

        }
        if (project.getStatus() != EnumProjectStatus.TESTED) {
            LOGGER.error("Status {} is not TESTED, can not open to MEC eco", project.getStatus());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Only TESTED App can be open."));
        }
        ProjectTestConfig test = projectMapper.getTestConfig(project.getLastTestId());
        if (test == null) {
            LOGGER.error("Can not get test config by {}", project.getLastTestId());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Can not get test config."));
        }

        // if has opened, delete before
        String openCapabilityDetailId = project.getOpenCapabilityId();
        if (openCapabilityDetailId != null) {
            openMepCapabilityMapper.deleteCapability(openCapabilityDetailId);
        }

        OpenMepCapabilityGroup capabilityGroup = openMepCapabilityMapper.getEcoGroupByName(project.getType());
        String groupId;
        if (capabilityGroup == null) {
            OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
            groupId = UUID.randomUUID().toString();
            group.setGroupId(groupId);
            group.setOneLevelName(project.getType());
            group.setType(EnumOpenMepType.OPENMEP_ECO);
            group.setDescription("Open MEP ecology group.");

            int groupRes = openMepCapabilityMapper.saveGroup(group);
            if (groupRes < 1) {
                LOGGER.error("Create capability group failed {}", group.getGroupId());
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "create capability group failed");
                return Either.left(error);
            }
        } else {
            groupId = capabilityGroup.getGroupId();
        }

        OpenMepCapabilityDetail detail = new OpenMepCapabilityDetail();
        detail.setDetailId(UUID.randomUUID().toString());
        detail.setGroupId(groupId);
        detail.setService(project.getName());
        detail.setVersion(project.getVersion());
        detail.setDescription(project.getDescription());
        detail.setProvider(project.getProvider());
        detail.setApiFileId(test.getAppApiFileId());
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        detail.setUploadTime(time.format(new Date()));

        int detailRes = openMepCapabilityMapper.saveCapability(detail);
        if (detailRes < 1) {
            LOGGER.error("Create capability detail failed {}", detail);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "create capability detail failed");
            return Either.left(error);
        }

        OpenMepCapabilityGroup result = openMepCapabilityMapper.getOpenMepCapabilitiesByGroupId(groupId);
        if (result == null) {
            LOGGER.error("Crete capability {} failed", groupId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "create capability failed");
            return Either.left(error);
        }

        project.setOpenCapabilityId(detail.getDetailId());
        int updateRes = projectMapper.updateProject(project);
        if (updateRes < 1) {
            LOGGER.error("update project is_open error");
            openMepCapabilityMapper.deleteGroup(groupId);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "update project is_open error."));
        }

        LOGGER.info("Open {} to Mec Success", groupId);
        return Either.right(result);
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
            //断开连接
            if (sshConnectInfo.getChannel() != null) {
                sshConnectInfo.getChannel().disconnect();
            }
            //map中移除
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

        // modify host status save host logs
        modifyHostStatus(testConfig, project, "terminate");

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
            //set access url
            List<ProjectImageConfig> imageConfigs = projectImageMapper.getAllImage(project.getId());
            if (!CollectionUtils.isEmpty(imageConfigs) && stage.equals("workStatus")) {
                StringBuilder sb = new StringBuilder();
                Type type = new TypeToken<List<MepHost>>() { }.getType();
                List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
                MepHost host = hosts.get(0);
                ProjectImageConfig imageConfig = imageConfigs.get(0);
                LOGGER.warn("svcNodePort:" + imageConfig.getSvcNodePort());
                if (imageConfig.getSvcNodePort().contains(",")) {
                    String svcPort = imageConfig.getSvcNodePort();
                    String[] svcNodePorts = svcPort.substring(1, svcPort.length() - 1).split(",");
                    for (String svc : svcNodePorts) {
                        String node = "http://" + host.getLcmIp() + ":" + svc;
                        sb.append(node + ",");
                    }
                } else {
                    String svcPort = imageConfig.getSvcNodePort();
                    String node = "http://" + host.getLcmIp() + ":" + svcPort.substring(1, svcPort.length() - 1);
                    sb.append(node + ",");
                }
                LOGGER.warn("sb:" + sb.toString());
                testConfig.setAccessUrl(sb.toString().substring(0, sb.toString().length() - 1));
            }

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

        threadPool.execute(new GetAtpStatusProcessor(config, token));
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
            List<VmCreateConfig> vmCreateConfigs = vmConfigMapper.getVmCreateConfigs(projectId);
            if (CollectionUtils.isEmpty(vmCreateConfigs)) {
                LOGGER.info("This project has not vm config, do not terminate.");
                return null;
            }
            VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);
            return null != vmCreateConfig ? vmCreateConfig.getAppInstanceId() : null;
        }

    }

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

    private class GetAtpStatusProcessor implements Runnable {
        ReleaseConfig config;

        String token;

        public GetAtpStatusProcessor(ReleaseConfig config, String token) {
            this.config = config;
            this.token = token;
        }

        @Override
        public void run() {
            AtpResultInfo atpResultInfo = config.getAtpTest();
            String taskId = atpResultInfo.getId();
            atpResultInfo.setStatus(AtpUtil.getTaskStatusFromAtp(taskId, token));
            LOGGER.info("after status update: {}", config.getAtpTest().getStatus());
            configMapper.updateAtpStatus(config);
            ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
            //update project status
            if (config.getAtpTest().getStatus().equals("success")) {
                project.setStatus(EnumProjectStatus.TESTED);
            } else {
                project.setStatus(EnumProjectStatus.TESTING);
            }
            projectMapper.updateProject(project);
        }

    }

    /**
     * deleteDeployApp.
     *
     * @return
     */
    private boolean deleteDeployApp(ProjectTestConfig testConfig, String userId, String token) {
        String workloadId = testConfig.getWorkLoadId();

        if (!CollectionUtils.isEmpty(testConfig.getHosts())) {
            Type type = new TypeToken<List<MepHost>>() { }.getType();
            List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
            MepHost host = hosts.get(0);
            if (StringUtils.isNotEmpty(testConfig.getPackageId())) {
                // delete hosts
                boolean deleteHostRes = HttpClientUtil
                    .deleteHost(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token,
                        testConfig.getPackageId(), host.getLcmIp());

                // delete pkg
                boolean deletePkgRes = HttpClientUtil
                    .deletePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token,
                        testConfig.getPackageId());
                if (!deleteHostRes || !deletePkgRes) {
                    return false;
                }
            }
            if (StringUtils.isNotEmpty(workloadId)) {
                boolean terminateApp = HttpClientUtil
                    .terminateAppInstance(host.getProtocol(), host.getLcmIp(), host.getPort(), workloadId, userId,
                        token);
                if (!terminateApp) {
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
        //获取所有的项目
        List<ApplicationProject> projects = projectMapper.getAllProjectNoCondtion();
        if (CollectionUtils.isEmpty(projects)) {
            LOGGER.warn("DB have no record of app project!");
            return;
        }
        //登录user-mgmt
        //通过服务名调用user-mgmt的登录接口
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {
            URL url = new URL(loginUrl);
            String userLoginUrl = url.getProtocol() + "://" + url.getAuthority() + "/index.html";
            LOGGER.warn("user login url: {}", userLoginUrl);
            HttpPost httpPost = new HttpPost(userLoginUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("username", clientId + ":" + new Date().getTime());
            builder.addTextBody("password", clientPW);
            httpPost.setEntity(builder.build());
            client.execute(httpPost);
            String xsrf = getXsrf();
            httpPost.setHeader("X-XSRF-TOKEN", xsrf);
            client.execute(httpPost);
            //判断已有项目状态为部署成功或者部署失败，且项目创建时间距今已超24小时，调用cleanenv接口
            for (ApplicationProject project : projects) {
                EnumProjectStatus status = project.getStatus();
                if (status.equals(EnumProjectStatus.DEPLOYED) || status.equals(EnumProjectStatus.DEPLOYED_FAILED)) {
                    String devSvc = "http://developer-be-svc:9082";
                    String cleanUrl = String
                        .format(Consts.DEV_CLEAN_ENV_URL, devSvc, project.getId(), project.getUserId());
                    LOGGER.warn("clean env url {}", cleanUrl);
                    HttpPost httpClean = new HttpPost(cleanUrl);
                    httpClean.setHeader("X-XSRF-TOKEN", xsrf);
                    client.execute(httpClean);
                }
            }
        } catch (IOException e) {
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
