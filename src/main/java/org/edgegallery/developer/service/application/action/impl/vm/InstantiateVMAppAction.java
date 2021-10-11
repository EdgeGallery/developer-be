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
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.vm.NetworkInfo;
import org.edgegallery.developer.model.vm.VmInstantiateWorkload;
import org.edgegallery.developer.service.application.action.impl.InstantiateAppAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.IpCalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class InstantiateVMAppAction extends InstantiateAppAction {

    private static Gson gson = new Gson();

    @Autowired
    private VMAppOperationServiceImpl VmAppOperationService;

    @Autowired
    private HostLogMapper hostLogMapper;

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VMInstantiateInfo instantiateInfo = VmAppOperationService.getInstantiateInfo(vmId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        return VmAppOperationService.updateInstantiateInfo(vmId, instantiateInfo);
    }

    public Map<String, String> getInputParams(MepHost mepHost) {
        String parameter = mepHost.getNetworkParameter();
        String mecHost = mepHost.getMecHostIp();
        int count = hostLogMapper.getHostLogCount(mecHost);
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
        VMInstantiateInfo vmInstantiateInfo = VmAppOperationService.getInstantiateInfo(vmId);
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
        return VmAppOperationService.updateInstantiateInfo(vmId, vmInstantiateInfo);
    }
}
