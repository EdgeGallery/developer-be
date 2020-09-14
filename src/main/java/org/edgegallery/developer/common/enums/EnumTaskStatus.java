/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.common.enums;

import lombok.Getter;

@Getter
public enum EnumTaskStatus {

    TASK_STATUS_IN_PROGRESS(0, "IN_PROGRESS"),
    TASK_STATUS_ENV_PREPARE(1, "Environmental preparation"),
    TASK_STATUS_TEST_PREPARATION(2, "Test preparation"),
    TASK_STATUS_TEST_EXECUTION(3, "Test execution"),
    TASK_STATUS_COMPLETED(4, "COMPLETED");

    private int code;

    private String value;

    EnumTaskStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }
}
