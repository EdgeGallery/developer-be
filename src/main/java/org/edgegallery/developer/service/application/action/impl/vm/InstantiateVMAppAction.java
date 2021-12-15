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

import static org.edgegallery.developer.util.HttpClientUtil.getUrlPrefix;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.application.vm.EnumVMStatus;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.lcm.NetworkInfo;
import org.edgegallery.developer.model.lcm.VmInstantiateWorkload;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.action.impl.InstantiateAppAction;
import org.edgegallery.developer.service.application.common.EnumInstantiateStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.IpCalculateUtil;
import org.edgegallery.developer.util.SpringContextUtil;

public class InstantiateVMAppAction extends InstantiateAppAction {

    private static Gson gson = new Gson();

    VMAppOperationService vmAppOperationService = (VMAppOperationService) SpringContextUtil.getBean(VMAppOperationService.class);

    OperationStatusService operationStatusService = (OperationStatusService) SpringContextUtil.getBean(
        OperationStatusService.class);

    VMAppVmService vmAppVmService = (VMAppVmService) SpringContextUtil.getBean(VMAppVmService.class);

    private static final String INSTANTIATE_SUCCESS = "Instantiated";

    private static final String INSTANTIATE_FAILURE = "Failure";

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId, EnumAppInstantiateStatus status) {
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(vmId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        instantiateInfo.setStatus(status);
        if (status.equals(EnumAppInstantiateStatus.SUCCESS)) {
            VirtualMachine vm = vmAppVmService.getVm(applicationId, vmId);
            vm.setStatus(EnumVMStatus.DEPLOYED);
            vmAppVmService.updateVmStatus(vmId, EnumVMStatus.DEPLOYED, null);
        }
        return vmAppOperationService.updateInstantiateInfo(vmId, instantiateInfo);
    }

    public Map<String, String> getInputParams(MepHost mepHost) {
        String parameter = mepHost.getNetworkParameter();
        int count = operationStatusService.getOperationCountByObjectType(EnumOperationObjectType.APPLICATION_INSTANCE.toString());
        Map<String, String> vmInputParams = InputParameterUtil.getParams(parameter);
        String n6Range = vmInputParams.get("VDU1_APP_Plane03_IP");
        String mepRange = vmInputParams.get("VDU1_APP_Plane01_IP");
        String internetRange = vmInputParams.get("VDU1_APP_Plane02_IP");
        vmInputParams.put("VDU1_APP_Plane03_IP", IpCalculateUtil.getStartIp(n6Range, count));
        vmInputParams.put("VDU1_APP_Plane01_IP", IpCalculateUtil.getStartIp(mepRange, count));
        vmInputParams.put("VDU1_APP_Plane02_IP", IpCalculateUtil.getStartIp(internetRange, count));
        if (vmInputParams.getOrDefault("VDU1_APP_Plane03_GW", null) == null) {
            vmInputParams.put("VDU1_APP_Plane03_GW", IpCalculateUtil.getStartIp(n6Range, 0));
        }
        if (vmInputParams.getOrDefault("VDU1_APP_Plane01_GW", null) == null) {
            vmInputParams.put("VDU1_APP_Plane01_GW", IpCalculateUtil.getStartIp(mepRange, 0));
        }
        if (vmInputParams.getOrDefault("VDU1_APP_Plane02_GW", null) == null) {
            vmInputParams.put("VDU1_APP_Plane02_GW", IpCalculateUtil.getStartIp(internetRange, 0));
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
            String basePath = getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
            String workStatus = HttpClientUtil.getWorkloadStatus(basePath, appInstanceId, getContext().getUserId(), getContext().getToken());
            LOGGER.info("get instantiate status: {}", workStatus);
            if (workStatus == null) {
                // compare time between now and deployDate
                return EnumInstantiateStatus.INSTANTIATE_STATUS_ERROR;
            }
            JsonObject jsonObject = new JsonParser().parse(workStatus).getAsJsonObject();
            if (INSTANTIATE_SUCCESS.equals(jsonObject.get("status").getAsString())) {
                LOGGER.info("Query instantiate result, lcm return success. workload:{} ", workStatus);
                saveWorkloadToInstantiateInfo(workStatus);
                return EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS;
            }
            if (INSTANTIATE_FAILURE.equals(jsonObject.get("status").getAsString())) {
                LOGGER.error("Query instantiate failed:{}", jsonObject.get("msg").getAsString());
                return EnumInstantiateStatus.INSTANTIATE_STATUS_FAILED;
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
