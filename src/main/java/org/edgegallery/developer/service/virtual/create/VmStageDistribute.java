package org.edgegallery.developer.service.virtual.create;

import java.io.File;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.virtual.VmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_distributeInfo_service")
public class VmStageDistribute implements VmCreateStage {
    private static final Logger LOGGER = LoggerFactory.getLogger(VmStageInstantiate.class);

    /**
     * image package upload  workStatus:fail.
     */
    private static final String PACKAGE_FAIL = "killed";

    /**
     * image package upload  workStatus:success.
     */
    private static final String PACKAGE_SUCCESS = "uploaded";

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private VmService vmService;

    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processSuccess = false;
        EnumTestConfigStatus distributeStatus = EnumTestConfigStatus.Failed;
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        // deploy app
        File csar = new File(
            projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId() + ".csar");
        String distributeResult = vmService.distributeVmToAppLcm(csar, project, config, userId, config.getLcmToken());
        // over time
        if (vmService.runOverTime(config.getCreateTime())) {
            vmService.updateCreateVmResult(config, project, "distributeInfo", distributeStatus);
            LOGGER.info("update config result:{}", config.getStatus());
            return false;
        }
        if (distributeResult == null) {
            LOGGER.error("Failed to create vm which packageId is : {}.", config.getPackageId());
            vmService.updateCreateVmResult(config, project, "distributeInfo", distributeStatus);
        }
        if (PACKAGE_FAIL.equals(distributeResult)) {
            LOGGER.error("Failed to upload vm image packageId is : {}.", config.getPackageId());
        } else if (PACKAGE_SUCCESS.equals(distributeResult)) {
            processSuccess = true;
            distributeStatus = EnumTestConfigStatus.Success;
            config.setLog("vm package distribute success");
        } else {
            return true;
        }
        vmService.updateCreateVmResult(config, project, "distributeInfo", distributeStatus);
        LOGGER.info("update config result:{}", config.getStatus());
        return processSuccess;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public boolean immediateExecute(VmCreateConfig config) {
        return true;
    }

}
