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
import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.instantiate.vm.EnumVMInstantiateStatus;
import org.edgegallery.developer.model.lcm.DistributeResponse;
import org.edgegallery.developer.model.lcm.MecHostInfo;
import org.edgegallery.developer.model.lcm.UploadResponse;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.common.EnumDistributeStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.util.ApplicationUtil;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class DistributePackageAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributePackageAction.class);

    public static final String ACTION_NAME = "Distribute Application Package";

    private static Gson gson = new Gson();

    // time out: 1 hour.
    private static final int TIMEOUT = 60 * 60 * 1000;

    //interval of the query, 5s.
    private static final int INTERVAL = 5000;

    ApplicationService applicationService = (ApplicationService) SpringContextUtil.getBean(ApplicationService.class);

    MepHostService mepHostService = (MepHostService) SpringContextUtil.getBean(MepHostService.class);

    AppPackageService appPackageService = (AppPackageService) SpringContextUtil.getBean(AppPackageService.class);

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        // Start action , save action status.
        String packageId = (String) getContext().getParameter(IContextParameter.PARAM_PACKAGE_ID);
        String statusLog = "Start to distribute the app package for package Id：" + packageId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.APPLICATION_PACKAGE, packageId,
            ACTION_NAME, statusLog);

        // get Sandbox info.
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        Application application = applicationService.getApplication(applicationId);
        String mepHostId = application.getMepHostId();
        if (StringUtils.isEmpty(mepHostId)) {
            updateActionError(actionStatus, "Sandbox not selected. Failed to distribute package");
            saveDistributeInstantiateInfo("", "", EnumVMInstantiateStatus.PACKAGE_DISTRIBUTE_FAILED);
            return false;
        }
        MepHost mepHost = mepHostService.getHost(mepHostId);
        LOGGER.info("Distribute package destination: {}", mepHost.getMecHostIp());
        // Upload package file to lcm.
        AppPackage appPkg = appPackageService.getAppPackage(packageId);
        String mepmPkgId = uploadPackageToLcm(getContext().getUserId(), appPkg.queryPkgPath(), mepHost);
        if (null == mepmPkgId) {
            updateActionError(actionStatus, "Upload app package file to lcm failed.");
            saveDistributeInstantiateInfo("", "", EnumVMInstantiateStatus.PACKAGE_DISTRIBUTE_FAILED);
            return false;
        }
        saveDistributeInstantiateInfo("", mepmPkgId, EnumVMInstantiateStatus.PACKAGE_DISTRIBUTE_SUCCESS);
        updateActionProgress(actionStatus, 25, "Upload app package to lcm success.");

        //Distribute package to edge host.
        boolean res = distributePackageToEdgeHost(getContext().getUserId(), mepmPkgId, mepHost);
        if (!res) {
            saveDistributeInstantiateInfo(mepHost.getMecHostIp(), mepmPkgId,
                EnumVMInstantiateStatus.PACKAGE_DISTRIBUTE_FAILED);
            updateActionError(actionStatus, "Distribute app package file to edge host failed.");
            return false;
        }
        updateActionProgress(actionStatus, 50, "Distribute app package to edge host success.");
        //save vm instantiate info.
        boolean updateRes = saveDistributeInstantiateInfo(mepHost.getMecHostIp(), mepmPkgId,
            EnumVMInstantiateStatus.PACKAGE_DISTRIBUTE_SUCCESS);
        if (!updateRes) {
            updateActionError(actionStatus, "Update instantiate info for VM failed.");
            return false;
        }
        //Query Distribute Status
        EnumDistributeStatus distributeStatus = getDistributeStatus(getContext().getUserId(), mepmPkgId, mepHost);
        if (!EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_SUCCESS.equals(distributeStatus)) {
            String msg = "Query Distribute package status failed, the result is: " + distributeStatus;
            updateActionError(actionStatus, msg);
            return false;
        }

        getContext().addParameter(IContextParameter.PARAM_MEPM_PACKAGE_ID, mepmPkgId);
        updateActionProgress(actionStatus, 100, "Query distribute app package status success.");
        LOGGER.info("Distribute package action finished.");
        return true;
    }

    public boolean saveDistributeInstantiateInfo(String mecHostIp, String mepmPkgId, EnumVMInstantiateStatus status) {
        return true;
    }

    private String uploadPackageToLcm(String userId, String packagePath, MepHost mepHost) {
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        String uploadRes = HttpClientUtil.uploadPkg(basePath, packagePath, userId, getContext().getToken(),
            new LcmLog());
        LOGGER.info("Upload package result: {}", uploadRes);
        if (StringUtils.isEmpty(uploadRes)) {
            LOGGER.error("Upload package to lcm failed, package:{}  result: {}", packagePath, uploadRes);
            return null;
        }
        UploadResponse uploadResponse = gson.fromJson(uploadRes, UploadResponse.class);
        return uploadResponse.getPackageId();
    }

    private boolean distributePackageToEdgeHost(String userId, String packageId, MepHost mepHost) {
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        String distributeRes = HttpClientUtil.distributePkg(basePath, userId, getContext().getToken(), packageId,
            mepHost.getMecHostIp(), new LcmLog());
        LOGGER.info("Distribute package result: {}", distributeRes);
        if (distributeRes == null) {
            LOGGER.error("Distribute package failed. packageId： {}", packageId);
            return false;
        }
        return true;
    }

    private EnumDistributeStatus getDistributeStatus(String userId, String packageId, MepHost mepHost) {
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(),
            mepHost.getLcmPort());
        int waitingTime = 0;
        while (waitingTime < TIMEOUT) {
            String distributeResult = HttpClientUtil.getDistributeRes(basePath, userId, getContext().getToken(),
                packageId);
            LOGGER.info("Distribute package result: {}", distributeResult);
            if (distributeResult == null) {
                LOGGER.error("Get distribute package result failed");
                return EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_ERROR;
            }
            List<DistributeResponse> list = gson.fromJson(distributeResult,
                new TypeToken<List<DistributeResponse>>() { }.getType());
            List<MecHostInfo> mecHostInfo = list.get(0).getMecHostInfo();
            if (mecHostInfo == null) {
                LOGGER.error("Get distribute package status failed, null mec host info.");
                return EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_ERROR;
            }
            String status = mecHostInfo.get(0).getStatus();
            if (EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_FAILED.toString().equals(status)
                || EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_ERROR.toString().equals(status)) {
                LOGGER.error("Failed to upload vm image packageId is : {}.", packageId);
                return EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_FAILED;
            } else if (EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_SUCCESS.toString().equals(status)) {
                LOGGER.info("Distribute package result: {}", distributeResult);
                return EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_SUCCESS;
            }
            try {
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            } catch (InterruptedException e) {
                LOGGER.error("Distribute package sleep failed.");
                return EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_ERROR;
            }
        }
        return EnumDistributeStatus.DISTRIBUTE_PACKAGE_STATUS_TIMEOUT;
    }
}
