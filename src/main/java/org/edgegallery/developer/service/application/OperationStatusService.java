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

package org.edgegallery.developer.service.application;

import java.util.Map;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.service.application.common.ActionProgressRange;

public interface OperationStatusService {

    /**
     * when launch application(deploy and test),get this operation's status.
     *
     * @param operationId operationId
     * @return
     */
    OperationStatus getOperationStatusById(String operationId);

    /**
     * create operation status.
     *
     * @param operationStatus operationStatus
     * @return
     */
    Boolean createOperationStatus(OperationStatus operationStatus);

    /**
     * When running to a certain stage, it needs to change its state.
     *
     * @param operationStatus operationStatus
     * @return
     */
    Boolean modifyOperationStatus(OperationStatus operationStatus);

    /**
     * get operation's count with object type.
     *
     * @param objectType objectType
     * @return
     */
    int getOperationCountByObjectType(String objectType);

    /**
     * add action status with operation status.
     *
     * @param operationId operationId
     * @param status status
     * @param actionProgressRangeMap actionProgressRangeMap
     * @return
     */
    int addActionStatusWithUpdateOperationStatus(String operationId, ActionStatus status,
        Map<String, ActionProgressRange> actionProgressRangeMap);

    /**
     * update action status with operation status.
     *
     * @param operationId operationId
     * @param status status
     * @param actionProgressRangeMap actionProgressRangeMap
     * @return
     */
    int updateActionStatusWithUpdateOperationStatus(String operationId, ActionStatus status,
        Map<String, ActionProgressRange> actionProgressRangeMap);
}
