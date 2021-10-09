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

import java.util.Map;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.common.ActionProgressRange;
import org.springframework.beans.factory.annotation.Autowired;

public class OperationContext implements IContext {

    //user of the operation
    private User user;

    //action token from FE, need to be used in rest call to lcm controller.
    private String token;

    private OperationStatus operationStatus;

    Map<String, Object> contextMap;

    private Map<String, ActionProgressRange> actionProgressRangeMap;

    @Autowired
    private OperationStatusService operationStatusService;

    public OperationContext(User user, String token, OperationStatus operationStatus,
        Map<String, ActionProgressRange> actionProgressRangeMap) {
        this.user = user;
        this.token = token;
        this.operationStatus = operationStatus;
        this.actionProgressRangeMap = actionProgressRangeMap;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getUserId() {
        return this.user.getUserId();
    }

    @Override
    public String getUserName() {
        return this.user.getUserName();
    }

    @Override
    public void addParameter(String key, Object value) {
        contextMap.put(key, value);
    }

    @Override
    public Object getParameter(String key) {
        return contextMap.get(key);
    }

    @Override
    public int addActionStatus(ActionStatus status) {
        return operationStatusService.addActionStatusWithUpdateOperationStatus(operationStatus.getId(), status,
            actionProgressRangeMap);
    }

    @Override
    public int updateActionStatus(ActionStatus status) {
        return operationStatusService.updateActionStatusWithUpdateOperationStatus(operationStatus.getId(), status,
            actionProgressRangeMap);
    }

    @Override
    public OperationStatus getOperationStatus() {
        return null;
    }

}
