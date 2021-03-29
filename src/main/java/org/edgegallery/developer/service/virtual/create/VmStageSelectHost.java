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

import java.util.List;
import javax.annotation.Resource;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("vm_hostInfo_service")
public class VmStageSelectHost implements VmCreateStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmStageSelectHost.class);

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmService vmService;

    @Resource(name = "vm_csar_service")
    private VmCreateStage vmCreateStage;

    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processSuccess = false;
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        EnumTestConfigStatus hostStatus = EnumTestConfigStatus.Failed;
        List<MepHost> enabledHosts = hostMapper
            .getHostsByStatus(EnumHostStatus.NORMAL, "admin", project.getPlatform().get(0), "OpenStack");
        if (CollectionUtils.isEmpty(enabledHosts)) {
            processSuccess = false;
            LOGGER.error("Cannot find available hosts information");
            config.setLog("Cannot find available hosts information");
        } else {
            processSuccess = true;
            config.setHost(enabledHosts.get(0));
            hostStatus = EnumTestConfigStatus.Success;
            config.setLog("get mecHost success");

        }
        vmService.updateCreateVmResult(config, project, "hostInfo", hostStatus);
        if (processSuccess) {
            return vmCreateStage.execute(config);
        } else {
            return false;
        }
    }

    @Override
    public boolean destroy() {
        return false;
    }

    @Override
    public boolean immediateExecute(VmCreateConfig config) {
        return false;
    }
}
