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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.edgegallery.developer.common.enums.EnumTaskStatus;
import org.edgegallery.developer.model.SubTaskBean;
import org.edgegallery.developer.model.TestCase;
import org.edgegallery.developer.model.VnfParas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For test case task start/status calling.
 */
public final class TaskUtil {

    private static final Logger log = LoggerFactory.getLogger(TaskUtil.class);

    private TaskUtil() {
    }

    /**
     * Start a test task of XNF type.
     *
     * @return
     */
    public static Set<SubTaskBean> startXnfTestCaseMock(Map<TestCase, VnfParas> testCases, String taskId) {
        Set<SubTaskBean> subTaskBeanSet = new HashSet<>();
        if (testCases == null) {
            log.error("TaskUtil-startXNFTestCaseMock param testCases is null");
            return new HashSet<>();
        }
        if (taskId == null || taskId.equals("")) {
            log.error("TaskUtil-startXNFTestCaseMock param taskId is null");
            return new HashSet<>();
        }
        Set<Entry<TestCase, VnfParas>> set = testCases.entrySet();
        for (Entry<TestCase, VnfParas> entry : set) {
            String executionId = UUID.randomUUID().toString();
            SubTaskBean subTaskBean = new SubTaskBean();
            subTaskBean.setExecutionid(executionId);
            subTaskBean.setTaskid(taskId);
            subTaskBean.setTestcaseid(entry.getKey().getId());
            subTaskBean.setStatus(EnumTaskStatus.TASK_STATUS_ENV_PREPARE.getValue());
            subTaskBeanSet.add(subTaskBean);
        }
        return subTaskBeanSet;
    }

}
