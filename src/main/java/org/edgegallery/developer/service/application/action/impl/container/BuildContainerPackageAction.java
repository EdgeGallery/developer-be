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

package org.edgegallery.developer.service.application.action.impl.container;

import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.action.impl.AbstractAction;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.edgegallery.developer.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildContainerPackageAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildContainerPackageAction.class);

    public static final String ACTION_NAME = "Build Application Package";

    ContainerAppOperationServiceImpl containerAppOperationService = (ContainerAppOperationServiceImpl) SpringContextUtil
        .getBean(ContainerAppOperationServiceImpl.class);

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public boolean execute() {
        //Start action , save action status.
        String applicationId = (String) getContext().getParameter(IContextParameter.PARAM_APPLICATION_ID);
        String statusLog = "Start to build the package for application: " + applicationId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = initActionStatus(EnumOperationObjectType.HELM_CHART, applicationId, ACTION_NAME,
            statusLog);

        //build application package for container app
        AppPackage appPkg = containerAppOperationService.generatePackage(applicationId);
        if (appPkg == null) {
            statusLog = "Build package for application failed.";
            LOGGER.error(statusLog);
            updateActionError(actionStatus, statusLog);
            return false;
        }
        getContext().addParameter(IContextParameter.PARAM_PACKAGE_ID, appPkg.getId());
        statusLog = "Build package for container application finished.";
        LOGGER.info(statusLog);
        updateActionProgress(actionStatus, 50, statusLog);

        //save vm instantiate info.
        boolean saveResult = saveBuildContainerPackageInfo(applicationId, appPkg.getId());
        if (!saveResult) {
            updateActionError(actionStatus, "Update instantiate info for container failed.");
            return false;
        }
        statusLog = "Update package info to container application instantiate info finished.";
        LOGGER.info(statusLog);
        updateActionProgress(actionStatus, 100, statusLog);
        return true;
    }

    private boolean saveBuildContainerPackageInfo(String applicationId, String packageId) {
        ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService.getInstantiateInfo(applicationId);
        if (instantiateInfo == null) {
            LOGGER.error("modify Container App InstantiateInfo fail, InstantiateInfo is null");
            return false;
        }
        instantiateInfo.setAppPackageId(packageId);
        instantiateInfo.setStatus(EnumAppInstantiateStatus.PACKAGE_GENERATE_SUCCESS);
        boolean res = containerAppOperationService.updateInstantiateInfo(applicationId, instantiateInfo);
        if (!res) {
            LOGGER.error("modify Container App InstantiateInfo fail");
            return false;
        }
        return true;
    }

}
