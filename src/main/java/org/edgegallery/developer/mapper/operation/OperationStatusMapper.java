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
import org.apache.ibatis.annotations.Param;
import org.edgegallery.developer.model.operation.ActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;

@Mapper
public interface OperationStatusMapper {

    int createOperationStatus(OperationStatus operationStatus);

    int modifyOperationStatus(OperationStatus operationStatus);

    OperationStatus getOperationStatusById(String id);

    int createActionStatus(@Param("operationId") String operationId, @Param("action") ActionStatus action);

    int modifyActionStatus(ActionStatus operationStatus);

    List<ActionStatus> getActionStatusByOperationId(String operationId);

    int getOperationCountByObjectType(String objectType);

    int deleteOperationStatus(String id);

    int deleteActionStatus(String id);
}
