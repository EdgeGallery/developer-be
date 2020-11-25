package org.edgegallery.developer.service.deploy;

import java.io.File;
import org.edgegallery.developer.config.security.AccessUserUtil;
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
        String userId = AccessUserUtil.getUserId();
        ApplicationProject project = projectMapper.getProject(userId, config.getProjectId());
        EnumTestConfigStatus instantiateStatus = EnumTestConfigStatus.Failed;
        File csar = null;
        try {
            csar = new File(projectService.getProjectPath(config.getProjectId()));
        } catch (Exception e) {
            // cannot find csar file
            config.setErrorLog("Cannot find csar file: " + projectService.getProjectPath(config.getProjectId()));
            LOGGER.error("Cannot find csar file: {}.", projectService.getProjectPath(config.getProjectId()));
        }
        if (csar != null) {
            boolean instantiateAppResult = projectService
                .deployTestConfigToAppLcm(csar, project, config, userId, config.getLcmToken());
            if (!instantiateAppResult) {
                // deploy failed
                config.setErrorLog("Failed to instantiate app which appInstanceId is: " + config.getAppInstanceId());
                LOGGER.error("Failed to instantiate app which appInstanceId is : {}.", config.getAppInstanceId());
            }
            // update status
            config.setAppInstanceId(config.getAppInstanceId());
            config.setWorkLoadId(config.getAppInstanceId());
            processSuccess = true;
            instantiateStatus = EnumTestConfigStatus.Success;
        }
        projectService.updateDeployResult(config, project, "instantiateInfo", instantiateStatus);
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
