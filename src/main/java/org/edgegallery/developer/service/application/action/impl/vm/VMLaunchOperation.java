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
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionCollection;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.common.ActionProgressRange;
import org.edgegallery.developer.service.application.action.impl.ActionIterator;
import org.edgegallery.developer.service.application.action.impl.BuildPackageAction;
import org.edgegallery.developer.service.application.action.impl.DistributePackageAction;
import org.edgegallery.developer.service.application.action.impl.OperationContext;
import org.edgegallery.developer.service.application.action.impl.QueryDistributePackageStatusAction;

public class VMLaunchOperation implements IActionCollection {

    private OperationContext context;

    public List<IAction> actions;

    public VMLaunchOperation(String lcmController, OperationStatus operationStatus) {

        IAction buildPackageAction = new BuildPackageAction();
        IAction distributePackageAction = new DistributePackageAction();
        IAction queryDistributePackageStatusAction = new QueryDistributePackageStatusAction();
        IAction instantiateVMAppAction = new InstantiateVMAppAction();
        IAction queryInstantiateVMAppStatusAction = new QueryInstantiateVMAppStatusAction();

        Map<String, ActionProgressRange> actionProgressRangeMap = new HashMap<String, ActionProgressRange>();
        actionProgressRangeMap.put(buildPackageAction.getActionName(), new ActionProgressRange(0, 20));
        actionProgressRangeMap.put(distributePackageAction.getActionName(), new ActionProgressRange(20, 40));
        actionProgressRangeMap.put(queryDistributePackageStatusAction.getActionName(), new ActionProgressRange(40, 60));
        actionProgressRangeMap.put(instantiateVMAppAction.getActionName(), new ActionProgressRange(60, 80));
        actionProgressRangeMap.put(queryInstantiateVMAppStatusAction.getActionName(), new ActionProgressRange(80, 100));

        OperationContext context = new OperationContext(lcmController, operationStatus, actionProgressRangeMap);
        buildPackageAction.setContext(context);
        distributePackageAction.setContext(context);
        queryDistributePackageStatusAction.setContext(context);
        instantiateVMAppAction.setContext(context);
        queryInstantiateVMAppStatusAction.setContext(context);

        actions = Arrays.asList(buildPackageAction, distributePackageAction, queryDistributePackageStatusAction,
            instantiateVMAppAction, queryInstantiateVMAppStatusAction);
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
