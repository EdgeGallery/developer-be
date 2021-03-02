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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmCreateStageStatus;
import org.edgegallery.developer.model.vm.VmInfo;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.vm.VmSystem;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.csar.NewCreateVmCsar;
import org.edgegallery.developer.service.virtual.create.VmCreateStage;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Map<String, VmCreateStage> createServiceMap;

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
        File csarPkgDir;
        csarPkgDir = new NewCreateVmCsar().create(projectPath, config, project);
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
        // delete resource after deploying failed
        if (EnumTestConfigStatus.Failed.equals(stageStatus) && testConfig.getAppInstanceId() != null) {
            deleteVmCreate(testConfig, project.getUserId(), testConfig.getLcmToken());
            LOGGER.warn("create vm failed, delete create vm  info.");
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
        ApplicationProject project = projectMapper.getProject(userId, projectId);
        if (project == null) {
            LOGGER.error("Can not find the project by userId {} and projectId {}", userId, projectId);
            FormatRespDto error = new FormatRespDto(Status.BAD_REQUEST, "Can not find the project.");
            return Either.left(error);
        }

        LOGGER.info("Get project information success");
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(projectId, vmId);
        if (vmCreateConfig==null) {
            LOGGER.info("Can not find the vm create config by vmId {} and projectId {}", vmId, projectId);
            return Either.right(true);
        }
        if (!EnumTestConfigStatus.Success.equals(vmCreateConfig.getStageStatus().getInstantiateInfo())) {
            LOGGER.error("Failed to terminate vm when instantiateInfo not success.");
            FormatRespDto error = new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                "Failed to terminate vm when instantiateInfo not success.");
            return Either.left(error);
        }

        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        boolean terminateResult = HttpClientUtil
            .terminateAppInstance(host.getProtocol(), host.getIp(), host.getPort(), vmCreateConfig.getAppInstanceId(),
                userId, token);
        if (!terminateResult) {
            LOGGER.error("Failed to terminate vm which userId is: {}, instanceId is {}", userId,
                vmCreateConfig.getAppInstanceId());
        }
        int res = vmConfigMapper.deleteVmCreateConfig(projectId, vmId);
        if (res<1) {
            LOGGER.error("Delete vm create config {} failed.", vmId);
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Delete vm create config failed."));
        }
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
}

