package org.edgegallery.developer.service.deploy;

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


import javax.annotation.Resource;

/**
 * @author chenhui
 */
@Service("csar_service")
public class StageCreateCsar implements IConfigDeployStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageCreateCsar.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Resource(name = "hostInfo_service")
    private IConfigDeployStage stageService;

    @Override
    public boolean execute(ProjectTestConfig config) throws InterruptedException {
        boolean processSuccess = false;
        EnumTestConfigStatus csarStatus = EnumTestConfigStatus.Failed;
        String userId = AccessUserUtil.getUserId();
        ApplicationProject project = projectMapper.getProject(userId, config.getProjectId());
        try {
            // create csar package
            projectService.createCsarPkg(userId, project, config);
            csarStatus = EnumTestConfigStatus.Success;
            processSuccess = true;
        } catch (Exception e) {
            processSuccess = false;
            config.setErrorLog("Deploying on csar failed:" + e.getMessage());
            LOGGER.error("Deploying with test id:{} on csar failed:{}", config.getTestId(), e.getMessage());
        } finally {
            projectService.updateDeployResult(config, project,"csar", csarStatus);
        }
        return processSuccess == true?stageService.execute(config):processSuccess;
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
