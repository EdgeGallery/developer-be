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

import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.EnumExportImageStatus;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class QueryImageStatusAction extends AbstractAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateImageAction.class);

    public static final String ACTION_NAME = "Query Image Status";

    ApplicationService applicationService = (ApplicationService) SpringContextUtil.getBean(ApplicationService.class);

    MepHostService mepHostService = (MepHostService) SpringContextUtil.getBean(MepHostService.class);

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
        updateActionProgress(actionStatus, 0, statusLog);
        LcmLog lcmLog = new LcmLog();
        EnumExportImageStatus exportImageStatus = queryImageStatus(mepHost, lcmLog);
        if (!EnumExportImageStatus.EXPORT_IMAGE_STATUS_SUCCESS.equals(exportImageStatus)) {
            String msg = "Query export image status failed, the result is: " + exportImageStatus;
            updateActionError(actionStatus, msg);
            return false;
        }
        updateActionProgress(actionStatus, 100, "Query export image status success.");
        LOGGER.info("Distribute package action finished.");
        return true;
    }

    private EnumExportImageStatus queryImageStatus(MepHost mepHost, LcmLog lcmLog) {
        return null;
    }
}
