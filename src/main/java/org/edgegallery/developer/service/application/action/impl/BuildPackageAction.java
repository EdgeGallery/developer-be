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

import org.edgegallery.developer.service.application.action.IAction;
import org.edgegallery.developer.service.application.action.IContext;

public class BuildPackageAction implements IAction {

    public static final String ACTION_NAME = "Build Application Package";

    private OperationContext context;

    @Override
    public void setContext(OperationContext context) {
        this.context = context;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public int execute() {
        return 0;
    }
}
