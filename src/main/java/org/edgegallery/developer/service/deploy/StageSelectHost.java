package org.edgegallery.developer.service.deploy;

import java.util.List;
import javax.annotation.Resource;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * StageSelectHost.
 *
 * @author chenhui
 */
@Service("hostInfo_service")
public class StageSelectHost implements IConfigDeployStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageSelectHost.class);

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Resource(name = "instantiateInfo_service")
    private IConfigDeployStage instantiateService;

    @Override
    public boolean execute(ProjectTestConfig config) throws InterruptedException {
        boolean processSuccess = false;
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        EnumTestConfigStatus hostStatus = EnumTestConfigStatus.Failed;
        if (config.isPrivateHost()) {
            List<MepHost> privateHosts = hostMapper.getHostsByUserId(project.getUserId());
            config.setHosts(privateHosts.subList(0, 1));
            hostStatus = EnumTestConfigStatus.Success;
            processSuccess = true;
        } else {
            List<MepHost> enabledHosts = hostMapper.getHostsByStatus(EnumHostStatus.NORMAL,"admin");
            if (CollectionUtils.isEmpty(enabledHosts)) {
                processSuccess = false;
                LOGGER.error("Cannot find enabledHosts");
            } else {
                processSuccess = true;
                config.setHosts(enabledHosts.subList(0, 1));
                hostStatus = EnumTestConfigStatus.Success;
            }
        }
        projectService.updateDeployResult(config, project, "hostInfo", hostStatus);
        if (processSuccess) {
            return instantiateService.execute(config);
        } else {
            return false;
        }
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
