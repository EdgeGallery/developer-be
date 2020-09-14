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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.edgegallery.developer.common.enums.EnumTaskStatus;
import org.edgegallery.developer.model.SubTaskBean;
import org.edgegallery.developer.model.TestCase;

/**
 * for task running schedule.
 */
public class TaskRunningHandler {

    private static final TaskRunningHandler instance = new TaskRunningHandler();

    private Set<TestCase> testCaseSet = new HashSet<>();

    private Map<String, String> appTestTaskIdAndStatusMap = new HashMap<>();

    private Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap = new HashMap<>();

    public TaskRunningHandler() {
        initData();
    }

    public static TaskRunningHandler getInstance() {
        return instance;
    }

    private void initData() {
        clear();
        testCaseSet.clear();
        String[] descriptions = new String[] {
            "The VNF Package MUST include appropriate credentials so that ONAP can interact with the Chef Server",
            "The VNF provider MUST provide cookbooks to be loaded on the appropriate Chef Server.",
            "VNF provider MUST include manifest file that contains a list of all the components in VNF package",
            "The VNF provider MUST provide their testing scripts to support testing as specified in "
                + "ETSI NFV-SOL004 - Testing directory in CSAR.",
            "The VNF Package MUST include all relevant Chef artifacts (roles/cookbooks/recipes)"
                + "required to execute VNF actions requested by ONAP for loading on appropriate Chef Server."
        };
        for (int i = 0; i < 5; i++) {
            TestCase tc1 = new TestCase();
            tc1.setId(i + 1);
            tc1.setDescription(descriptions[i]);
            tc1.setAuthor("Author");
            tc1.setScenarios("mec-app");
            tc1.setTestsuite("validation");
            tc1.setInputs("[{\"name\""
                + ":\"csar\",\"description\":\"CSAR file path\",\"type\":\"binary\",\"isOptional\":false}]");
            testCaseSet.add(tc1);
        }
    }

    private void clear() {
        appTestTaskIdAndStatusMap.clear();
        taskIdAndSubTaskMap.clear();
    }

    /**
     * getTestCaseSet.
     *
     * @return
     */
    public Set<TestCase> getTestCaseSet() {
        if (testCaseSet.isEmpty()) {
            initData();
        }
        return testCaseSet;
    }

    /**
     * isTaskNeedLoaded.
     *
     * @return
     */
    public boolean isTaskNeedLoaded() {
        return appTestTaskIdAndStatusMap.isEmpty();
    }

    /**
     * setRunningData.
     */
    public void setRunningData(Map<String, String> taskIdAndStatusMap,
        Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap) {
        clear();
        this.appTestTaskIdAndStatusMap = taskIdAndStatusMap;
        this.taskIdAndSubTaskMap = taskIdAndSubTaskMap;
    }

    public void addTaskAndSubTaskStatus(String taskId, Set<SubTaskBean> subTaskSet) {
        taskIdAndSubTaskMap.put(taskId, subTaskSet);
    }

    public void addTaskIdAndStatus(String taskId) {
        appTestTaskIdAndStatusMap.put(taskId, EnumTaskStatus.TASK_STATUS_ENV_PREPARE.getValue());
    }

    public Map<String, String> getAppTestTaskIdAndStatusMap() {
        return appTestTaskIdAndStatusMap;
    }

    public void setAppTestTaskIdAndStatusMap(Map<String, String> appTestTaskIdAndStatusMap) {
        this.appTestTaskIdAndStatusMap = appTestTaskIdAndStatusMap;
    }

    public Map<String, Set<SubTaskBean>> getTaskIdAndSubTaskMap() {
        return taskIdAndSubTaskMap;
    }

    public void setTaskIdAndSubTaskMap(Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap) {
        this.taskIdAndSubTaskMap = taskIdAndSubTaskMap;
    }
}
