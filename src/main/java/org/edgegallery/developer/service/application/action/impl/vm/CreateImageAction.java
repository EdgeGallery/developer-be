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

import java.lang.reflect.Type;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.vm.VmImageInfo;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.EnumExportImageStatus;
import org.edgegallery.developer.service.application.common.EnumInstantiateStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class CreateImageAction extends AbstractAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateImageAction.class);

    private static Gson gson = new Gson();

    public static final String ACTION_NAME = "Create Image";

    // time out: 10 min.
    public static final int TIMEOUT = 10 * 60 * 1000;
    //interval of the query, 5s.
    public static final int INTERVAL = 5000;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MepHostService mepHostService;

    @Autowired
    ImageExportInfoMapper imageExportInfoMapper;

    private IContext context;

    public IContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(IContext context) {
        this.context = context;
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
        String statusLog = "Start to create vm image for package Id：" + packageId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.VM_IMAGE_INSTANCE, packageId,
            ACTION_NAME, statusLog);
        String mepHostId = application.getMepHostId();
        if (null == mepHostId || "".equals(mepHostId)) {
            actionStatus.setStatus(EnumActionStatus.FAILED);
            updateActionError(actionStatus, "Sandbox not selected. Failed to instantiate package");
            return false;
        }
        MepHost mepHost = mepHostService.getHost(mepHostId);

        //create image.
        LcmLog lcmLog = new LcmLog();
        String imageId = createImageByLcm(mepHost, mepmPkgId, applicationId, lcmLog);
        if (null == imageId) {
            actionStatus.setStatus(EnumActionStatus.FAILED);
            String msg = "create vm  image  failed. The log from lcm is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
        }
        String msg = "create vm  image request sent to lcm controller success. imageId is: "
            + imageId;
        updateActionProgress(actionStatus, 30, msg);
        //Save  imageId to ImageExportInfo.
        Boolean updateRes = saveImageIdToImageExportInfo(imageId);
        if (!updateRes) {
            actionStatus.setStatus(EnumActionStatus.FAILED);
            updateActionError(actionStatus, "Update ImageId To image export info failed.");
            return false;
        }
        getContext().addParameter(IContextParameter.PARAM_IMAGE_INSTANCE_ID, imageId);
        // get vm export image status
        EnumExportImageStatus exportImageStatus = queryImageStatus(mepHost, applicationId, imageId, lcmLog);
        if (!EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS.equals(exportImageStatus)) {
            actionStatus.setStatus(EnumActionStatus.FAILED);
            String imageErrorLog = "Query export image status failed, the result is: " + exportImageStatus;
            updateActionError(actionStatus, imageErrorLog);
            return false;
        }
        actionStatus.setStatus(EnumActionStatus.SUCCESS);
        updateActionProgress(actionStatus, 100, "Query export image status success.");
        LOGGER.info("Distribute package action finished.");

        return true;
    }
    

    private Boolean saveImageIdToImageExportInfo(String imageId) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = new ImageExportInfo();
        imageExportInfo.setImageInstanceId(imageId);
        int res = imageExportInfoMapper.createImageExportInfoInfo(vmId, imageExportInfo);
        if (res < 1) {
            LOGGER.warn("create image export info baseDate fail");
            return false;
        }
        return true;
    }

    private String createImageByLcm(MepHost mepHost, String mepmPkgId, String appInstanceId, LcmLog lcmLog) {
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
        String imageResult = HttpClientUtil.vmInstantiateImage(basePath, getContext().getUserId(), getContext().getToken(), mepmPkgId, appInstanceId, lcmLog);
        LOGGER.info("import image result: {}", imageResult);
        if (StringUtils.isEmpty(imageResult)) {
            return null;
        }
        JsonObject jsonObject = new JsonParser().parse(imageResult).getAsJsonObject();
        JsonElement imageId = jsonObject.get("imageId");
        return imageId.getAsString();
    }

    private EnumExportImageStatus queryImageStatus(MepHost mepHost, String appInstanceId, String imageId, LcmLog lcmLog) {
        int waitingTime = 0;
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
        while (waitingTime < TIMEOUT) {
            String workStatus = HttpClientUtil.getImageStatus(basePath, appInstanceId, getContext().getUserId(), 
                imageId, getContext().getToken());
            LOGGER.info("export image result: {}", workStatus);
            if (workStatus == null) {
                // compare time between now and deployDate
                return EnumExportImageStatus.EXPORT_IMAGE_STATUS_ERROR;
            }

            Type vmInfoType = new TypeToken<VmImageInfo>() { }.getType();
            VmImageInfo vmImageInfo = gson.fromJson(workStatus, vmInfoType);
            if (vmImageInfo.getStatus().equals(EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS.toString())) {
                getContext().addParameter(IContextParameter.PARAM_IMAGE_INSTANCE_ID, imageId);
                return EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS;
            }
            if (vmImageInfo.getStatus().equals(EnumExportImageStatus.EXPORT_IMAGE_STATUS_FAILED.toString())) {
                return EnumExportImageStatus.EXPORT_IMAGE_STATUS_FAILED;
            }
            try {
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            }catch (InterruptedException e) {
                LOGGER.error("export image sleep failed.");
                return EnumExportImageStatus.EXPORT_IMAGE_STATUS_ERROR;
            }
            
        }
        return EnumExportImageStatus.EXPORT_IMAGE_STATUS_TIMEOUT;
    }

}
