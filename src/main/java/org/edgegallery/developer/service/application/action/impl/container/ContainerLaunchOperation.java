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

import java.util.Arrays;
import java.util.List;
import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IActionCollection;
import org.edgegallery.developer.service.application.action.IActionIterator;
import org.edgegallery.developer.service.application.action.impl.BuildPackageAction;
import org.edgegallery.developer.service.application.action.impl.DistributePackageAction;
import org.edgegallery.developer.service.application.action.impl.QueryDistributePackageStatusAction;
import org.edgegallery.developer.service.application.action.impl.vm.InstantiateVMAppAction;
import org.edgegallery.developer.service.application.action.impl.vm.QueryInstantiateVMAppStatusAction;
import org.edgegallery.developer.service.application.action.impl.ActionIterator;

public class ContainerLaunchOperation implements IActionCollection {

    public List<IAction> actions = Arrays.asList(
        new BuildPackageAction(),
        new DistributePackageAction(),
        new QueryDistributePackageStatusAction(),
        new InstantiateContainerAppAction(),
        new QueryInstantiateContainerAppStatusAction());

    @Override
    public IActionIterator getActionIterator() {
        return new ActionIterator(this);
    }

    @Override
    public List<IAction> getActionList() {
        return actions;
    }
}
