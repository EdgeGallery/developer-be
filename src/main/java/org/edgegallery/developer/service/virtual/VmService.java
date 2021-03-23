package org.edgegallery.developer.service.virtual;

import static org.edgegallery.developer.util.AtpUtil.getProjectPath;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.lcm.UploadResponse;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.EnumVmImportStatus;
import org.edgegallery.developer.model.vm.FileUploadEntity;
import org.edgegallery.developer.model.vm.ScpConnectEntity;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmCreateStageStatus;
import org.edgegallery.developer.model.vm.VmFlavor;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmImportStageStatus;
import org.edgegallery.developer.model.vm.VmInfo;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.vm.VmSystem;
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
import org.edgegallery.developer.util.ShhFileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

@Service("vmService")
public class VmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    private static Gson gson = new Gson();

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
     * @return
     */
    public Either<FormatRespDto, VmResource> getVirtualResource() {
        List<VmRegulation> vmRegulation = vmConfigMapper.getVmRegulation();
        List<VmSystem> vmSystem = vmConfigMapper.getVmSystem();
        List<VmNetwork> vmNetwork = vmConfigMapper.getVmNetwork();
        VmResource vmResource = new VmResource();
        vmResource.setVmRegulationList(vmRegulation);
        vmResource.setVmSystemList(vmSystem);
        vmResource.setVmNetworkList(vmNetwork);
        LOGGER.info("Get all vm resource success");
        return Either.right(vmResource);

    }

    /**
     * createVm.
     *
     * @return
     */
    public Either<FormatRespDto, VmCreateConfig> createVm(String userId, String projectId,
        VmCreateConfig vmCreateConfig, String token) {

        String vmId = UUID.randomUUID().toString();
        String appInstanceId = UUID.randomUUID().toString();
        vmCreateConfig.setAppInstanceId(appInstanceId);
        vmCreateConfig.setLcmToken(token);
        vmCreateConfig.setProjectId(projectId);
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
     * create vm package.
     */
    public File generateVmPackage(VmCreateConfig config) throws IOException {
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String projectPath = getProjectPath(config.getProjectId());
        VmFlavor flavor = vmConfigMapper.getVmFlavor(config.getVmRegulation().getArchitecture());
        List<VmNetwork> vmNetworks = vmConfigMapper.getVmNetwork();
        File csarPkgDir;
        csarPkgDir = new NewCreateVmCsar().create(projectPath, config, project, flavor.getFlavor(), vmNetworks);
        return CompressFileUtilsJava
            .compressToCsarAndDeleteSrc(csarPkgDir.getCanonicalPath(), projectPath, csarPkgDir.getName());
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
            case "csar":
                testConfig.getStageStatus().setCsar(stageStatus);
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
            testConfig.setLog("");
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
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmConfig.getHost()), type);

        // upload pkg
        LcmLog lcmLog = new LcmLog();
        String uploadRes = HttpClientUtil
            .uploadPkg(host.getProtocol(), host.getLcmIp(), host.getPort(), csar.getPath(), userId, lcmToken, lcmLog);
        LOGGER.info("upload package result: {}", uploadRes);
        if (org.springframework.util.StringUtils.isEmpty(uploadRes)) {
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
            .distributePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, lcmToken, pkgId,
                host.getMecHost(), lcmLog);
        LOGGER.info("distribute package result: {}", distributeRes);
        if (!distributeRes) {
            vmConfig.setLog(lcmLog.getLog());
            return false;
        }
        // instantiate application
        String appInstanceId = vmConfig.getAppInstanceId();
        boolean instantRes = HttpClientUtil
            .instantiateApplication(host.getProtocol(), host.getLcmIp(), host.getPort(), appInstanceId, userId,
                lcmToken, lcmLog, pkgId, host.getMecHost());
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
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteDir(projectPath + vmCreateConfig.getAppInstanceId());
        FileUtils.deleteQuietly(new File(projectPath + vmCreateConfig.getAppInstanceId() + ".csar"));

        LOGGER.info("delete vm create config success");
        return Either.right(true);

    }

    /**
     * uploadFileToVm.
     */
    public Either<FormatRespDto, Boolean> uploadFileToVm(String userId, String projectId, String vmId,
        MultipartFile uploadFile) throws Exception {
        LOGGER.info("Begin upload file");

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

        Type type = new TypeToken<List<VmInfo>>() { }.getType();
        List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), type);
        String networkIp = vmInfo.get(0).getNetworks().get(0).getIp();
        LOGGER.info("network ip is {}", networkIp);
        // ssh upload file
        String targetPath = "/home";
        ScpConnectEntity scpConnectEntity = new ScpConnectEntity();
        scpConnectEntity.setTargetPath(targetPath);
        scpConnectEntity.setUrl(networkIp);
        scpConnectEntity.setPassWord("ubuntu");
        scpConnectEntity.setUserName("123456");
        File file = transferToFile(uploadFile);
        String remoteFileName = file.getName();

        ShhFileUploadUtil sshFileUploadUtil = new ShhFileUploadUtil();
        FileUploadEntity fileUploadEntity = sshFileUploadUtil.uploadFile(file, remoteFileName, scpConnectEntity);
        if (fileUploadEntity.getCode().equals("ok")) {
            return Either.right(true);
        } else {
            LOGGER.warn("upload fail, ip:{}", vmInfo.get(0).getNetworks().get(0).getIp());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, fileUploadEntity.getMessage());
            return Either.left(error);
        }
    }

    private File transferToFile(MultipartFile multipartFile) {
        File file = null;
        if (multipartFile != null) {
            try {
                String originalFilename = multipartFile.getOriginalFilename();
                if (!org.springframework.util.StringUtils.isEmpty(originalFilename)) {
                    String[] filename = originalFilename.split("\\.");
                    file = File.createTempFile(filename[0], filename[1]);
                    multipartFile.transferTo(file);
                    file.deleteOnExit();
                }
            } catch (IOException e) {
                LOGGER.error("transfer multiFile to file failed! {}", e.getMessage());
            }
        }
        return file;
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
        vmImageConfig.setVmName(vmCreateConfig.getVmName());
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
            HttpClientUtil
                .deleteVmImage(host.getProtocol(), host.getLcmIp(), host.getPort(), vmImageConfig.getAppInstanceId(),
                    userId, vmImageConfig.getImageId(), token);
        }

        int res = vmConfigMapper.deleteVmImage(projectId, vmCreateConfig.getVmId());
        if (res < 1) {
            LOGGER.error("Delete vm image config {} failed.", vmCreateConfig.getVmId());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm image config failed."));
        }
        String projectPath = getProjectPath(projectId);
        String imagePath = "/Image" + vmImageConfig.getImageName() + ".zip";
        DeveloperFileUtils.deleteDir(projectPath + File.separator + vmImageConfig.getAppInstanceId() + imagePath);

        LOGGER.info("delete vm create config success");
        return Either.right(true);

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

        String packagePath = getProjectPath(config.getProjectId()) + config.getAppInstanceId() + File.separator
            + "Image" + File.separator + "";
        for (int chunkNum = 0; chunkNum < config.getSumChunkNum(); chunkNum++) {
            LOGGER.info("download image chunkNum:{}", chunkNum);
            boolean res = HttpClientUtil
                .downloadVmImage(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, packagePath,
                    config.getAppInstanceId(), config.getImageId(), Integer.toString(chunkNum), config.getLcmToken());
            if (!res) {
                LOGGER.info("download image fail");
                return false;
            }
            if (chunkNum % 10 == 0) {
                config.setLog("download image file:" + chunkNum + "/" + config.getSumChunkNum());
                vmConfigMapper.updateVmImageConfig(config);
            }
        }

        try {
            CompressFileUtilsJava
                .compressToCsarAndDeleteSrc(packagePath, projectService.getProjectPath(config.getProjectId()),
                    config.getAppInstanceId());
        } catch (IOException e) {
            LOGGER.error("generate csar failed: occur IOException {}.", e.getMessage());
            return false;
        }
        LOGGER.info("download image success");
        return true;
    }

}

