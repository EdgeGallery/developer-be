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

import java.util.Map;
import java.util.UUID;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InputParameterUtil;
import org.edgegallery.developer.util.IpCalculateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class InstantiateAppAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributePackageAction.class);

    public static final String ACTION_NAME = "Instantiate Application";

    private IContext context;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MepHostService mepHostService;

    @Autowired
    private HostLogMapper hostLogMapper;

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
        String appInstanceId = instantiateApplication(application.getUserId(), mepmPkgId, mepHost, lcmLog);
        if(null == appInstanceId){
            String msg = "Instantiate application failed. The log from lcm is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
        }
        String msg = "Instantiate application request sent to lcm controller success. application InstanceId is: " + appInstanceId;
        updateActionProgress(actionStatus, 100, msg);

        //update
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
        if(!instantRes){
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
}
