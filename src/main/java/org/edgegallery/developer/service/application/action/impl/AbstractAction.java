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

import java.util.UUID;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.EnumOperationObjectType;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IContext;

public abstract class AbstractAction implements IAction {

    private
    IContext context;

    public void setContext(IContext context) {
        this.context = context;
    }

    public IContext getContext(){
        return context;
    }

    public abstract String getActionName();

    public abstract boolean execute();

    public ActionStatus  initActionStatus(EnumOperationObjectType objectType, String objectId, String actionName, String statusLog){
        ActionStatus actionStatus = new ActionStatus();
        actionStatus.setId(UUID.randomUUID().toString());
        actionStatus.setObjectType(objectType);
        actionStatus.setObjectId(objectId);
        actionStatus.setActionName(actionName);
        actionStatus.setProgress(0);
        actionStatus.setStatus(EnumActionStatus.ONGOING);
        actionStatus.appendStatusLog(statusLog);
        context.addActionStatus(actionStatus);
        return actionStatus;
    }

    public void updateActionProgress(ActionStatus actionStatus, int progress, String statusLog){
        actionStatus.setProgress(progress);
        actionStatus.appendStatusLog(statusLog);
        getContext().updateActionStatus(actionStatus);
    }

    public void updateActionError(ActionStatus actionStatus, String errorMsg){
        actionStatus.setStatus(EnumActionStatus.FAILED);
        actionStatus.setErrorMsg(errorMsg);
        actionStatus.appendStatusLog(errorMsg);
        getContext().updateActionStatus(actionStatus);
    }
}
