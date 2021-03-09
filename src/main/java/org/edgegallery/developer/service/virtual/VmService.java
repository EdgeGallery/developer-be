package org.edgegallery.developer.service.virtual;

import static org.edgegallery.developer.util.AtpUtil.getProjectPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.EnumVmImportStatus;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;

@Service("vmService")
public class VmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

    private static final String VMPATH = "/home/developer";


    private static Gson gson = new Gson();

    @Autowired
    private VmConfigMapper vmConfigMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private VmService vmService;
    @Autowired
    private ProjectService projectService;

    @Autowired
    private Map<String, VmCreateStage> createServiceMap;

    @Autowired
    private Map<String, VmImageStage> imageServiceMap;

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
        File csarPkgDir;
        csarPkgDir = new NewCreateVmCsar().create(projectPath, config, project, flavor.getFlavor());
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
        List<VmCreateConfig> VmConfigList = vmConfigMapper
            .getVmCreateConfigStatus(EnumVmCreateStatus.CREATING.toString());
        if (CollectionUtils.isEmpty(VmConfigList)) {
            return;
        }
        VmConfigList.forEach(this::processVmCreateConfig);
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
            LOGGER.error("create vm config:{} failed on stage :{}, res:{}", config.getVmId(), nextStage,
                e.getMessage());
        }
    }


    public boolean createVmToAppLcm(File csar, ApplicationProject project, VmCreateConfig vmConfig, String userId, String lcmToken) {
        String projectName = project.getName().replaceAll(Consts.PATTERN, "").toLowerCase();
        String appInstanceId = vmConfig.getAppInstanceId();
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmConfig.getHost()), type);
        return HttpClientUtil
            .vmInstantiateApplication(host.getProtocol(), host.getIp(), host.getPort(), csar.getPath(), appInstanceId,
                userId, projectName, vmConfig);
    }

    private void deleteVmCreate(VmCreateConfig testConfig, String userId, String lcmToken) {
    }

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

    public Either<FormatRespDto, Boolean> deleteCreateVm(String userId, String projectId, String vmId, String token) {

        ApplicationProject project = projectMapper.getProjectById(projectId);
        if (project == null) {
            LOGGER.error("Can not find the project projectId {}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        LOGGER.info("Get project information success");
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig==null) {
            LOGGER.info("Can not find the vm create config by vmId {} and projectId {}", vmId, projectId);
            return Either.right(true);
        }

        if(EnumTestConfigStatus.Success.equals(vmCreateConfig.getStageStatus().getInstantiateInfo())) {
            Type type = new TypeToken<MepHost>() { }.getType();
            MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
            boolean terminateResult = HttpClientUtil
                .terminateAppInstance(host.getProtocol(), host.getIp(), host.getPort(), vmCreateConfig.getAppInstanceId(),
                    userId, token);
            if (!terminateResult) {
                LOGGER.error("Failed to terminate vm which userId is: {}, instanceId is {}", userId,
                    vmCreateConfig.getAppInstanceId());
            }
        }

        int res = vmConfigMapper.deleteVmCreateConfig(projectId, vmId);
        if (res<1) {
            LOGGER.error("Delete vm create config {} failed.", vmId);
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm create config failed."));
        }
        String projectPath = getProjectPath(projectId);
        DeveloperFileUtils.deleteDir(projectPath + vmCreateConfig.getAppInstanceId());
        DeveloperFileUtils.deleteDir(projectPath + vmCreateConfig.getAppInstanceId() + ".csar");

        LOGGER.info("delete vm create config success");
        return Either.right(true);

    }

    public Either<FormatRespDto, Boolean> uploadFileToVm(String userId, String projectId, String vmId, MultipartFile uploadFile)
        throws IOException {
        LOGGER.info("Begin upload file");

        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig==null) {
            LOGGER.info("Can not find the vm create config by vmId {} and projectId {}", vmId, projectId);
            return Either.right(true);
        }
        File file = transferToFile(uploadFile);

        Type type = new TypeToken<List<VmInfo>>() { }.getType();
        List<VmInfo> vmInfo = gson.fromJson(gson.toJson(vmCreateConfig.getVmInfo()), type);

        FTPClient ftpClient = new FTPClient();//import org.apache.commons.net.ftp.FTPClient;
        ftpClient.connect(vmInfo.get(0).getVncUrl(), 21);//连接ftp
        ftpClient.login("root", "root");//登陆ftp
        ftpClient.changeWorkingDirectory(VMPATH);//需要把文件上传到FTP哪个目录
        boolean result = ftpClient.storeFile(file.getName(), new FileInputStream(file));//存储文件,成功返回true,失败false
        if(!result) {
            LOGGER.warn("upload fail, ip:{}", vmInfo.get(0).getVncUrl());
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "upload file fail to vm");
            return Either.left(error);
        }
        return Either.right(true);
    }

    private File transferToFile(MultipartFile multipartFile) {
//        选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String[] filename = originalFilename.split(".");
            file=File.createTempFile(filename[0], filename[1]);
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    public Either<FormatRespDto, ResponseEntity<byte[]>> downloadVmCsar(String userId, String projectId, String vmId) {
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig==null) {
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
        List<VmImageConfig> VmImageList = vmConfigMapper
            .getVmImageConfigStatus(EnumVmImportStatus.CREATING.toString());
        if (CollectionUtils.isEmpty(VmImageList)) {
            return;
        }
        VmImageList.forEach(this::processVmImageConfig);
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
            LOGGER.error(" vm image config:{} failed on stage :{}, res:{}", config.getVmId(), nextStage,
                e.getMessage());
        }
    }


    // import image
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
        // vmId 存在 返回失敗 todo

        VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);
        if (vmCreateConfig.getStatus()!=EnumVmCreateStatus.SUCCESS) {
            LOGGER.error("vm create fail, can not import image,projectId:{}", projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "vm create fail, can not import image");
            return Either.left(error);
        }
        if ( vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId())!=null) {
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
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the vm config.");
            return Either.left(error);
        }
        VmCreateConfig vmCreateConfig = vmCreateConfigs.get(0);

        VmImageConfig vmImageConfig = vmConfigMapper.getVmImage(projectId, vmCreateConfig.getVmId());
        return Either.right(vmImageConfig);
    }

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
        if (vmImageConfig==null) {
            LOGGER.info("Can not find the vm image config by vmId {} and projectId {}", vmCreateConfig.getVmId(), projectId);
            return Either.right(true);
        }
        // delete lcm image todo

        int res = vmConfigMapper.deleteVmImage(projectId, vmCreateConfig.getVmId());
        if (res<1) {
            LOGGER.error("Delete vm image config {} failed.", vmCreateConfig.getVmId());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm image config failed."));
        }
        String projectPath = getProjectPath(projectId);
        String imagePath = "/Image" + vmImageConfig.getImageName() + ".zip";
        DeveloperFileUtils.deleteDir(projectPath + File.separator + vmImageConfig.getAppInstanceId() + imagePath);

        LOGGER.info("delete vm create config success");
        return Either.right(true);

    }

    public boolean createVmImageToAppLcm(MepHost host, VmImageConfig imageConfig, String userId) {

        return HttpClientUtil
            .vmInstantiateImage(host.getProtocol(), host.getIp(), host.getPort(),
                userId, imageConfig);
    }
}
