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

package org.edgegallery.developer.util;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.edgegallery.developer.common.enums.EnumTaskStatus;

/**
 * for task running schedule.
 */
public class SubTaskRunningHandler {

    private static final SubTaskRunningHandler instance = new SubTaskRunningHandler();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Map<String, Integer> subTaskStatusQueryCount = new HashMap<>();

    private Map<String, Integer> taskStatusQueryCount = new HashMap<>();

    private SubTaskRunningHandler() {
    }

    public static SubTaskRunningHandler getInstance() {
        return instance;
    }

    /**
     * getStatus.
     *
     * @return
     */
    public String getStatusMock(String executionId) {
        if (subTaskStatusQueryCount.containsKey(executionId)) {
            int count = subTaskStatusQueryCount.get(executionId);
            if (count < 2) {
                subTaskStatusQueryCount.put(executionId, ++count);
                return EnumTaskStatus.TASK_STATUS_IN_PROGRESS.getValue();
            } else {
                return EnumTaskStatus.TASK_STATUS_COMPLETED.getValue();
            }
        } else {
            subTaskStatusQueryCount.put(executionId, getRandomNum());
            return EnumTaskStatus.TASK_STATUS_IN_PROGRESS.getValue();
        }
    }

    private int getRandomNum() {
        return SECURE_RANDOM.nextInt(3);
    }

    /**
     * getTaskStatus.
     *
     * @return
     */
    public String getTaskStatus(String taskId) {
        if (taskStatusQueryCount.containsKey(taskId)) {
            int count = taskStatusQueryCount.get(taskId);
            String status;
            switch (count) {
                case 4:
                    status = EnumTaskStatus.TASK_STATUS_COMPLETED.getValue();
                    break;
                case 3:
                    status = EnumTaskStatus.TASK_STATUS_TEST_EXECUTION.getValue();
                    break;
                case 2:
                    status = EnumTaskStatus.TASK_STATUS_TEST_PREPARATION.getValue();
                    break;
                default:
                    status = EnumTaskStatus.TASK_STATUS_ENV_PREPARE.getValue();
                    break;
            }
            if (count <= 4) {
                taskStatusQueryCount.put(taskId, ++count);
            }

            return status;
        } else {
            taskStatusQueryCount.put(taskId, 0);
            return EnumTaskStatus.TASK_STATUS_ENV_PREPARE.getValue();
        }
    }
}
