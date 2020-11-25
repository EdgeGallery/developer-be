package org.edgegallery.developer.service.deploy;

import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.workspace.*;
import org.edgegallery.developer.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
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
        String userId = AccessUserUtil.getUserId();
        ApplicationProject project = projectMapper.getProject(userId, config.getProjectId());
        EnumTestConfigStatus hostStatus = EnumTestConfigStatus.Failed;
        if (config.isPrivateHost()){
            hostStatus = EnumTestConfigStatus.Success;
            processSuccess = true;
        }else{
            List<MepHost> enabledHosts = hostMapper.getHostsByStatus(EnumHostStatus.NORMAL);
            if (CollectionUtils.isEmpty(enabledHosts)){
                processSuccess = false;
                LOGGER.error("Cannot find enabledHosts");
            } else{
                processSuccess = true;
                config.setHosts(enabledHosts.subList(0,1));
                hostStatus = EnumTestConfigStatus.Success;
            }
        }
        projectService.updateDeployResult(config, project,"hostInfo", hostStatus);
        if(processSuccess){
            return  instantiateService.execute(config);
        }else {
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
