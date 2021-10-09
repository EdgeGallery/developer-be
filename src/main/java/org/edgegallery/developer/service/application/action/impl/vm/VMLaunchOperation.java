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
import org.edgegallery.developer.service.application.common.ActionProgressRange;
import org.edgegallery.developer.service.application.action.impl.ActionIterator;
import org.edgegallery.developer.service.application.action.impl.DistributePackageAction;
import org.edgegallery.developer.service.application.action.impl.OperationContext;
import org.edgegallery.developer.service.application.common.IContextParameter;

public class VMLaunchOperation implements IActionCollection {

    private OperationContext context;

    public List<IAction> actions;

    public VMLaunchOperation(User user, String applicationId, String vmId, String token, OperationStatus operationStatus) {

        IAction buildPackageAction = new BuildVMPackageAction();
        IAction distributePackageAction = new DistributePackageAction();
        IAction instantiateVMAppAction = new InstantiateVMAppAction();

        Map<String, ActionProgressRange> actionProgressRangeMap = new HashMap<String, ActionProgressRange>();
        actionProgressRangeMap.put(buildPackageAction.getActionName(), new ActionProgressRange(0, 20));
        actionProgressRangeMap.put(distributePackageAction.getActionName(), new ActionProgressRange(20, 50));
        actionProgressRangeMap.put(instantiateVMAppAction.getActionName(), new ActionProgressRange(50, 100));

        this.context = new OperationContext(user, token, operationStatus, actionProgressRangeMap);
        buildPackageAction.setContext(context);
        distributePackageAction.setContext(context);
        instantiateVMAppAction.setContext(context);
        context.addParameter(IContextParameter.PARAM_APPLICATION_ID, applicationId);
        context.addParameter(IContextParameter.PARAM_VM_ID, vmId);

        actions = Arrays.asList(buildPackageAction, distributePackageAction, instantiateVMAppAction);
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
