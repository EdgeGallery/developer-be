package org.edgegallery.developer.service.deploy;

import java.io.File;
import java.util.Date;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * StageInstantiate.
 *
 * @author chenhui
 */
@Service("instantiateInfo_service")
public class StageInstantiate implements IConfigDeployStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageInstantiate.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean execute(ProjectTestConfig config) {
        boolean processSuccess = false;
        boolean instantiateAppResult;
        boolean dependencyResult;

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        EnumTestConfigStatus instantiateStatus = EnumTestConfigStatus.Failed;
        // check dependency app
        dependencyResult = projectService.checkDependency(project);
        if (!dependencyResult) {
            config.setAccessUrl("");
            config.setErrorLog("dependency app not deploy");
            LOGGER.error("Failed to instantiate app: dependency app not deploy");
            projectService.updateDeployResult(config, project, "instantiateInfo", instantiateStatus);
            return false;
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        // deploy app
        File csar;
        try {
            csar = new File(projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId() + ".csar");
            instantiateAppResult = projectService
                    .deployTestConfigToAppLcm(csar, project, config, userId, config.getLcmToken());
            if (!instantiateAppResult) {
                config.setAccessUrl("");
                config.setErrorLog("Instantiate failed: deploy env error ");
                LOGGER.error("Failed to instantiate app which appInstanceId is : {}.", config.getAppInstanceId());
            } else {
                // update status when instantiate success
                config.setAppInstanceId(config.getAppInstanceId());
                config.setWorkLoadId(config.getAppInstanceId());
                config.setDeployDate(new Date());
                processSuccess = true;
                instantiateStatus = EnumTestConfigStatus.Success;
            }
        } catch (Exception e) {
            config.setErrorLog("Failed to instantiate app with err:" + e.getMessage());
            LOGGER.error("Failed to instantiate app with err: {}.", e.getMessage());
        } finally {
            projectService.updateDeployResult(config, project, "instantiateInfo", instantiateStatus);
        }
        return processSuccess;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public boolean immediateExecute(ProjectTestConfig config) {
        return true;
    }

}
