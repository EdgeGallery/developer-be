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
package org.edgegallery.developer.service.virtual.create;

import javax.annotation.Resource;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.service.virtual.VmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_csar_service")
public class VmStageCreateCsar implements VmCreateStage{
    private static final Logger LOGGER = LoggerFactory
        .getLogger(VmStageCreateCsar.class);

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Resource(name = "vm_hostInfo_service")
    private VmCreateStage vmCreateStage;

    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processSuccess = false;
        EnumTestConfigStatus vmPackageStatus = EnumTestConfigStatus.Failed;
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        try {
            // create csar package
            vmService.generateVmPackage(config);
            vmPackageStatus = EnumTestConfigStatus.Success;
            processSuccess = true;
        } catch (Exception e) {
            processSuccess = false;
            config.setLog("generate  vm csar failed:" + e.getMessage());
            LOGGER.error("generate vm csar with vmId:{} on csar failed:{}", config.getVmId(), e.getMessage());
        } finally {
            vmService.updateCreateVmResult(config, project, "csar", vmPackageStatus);
        }
        return processSuccess ? vmCreateStage.execute(config) : processSuccess;
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
