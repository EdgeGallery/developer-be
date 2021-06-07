/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.virtual;

import static org.edgegallery.developer.util.AtpUtil.getProjectPath;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.lcm.UploadResponse;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.EnumVmImportStatus;
import org.edgegallery.developer.model.vm.FileUploadEntity;
import org.edgegallery.developer.model.vm.NetworkInfo;
import org.edgegallery.developer.model.vm.ScpConnectEntity;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmCreateStageStatus;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmImportStageStatus;
import org.edgegallery.developer.model.vm.VmInfo;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.model.vm.VmUserData;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.csar.NewCreateVmCsar;
import org.edgegallery.developer.service.virtual.create.VmCreateStage;
import org.edgegallery.developer.service.virtual.image.VmImageStage;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.ShhFileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("vmService")
public class VmService {
    @Value("${vm.username:}")
    private String vmUsername;

    @Value("${vm.password:}")
    private String vmPassword;

    @Value("${upload.tempPath}")
    private String tempUploadPath;

    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    private static final String SUBDIR_FILE = "uploadFile";

    private static Gson gson = new Gson();

    private static final String TEMPLATE_TOSCA_METADATA_PATH = "/TOSCA-Metadata/TOSCA.meta";

    private static final String TEMPLATE_TOSCA_IMAGE_DESC_PATH = "Image/SwImageDesc.json";

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private Map<String, VmCreateStage> createServiceMap;

    @Autowired
    private Map<String, VmImageStage> imageServiceMap;

    /**
     * getVirtualResource.
     *
     * @param currUserId Current User ID
     * @return
     */
    public Either<FormatRespDto, VmResource> getVirtualResource(String currUserId) {
        List<VmRegulation> vmRegulation = vmConfigMapper.getVmRegulation();
        List<VmSystem> vmSystem = vmConfigMapper.getVmSystem(currUserId);
        List<VmNetwork> vmNetwork = vmConfigMapper.getVmNetwork();
        List<VmUserData> vmUserData = vmConfigMapper.getVmUserData();
        VmResource vmResource = new VmResource();
        vmResource.setVmRegulationList(vmRegulation);
        vmResource.setVmSystemList(vmSystem);
        vmResource.setVmNetworkList(vmNetwork);
        vmResource.setVmUserDataList(vmUserData);
        LOGGER.info("Get all vm resource success");
        return Either.right(vmResource);

    }

    /**
     * createVm.
     *
     * @return
     */
    public Either<FormatRespDto, VmCreateConfig> createVm(String userId, String projectId, String token) {

        VmPackageConfig vmPackageConfig = vmConfigMapper.getVmPackageConfig(projectId);
        if (vmPackageConfig == null) {
            LOGGER.error("Can not get vm package config by  project {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not get vm package config");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = new VmCreateConfig();
        vmCreateConfig.setAppInstanceId(vmPackageConfig.getAppInstanceId());
        vmCreateConfig.setVmName(vmPackageConfig.getVmName());
        vmCreateConfig.setLcmToken(token);
        vmCreateConfig.setProjectId(projectId);
        String vmId = UUID.randomUUID().toString();
        vmCreateConfig.setVmId(vmId);
        vmCreateConfig.setStatus(EnumVmCreateStatus.CREATING);
        VmCreateStageStatus stageStatus = new VmCreateStageStatus();
        vmCreateConfig.setStageStatus(stageStatus);
        // create vm config
        int tes = vmConfigMapper.saveVmCreateConfig(vmCreateConfig);
        if (tes < 1) {
            LOGGER.error("create vm config {} failed.", vmCreateConfig.getVmId());
        }
        // update project status
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        project.setStatus(EnumProjectStatus.DEPLOYING);
        int res = projectMapper.updateProject(project);
        if (res < 1) {
            LOGGER.error("Update project {} in db failed.", project.getId());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "update project in db failed.");
            return Either.left(error);
        }
        return Either.right(vmCreateConfig);

    }

