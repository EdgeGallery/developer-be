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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.edgegallery.developer.mapper.ProjectMapper;
import org.edgegallery.developer.model.vm.NetworkInfo;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmInstantiateInfo;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.service.virtual.VmService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vm_workStatus_service")
public class VmStageWorkStatus implements VmCreateStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(VmStageWorkStatus.class);

    private static Gson gson = new Gson();

    /**
     * the max time for wait workStatus.
     */
    private static final Long MAX_SECONDS = 360L;

    @Autowired
    private VmService vmService;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public boolean execute(VmCreateConfig config) throws InterruptedException {
        boolean processStatus = false;
        EnumTestConfigStatus status = EnumTestConfigStatus.Failed;
        Type type = new TypeToken<MepHost>() { }.getType();
        MepHost host = gson.fromJson(gson.toJson(config.getHost()), type);
        ApplicationProject project = projectMapper.getProjectById(config.getProjectId());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), config.getAppInstanceId(),
                project.getUserId(), config.getLcmToken());
        LOGGER.info("get instantiate status: {}", workStatus);
        if (workStatus == null) {
            // compare time between now and deployDate
            long time = System.currentTimeMillis() - config.getCreateTime().getTime();
            LOGGER.info("over time:{}, wait max time:{}, start time:{}", time, MAX_SECONDS,
                config.getCreateTime().getTime());
            if (config.getCreateTime() == null || time > MAX_SECONDS * 1000) {
                config.setLog("Failed to get create vm result ");
                String message = "Failed to get create vm result after wait {} seconds which appInstanceId is : {}";
                LOGGER.error(message, MAX_SECONDS, config.getAppInstanceId());
            } else {
                return true;
            }
        } else {
            JsonObject jsonObject = new JsonParser().parse(workStatus).getAsJsonObject();
            JsonElement code = jsonObject.get("code");
            JsonElement msg = jsonObject.get("msg");
            if (!code.getAsString().equals("200")) {
                if (msg != null) {
                    config.setLog(msg.getAsString());
                }
                config.setLog("get vm status fail");
            } else {
                Type vmInfoType = new TypeToken<VmInstantiateInfo>() { }.getType();
                VmInstantiateInfo vmInstantiateInfo = gson.fromJson(workStatus, vmInfoType);
                List<NetworkInfo> networkInfo = vmInstantiateInfo.getData().get(0).getNetworks();
                if (networkInfo == null || networkInfo.isEmpty()) {
                    config.setLog("lack of resources");
                } else {
                    processStatus = true;
                    status = EnumTestConfigStatus.Success;
                    config.setLog("get vm status success");
                }
                config.setVmInfo(vmInstantiateInfo.getData());

            }

        }
        // update test-config
        vmService.updateCreateVmResult(config, project, "workStatus", status);
        LOGGER.info("update config result:{}", config.getStatus());
        return processStatus;
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
