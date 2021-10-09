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

package org.edgegallery.developer.service.application.action.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.instantiate.vm.EnumVMInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.PortInstantiateInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.vm.NetworkInfo;
import org.edgegallery.developer.model.vm.VmInstantiateWorkload;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.common.EnumDistributeStatus;
import org.edgegallery.developer.service.application.common.EnumInstantiateStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.IpCalculateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class InstantiateAppAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributePackageAction.class);

    public static final String ACTION_NAME = "Instantiate Application";

    private IContext context;

    private static Gson gson = new Gson();

    // time out: 10 min.
    private static final int TIMEOUT = 10 * 60 * 1000;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MepHostService mepHostService;

    @Autowired
    private HostLogMapper hostLogMapper;

    @Autowired
    private VMAppOperationServiceImpl VmAppOperationService;

    @Override
    public void setContext(IContext context) {
        this.context = context;
    }

    public IContext getContext() {
        return this.context;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        //Start action , save action status.
        String packageId = (String) getContext().getParameter(IContextParameter.PARAM_PACKAGE_ID);
        String mepmPkgId = (String) getContext().getParameter(IContextParameter.PARAM_MEPM_PACKAGE_ID);
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        Application application = applicationService.getApplication(applicationId);
        String statusLog = "Start to instantiate the app for package Id：" + packageId + ", mepm package id:"
            + mepmPkgId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.APPLICATION_PACKAGE, packageId,
            ACTION_NAME, statusLog);
        String mepHostId = application.getMepHostId();
        if (null == mepHostId || "".equals(mepHostId)) {
            updateActionError(actionStatus, "Sandbox not selected. Failed to distribute package");
            return false;
        }
        MepHost mepHost = mepHostService.getHost(mepHostId);

        //Instantiate application.
        LcmLog lcmLog = new LcmLog();
        String appInstanceId = instantiateApplication(getContext().getUserId(), mepmPkgId, mepHost, lcmLog);
        if (null == appInstanceId) {
            String msg = "Instantiate application failed. The log from lcm is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
        }
        String msg = "Instantiate application request sent to lcm controller success. application InstanceId is: "
            + appInstanceId;
        updateActionProgress(actionStatus, 30, msg);

        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        VMInstantiateInfo instantiateInfo = VmAppOperationService.getInstantiateInfo(vmId);
        instantiateInfo.setAppInstanceId(appInstanceId);
        Boolean updateRes = VmAppOperationService.updateInstantiateInfo(vmId, instantiateInfo);
        if (!updateRes) {
            updateActionError(actionStatus, "Update instantiate info for VM failed.");
            return false;
        }

        //Query instantiate status.
        EnumInstantiateStatus status = queryInstantiateStatus(appInstanceId, mepHost, instantiateInfo);
        if (!EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS.equals(status)) {
            msg = "Query instantiate status failed, the result is: " + status;
            updateActionError(actionStatus, msg);
            return false;
        }
        updateActionProgress(actionStatus, 100, "Query instantiate status success.");
        return true;
    }

    public String instantiateApplication(String userId, String mepmPackageId, MepHost mepHost, LcmLog lcmLog) {
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        // instantiate application
        Map<String, String> vmInputParams;
        try {
            vmInputParams = getInputParams(mepHost.getNetworkParameter(), mepHost.getMecHostIp());
        } catch (Exception e) {
            LOGGER.error("Get input params error");
            return null;
        }
        String appInstanceId = UUID.randomUUID().toString();
        boolean instantRes = HttpClientUtil.instantiateApplication(basePath, appInstanceId, userId,
            getContext().getToken(), lcmLog, mepmPackageId, mepHost.getMecHostIp(), vmInputParams);
        LOGGER.info("Instantiate application result: {}", instantRes);
        if (!instantRes) {
            return null;
        }
        return appInstanceId;
    }

    private Map<String, String> getInputParams(String parameter, String mecHost) {
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

    private EnumInstantiateStatus queryInstantiateStatus(String appInstanceId, MepHost mepHost,
        VMInstantiateInfo vmInstantiateInfo) {
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
                LOGGER.error("Query instantiate result, lcm return success.", workStatus);
                Type vmInfoType = new TypeToken<VmInstantiateWorkload>() { }.getType();
                VmInstantiateWorkload vmInstantiateWorkload = gson.fromJson(workStatus, vmInfoType);
                vmInstantiateInfo.setLog(workStatus);
                if (vmInstantiateWorkload.getData().size() > 0) {
                    vmInstantiateInfo.setVncUrl(vmInstantiateWorkload.getData().get(0).getVncUrl());
                    vmInstantiateInfo.setVmInstanceId(vmInstantiateWorkload.getData().get(0).getVmId());
                    for(NetworkInfo info:vmInstantiateWorkload.getData().get(0).getNetworks()){
                        PortInstantiateInfo port = new PortInstantiateInfo();
                        port.setIpAddress(info.getIp());
                        port.setNetworkName(info.getName());
                        vmInstantiateInfo.getPortInstanceList().add(port);
                    }
                }
                return EnumInstantiateStatus.INSTANTIATE_STATUS_FAILED;
            }
            try {
                Thread.sleep(5000);
                waitingTime += 5000;
            } catch (InterruptedException e) {
                LOGGER.error("Distribute package sleep failed.");
                return EnumInstantiateStatus.INSTANTIATE_STATUS_ERROR;
            }
        }
        return EnumInstantiateStatus.INSTANTIATE_STATUS_TIMEOUT;
    }
}
