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

import java.util.ArrayList;
import java.util.UUID;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.common.IContextParameter;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BuildPackageAction implements IAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildPackageAction.class);

    public static final String ACTION_NAME = "Build Application Package";

    private IContext context;

    @Autowired
    private VMAppOperationServiceImpl VmAppOperationService;

    @Autowired
    private ApplicationService applicationService;

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
        String vmId = (String)context.getParameter(IContextParameter.PARAM_VM_ID);
        String statusLog = "Start to build the package for vm: " + vmId;
        LOGGER.info(statusLog);
        ActionStatus actionStatus = new ActionStatus();
        actionStatus.setId(UUID.randomUUID().toString());
        actionStatus.setObjectType(EnumOperationObjectType.VM);
        actionStatus.setObjectId(vmId);
        actionStatus.setActionName(ACTION_NAME);
        actionStatus.setProgress(0);
        actionStatus.setStatus(EnumActionStatus.ONGOING);
        actionStatus.appendStatusLog(statusLog);
        context.addActionStatus(actionStatus);

        //create new application object with single vm.
        ApplicationDetail detail = applicationService.getApplicationDetail((String) context.getParameter(IContextParameter.PARAM_APPLICATION_ID));
        VMApplication tempApp = detail.getVmApp();
        tempApp.setId(UUID.randomUUID().toString());
        for(VirtualMachine vm : tempApp.getVmList()){
            if(vmId.equals(vm.getId())){
                tempApp.getVmList().clear();
                tempApp.getVmList().add(vm);
                break;
            }
        }
        statusLog = "Build application for single vm finished.";
        LOGGER.info(statusLog);
        actionStatus.setProgress(50);
        actionStatus.appendStatusLog(statusLog);
        context.updateActionStatus(actionStatus);

        //build application package for launch VM.
        AppPackage appPkg = VmAppOperationService.generatePackage(tempApp);
        if(appPkg == null){
            statusLog = "Build package for VM failed.";
            LOGGER.error(statusLog);
            actionStatus.setStatus(EnumActionStatus.FAILED);
            actionStatus.setErrorMsg(statusLog);
            actionStatus.appendStatusLog(statusLog);
            context.updateActionStatus(actionStatus);
            return false;
        }

        statusLog = "Build package for single vm finished.";
        LOGGER.info(statusLog);
        actionStatus.setProgress(100);
        actionStatus.appendStatusLog(statusLog);
        context.updateActionStatus(actionStatus);
        return true;
    }
}
