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
package org.edgegallery.developer.mapper.operation;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;

@Mapper
public interface OperationStatusMapper {

    int createOperationStatus(OperationStatus operationStatus);

    int modifyOperationStatus(OperationStatus operationStatus);

    int deleteOperationStatus(String id);

    OperationStatus getOperationStatusById(String id);

    int createActionStatus(String operationId, ActionStatus operationStatus);

    int modifyActionStatus(ActionStatus operationStatus);

    int deleteActionStatus(String id);

    ActionStatus getActionStatusById(String id);

    List<ActionStatus> getActionStatusByOperationId(String operationId);
}
