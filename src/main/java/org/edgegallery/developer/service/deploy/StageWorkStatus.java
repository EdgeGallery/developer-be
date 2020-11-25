package org.edgegallery.developer.service.deploy;

import com.google.common.collect.Lists;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * StageWorkStatus.
 *
 * @author chenhui
 */
@Service("workStatus_service")
public class StageWorkStatus implements IConfigDeployStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(StageWorkStatus.class);

    /**
     * the max retry time for get workStatus.
     */
    private static final int maxRetry = 30;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean execute(ProjectTestConfig config) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;
        String userId = AccessUserUtil.getUserId();
        ApplicationProject project = projectMapper.getProject(userId, config.getProjectId());
        MepHost host = config.getHosts().get(0);
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getIp(), host.getPort(), config.getAppInstanceId(), userId,
                config.getLcmToken());
        if (workStatus == null) {
            // verify retry times
            if (config.getRetry() > maxRetry) {
                config.setErrorLog("Failed to get workloadStatus with appInstanceId:" + config.getAppInstanceId());
                LOGGER.error("Failed to get workloadStatus after {} times which appInstanceId is : {}", maxRetry,
                    config.getAppInstanceId());
            } else {
                config.setRetry(config.getRetry() + 1);
                projectMapper.updateTestConfig(config);
                return true;
            }
        } else {
            processStatus = true;
            status = EnumTestConfigStatus.Success;
            config.setPods(Lists.newArrayList(workStatus));
            LOGGER.info("Query workload status response: {}", workStatus);
        }
        // update test-config
        projectService.updateDeployResult(config, project, "workStatus", status);
        return processStatus;
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
