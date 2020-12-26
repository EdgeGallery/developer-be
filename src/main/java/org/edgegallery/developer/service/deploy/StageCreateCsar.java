/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.service.deploy;

import javax.annotation.Resource;
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
 * StageCreateCsar.
 *
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
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
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
            projectService.updateDeployResult(config, project, "csar", csarStatus);
        }
        return processSuccess == true ? stageService.execute(config) : processSuccess;
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