    /**
     * update create vm result.
     *
     * @return
     */
    @Transactional
    public void updateCreateVmResult(VmCreateConfig testConfig, ApplicationProject project, String stage,
        EnumTestConfigStatus stageStatus) {
        LOGGER.info("Update deploy test on stage:{} status: {}", stage, stageStatus);
        // update test config always && update product if necessary
        switch (stage) {
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
                testConfig.setStageStatus(new VmCreateStageStatus());
                break;
        }
        boolean productUpdate = false;
        LOGGER.info("get workStatus status:{}, stage:{}", stageStatus, stage);
        if (EnumTestConfigStatus.Success.equals(stageStatus) && "workStatus".equalsIgnoreCase(stage)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED);
            testConfig.setLog("vm create success");
            testConfig.setStatus(EnumVmCreateStatus.SUCCESS);
        } else if (EnumTestConfigStatus.Failed.equals(stageStatus)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED_FAILED);
            testConfig.setStatus(EnumVmCreateStatus.FAILED);
        }
        // update status if necessary
        if (productUpdate) {
            int res = projectMapper.updateProject(project);
            if (res < 1) {
                LOGGER.error("Update project {} error.", project.getId());
            }
        }

        int tes = vmConfigMapper.updateVmCreateConfig(testConfig);
        if (tes < 1) {
            LOGGER.error("Update test-config {} error.", testConfig.getVmId());
        }
    }

    /**
     * processDeploy.
     * task job for scheduler
     *
     * @return
     */
    public void processCreateVm() {
        // get deploying config list from db
        List<VmCreateConfig> vmConfigList = vmConfigMapper
            .getVmCreateConfigStatus(EnumVmCreateStatus.CREATING.toString());
        if (CollectionUtils.isEmpty(vmConfigList)) {
            return;
        }
        vmConfigList.forEach(this::processVmCreateConfig);
    }

    /**
     * processConfig.
     */
    public void processVmCreateConfig(VmCreateConfig config) {
        String nextStage = config.getNextStage();
        if (StringUtils.isBlank(nextStage)) {
            return;
        }
        try {
            VmCreateStage stageService = createServiceMap.get("vm_" + nextStage + "_service");
            stageService.execute(config);
        } catch (Exception e) {
            LOGGER
                .error("create vm config:{} failed on stage :{}, res:{}", config.getVmId(), nextStage, e.getMessage());
        }
    }

    /**
     * createVmToAppLcm.
     *
     * @return
     */
    public boolean createVmToAppLcm(File csar, ApplicationProject project, VmCreateConfig vmConfig, String userId,
        String lcmToken) {
        VmPackageConfig config = vmConfigMapper.getVmPackageConfig(project.getId());
        if (config == null) {
            LOGGER.error("get vm package config failed.");
            return false;
        }
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmConfig.getHost()), type);

        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        // upload pkg
        LcmLog lcmLog = new LcmLog();
        String uploadRes = HttpClientUtil
            .uploadPkg(basePath, csar.getPath(), userId, lcmToken, lcmLog);
        LOGGER.info("upload package result: {}", uploadRes);
        if (StringUtils.isEmpty(uploadRes)) {
            vmConfig.setLog(lcmLog.getLog());
            return false;
        }
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<UploadResponse>() { }.getType();
        UploadResponse uploadResponse = gson.fromJson(uploadRes, typeEvents);
        String pkgId = uploadResponse.getPackageId();
        vmConfig.setPackageId(pkgId);
        vmConfig.setLog("upload csar package success" + pkgId);
        vmConfigMapper.updateVmCreateConfig(vmConfig);

        // distribute pkg
        boolean distributeRes = HttpClientUtil
            .distributePkg(basePath, userId, lcmToken, pkgId,
                host.getMecHost(), lcmLog);
        LOGGER.info("distribute package result: {}", distributeRes);
        if (!distributeRes) {
            vmConfig.setLog(lcmLog.getLog());
            return false;
        }
        // instantiate application

        Map<String, String> vmInputParams = InputParameterUtil.getParams(host.getParameter());

        if (!config.getAk().equals("") && !config.getSk().equals("")) {
            vmInputParams.put("ak",config.getAk());
            vmInputParams.put("sk",config.getSk());
        }

        String appInstanceId = vmConfig.getAppInstanceId();
        boolean instantRes = HttpClientUtil
            .instantiateApplication(basePath, appInstanceId, userId,
                lcmToken, lcmLog, pkgId, host.getMecHost(), vmInputParams);
        LOGGER.info("distribute package result: {}", instantRes);
        if (!instantRes) {
            vmConfig.setLog(lcmLog.getLog());
            return false;
        }

        return true;
    }

    private boolean deleteVmCreate(VmCreateConfig vmConfig, String userId, String lcmToken) {
        String appInstanceId = vmConfig.getAppInstanceId();
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmConfig.getHost()), type);
        // delete hosts
        boolean deleteHostRes = HttpClientUtil
            .deleteHost(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, lcmToken, vmConfig.getPackageId(),
                host.getMecHost());

        // delete pkg
        boolean deletePkgRes = HttpClientUtil
            .deletePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, lcmToken, vmConfig.getPackageId());

        boolean terminateApp = HttpClientUtil
            .terminateAppInstance(host.getProtocol(), host.getLcmIp(), host.getPort(), appInstanceId, userId, lcmToken);
        if (!terminateApp || !deleteHostRes || !deletePkgRes) {
            return false;
        }
        return true;
    }

    /**
     * getCreateVm.
     */
    public Either<FormatRespDto, List<VmCreateConfig>> getCreateVm(String userId, String projectId) {

        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        LOGGER.info("Get project information success");

        List<VmCreateConfig> vmCreateConfigs = vmConfigMapper.getVmCreateConfigs(projectId);
        return Either.right(vmCreateConfigs);
    }

    /**
     * deleteCreateVm.
     */
    public Either<FormatRespDto, Boolean> deleteCreateVm(String userId, String projectId, String vmId, String token) {

        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        LOGGER.info("Get project information success");
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig == null) {
            LOGGER.info("Can not find the vm create config by vmId {} and projectId {}", vmId, projectId);
            return Either.right(true);
        }
        VmImageConfig vmImageConfig = vmConfigMapper.getVmImage(projectId, vmId);
        if (vmImageConfig != null) {
            LOGGER.error("Can not delete vm config, first delete vm image by  vmId {}", vmId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST,
                "Can not delete vm config, first delete vm image");
            return Either.left(error);
        }

        if (vmCreateConfig.getStageStatus().getInstantiateInfo() == EnumTestConfigStatus.Success && !StringUtils
            .isEmpty(vmCreateConfig.getPackageId())) {
            deleteVmCreate(vmCreateConfig, project.getUserId(), token);
        }

        int res = vmConfigMapper.deleteVmCreateConfig(projectId, vmId);
        if (res < 1) {
            LOGGER.error("Delete vm create config {} failed.", vmId);
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm create config failed."));
        }

        LOGGER.info("delete vm create config success");
        return Either.right(true);

    }

    /**
     * uploadFileToVm.
     */
    public Either<FormatRespDto, Boolean> uploadFileToVm(String userId, String projectId, String vmId,
        HttpServletRequest request, Chunk chunk) throws Exception {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig == null) {
            LOGGER.info("Can not find the vm create config by vmId {} and projectId {}", vmId, projectId);
            return Either.right(true);
        }

        LOGGER.info("upload system image file, fileName = {}, identifier = {}, chunkNum = {}",
            chunk.getFilename(), chunk.getIdentifier(), chunk.getChunkNumber());
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            LOGGER.error("upload request is invalid.");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "upload request is invalid.");
            return Either.left(error);
        }

        MultipartFile file = chunk.getFile();
        if (file == null) {
            LOGGER.error("can not find any needed file");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "upload request is invalid.");
            return Either.left(error);
        }

        File tmpUploadDir = new File(getUploadFileRootDir());
        if (!tmpUploadDir.exists()) {
            boolean isMk = tmpUploadDir.mkdirs();
            if (!isMk) {
                LOGGER.error("create temporary upload path failed");
                FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "create temporary upload path failed");
                return Either.left(error);
            }
        }

        Integer chunkNumber = chunk.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }
        File outFile = new File(getUploadFileRootDir()
            + chunk.getIdentifier(), chunkNumber + ".part");
        InputStream inputStream = file.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, outFile);

        String partFilePath = getUploadFileRootDir()  + chunk.getIdentifier();
        File partFileDir = new File(partFilePath);

        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            LOGGER.error("uploaded part file not found!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "uploaded part file not found!");
            return Either.left(error);
        }

        File mergedFile = new File(getUploadFileRootDir() + File.separator + chunk.getFilename());
        FileOutputStream destTempfos = new FileOutputStream(mergedFile, true);
        for (File partFile : partFiles) {
            FileUtils.copyFile(partFile, destTempfos);
        }
        destTempfos.close();
        FileUtils.deleteDirectory(partFileDir);

        Boolean pushFileToVmRes = pushFileToVm(mergedFile, vmCreateConfig);
        if (!pushFileToVmRes) {
            LOGGER.error("push app file failed!");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "push app file failed!");
            return Either.left(error);
        }

        return Either.right(true);


    }

    /**
     * downloadVmCsar.
     */
    public Either<FormatRespDto, ResponseEntity<byte[]>> downloadVmCsar(String userId, String projectId, String vmId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig == null) {
            LOGGER.error("Can not find the vm config by vmId {} and projectId {}", vmId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the vm config.");
            return Either.left(error);
        }
        String csarFilePath = projectService.getProjectPath(projectId) + vmCreateConfig.getAppInstanceId() + ".csar";
        File csarFile = new File(csarFilePath);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add("Content-Disposition", "attachment; filename=" + project.getName() + ".csar");
            byte[] fileData = FileUtils.readFileToByteArray(csarFile);
            LOGGER.info("get vm csar package success");
            return Either.right(ResponseEntity.ok().headers(headers).body(fileData));
        } catch (IOException e) {
            LOGGER.error("get vm csar package failed : {}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "get vm csar package failed "));
        }
    }

    /**
     * update create vm image result.
     *
     * @return
     */
    @Transactional
    public void updateVmImageResult(VmImageConfig config, ApplicationProject project, String stage,
        EnumTestConfigStatus stageStatus) {
        LOGGER.info("Update vm image on stage:{} status: {}", stage, stageStatus);
        // update test config always && update product if necessary
        switch (stage) {
            case "createImageInfo":
                config.getStageStatus().setCreateImageInfo(stageStatus);
                break;
            case "imageStatus":
                config.getStageStatus().setImageStatus(stageStatus);
                break;
            case "downloadImageInfo":
                config.getStageStatus().setDownloadImageInfo(stageStatus);
                break;
            default:
                config.setStageStatus(new VmImportStageStatus());
                break;
        }
        boolean productUpdate = false;
        LOGGER.info("get downloadImageInfo status:{}, stage:{}", stageStatus, stage);
        if (EnumTestConfigStatus.Success.equals(stageStatus) && "DownloadImageInfo".equalsIgnoreCase(stage)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED);
            config.setLog("vm image import success");
            config.setStatus(EnumVmImportStatus.SUCCESS);
        } else if (EnumTestConfigStatus.Failed.equals(stageStatus)) {
            productUpdate = true;
            project.setStatus(EnumProjectStatus.DEPLOYED_FAILED);
            config.setStatus(EnumVmImportStatus.FAILED);
        }
        // update status if necessary
        if (productUpdate) {
            int res = projectMapper.updateProject(project);
            if (res < 1) {
                LOGGER.error("Update project {} error.", project.getId());
            }
        }

        int tes = vmConfigMapper.updateVmImageConfig(config);
        if (tes < 1) {
            LOGGER.error("Update vm image config {} error.", config.getVmId());
        }
    }

    /**
     * import vm image process.
     * task job for scheduler
     *
     * @return
     */
    public void processVmImage() {
        // get deploying config list from db
        List<VmImageConfig> vmImageList = vmConfigMapper.getVmImageConfigStatus(EnumVmImportStatus.CREATING.toString());
        if (CollectionUtils.isEmpty(vmImageList)) {
            return;
        }
        vmImageList.forEach(this::processVmImageConfig);
    }

    /**
     * processConfig.
     */
    public void processVmImageConfig(VmImageConfig config) {
        String nextStage = config.getNextStage();
        if (StringUtils.isBlank(nextStage)) {
            return;
        }
        try {
            VmImageStage stageService = imageServiceMap.get("vm_" + nextStage + "_service");
            stageService.execute(config);
        } catch (Exception e) {
            LOGGER
                .error(" vm image config:{} failed on stage :{}, res:{}", config.getVmId(), nextStage, e.getMessage());
        }
    }

    /**
     * importVmImage.
     */
    public Either<FormatRespDto, Boolean> importVmImage(String userId, String projectId, String token) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        VmPackageConfig vmPackageConfig = vmConfigMapper.getVmPackageConfig(projectId);
        if (vmPackageConfig == null) {
            LOGGER.error("Can not get vm package config by  project {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not get vm package config");
            return Either.left(error);
        }
        List<VmCreateConfig> vmCreateConfigs = vmConfigMapper.getVmCreateConfigs(projectId);

        if (CollectionUtils.isEmpty(vmCreateConfigs)) {
            LOGGER.error("Can not find the vm create config by projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the vm config.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);
        if (vmCreateConfig.getStatus() != EnumVmCreateStatus.SUCCESS) {
            LOGGER.error("vm create fail, can not import image,projectId:{}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "vm create fail, can not import image");
            return Either.left(error);
        }
        if (vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId()) != null) {
            LOGGER.error("vm create fail,vm create config have exited ,vmId:{}", vmCreateConfig.getVmId());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "vm create fail,vm create config have exited");
            return Either.left(error);
        }

        VmImageConfig vmImageConfig = new VmImageConfig();
        vmImageConfig.setVmId(vmCreateConfig.getVmId());
        vmImageConfig.setVmName(vmPackageConfig.getVmName());
        vmImageConfig.setAppInstanceId(vmCreateConfig.getAppInstanceId());
        vmImageConfig.setLcmToken(token);
        vmImageConfig.setProjectId(projectId);
        vmImageConfig.setStatus(EnumVmImportStatus.CREATING);
        VmImportStageStatus stageStatus = new VmImportStageStatus();
        vmImageConfig.setStageStatus(stageStatus);
        int tes = vmConfigMapper.saveVmImageConfig(vmImageConfig);
        if (tes < 1) {
            LOGGER.error("create vm config {} failed.", vmCreateConfig.getVmId());
        }
        return Either.right(true);

    }

    /**
     * getVmImage.
     */
    public Either<FormatRespDto, VmImageConfig> getVmImage(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        List<VmCreateConfig> vmCreateConfigs = vmConfigMapper.getVmCreateConfigs(projectId);
        if (CollectionUtils.isEmpty(vmCreateConfigs)) {
            LOGGER.error("Can not find the vm create config by projectId {}", projectId);
            return Either.right(null);
        }
        VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);

        VmImageConfig vmImageConfig = vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId());
        return Either.right(vmImageConfig);
    }

    /**
     * deleteVmImage.
     */
    public Either<FormatRespDto, Boolean> deleteVmImage(String userId, String projectId, String token) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        List<VmCreateConfig> vmCreateConfigs = vmConfigMapper.getVmCreateConfigs(projectId);
        if (CollectionUtils.isEmpty(vmCreateConfigs)) {
            LOGGER.error("Can not find the vm create config by projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the vm config.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);

        LOGGER.info("Get vm create information success");
        VmImageConfig vmImageConfig = vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId());
        if (vmImageConfig == null) {
            LOGGER.info("Can not find the vm image config by vmId {} and projectId {}", vmCreateConfig.getVmId(),
                projectId);
            return Either.right(true);
        }
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        if (!StringUtils.isEmpty(vmImageConfig.getImageId())) {
            HttpClientUtil.deleteVmImage(host.getProtocol(), host.getLcmIp(), host.getPort(), userId,
                vmImageConfig.getAppInstanceId(), vmImageConfig.getImageId(), token);
        }

        int res = vmConfigMapper.deleteVmImage(projectId, vmCreateConfig.getVmId());
        if (res < 1) {
            LOGGER.error("Delete vm image config {} failed.", vmCreateConfig.getVmId());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm image config failed."));
        }
        String packagePath = projectService.getProjectPath(vmImageConfig.getProjectId()) + vmImageConfig
            .getAppInstanceId() + File.separator + "Image";
        FileUtils.deleteQuietly(new File(packagePath + File.separator + vmImageConfig.getImageName() + ".zip"));

        LOGGER.info("delete vm create config success");
        return Either.right(true);

    }

    public Either<FormatRespDto, VmPackageConfig> vmPackage(String userId, String projectId,
        VmPackageConfig vmPackageConfig) {
        //A project has only one virtual machine configuration
        List<VmCreateConfig> vmConfigs = vmConfigMapper.getVmCreateConfigs(projectId);
        if (!CollectionUtils.isEmpty(vmConfigs)) {
            int res = vmConfigMapper.deleteVmCreateConfigs(projectId);
            if (res < 1) {
                FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                    "delete vm create config failed!");
                return Either.left(error);
            }
        }
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        String id = UUID.randomUUID().toString();
        String appInstanceId = UUID.randomUUID().toString();
        vmPackageConfig.setProjectId(projectId);
        vmPackageConfig.setId(id);
        vmPackageConfig.setAppInstanceId(appInstanceId);
        vmPackageConfig.setCreateTime(new Date());
        try {
            generateVmPackageByConfig(vmPackageConfig);
        } catch (Exception e) {
            LOGGER.error("generate vm csar with id:{} on csar failed:{}", vmPackageConfig.getId(), e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "generate vm csar fail"));
        }
        // create vm package config
        int tes = vmConfigMapper.saveVmPackageConfig(vmPackageConfig);
        if (tes < 1) {
            LOGGER.error("create vm package config {} failed.", vmPackageConfig.getId());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "save vm info fail"));
        }

        return Either.right(vmPackageConfig);
    }

    /**
     * get vm package .
     */
    public Either<FormatRespDto, VmPackageConfig> getVmPackage(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        VmPackageConfig tes = vmConfigMapper.getVmPackageConfig(projectId);
        return Either.right(tes);
    }

    /**
     * delete vm package file.
     */
    public Either<FormatRespDto, Boolean> deleteVmPackage(String userId, String projectId) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        VmPackageConfig config = vmConfigMapper.getVmPackageConfig(projectId);
        if (config == null) {
            LOGGER.error("get vm package config failed.");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "get vm package config failed."));
        }
        int res = vmConfigMapper.deleteVmPackageConfig(config.getId());
        if (res < 1) {
            LOGGER.error("delete vm package config failed.");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "delete vm package config failed."));
        }
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteDir(projectPath + config.getAppInstanceId());
        FileUtils.deleteQuietly(new File(projectPath + config.getAppInstanceId() + ".csar"));

        return Either.right(true);

    }

    /**
     * create vm package.
     */
    public File generateVmPackageByConfig(VmPackageConfig config) throws IOException, DomainException {
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String projectPath = getProjectPath(config.getProjectId());
        File csarPkgDir;
        csarPkgDir = new NewCreateVmCsar().create(projectPath, config, project);
        return CompressFileUtilsJava
            .compressToCsarAndDeleteSrc(csarPkgDir.getCanonicalPath(), projectPath, csarPkgDir.getName());
    }

    /**
     * createVmImageToAppLcm.
     */
    public boolean createVmImageToAppLcm(MepHost host, VmImageConfig imageConfig, String userId) {
        String appInstanceId = imageConfig.getAppInstanceId();
        String lcmToken = imageConfig.getLcmToken();
        LcmLog lcmLog = new LcmLog();
        String id = imageConfig.getVmId();
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(imageConfig.getProjectId(), id);

        Type vmInfoType = new TypeToken<List<VmInfo>>() { }.getType();
        List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), vmInfoType);
        String vmId = vmInfo.get(0).getVmId();

        String imageResult = HttpClientUtil
            .vmInstantiateImage(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, lcmToken, vmId,
                appInstanceId, lcmLog);
        LOGGER.info("import image result: {}", imageResult);
        if (StringUtils.isEmpty(imageResult)) {
            imageConfig.setLog(lcmLog.getLog());
            return false;
        }
        JsonObject jsonObject = new JsonParser().parse(imageResult).getAsJsonObject();
        JsonElement imageId = jsonObject.get("imageId");
        imageConfig.setImageId(imageId.getAsString());
        imageConfig.setLog("Create vm image success");
        return true;
    }

    /**
     * downloadImageResult.
     */
    public boolean downloadImageResult(MepHost host, VmImageConfig config, String userId) {

        String packagePath = projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId();
        LOGGER.info(packagePath);
        for (int chunkNum = 0; chunkNum < config.getSumChunkNum(); chunkNum++) {
            LOGGER.info("download image chunkNum:{}", chunkNum);
            boolean res = HttpClientUtil
                .downloadVmImage(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, packagePath,
                    config.getAppInstanceId(), config.getImageId(), config.getImageName(), Integer.toString(chunkNum),
                    config.getLcmToken());
            if (!res) {
                LOGGER.info("no more data");
                config.setLog("no more data");
                break;
            }
            if (chunkNum == 0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("sleep fail! {}", e.getMessage());
                }
            }
            if (chunkNum % 10 == 0) {
                config.setLog("download image file:" + chunkNum + "/" + config.getSumChunkNum());
                vmConfigMapper.updateVmImageConfig(config);
            }
        }

        String imagePath = packagePath + File.separator + config.getImageName();
        LOGGER.info("image file path:{}", imagePath);
        try {
            File file = new File(imagePath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    File partFile = new File(imagePath + File.separator + config.getImageName() + ".qcow2");
                    for (int i = 0; i < files.length; i++) {
                        File s = new File(imagePath, "temp_" + i);
                        FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                        FileUtils.copyFile(s, destTempfos);
                        destTempfos.close();
                        FileUtils.deleteQuietly(s);
                    }
                }
                CompressFileUtilsJava.compressToZip(imagePath, packagePath, config.getImageName());
                FileUtils.deleteDirectory(new File(imagePath));
            }

        } catch (IOException e) {
            LOGGER.error("image generate failed: occur IOException {}.", e.getMessage());
            return false;
        }

        // modify the csar  TOSCA-Metadata/TOSCA.meta file
        String toscaPath = packagePath + TEMPLATE_TOSCA_METADATA_PATH;
        LOGGER.info(toscaPath);
        try {
            File toscaValue = new File(toscaPath);

            FileUtils.writeStringToFile(toscaValue, FileUtils.readFileToString(toscaValue, StandardCharsets.UTF_8)
                .replace("{imageFile}", config.getImageName()), StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            LOGGER.error("modify image file fail: occur IOException {}.", e.getMessage());
            return false;
        }

        // modify image file
        File swImageDesc = new File(packagePath + TEMPLATE_TOSCA_IMAGE_DESC_PATH);
        try {
            List<ImageDesc> swImgDescs = getSwImageDescrInfo(
                FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8));

            swImgDescs.get(0).setName(config.getImageName());
            swImgDescs.get(0).setSwImage(
                "Image/" + config.getImageName() + ".zip/" + config.getImageName() + "/" + config.getImageName()
                    + ".qcow2");
            writeFile(swImageDesc, gson.toJson(swImgDescs));
        } catch (IOException e) {
            LOGGER.error("modify image file fail: occur IOException {}.", e.getMessage());
            return false;
        }

        String outPath = projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId();

        try {
            CompressFileUtilsJava
                .compressToCsarAndDeleteSrc(outPath, projectService.getProjectPath(config.getProjectId()),
                    config.getAppInstanceId());
        } catch (IOException e) {
            LOGGER.error("generate csar failed: occur IOException {}.", e.getMessage());
            return false;
        }
        LOGGER.info("download image success");
        return true;
    }

    /**
     * Returns list of image details.
     *
     * @param swImageDescr software image descriptor file content
     * @return list of image details
     */
    public static List<ImageDesc> getSwImageDescrInfo(String swImageDescr) {

        List<ImageDesc> swImgDescrs = new LinkedList<>();
        JsonArray swImgDescrArray = new JsonParser().parse(swImageDescr).getAsJsonArray();
        ImageDesc swDescr;
        for (JsonElement descr : swImgDescrArray) {
            swDescr = new Gson().fromJson(descr.getAsJsonObject().toString(), ImageDesc.class);
            swImgDescrs.add(swDescr);
        }
        LOGGER.info("sw image descriptors: {}", swImgDescrs);
        return swImgDescrs;
    }

    private void writeFile(File file, String content) {
        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

    private String getUploadFileRootDir() {
        return tempUploadPath + File.separator + SUBDIR_FILE + File.separator;
    }
    private Boolean pushFileToVm(File appFile, VmCreateConfig vmCreateConfig) {
        String networkType = "Network_N6";
        VmNetwork vmNetwork = vmConfigMapper.getVmNetworkByType(networkType);
        Type type = new TypeToken<List<VmInfo>>() { }.getType();
        List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), type);
        List<NetworkInfo> networkInfos = vmInfo.get(0).getNetworks();
        String networkIp = "";
        for (NetworkInfo networkInfo : networkInfos) {
            if (networkInfo.getName().equals(vmNetwork.getNetworkName())) {
                networkIp = networkInfo.getIp();
            }
        }
        LOGGER.info("network Ip, username is {},{}", networkIp, vmUsername);
        // ssh upload file
        String targetPath = "";
        ScpConnectEntity scpConnectEntity = new ScpConnectEntity();
        scpConnectEntity.setTargetPath(targetPath);
        scpConnectEntity.setUrl(networkIp);
        scpConnectEntity.setPassWord(vmPassword);
        scpConnectEntity.setUserName(vmUsername);
        String remoteFileName = appFile.getName();
        LOGGER.info("path:{}", targetPath);
        ShhFileUploadUtil sshFileUploadUtil = new ShhFileUploadUtil();
        FileUploadEntity fileUploadEntity = sshFileUploadUtil.uploadFile(appFile, remoteFileName, scpConnectEntity);
        if (fileUploadEntity.getCode().equals("ok")) {
            return true;
        } else {
            LOGGER.warn("upload fail, ip:{}", vmInfo.get(0).getNetworks().get(0).getIp());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, fileUploadEntity.getMessage());
            return false;
        }

    }



}

