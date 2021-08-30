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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.mapper.HostMapper;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
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
    private VmConfigMapper vmConfigMapper;

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private VmService vmService;


    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processSuccess = false;
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());

        VmPackageConfig vmPackageConfig = vmConfigMapper.getVmPackageConfig(project.getId());
        EnumTestConfigStatus hostStatus = EnumTestConfigStatus.Failed;
        List<MepHost> openStackHosts = hostMapper
            .getHostsByStatus(EnumHostStatus.NORMAL, project.getPlatform().get(0), "OpenStack");
        List<MepHost> fusionSphereHosts = hostMapper
            .getHostsByStatus(EnumHostStatus.NORMAL, project.getPlatform().get(0), "FusionSphere");
        if (CollectionUtils.isEmpty(openStackHosts) && CollectionUtils.isEmpty(fusionSphereHosts)) {
            processSuccess = false;
            LOGGER.error("Cannot find available hosts information");
            config.setLog("Cannot find available hosts information");
        } else {
            if(!CollectionUtils.isEmpty(openStackHosts)) {
                config.setHost(openStackHosts.get(0));
            }else {
                config.setHost(fusionSphereHosts.get(0));
            }
            MepHost host = config.getHost();
            MepHostLog mepHostLog = new MepHostLog();
            mepHostLog.setAppInstancesId(vmPackageConfig.getAppInstanceId());
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            mepHostLog.setDeployTime(time.format(new Date()));
            mepHostLog.setHostId(host.getHostId());
            mepHostLog.setHostIp(host.getMecHost());
            mepHostLog.setLogId(UUID.randomUUID().toString());
            mepHostLog.setUserId(project.getUserId());
            mepHostLog.setProjectId(project.getId());
            mepHostLog.setProjectName(project.getName());
            mepHostLog.setStatus(host.getStatus());
            hostLogMapper.insert(mepHostLog);
            processSuccess = true;
            hostStatus = EnumTestConfigStatus.Success;
            config.setLog("select host success");
        }
        vmService.updateCreateVmResult(config, project, "hostInfo", hostStatus);
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
