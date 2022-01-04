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

package org.edgegallery.developer.service.application.impl;

import java.util.List;
import java.util.Map;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.mapper.operation.OperationStatusMapper;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.common.ActionProgressRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationStatusServiceImpl implements OperationStatusService {

    @Autowired
    private OperationStatusMapper operationStatusMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationStatusServiceImpl.class);

    @Override
    public OperationStatus getOperationStatusById(String operationId) {

        OperationStatus operationStatus = operationStatusMapper.getOperationStatusById(operationId);
        if (null == operationStatus) {
            LOGGER.error("The Operation does not exist, operationId: {}", operationId);
            return null;
        }
        List<ActionStatus> actionStatusList = operationStatusMapper.getActionStatusByOperationId(operationId);
        operationStatus.setActionStatusList(actionStatusList);
        return operationStatus;
    }

    @Override
    public Boolean createOperationStatus(OperationStatus operationStatus) {
        int res = operationStatusMapper.createOperationStatus(operationStatus);
        if (res < 1) {
            LOGGER.error("Create operationStatus in db error.");
            throw new DataBaseException("Create operationStatus in db error.",
                ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public Boolean modifyOperationStatus(OperationStatus operationStatus) {
        int res = operationStatusMapper.modifyOperationStatus(operationStatus);
        if (res <1) {
            LOGGER.error("Modify operationStatus in db error.");
            throw new DataBaseException("Modify operationStatus in db error.",
                ResponseConsts.RET_CREATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public int getOperationCountByObjectType(String objectType) {
        return operationStatusMapper.getOperationCountByObjectType(objectType);
    }

    @Override
    public int addActionStatusWithUpdateOperationStatus(String operationId, ActionStatus status,
        Map<String, ActionProgressRange> actionProgressRangeMap) {
        updateOperationStatusByActionStatus(operationId, status, actionProgressRangeMap);
        return operationStatusMapper.createActionStatus(operationId, status);
    }

    @Override
    public int updateActionStatusWithUpdateOperationStatus(String operationId, ActionStatus status,
        Map<String, ActionProgressRange> actionProgressRangeMap) {
        updateOperationStatusByActionStatus(operationId, status, actionProgressRangeMap);
        return operationStatusMapper.modifyActionStatus(status);
    }

    private void updateOperationStatusByActionStatus(String operationId, ActionStatus actionStatus,
        Map<String, ActionProgressRange> actionProgressRangeMap) {
        ActionProgressRange actionProgressRange = actionProgressRangeMap.get(actionStatus.getActionName());
        int actionProgressLength = actionProgressRange.getEnd() - actionProgressRange.getStart();
        int progress = actionProgressRangeMap.get(actionStatus.getActionName()).getStart()
            + actionProgressLength * actionStatus.getProgress()/100;
        OperationStatus operationStatus = getOperationStatusById(operationId);
        operationStatus.setProgress(progress);
        if (EnumActionStatus.FAILED.equals(actionStatus.getStatus())) {
            operationStatus.setStatus(EnumActionStatus.FAILED);
            operationStatus.setErrorMsg(actionStatus.getErrorMsg());
        }
        if (progress==100) {
            operationStatus.setStatus(EnumActionStatus.SUCCESS);
        }
        operationStatusMapper.modifyOperationStatus(operationStatus);
    }
}
