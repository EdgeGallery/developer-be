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
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.HelmTemplateYamlMapper;
import org.edgegallery.developer.mapper.OpenMepCapabilityMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.UploadedFileMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.CommonImage;
import org.edgegallery.developer.model.workspace.EnumImagePullPolicy;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestStatus;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.ProjectImageResponse;
import org.edgegallery.developer.service.csar.CreateCsarFromTemplate;
import org.edgegallery.developer.service.dao.ProjectDao;
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

    private static final int TIMES = Consts.QUERY_APPLICATIONS_TIMES;

    private static final int PERIOD = Consts.QUERY_APPLICATIONS_PERIOD;

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

    private String getProjectPath(String projectId) {
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
     * deployPorject.
     *
     * @return
     */
    // deploy this app to test hosts
    public Either<FormatRespDto, ApplicationProject> deployProject(String userId, String projectId, String token) {
        // check config. upload app image, hosts must be required.
        List<ProjectTestConfig> testConfigList = projectMapper.getTestConfigByProjectId(projectId);
        if (CollectionUtils.isEmpty(testConfigList)) {
            LOGGER.error("Can not find test config by project id.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find project test config.");
            return Either.left(error);
        }

        ProjectTestConfig testConfig = testConfigList.get(0);
        if (testConfig.getStatus() == EnumTestStatus.RUNNING && !deleteDeployApp(testConfig, userId, token)) {
            LOGGER.error("Delete deployed app failed.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Delete deployed app failed.");
            return Either.left(error);
        }

        List<String> imageIds = testConfig.getImageFileIds();
        if (CollectionUtils.isEmpty(imageIds)) {
            LOGGER.error("Image file id is null.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Image file id is null");
            return Either.left(error);
        }
        List<MepHost> hosts = testConfig.getHosts();
        if (CollectionUtils.isEmpty(hosts)) {
            LOGGER.error("Host config is null");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Host config is null");
            return Either.left(error);
        }

        // set the appImages and otherImages in testConfig
        for (String imageId : imageIds) {
            ProjectImageConfig imageConfig = projectMapper.getImageConfigByImageId(imageId);
            LOGGER.info("Set image id: {}", imageConfig.getId());

            CommonImage commonImage = new CommonImage();
            commonImage.setImageId(imageConfig.getId());
            commonImage.setImageName(imageConfig.getName());
            commonImage.setImagePullPolicy(EnumImagePullPolicy.IF_NOT_PRESENT);
            commonImage.setVersion(imageConfig.getVersion());
            commonImage.addPort(imageConfig.getPort(), imageConfig.getNodePort());
            testConfig.addAppImages(commonImage);

        }

        // move api.yaml to project workspace dir, if has moved ,continue

        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find project by userId and projectId.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, Consts.RESPONSE_MESSAGE_CAN_NOT_FIND_PROJECT);
            return Either.left(error);
        }

        // create app instance id
        String appInstanceId = UUID.randomUUID().toString();
        testConfig.setAppInstanceId(appInstanceId);

        File csar;
        // make deploy csar pkg
        try {
            csar = createCsarPkg(userId, project, testConfig);
        } catch (IOException e) {
            LOGGER.error("Create csar package failed: {}", e.getMessage());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Create csar package fail.");
            return Either.left(error);
        }

        // send to APPLcm to deploy the app
        // get result from APPLcm. deploy ok or error, error messages
        // maybe need so much time to deploy, so it is a asyn-interface, deployed result maybe saved to db.
        // and another api to query the result.
        project.setStatus(EnumProjectStatus.DEPLOYING);
        project.setLastTestId(testConfig.getTestId());
        int res = projectMapper.updateProject(project);
        if (res < 1) {
            LOGGER.error("Update project {} in db error.", project.getId());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "update db error.");
            return Either.left(error);
        }
        CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Begin deploy csar to AppLcm.");
            deployCsarToAppLcm(csar, project, testConfig, userId, token);
            return null;
        });

        return Either.right(projectMapper.getProject(userId, projectId));
    }

    private File createCsarPkg(String userId, ApplicationProject project, ProjectTestConfig testConfig)
        throws IOException {
        String projectId = project.getId();
        String projectPath = getProjectPath(projectId);
        String projectName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();
        List<HelmTemplateYamlPo> yamlPoList = helmTemplateYamlMapper.queryTemplateYamlByProjectId(userId, projectId);
        File csarPkgDir;
        if (!CollectionUtils.isEmpty(yamlPoList)) {
            // create chart file
            ChartFileCreator chartFileCreator = new ChartFileCreator();
            chartFileCreator.setChartName(projectName);
            yamlPoList.forEach(helmTemplateYamlPo -> chartFileCreator
                .addTemplateYaml(helmTemplateYamlPo.getFileName(), helmTemplateYamlPo.getContent()));
            String tgzFilePath = chartFileCreator.build();

            // create csar file directory
            csarPkgDir = new CreateCsarFromTemplate().create(projectPath, testConfig, project, new File(tgzFilePath));
        } else {
            csarPkgDir = new CreateCsarFromTemplate().create(projectPath, testConfig, project, null);
        }
        return CompressFileUtilsJava
            .compressToCsarAndDeleteSrc(csarPkgDir.getCanonicalPath(), projectPath, csarPkgDir.getName());
    }

    private void deployCsarToAppLcm(File csar, ApplicationProject project, ProjectTestConfig testConfig, String userId,
        String token) {

        String appInstanceId = testConfig.getAppInstanceId();
        Type type = new TypeToken<List<MepHost>>() { }.getType();
        List<MepHost> hosts = gson.fromJson(gson.toJson(testConfig.getHosts()), type);
        MepHost host = hosts.get(0);

        // update testconfig accessUrl
        List<CommonImage> images = testConfig.getAppImages();
        StringBuilder accessPorts = new StringBuilder();
        for (CommonImage image : images) {
            accessPorts.append(image.getPorts().get(0).getNodePort());
            accessPorts.append("||");
        }
        testConfig.setAccessUrl(
            "http://" + host.getIp() + ":" + accessPorts.toString().substring(0, accessPorts.length() - 2));

        boolean instantiateAppResult = HttpClientUtil
            .instantiateApplication(host.getProtocol(), host.getIp(), host.getPort(), csar.getPath(), appInstanceId,
                userId, token);

        if (!instantiateAppResult) {
            // deploy failed
            LOGGER.error("Failed to instantiate app which appInstanceId is : {}.", appInstanceId);
            updateDeployResult(EnumTestStatus.NETWORK_ERROR, EnumProjectStatus.DEPLOYED_FAILED, project, testConfig,
                userId, token);
            return;
        }

        testConfig.setAppInstanceId(appInstanceId);
        testConfig.setWorkLoadId(appInstanceId);
        String workloadStatus = null;
        try {
            LOGGER.info("Wait for deploy");
            Thread.sleep(Consts.QUERY_APPLICATIONS_PERIOD);
            workloadStatus = getWorkloadStatus(host, appInstanceId, userId, token);
        } catch (InterruptedException e) {
            LOGGER.error("sleep exception: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        if (workloadStatus == null) {
            // deploy failed
            LOGGER.error("Failed to get workloadStatus after {} times which appInstanceId is : {}",
                Consts.QUERY_APPLICATIONS_TIMES, appInstanceId);
            updateDeployResult(EnumTestStatus.NETWORK_ERROR, EnumProjectStatus.DEPLOYED_FAILED, project, testConfig,
                userId, token);
        } else {
            LOGGER.info("Query workload status response: {}", workloadStatus);
            updateDeployResult(EnumTestStatus.RUNNING, EnumProjectStatus.DEPLOYED, project, testConfig, userId, token);
        }
    }

    private String getWorkloadStatus(MepHost host, String appInstanceId, String userId, String token)
        throws InterruptedException {
        String workloadStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getIp(), host.getPort(), appInstanceId, userId, token);
        int times = 1;
        while (workloadStatus == null) {
            LOGGER.error("Failed to get workloadStatus which appInstanceId is : "
                + "{}, and will try for {} times every {} milliseconds", appInstanceId, TIMES, PERIOD);
            Thread.sleep(Consts.QUERY_APPLICATIONS_PERIOD);
            workloadStatus = HttpClientUtil
                .getWorkloadStatus(host.getProtocol(), host.getIp(), host.getPort(), appInstanceId, userId, token);
            if (++times > Consts.QUERY_APPLICATIONS_TIMES) {
                break;
            }
        }
        return workloadStatus;
    }

    private void updateDeployResult(EnumTestStatus testStatus, EnumProjectStatus status, ApplicationProject project,
        ProjectTestConfig testConfig, String userId, String token) {
        LOGGER.info("Update deploy test status: {}", testStatus);
        project.setStatus(status);
        int res = projectMapper.updateProject(project);
        if (res < 1) {
            LOGGER.error("Update project {} error ", project.getId());
        }
        testConfig.setStatus(testStatus);
        testConfig.setErrorLog(testStatus.name());
        testConfig.setDeployDate(new Date());
        int tes = projectMapper.updateTestConfig(testConfig);
        if (tes < 1) {
            LOGGER.error("Update testconfig {} error ", testConfig.getTestId());
        }
        if (status == EnumProjectStatus.DEPLOYED_FAILED && testConfig.getWorkLoadId() != null) {
            deleteDeployApp(testConfig, userId, token);
            LOGGER.warn("Deploy failed, delete deploy app.");
        }
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
        // move api.yaml to project directory
        String apiFileId = testConfig.getAppApiFileId();
        if (apiFileId != null) {
            try {
                moveFileToWorkSpaceById(apiFileId, projectId);
            } catch (IOException e) {
                LOGGER.error("Move api file error {}", e.getMessage());
                FormatRespDto error = gson.fromJson(e.getMessage(), FormatRespDto.class);
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
    public Either<FormatRespDto, Boolean> cleanTestEnv(String userId, String projectId, boolean completed,
        String token) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find project by userId and projectId");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "can not find project");
            return Either.left(error);
        }

        ProjectTestConfig testConfig = projectMapper.getTestConfig(project.getLastTestId());
        if (testConfig == null) {
            LOGGER.info("This project is not tested, do not need to clean env.");
            return Either.right(true);
        }
        if (project.getStatus() == EnumProjectStatus.DEPLOYED_FAILED
            || testConfig.getStatus() != EnumTestStatus.RUNNING) {
            LOGGER.error("Deploy failed, can not finish test by status {}", project.getStatus());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "deploy failed, can not finish test");
            return Either.left(error);
        }
        if (deleteDeployApp(testConfig, userId, token)) {
            project.setStatus(completed ? EnumProjectStatus.TESTED : EnumProjectStatus.ONLINE);
            int res = projectMapper.updateProject(project);
            if (res < 1) {
                LOGGER.error("Update project status failed");
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "update project failed");
                return Either.left(error);
            }
            LOGGER.info("Update project status to TESTED success");

            testConfig.setStatus(EnumTestStatus.DELETED);
            int tes = projectMapper.updateTestConfig(testConfig);
            if (tes < 1) {
                LOGGER.error("Update test config {} failed", testConfig.getTestId());
            }
            LOGGER.info("Update test config {} status to Deleted success", testConfig.getTestId());
            return Either.right(true);
        } else {
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "delete deploy app failed");
            return Either.left(error);
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
