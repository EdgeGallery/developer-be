package org.edgegallery.developer.service.virtual;

import static org.edgegallery.developer.util.AtpUtil.getProjectPath;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.EnumVmCreateStatus;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmCreateStageStatus;
import org.edgegallery.developer.model.vm.VmNetwork;
import org.edgegallery.developer.model.vm.VmRegulation;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.vm.VmSystem;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.csar.NewCreateVmCsar;
import org.edgegallery.developer.service.virtual.create.VmCreateStage;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.spencerwi.either.Either;

@Service("vmService")
public class VmService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VmService.class);

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
        String packageId = UUID.randomUUID().toString();
        vmCreateConfig.setPackageId(packageId);
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
        if (EnumTestConfigStatus.Failed.equals(stageStatus) && testConfig.getPackageId() != null) {
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

    private void deleteVmCreate(VmCreateConfig testConfig, String userId, String lcmToken) {
    }

}

