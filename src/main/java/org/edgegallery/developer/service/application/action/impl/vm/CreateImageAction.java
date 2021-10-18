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
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CreateImageAction extends AbstractAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateImageAction.class);

    public static final String ACTION_NAME = "Create Image";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MepHostService mepHostService;

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
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        Application application = applicationService.getApplication(applicationId);
        String statusLog = "Start to create vm image for package Id：" + packageId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.VM_IMAGE_INSTANCE, packageId,
            ACTION_NAME, statusLog);
        String mepHostId = application.getMepHostId();
        if (null == mepHostId || "".equals(mepHostId)) {
            updateActionError(actionStatus, "Sandbox not selected. Failed to instantiate package");
            return false;
        }
        MepHost mepHost = mepHostService.getHost(mepHostId);

        //create image.
        LcmLog lcmLog = new LcmLog();
        String imageId = createImageByLcm(mepHost, lcmLog);
        if (null == imageId) {
            String msg = "create vm  image  failed. The log from lcm is : " + lcmLog.getLog();
            updateActionError(actionStatus, msg);
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
        updateActionProgress(actionStatus, 100, "create vm  image success");

        getContext().addParameter(IContextParameter.PARAM_IMAGE_INSTANCE_ID, imageId);



        return true;
    }

    private Boolean saveImageIdToImageExportInfo(String imageId) {
        return true;
    }

    private String createImageByLcm(MepHost mepHost, LcmLog lcmLog) {
        String userId = getContext().getUserId();
        String mepmPkgId = (String) getContext().getParameter(IContextParameter.PARAM_MEPM_PACKAGE_ID);
        String appInstanceId = (String) getContext().getParameter(IContextParameter.PARAM_APP_INSTANCE_ID);
        String basePath = HttpClientUtil.getUrlPrefix(mepHost.getLcmProtocol(), mepHost.getLcmIp(), mepHost.getLcmPort());

        String imageResult = HttpClientUtil.vmInstantiateImage(basePath, userId, getContext().getToken(), mepmPkgId, appInstanceId, lcmLog);
        LOGGER.info("import image result: {}", imageResult);
        if (StringUtils.isEmpty(imageResult)) {
            return null;
        }
        JsonObject jsonObject = new JsonParser().parse(imageResult).getAsJsonObject();
        JsonElement imageId = jsonObject.get("imageId");
        return imageId.getAsString();
    }
}
