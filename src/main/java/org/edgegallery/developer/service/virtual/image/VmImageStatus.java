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

package org.edgegallery.developer.service.virtual.image;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import javax.annotation.Resource;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.mapper.VmConfigMapper;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmImageInfo;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.service.virtual.VmService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_imageStatus_service")
public class VmImageStatus implements VmImageStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmImageStatus.class);

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 120L;

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private VmConfigMapper vmConfigMapper;

    @Resource(name = "vm_downloadImageInfo_service")
    private VmImageStage vmImageStage;

    @Override
    public boolean execute(VmImageConfig config) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;
        VmCreateConfig vmCreateConfig = vmConfigMapper.getVmCreateConfig(config.getProjectId(), config.getVmId());
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(vmCreateConfig.getHost()), type);
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        String basePath = HttpClientUtil.getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort());
        String workStatus = HttpClientUtil
            .getImageStatus(basePath, config.getAppInstanceId(), project.getUserId(), config.getImageId(),
                config.getLcmToken());
        LOGGER.info("import image result: {}", workStatus);
        if (workStatus == null) {
            // compare time between now and deployDate
            long time = System.currentTimeMillis() - config.getCreateTime().getTime();
            LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS,
                config.getCreateTime().getTime());
            if (config.getCreateTime() == null || time > MAX_SECONDS * 2000) {
                config.setLog("Failed to get vm image result ");
                String message = "Failed to get vm image result after wait {} seconds which appInstanceId is : {}";
                LOGGER.error(message, MAX_SECONDS, config.getAppInstanceId());
            } else {
                return true;
            }
        } else {
            Type vmInfoType = new TypeToken<VmImageInfo>() { }.getType();
            VmImageInfo vmImageInfo = gson.fromJson(workStatus, vmInfoType);
            if (vmImageInfo.getStatus().equals("active")) {
                processStatus = true;
                status = EnumTestConfigStatus.Success;
                config.setLog("get vm status success");
                config.setImageName(vmImageInfo.getImageName());
                config.setSumChunkNum(vmImageInfo.getSumChunkNum());
                config.setChunkSize(vmImageInfo.getChunkSize());
                config.setChecksum(vmImageInfo.getChecksum());
                // get config
                LOGGER.info("update config result:{}", config.getStatus());
            } else {
                return true;
            }

        }
        vmService.updateVmImageResult(config, project, "imageStatus", status);
        return processStatus == true ? vmImageStage.execute(config) : processStatus;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public boolean immediateExecute(VmImageConfig config) {
        return true;
    }

}
