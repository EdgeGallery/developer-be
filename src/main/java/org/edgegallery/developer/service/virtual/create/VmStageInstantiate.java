/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.service.virtual.create;

import java.io.File;
import java.util.Date;
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

@Service("vm_instantiateInfo_service")
public class VmStageInstantiate implements VmCreateStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmStageInstantiate.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private VmService vmService;

    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processSuccess = false;
        boolean instantiateAppResult;

        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        String userId = project.getUserId();
        EnumTestConfigStatus instantiateStatus = EnumTestConfigStatus.Failed;
        // deploy app
        File csar;
        try {
            csar = new File(projectService.getProjectPath(config.getProjectId()) + config.getAppInstanceId() + ".csar");
            instantiateAppResult = vmService.createVmToAppLcm(csar, project, config, userId, config.getLcmToken());
            if (!instantiateAppResult) {
                LOGGER.error("Failed to create vm which packageId is : {}.", config.getPackageId());
            } else {
                // update status when instantiate success
                config.setCreateTime(new Date());
                processSuccess = true;
                instantiateStatus = EnumTestConfigStatus.Success;
                config.setLog("vm instantiate success");
            }
        } catch (Exception e) {
            config.setLog("Failed to create vm  with err:" + e.getMessage());
            LOGGER.error("Failed to create vm with err: {}.", e.getMessage());
        } finally {
            vmService.updateCreateVmResult(config, project, "instantiateInfo", instantiateStatus);
            LOGGER.info("update config result:{}", config);
        }
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
