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

package org.edgegallery.developer.service.application.action.impl.vm;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.instantiate.vm.EnumVMInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.vm.NetworkInfo;
import org.edgegallery.developer.model.vm.VmInstantiateWorkload;
import org.edgegallery.developer.service.application.action.impl.InstantiateAppAction;
import org.edgegallery.developer.service.application.common.EnumInstantiateStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.IpCalculateUtil;
import org.edgegallery.developer.util.SpringContextUtil;

public class InstantiateVMAppAction extends InstantiateAppAction {

    private static Gson gson = new Gson();

    VMAppOperationService vmAppOperationService = (VMAppOperationService) SpringContextUtil.getBean(VMAppOperationService.class);

    OperationStatusMapper operationStatusMapper = (OperationStatusMapper) SpringContextUtil.getBean(
        OperationStatusMapper.class);

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId, EnumVMInstantiateStatus status) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        instantiateInfo.setStatus(status);
        return vmAppOperationService.updateInstantiateInfo(vmId, instantiateInfo);
    }

    public Map<String, String> getInputParams(MepHost mepHost) {
        String parameter = mepHost.getNetworkParameter();
        int count = operationStatusMapper.getOperationCountByObjectType(EnumOperationObjectType.APPLICATION_INSTANCE.toString());
        Map<String, String> vmInputParams = InputParameterUtil.getParams(parameter);
        String n6Range = vmInputParams.get("app_n6_ip");
        String mepRange = vmInputParams.get("app_mp1_ip");
        String internetRange = vmInputParams.get("app_internet_ip");
        vmInputParams.put("app_n6_ip", IpCalculateUtil.getStartIp(n6Range, count));
        vmInputParams.put("app_mp1_ip", IpCalculateUtil.getStartIp(mepRange, count));
        vmInputParams.put("app_internet_ip", IpCalculateUtil.getStartIp(internetRange, count));
        if (vmInputParams.getOrDefault("app_n6_gw", null) == null) {
            vmInputParams.put("app_n6_gw", IpCalculateUtil.getStartIp(n6Range, 0));
        }
        if (vmInputParams.getOrDefault("app_mp1_gw", null) == null) {
            vmInputParams.put("app_mp1_gw", IpCalculateUtil.getStartIp(mepRange, 0));
        }
        if (vmInputParams.getOrDefault("app_internet_gw", null) == null) {
            vmInputParams.put("app_internet_gw", IpCalculateUtil.getStartIp(internetRange, 0));
        }
        return vmInputParams;
    }

    public boolean saveWorkloadToInstantiateInfo(String respBody) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VMInstantiateInfo vmInstantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        Type vmInfoType = new TypeToken<VmInstantiateWorkload>() { }.getType();
        VmInstantiateWorkload vmInstantiateWorkload = gson.fromJson(respBody, vmInfoType);
        vmInstantiateInfo.setLog(respBody);
        if (vmInstantiateWorkload.getData().size() > 0) {
            vmInstantiateInfo.setVncUrl(vmInstantiateWorkload.getData().get(0).getVncUrl());
            vmInstantiateInfo.setVmInstanceId(vmInstantiateWorkload.getData().get(0).getVmId());
            for (NetworkInfo info : vmInstantiateWorkload.getData().get(0).getNetworks()) {
                PortInstantiateInfo port = new PortInstantiateInfo();
                port.setIpAddress(info.getIp());
                port.setNetworkName(info.getName());
                vmInstantiateInfo.getPortInstanceList().add(port);
            }
        }
        return vmAppOperationService.updateInstantiateInfo(vmId, vmInstantiateInfo);
    }

    public EnumInstantiateStatus queryInstantiateStatus(String appInstanceId, MepHost mepHost) {
        int waitingTime = 0;
        while (waitingTime < TIMEOUT) {
            String workStatus = HttpClientUtil.getWorkloadStatus(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
                mepHost.getLcmPort(), appInstanceId, getContext().getUserId(), getContext().getToken());
            LOGGER.info("get instantiate status: {}", workStatus);
            if (workStatus == null) {
                // compare time between now and deployDate
                return EnumInstantiateStatus.INSTANTIATE_STATUS_ERROR;
            }
            JsonObject jsonObject = new JsonParser().parse(workStatus).getAsJsonObject();
            JsonElement code = jsonObject.get("code");
            if (code.getAsString().equals("200")) {
                LOGGER.info("Query instantiate result, lcm return success. workload: ", workStatus);
                saveWorkloadToInstantiateInfo(workStatus);
                return EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS;
            }
            try {
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.error("Distribute package sleep failed.");
                return EnumInstantiateStatus.INSTANTIATE_STATUS_ERROR;
            }
        }
        return EnumInstantiateStatus.INSTANTIATE_STATUS_TIMEOUT;
    }
}