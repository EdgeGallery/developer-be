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
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.service.application.action.IContext;
import org.edgegallery.developer.service.application.action.common.ActionProgressRange;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class OperationContext implements IContext {

    //token to lcmcontroller to be used by the thread.
    private String token;

    private OperationStatus operationStatus;

    Map<String, Object> contextMap;

    private Map<String, ActionProgressRange> actionProgressRangeMap;

    @Autowired
    private OperationStatusMapper operationStatusMapper;

    public OperationContext(String token, OperationStatus operationStatus,
        Map<String, ActionProgressRange> actionProgressRangeMap) {
        this.token = token;
        this.operationStatus = operationStatus;
        this.actionProgressRangeMap = actionProgressRangeMap;
    }

    @Override
    public String getLcmToken() {
        return token;
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
        updateOperationStatusByActionStatus(status);
        return operationStatusMapper.createActionStatus(operationStatus.getId(), status);
    }

    @Override
    public int updateActionStatus(ActionStatus status) {
        updateOperationStatusByActionStatus(status);
        return operationStatusMapper.modifyActionStatus(status);
    }

    private void updateOperationStatusByActionStatus(ActionStatus actionStatus) {
        ActionProgressRange actionProgressRange = actionProgressRangeMap.get(actionStatus.getActionName());
        int actionProgressLength = actionProgressRange.getEnd() - actionProgressRange.getStart();
        int progress = actionProgressRangeMap.get(actionStatus.getActionName()).getStart()
            + actionProgressLength * actionStatus.getProgress();
        operationStatus.setProgress(progress);
        if (EnumActionStatus.FAILED.equals(actionStatus.getStatus())) {
            operationStatus.setStatus(EnumActionStatus.FAILED);
            operationStatus.setErrorMsg(actionStatus.getErrorMsg());
        }
        operationStatusMapper.modifyOperationStatus(operationStatus);
    }
}
