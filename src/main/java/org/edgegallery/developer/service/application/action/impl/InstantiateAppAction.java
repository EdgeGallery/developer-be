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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.developer.mapper.HostLogMapper;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.common.EnumInstantiateStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class InstantiateAppAction extends AbstractAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstantiateAppAction.class);

    public static final String ACTION_NAME = "Instantiate Application";

    private IContext context;

    // time out: 10 min.
    public static final int TIMEOUT = 10 * 60 * 1000;

    //interval of the query, 5s.
    public static final int INTERVAL = 5000;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MepHostService mepHostService;

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
            updateActionError(actionStatus, "Sandbox not selected. Failed to instantiate package");
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

        //Save app instanceId to instantiate info.
        Boolean updateRes = saveInstanceIdToInstantiateInfo(appInstanceId);
        if (!updateRes) {
            updateActionError(actionStatus, "Update instantiate info for app instance id failed.");
            return false;
        }

        //Query instantiate status.
        EnumInstantiateStatus status = queryInstantiateStatus(appInstanceId, mepHost);
        if (!EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS.equals(status)) {
            msg = "Query instantiate status failed, the result is: " + status;
            updateActionError(actionStatus, msg);
            return false;
        }
        updateActionProgress(actionStatus, 100, "Query instantiate status success.");
        return true;
    }

    public boolean saveInstanceIdToInstantiateInfo(String appInstanceId) {
        return true;
    }

    public String instantiateApplication(String userId, String mepmPackageId, MepHost mepHost, LcmLog lcmLog) {
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        // instantiate application
        Map<String, String> inputParams;
        try {
            inputParams = getInputParams(mepHost);
        } catch (Exception e) {
            LOGGER.error("Get input params error");
            return null;
        }
        String appInstanceId = UUID.randomUUID().toString();
        boolean instantRes = HttpClientUtil.instantiateApplication(basePath, appInstanceId, userId,
            getContext().getToken(), lcmLog, mepmPackageId, mepHost.getMecHostIp(), inputParams);
        LOGGER.info("Instantiate application result: {}", instantRes);
        if (!instantRes) {
            return null;
        }
        return appInstanceId;
    }

    public Map<String, String> getInputParams(MepHost mepHost) {
        return new HashMap<String, String>();
    }

    public EnumInstantiateStatus queryInstantiateStatus(String appInstanceId, MepHost mepHost) {
        return EnumInstantiateStatus.INSTANTIATE_STATUS_SUCCESS;
    }
}
