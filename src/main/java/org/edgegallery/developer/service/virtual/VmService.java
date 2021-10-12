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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.deployyaml.ImageDesc;
import org.edgegallery.developer.model.lcm.DistributeResponse;
import org.edgegallery.developer.model.lcm.MecHostInfo;
import org.edgegallery.developer.model.lcm.UploadResponse;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.system.FileSystemResponse;
import org.edgegallery.developer.model.system.VmSystem;
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
import org.edgegallery.developer.model.vm.VmUserData;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumSystemImageSlimStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.EncryptedService;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.csar.NewCreateVmCsar;
import org.edgegallery.developer.service.virtual.create.VmCreateStage;
import org.edgegallery.developer.service.virtual.image.VmImageStage;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.IpCalculateUtil;
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

    @Value("${fileserver.address}")
    private String fileServerAddress;

    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    private static final String SUBDIR_FILE = "uploadFile";

    private static Gson gson = new Gson();

    private static final String TEMPLATE_TOSCA_IMAGE_DESC_PATH = "/SwImageDesc.json";

    private static final String IMAGE_PATH = "/action/download";

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 30 * 1000L;

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

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private EncryptedService encryptedService;

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
    public Either<FormatRespDto, VmCreateConfig> createVm(String projectId, String token) {

        VmPackageConfig vmPackageConfig = vmConfigMapper.getVmPackageConfig(projectId);
        if (vmPackageConfig == null) {
            LOGGER.error("Can not get vm package config by  project {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not get vm package config");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);
        if (vmCreateConfig == null) {
            VmCreateConfig newVmCreateConfig = new VmCreateConfig();

            newVmCreateConfig.setAppInstanceId(vmPackageConfig.getAppInstanceId());
            newVmCreateConfig.setVmName(vmPackageConfig.getVmName());
            newVmCreateConfig.setLcmToken(token);
            newVmCreateConfig.setProjectId(projectId);
            String vmId = UUID.randomUUID().toString();
            newVmCreateConfig.setVmId(vmId);
            newVmCreateConfig.setStatus(EnumVmCreateStatus.CREATING);
            VmCreateStageStatus stageStatus = new VmCreateStageStatus();
            newVmCreateConfig.setStageStatus(stageStatus);
            vmConfigMapper.saveVmCreateConfig(newVmCreateConfig);
        } else {
            vmCreateConfig.setAppInstanceId(vmPackageConfig.getAppInstanceId());
            vmCreateConfig.setVmName(vmPackageConfig.getVmName());
            vmCreateConfig.setLcmToken(token);
            vmCreateConfig.setStatus(EnumVmCreateStatus.CREATING);
            VmCreateStageStatus stageStatus = new VmCreateStageStatus();
            vmCreateConfig.setStageStatus(stageStatus);
            vmConfigMapper.updateVmCreateConfig(vmCreateConfig);
        }

        // update project status
        projectMapper.updateProjectStatus(projectId, EnumProjectStatus.DEPLOYING.toString());
        return Either.right(vmConfigMapper.getVmCreateConfigs(projectId));

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
            case "distributeInfo":
                testConfig.getStageStatus().setDistributeInfo(stageStatus);
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
    public String distributeVmToAppLcm(File csar, ApplicationProject project, VmCreateConfig config, String userId,
        String lcmToken) {
        MepHost host = gson.fromJson(gson.toJson(config.getHost()), new TypeToken<MepHost>() { }.getType());

        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        if (StringUtils.isEmpty(config.getPackageId())) {
            // upload pkg
            String packageId = uploadPackageToLcm(basePath, csar.getPath(), userId, lcmToken);
            if (packageId == null) {
                config.setLog("upload package fail");
                return null;
            }
            config.setPackageId(packageId);
            config.setCreateTime(new Date());
            config.setLog("upload package success");
            vmConfigMapper.updateVmCreateConfig(config);
            String distributeRes = HttpClientUtil
                .distributePkg(basePath, userId, lcmToken, config.getPackageId(), host.getMecHost(), new LcmLog());
            if (distributeRes == null) {
                config.setLog("distribute package fail");
                return null;
            }
        }

        // get distribute pkg status
        return getDistributeStatus(basePath, userId, lcmToken, config.getPackageId());
    }

    private String getDistributeStatus(String basePath, String userId, String lcmToken, String packageId) {
        String distributeResult = HttpClientUtil.getDistributeRes(basePath, userId, lcmToken, packageId);
        LOGGER.info("distribute package result: {}", distributeResult);
        if (distributeResult == null) {
            LOGGER.error("get distribute package status fail");
            return null;
        }
        List<DistributeResponse> list = gson
            .fromJson(distributeResult, new TypeToken<List<DistributeResponse>>() { }.getType());
        List<MecHostInfo> mecHostInfo = list.get(0).getMecHostInfo();
        if (mecHostInfo == null) {
            LOGGER.error("get distribute package status fail");
            return null;
        }
        String status = mecHostInfo.get(0).getStatus();
        return status;

    }

    private String uploadPackageToLcm(String basePath, String path, String userId, String lcmToken) {
        String uploadRes = HttpClientUtil.uploadPkg(basePath, path, userId, lcmToken, new LcmLog());
        LOGGER.info("upload package result: {}", uploadRes);
        if (StringUtils.isEmpty(uploadRes)) {
            LOGGER.error("upload package fail: {}", uploadRes);
            return null;
        }
        UploadResponse uploadResponse = gson.fromJson(uploadRes, UploadResponse.class);
        return uploadResponse.getPackageId();
    }

    /**
     * createVmToAppLcm.
     *
     * @return
     */
    public boolean createVmToAppLcm(VmCreateConfig vmConfig, String userId, String lcmToken) {
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmConfig.getHost()), type);
        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        LcmLog lcmLog = new LcmLog();
        // instantiate application
        Map<String, String> vmInputParams;
        try {
            vmInputParams = getInputParams(host.getParameter(), host.getMecHost());
        } catch (Exception e) {
            LOGGER.error("network params error");
            return false;
        }
        String appInstanceId = vmConfig.getAppInstanceId();
        boolean instantRes = HttpClientUtil
            .instantiateApplication(basePath, appInstanceId, userId, lcmToken, lcmLog, vmConfig.getPackageId(),
                host.getMecHost(), vmInputParams);
        LOGGER.info("distribute package result: {}", instantRes);
        if (!instantRes) {
            vmConfig.setLog(lcmLog.getLog());
            return false;
        }
        return true;
    }

    private Map<String, String> getInputParams(String parameter, String mecHost) {
        int count = hostLogMapper.getHostLogCount(mecHost);
        Map<String, String> vmInputParams = InputParameterUtil.getParams(parameter);
        String n6Range = vmInputParams.get("app_n6_ip");
        String mepRange = vmInputParams.get("app_mp1_ip");
        String internetRange = vmInputParams.get("app_internet_ip");
        vmInputParams.put("app_n6_ip", IpCalculateUtil.getStartIp(n6Range, count));
        vmInputParams.put("app_mp1_ip", IpCalculateUtil.getStartIp(mepRange, count));
        vmInputParams.put("app_internet_ip", IpCalculateUtil.getStartIp(internetRange, count));
        if (vmInputParams.getOrDefault("app_n6_gw", null) == null) {
            vmInputParams.put("app_n6_gw", IpCalculateUtil.getStartIp(n6Range, 0));
        }
        if (vmInputParams.getOrDefault("app_mp1_gw", null) == null) {
            vmInputParams.put("app_mp1_gw", IpCalculateUtil.getStartIp(mepRange, 0));
        }
        if (vmInputParams.getOrDefault("app_internet_gw", null) == null) {
            vmInputParams.put("app_internet_gw", IpCalculateUtil.getStartIp(internetRange, 0));
        }
        return vmInputParams;
    }

    private boolean deleteVmCreate(VmCreateConfig vmConfig, String userId, String lcmToken) {
        String appInstanceId = vmConfig.getAppInstanceId();
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmConfig.getHost()), type);
        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        boolean terminateApp = HttpClientUtil.terminateAppInstance(basePath, appInstanceId, userId, lcmToken);
        // delete hosts
        boolean deleteHostRes = HttpClientUtil
            .deleteHost(basePath, userId, lcmToken, vmConfig.getPackageId(), host.getMecHost());

        // delete pkg
        boolean deletePkgRes = HttpClientUtil.deletePkg(basePath, userId, lcmToken, vmConfig.getPackageId());

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

        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);
        List<VmCreateConfig> vmCreateConfigs = new LinkedList<>();
        if (vmCreateConfig != null) {
            vmCreateConfigs.add(vmCreateConfig);
        }
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

        if (!StringUtils.isEmpty(vmCreateConfig.getPackageId())) {
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

        LOGGER.info("upload system image file, fileName = {}, identifier = {}, chunkNum = {}", chunk.getFilename(),
            chunk.getIdentifier(), chunk.getChunkNumber());
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
        File outFile = new File(getUploadFileRootDir() + chunk.getIdentifier(), chunkNumber + ".part");
        InputStream inputStream = file.getInputStream();
        FileUtils.copyInputStreamToFile(inputStream, outFile);

        return Either.right(true);

    }

    /**
     * upload app file to vm.
     */

    public ResponseEntity mergeAppFile(String userId, String projectId, String vmId, String fileName, String identifier)
        throws IOException {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            return ResponseEntity.badRequest().build();
        }
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig == null) {
            LOGGER.info("Can not find the vm create config by vmId {} and projectId {}", vmId, projectId);
            return ResponseEntity.badRequest().build();
        }
        String partFilePath = getUploadFileRootDir() + identifier;
        File partFileDir = new File(partFilePath);

        File[] partFiles = partFileDir.listFiles();
        if (partFiles == null || partFiles.length == 0) {
            LOGGER.error("uploaded part file not found!");
            return ResponseEntity.badRequest().build();
        }

        File mergedFile = new File(getUploadFileRootDir() + File.separator + fileName);
        FileOutputStream destTempfos = new FileOutputStream(mergedFile, true);
        for (int i = 1; i <= partFiles.length; i++) {
            File partFile = new File(partFilePath, i + ".part");
            FileUtils.copyFile(partFile, destTempfos);
        }
        destTempfos.close();
        FileUtils.deleteDirectory(partFileDir);

        Boolean pushFileToVmRes = pushFileToVm(mergedFile, vmCreateConfig);
        FileUtils.deleteDirectory(new File(getUploadFileRootDir()));
        if (!pushFileToVmRes) {
            LOGGER.error("push app file failed!");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
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
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);
        if (vmCreateConfig == null) {
            LOGGER.error("Can not find the vm create config by projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the vm config.");
            return Either.left(error);
        }
        if (vmCreateConfig.getStatus() != EnumVmCreateStatus.SUCCESS) {
            LOGGER.error("vm create fail, can not import image,projectId:{}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "vm create fail, can not import image");
            return Either.left(error);
        }
        VmImageConfig vmImageConfig = vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId());
        if (vmImageConfig == null) {
            VmImageConfig createVmImageConfig = new VmImageConfig();
            createVmImageConfig.setVmId(vmCreateConfig.getVmId());
            createVmImageConfig.setVmName(vmPackageConfig.getVmName());
            createVmImageConfig.setAppInstanceId(vmCreateConfig.getAppInstanceId());
            createVmImageConfig.setLcmToken(token);
            createVmImageConfig.setProjectId(projectId);
            createVmImageConfig.setStatus(EnumVmImportStatus.CREATING);
            VmImportStageStatus stageStatus = new VmImportStageStatus();
            createVmImageConfig.setStageStatus(stageStatus);
            vmConfigMapper.saveVmImageConfig(createVmImageConfig);
        } else {
            deleteVmImageToLcm(vmCreateConfig, vmImageConfig, userId);
            vmImageConfig.setVmName(vmPackageConfig.getVmName());
            vmImageConfig.setAppInstanceId(vmCreateConfig.getAppInstanceId());
            vmImageConfig.setLcmToken(token);
            vmImageConfig.setStatus(EnumVmImportStatus.CREATING);
            VmImportStageStatus stageStatus = new VmImportStageStatus();
            vmImageConfig.setStageStatus(stageStatus);
            vmConfigMapper.updateVmImageConfig(vmImageConfig);
        }

        return Either.right(true);

    }

    private void deleteVmImageToLcm(VmCreateConfig vmCreateConfig, VmImageConfig vmImageConfig, String userId) {
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        if (!StringUtils.isEmpty(vmImageConfig.getImageId())) {
            HttpClientUtil.deleteVmImage(host.getProtocol(), host.getLcmIp(), host.getPort(), userId,
                vmImageConfig.getAppInstanceId(), vmImageConfig.getImageId(), vmImageConfig.getLcmToken());
        }
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

        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);

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
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);
        if (vmCreateConfig == null) {
            LOGGER.error("Can not find the vm create config by projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the vm config.");
            return Either.left(error);
        }

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

        String packagePath = projectService.getProjectPath(vmImageConfig.getProjectId()) + vmImageConfig
            .getAppInstanceId() + File.separator + "Image";
        FileUtils.deleteQuietly(new File(packagePath + File.separator + vmImageConfig.getImageName()));

        int res = vmConfigMapper.deleteVmImage(projectId, vmCreateConfig.getVmId());
        if (res < 1) {
            LOGGER.error("Delete vm image config {} failed.", vmCreateConfig.getVmId());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm image config failed."));
        }
        LOGGER.info("delete vm create config success");
        return Either.right(true);

    }

    /**
     * generate pkg.
     *
     * @return
     */
    public Either<FormatRespDto, VmPackageConfig> vmPackage(String userId, String projectId,
        VmPackageConfig vmPackageConfig) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        //check image info
        VmSystem vmSystem = vmPackageConfig.getVmSystem();
        Boolean checkImageRes = HttpClientUtil.checkImageInfo(vmSystem.getSystemPath());
        if (!checkImageRes) {
            LOGGER.error("image file no exit");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "image file no exit:"));
        }
        // generate vm package by config
        vmPackageConfig.setProjectId(projectId);
        try {
            generateVmPackageByConfig(vmPackageConfig);
        } catch (Exception e) {
            LOGGER.error("generate vm csar with id:{} on csar failed:{}", vmPackageConfig.getId(), e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "generate vm csar fail"));
        }

        // save vm package config to db
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
        ApplicationProject project = projectMapper.getProject(userId, projectId);
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
        ApplicationProject project = projectMapper.getProject(userId, projectId);
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
        // delete package file
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteDir(projectPath + config.getAppInstanceId());
        FileUtils.deleteQuietly(new File(projectPath + config.getAppInstanceId() + ".csar"));

        return Either.right(true);

    }

    /**
     * create vm package.
     */
    public File generateVmPackageByConfig(VmPackageConfig config) throws IOException, DomainException {
        config.setId(UUID.randomUUID().toString());
        config.setAppInstanceId(UUID.randomUUID().toString());
        config.setCreateTime(new Date());
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String projectPath = projectService.getProjectPath(config.getProjectId());
        File csarPkgDir;
        csarPkgDir = new NewCreateVmCsar().create(projectPath, config, project);
        // sign package
        encryptedService.encryptedFile(csarPkgDir.getCanonicalPath());
        encryptedService.encryptedCms(csarPkgDir.getCanonicalPath());
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
        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());

        Type vmInfoType = new TypeToken<List<VmInfo>>() { }.getType();
        List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), vmInfoType);
        String vmId = vmInfo.get(0).getVmId();

        String imageResult = HttpClientUtil.vmInstantiateImage(basePath, userId, lcmToken, vmId, appInstanceId, lcmLog);
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
    public boolean downloadImageResult(VmImageConfig config) {

        String imagePath = projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId() + "/Image";
        // modify image file
        String url = config.getChecksum();
        String slimResult = HttpClientUtil.getImageSlim(url);
        FileSystemResponse imageResult;
        if (slimResult==null) {
            return false;
        }
        try {
            imageResult = new ObjectMapper().readValue(slimResult.getBytes(), FileSystemResponse.class);
        } catch (Exception e) {
            return false;
        }
        String checkSum = imageResult.getCheckStatusResponse().getCheckInfo().getChecksum();
        File swImageDesc = new File(imagePath + TEMPLATE_TOSCA_IMAGE_DESC_PATH);
        try {
            List<ImageDesc> swImgDescs = getSwImageDescrInfo(
                FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8));
            swImgDescs.get(0).setSwImage(url + IMAGE_PATH);
            swImgDescs.get(0).setId(config.getImageId());
            swImgDescs.get(0).setChecksum(checkSum);
            writeFile(swImageDesc, gson.toJson(swImgDescs));
        } catch (IOException e) {
            LOGGER.error("modify image file fail: occur IOException {}.", e.getMessage());
            return false;
        }

        String outPath = projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId();
        config.setLog("start to generate csar file");
        vmConfigMapper.updateVmImageConfig(config);
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
        Type hostType = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), hostType);
        Map<String, String> vmInputParams = InputParameterUtil.getParams(host.getParameter());

        String networkName = vmInputParams.getOrDefault("network_name_n6", "mec_network_n6");
        Type type = new TypeToken<List<VmInfo>>() { }.getType();
        List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), type);
        List<NetworkInfo> networkInfos = vmInfo.get(0).getNetworks();
        String networkIp = "";
        for (NetworkInfo networkInfo : networkInfos) {
            if (networkInfo.getName().equals(networkName)) {
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
            return false;
        }

    }

    private String pushSystemImage(File systemImgFile) {
        try {
            String uploadResult = HttpClientUtil
                .uploadSystemImage(fileServerAddress, systemImgFile.getPath(), AccessUserUtil.getUserId());
            if (uploadResult == null) {
                LOGGER.error("upload system image file failed.");
                return null;
            }

            try {
                Gson gson = new Gson();
                Map<String, String> uploadResultModel = gson.fromJson(uploadResult, Map.class);
                return fileServerAddress + String
                    .format(Consts.SYSTEM_IMAGE_DOWNLOAD_URL, uploadResultModel.get("imageId"));
            } catch (JsonSyntaxException e) {
                LOGGER.error("upload system image file failed.");
                return null;
            }
        } finally {
            LOGGER.info("delete system image file.");
            boolean delResult = systemImgFile.delete();
            if (!delResult) {
                LOGGER.error("delete system image file failed!.");
            }
        }
    }

    /**
     * cleanVmDeploy.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> cleanVmDeploy(String projectId, String token) {
        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfigs(projectId);
        if (vmCreateConfig == null) {
            LOGGER.info("Can not find the vm create config by projectId {}", projectId);
            return Either.right(true);
        }
        VmImageConfig vmImageConfig = vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId());
        if (vmImageConfig != null) {
            //clean data
            vmImageConfig.initialVmImageConfig();
            vmConfigMapper.updateVmImageConfig(vmImageConfig);
        }
        if (vmImageConfig != null && !vmImageConfig.getImageId().isEmpty()) {
            deleteVmImageToLcm(vmCreateConfig, vmImageConfig, project.getUserId());
        }

        if (!StringUtils.isEmpty(vmCreateConfig.getPackageId())) {
            deleteVmCreate(vmCreateConfig, project.getUserId(), token);
        }
        //clean data
        vmCreateConfig.initialVmCreateConfig();
        vmConfigMapper.updateVmCreateConfig(vmCreateConfig);
        LOGGER.info("Update vm create config {} success", vmCreateConfig.getVmId());
        project.initialProject();
        int res = projectMapper.updateProject(project);
        if (res < 1) {
            LOGGER.error("Update project status failed");
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "update project failed");
            return Either.left(error);
        }
        LOGGER.info("Update project status to TESTED success");
        return Either.right(true);
    }

    /**
     * runOverTime.
     *
     * @param createTime createTime
     * @return
     */
    public boolean runOverTime(Date createTime) {
        long time = System.currentTimeMillis() - createTime.getTime();
        LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS, createTime.getTime());
        if (time > MAX_SECONDS * 20) {
            String message = "get status after wait {} seconds";
            LOGGER.error(message, MAX_SECONDS);
            return true;
        }
        return false;
    }

}

