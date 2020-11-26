/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigDeployStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfigStageStatus;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.ProjectImageResponse;
import org.edgegallery.developer.service.csar.CreateCsarFromTemplate;
import org.edgegallery.developer.service.csar.NewCreateCsar;
import org.edgegallery.developer.service.dao.ProjectDao;
import org.edgegallery.developer.service.deploy.IConfigDeployStage;
import org.edgegallery.developer.template.ChartFileCreator;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("projectService")
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private static Gson gson = new Gson();

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private HelmTemplateYamlMapper helmTemplateYamlMapper;

    @Autowired
    private OpenMepCapabilityMapper openMepCapabilityMapper;

    @Autowired
    private ProjectDao projectDto;

    @Autowired
    private UtilsService utilsService;

    @Autowired
    private Map<String, IConfigDeployStage> deployServiceMap;

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
        project.setCreateDate(new Date());
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
            LOGGER.info("Can not find project by userId {} and projectId {}, do not need delete.", userId, projectId);
            return Either.right(true);
        }

        //delete capabilityGroup and
        String openCapabilityDetailId = project.getOpenCapabilityId();
        LOGGER.info("detailId: {} .", openCapabilityDetailId);
        String groupId = "";
        if (openCapabilityDetailId != null) {
            groupId = openMepCapabilityMapper.getGroupIdByDetailId(openCapabilityDetailId);
            openMepCapabilityMapper.deleteCapability(openCapabilityDetailId);
        }

        if (!groupId.equals("")) {
            LOGGER.info("groupId: {} .", groupId);
            List<OpenMepCapabilityDetail> detailList = openMepCapabilityMapper.getDetailByGroupId(groupId);
            if (detailList != null) {
                LOGGER.info("detailList size: {} .", detailList.size());
                if (detailList.size() < 1) {
                    openMepCapabilityMapper.deleteGroup(groupId);
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
            LOGGER.error("Deploy project config:{} failed on stage :{}.", config.getTestId(), nextStage);
        }
    }

    /**
     * terminateProject.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> terminateProject(String userId, String projectId, String token) {
        ProjectTestConfig testConfig = projectMapper.getTestConfig(projectId);
        if (testConfig == null) {
            LOGGER.info("This project has not test config, do not terminate.");
            return Either.right(true);
        }
        if (!EnumTestConfigStatus.Success.equals(testConfig.getStageStatus().getInstantiateInfo())
            || testConfig.getWorkLoadId() == null) {
            LOGGER.error("Failed to terminate application when instantiateInfo not success.");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                "Failed to terminate application when instantiateInfo not success.");
            return Either.left(error);
        }
        MepHost host = testConfig.getHosts().get(0);
        boolean terminateResult = HttpClientUtil
            .terminateAppInstance(host.getProtocol(), host.getIp(), host.getPort(), testConfig.getAppInstanceId(),
                userId, token);
        if (!terminateResult) {
            LOGGER.error("Failed to terminate application which userId is: {}, instanceId is {}", userId,
                testConfig.getAppInstanceId());
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Failed to terminate application.");
            return Either.left(error);
        }
        // update config status
        testConfig.setDeployStatus(EnumTestConfigDeployStatus.TERMINATE);
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
        List<HelmTemplateYamlPo> yamlPoList = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
        File csarPkgDir;
        if (!CollectionUtils.isEmpty(yamlPoList)) {
            // create chart file
            ChartFileCreator chartFileCreator = new ChartFileCreator();
            chartFileCreator.setChartName(projectName);
            if (mepCapability == null || mepCapability.size() == 0) {
                chartFileCreator.setChartValues("false", "false", "default");
            } else {
                chartFileCreator.setChartValues("true", "false", "default");
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
        String appInstanceId = testConfig.getAppInstanceId();
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
        MepHost host = hosts.get(0);
        // Note(ch) only ip?
        testConfig.setAccessUrl("https://" + host.getIp());
        return HttpClientUtil
            .instantiateApplication(host.getProtocol(), host.getIp(), host.getPort(), csar.getPath(), appInstanceId,
                userId, token);
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
        int ret = projectMapper.updateTestConfig(testConfig);
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
        List<ProjectTestConfig> tests = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(tests)) {
            LOGGER.warn("Can not find the test config by projectId {}", projectId);
            return Either.right(null);
        } else {
            LOGGER.info("Get test config {} success.", tests.get(0).getTestId());
            return Either.right(tests.get(0));
        }
    }

    /**
     * uploadToAppStore.
     *
     * @return
     */
    public Either<FormatRespDto, String> uploadToAppStore(String userId, String projectId, String appInstanceId,
        String userName, String token) {
        // 0 check data. must be tested, and deployed status must be ok, can not be error.
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not get project by inputs of userId and projectId.");
            String message = "Can not get project, userId or projectId is error.";
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, message));

        }
        if (project.getStatus() != EnumProjectStatus.TESTED) {
            LOGGER.error("Status {} is not TESTED, can not upload to appstore", project.getStatus());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Only TESTED App can be upload.");
            return Either.left(error);
        }
        ProjectTestConfig testConfig = projectMapper.getTestConfig(project.getLastTestId());
        if (testConfig == null) {
            LOGGER.error("Can not find project by project id.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "can not get test config");
            return Either.left(error);
        }

        // 1 get CSAR package
        File csar = new File(getProjectPath(projectId) + appInstanceId + ".csar");
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
        return utilsService.storeToAppStore(map, userId, userName, token);
    }

    /**
     * createProjectImage.
     *
     * @return
     */
    public Either<FormatRespDto, ProjectImageConfig> createProjectImage(String projectId,
        ProjectImageConfig imageConfig) {
        if (imageConfig.getName() == null) {
            LOGGER.error("Crete project image failed, image name is error");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Create project image error"));
        }
        imageConfig.setId(UUID.randomUUID().toString());
        imageConfig.setProjectId(projectId);
        int res = projectMapper.saveImageConfig(imageConfig);
        if (res > 0) {
            LOGGER.info("Create project image {} success", imageConfig.getId());
            return Either.right(imageConfig);
        }
        LOGGER.error("Create project image error ");
        FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "create project image error");
        return Either.left(error);
    }

    /**
     * deleteImageById.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> deleteImageById(String projectId, String imageId) {
        ProjectImageConfig imageConfig = projectMapper.getImageConfigByImageId(imageId);
        if (imageConfig == null || !imageConfig.getProjectId().equals(projectId)) {
            LOGGER.error("Can not find image {} with projectId {}", imageId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "data error");
            return Either.left(error);
        }
        projectMapper.deleteImageConfigByImageId(imageId);
        LOGGER.info("Delete image {} success.", imageId);
        return Either.right(true);
    }

    /**
     * getImagesByProjectId.
     *
     * @return
     */
    public Either<FormatRespDto, ProjectImageResponse> getImagesByProjectId(String projectId) {
        List<ProjectImageConfig> imageConfigList = projectMapper.getImageConfigByProjectId(projectId);
        ProjectImageResponse res = new ProjectImageResponse();
        res.setImages(imageConfigList);
        LOGGER.info("Get project {} images success", projectId);
        return Either.right(res);
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
            group.setName(project.getType());
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
        detail.setGroupId(groupId);
        detail.setService(project.getName());
        detail.setVersion(project.getVersion());
        detail.setDescription(project.getDescription());
        detail.setProvider(project.getProvider());
        detail.setApiFileId(test.getAppApiFileId());

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
    public Either<FormatRespDto, Boolean> cleanTestEnv(String userId, String projectId) {
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
        if (EnumTestConfigStatus.Success.equals(stageStatus) && "workStatus".equalsIgnoreCase(stage)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED);
            testConfig.setDeployStatus(EnumTestConfigDeployStatus.SUCCESS);
        } else if (EnumTestConfigStatus.Failed.equals(stageStatus)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED_FAILED);
            testConfig.setDeployStatus(EnumTestConfigDeployStatus.FAILED);
        }
        // update status if necessary
        if (productUpdate == true) {
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
            deleteDeployApp(testConfig, AccessUserUtil.getUserId(), testConfig.getLcmToken());
            LOGGER.warn("Deploy failed, delete deploy app.");
        }
    }

    /**
     * deleteDeployApp.
     *
     * @return
     */
    private boolean deleteDeployApp(ProjectTestConfig testConfig, String userId, String token) {
        String workloadId = testConfig.getWorkLoadId();
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
        MepHost host = hosts.get(0);
        return HttpClientUtil
            .terminateAppInstance(host.getProtocol(), host.getIp(), host.getPort(), workloadId, userId, token);
    }
}
