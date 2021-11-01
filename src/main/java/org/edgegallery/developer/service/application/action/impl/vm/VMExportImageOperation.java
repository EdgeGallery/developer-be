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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionCollection;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.ActionIterator;
import org.edgegallery.developer.service.application.action.impl.OperationContext;
import org.edgegallery.developer.service.application.common.ActionProgressRange;
import org.edgegallery.developer.service.application.common.IContextParameter;

public class VMExportImageOperation implements IActionCollection {
    private OperationContext context;

    public List<IAction> actions;

    public VMExportImageOperation(User user, String applicationId, String vmId,
        OperationStatus operationStatus, String appInstanceId, String vmInstanceId) {
        IAction createImageAction = new CreateImageAction();
        IAction DownloadImageAction = new DownloadImageAction();

        Map<String, ActionProgressRange> actionProgressRangeMap = new HashMap<String, ActionProgressRange>();
        actionProgressRangeMap.put(createImageAction.getActionName(), new ActionProgressRange(0, 50));
        actionProgressRangeMap.put(DownloadImageAction.getActionName(), new ActionProgressRange(50, 100));

        this.context = new OperationContext(user, operationStatus, actionProgressRangeMap);
        createImageAction.setContext(context);
        DownloadImageAction.setContext(context);
        context.addParameter(IContextParameter.PARAM_APPLICATION_ID, applicationId);
        context.addParameter(IContextParameter.PARAM_VM_ID, vmId);
        context.addParameter(IContextParameter.PARAM_APP_INSTANCE_ID, appInstanceId);
        context.addParameter(IContextParameter.PARAM_VM_INSTANCE_ID, vmInstanceId);
        actions = Arrays.asList(createImageAction, DownloadImageAction);
    }

    @Override
    public IActionIterator getActionIterator() {
        return new ActionIterator(this);
    }

    @Override
    public List<IAction> getActionList() {
        return actions;
    }




}