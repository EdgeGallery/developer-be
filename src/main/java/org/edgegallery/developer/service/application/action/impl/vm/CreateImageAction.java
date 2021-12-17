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
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.mapper.application.vm.ImageExportInfoMapper;
import org.edgegallery.developer.model.lcm.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.instantiate.vm.EnumImageExportStatus;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.lcm.MepmVmImageInfo;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.EnumExportImageStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final int TIMEOUT = 60 * 60 * 1000;
    //interval of the query, 5s.
    public static final int INTERVAL = 20000;

    ApplicationService applicationService = (ApplicationService) SpringContextUtil.getBean(ApplicationService.class);

    MepHostService mepHostService = (MepHostService) SpringContextUtil.getBean(MepHostService.class);

    VMAppOperationServiceImpl vmAppOperationService = (VMAppOperationServiceImpl) SpringContextUtil
        .getBean(VMAppOperationServiceImpl.class);

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        //Start action , save action status.
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        Application application = applicationService.getApplication(applicationId);
        String statusLog = "Start to create vm image for vm Id：" + vmId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.VM_IMAGE_INSTANCE, vmId,
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
        String imageId = sentCreateImageRequestToLcm(mepHost, lcmLog);
        if (null == imageId) {
            String msg = "create vm  image  failed. The log from lcm is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
            return false;
        }
        String msg = "create vm  image request sent to lcm controller success. imageId is: "
            + imageId;
        updateActionProgress(actionStatus, 30, msg);
        //Save  imageId to ImageExportInfo.
        Boolean updateRes = saveImageIdToImageExportInfo(imageId);
        if (!updateRes) {
            updateActionError(actionStatus, "Update ImageId To image export info failed.");
            return false;
        }
        getContext().addParameter(IContextParameter.PARAM_IMAGE_INSTANCE_ID, imageId);
        // get vm export image status
        EnumExportImageStatus exportImageStatus = queryImageStatus(mepHost, imageId, lcmLog);
        if (!EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS.equals(exportImageStatus)) {
            String imageErrorLog = "Query export image status failed, the result is: " + exportImageStatus;
            updateActionError(actionStatus, imageErrorLog);
            modifyImageExportInfo(EnumImageExportStatus.FAILED, imageErrorLog);
            sendDeleteVmImageToLcm(mepHost);
            return false;
        }
        actionStatus.setStatus(EnumActionStatus.SUCCESS);
        updateActionProgress(actionStatus, 100, "Query export image status success.");
        modifyImageExportInfo(EnumImageExportStatus.IMAGE_QUERYING, "Query export image status success.");

        return true;
    }


    private Boolean saveImageIdToImageExportInfo(String imageId) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = vmAppOperationService.getImageExportInfo(vmId);
        imageExportInfo.setImageInstanceId(imageId);
        imageExportInfo.setStatus(EnumImageExportStatus.IMAGE_CREATING);
        vmAppOperationService.modifyExportInfo(vmId, imageExportInfo);
        return true;
    }

    private Boolean modifyImageExportInfo(EnumImageExportStatus status, String log) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = vmAppOperationService.getImageExportInfo(vmId);
        imageExportInfo.setStatus(status);
        imageExportInfo.setLog(log);
        vmAppOperationService.modifyExportInfo(vmId, imageExportInfo);
        return true;
    }

    private String sentCreateImageRequestToLcm(MepHost mepHost, LcmLog lcmLog) {
        String vmInstanceId = (String) getContext().getParameter(IContextParameter.PARAM_VM_INSTANCE_ID);
        String appInstanceId = (String) getContext().getParameter(IContextParameter.PARAM_APP_INSTANCE_ID);
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
        String imageResult = HttpClientUtil.vmInstantiateImage(basePath, getContext().getUserId(), getContext().getToken(), vmInstanceId, appInstanceId, lcmLog);
        LOGGER.info("import image result: {}", imageResult);
        if (StringUtils.isEmpty(imageResult)) {
            return null;
        }
        JsonObject jsonObject = new JsonParser().parse(imageResult).getAsJsonObject();
        JsonElement imageId = jsonObject.get("imageId");
        return imageId.getAsString();
    }

    private void sendDeleteVmImageToLcm(MepHost mepHost) {
        String imageId = (String) getContext().getParameter(IContextParameter.PARAM_IMAGE_INSTANCE_ID);
        String appInstanceId = (String) getContext().getParameter(IContextParameter.PARAM_APP_INSTANCE_ID);
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());
        HttpClientUtil.deleteVmImage(basePath, getContext().getUserId(), appInstanceId,
            imageId, getContext().getToken());
    }

    private EnumExportImageStatus queryImageStatus(MepHost mepHost, String imageId, LcmLog lcmLog) {
        String appInstanceId = (String) getContext().getParameter(IContextParameter.PARAM_APP_INSTANCE_ID);
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
            Type vmInfoType = new TypeToken<MepmVmImageInfo>() { }.getType();
            MepmVmImageInfo mepmVmImageInfo = gson.fromJson(workStatus, vmInfoType);
            if (mepmVmImageInfo.getStatus().equals(EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS.toString())) {
                saveImageNameAndUrl(mepmVmImageInfo.getUrl(), mepmVmImageInfo.getImageName());
                return EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS;
            }
            if (mepmVmImageInfo.getStatus().equals(EnumExportImageStatus.EXPORT_IMAGE_STATUS_FAILED.toString())) {
                return EnumExportImageStatus.EXPORT_IMAGE_STATUS_FAILED;
            }
            try {
                Thread.sleep(INTERVAL);
                waitingTime += INTERVAL;
            }catch (InterruptedException e) {
                LOGGER.error("export image sleep failed.");
                Thread.currentThread().interrupt();
            }
            
        }
        return EnumExportImageStatus.EXPORT_IMAGE_STATUS_TIMEOUT;
    }

    private boolean saveImageNameAndUrl(String url, String imageName) {
        String vmId = (String) getContext().getParameter(IContextParameter.PARAM_VM_ID);
        ImageExportInfo imageExportInfo = vmAppOperationService.getImageExportInfo(vmId);
        imageExportInfo.setName(imageName);
        imageExportInfo.setDownloadUrl(url);
        vmAppOperationService.modifyExportInfo(vmId, imageExportInfo);
        return true;
    }

}
