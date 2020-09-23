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

package org.edgegallery.developer.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.spencerwi.either.Either;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.ibatis.exceptions.PersistenceException;
import org.edgegallery.developer.common.enums.EnumTaskStatus;
import org.edgegallery.developer.mapper.TestAppMapper;
import org.edgegallery.developer.mapper.TestCaseMapper;
import org.edgegallery.developer.model.SubTaskBean;
import org.edgegallery.developer.model.TestApp;
import org.edgegallery.developer.model.TestCase;
import org.edgegallery.developer.model.TestTask;
import org.edgegallery.developer.model.VnfParas;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.SubTaskListResponse;
import org.edgegallery.developer.util.SubTaskRunningHandler;
import org.edgegallery.developer.util.TaskRunningHandler;
import org.edgegallery.developer.util.TaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("testCaseService")
public class TestCaseService {

    private static final Logger log = LoggerFactory.getLogger(TestCaseService.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final int TASK_START_SUCCESS = 0;

    private static final int TASK_START_FAILED = 1;

    @Autowired
    private TestAppMapper testMapper;

    @Autowired
    private TestCaseMapper testCaseMapper;

    /**
     * startToTest.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Boolean> startToTest(String appId, String userId) {
        if (appId == null || "".equals(appId)) {
            log.error("test-case,param does not provide appid.");
            return Either
                .left(new FormatRespDto(Response.Status.BAD_REQUEST, "test-case,param does not provide appid"));
        }

        int result = startTask(appId);
        if (result == TASK_START_SUCCESS) {
            return Either.right(true);
        } else {
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "internal exception"));
        }

    }

    /**
     * startTask.
     *
     * @return
     */
    public int startTask(String appId) {
        String taskId = UUID.randomUUID().toString();
        TestApp app = testMapper.getAppById(appId);
        Map<TestCase, VnfParas> testMap = getXnfTestCases(app.getAppFile());
        if (testMap.isEmpty()) {
            log.error("get vnfparams failed");
            return TASK_START_SUCCESS;
        }
        try {
            TestTask task = new TestTask();
            task.setTaskId(taskId);
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
            String dateStr = df.format(LocalDateTime.now());
            String query = "MEC" + dateStr + "%";
            String taskno = testMapper.getTaskNo(query);
            String newTaskNo = getTaskNo(taskno, dateStr);
            task.setTaskNo(newTaskNo);
            task.setBeginTime(new Date());
            task.setStatus(EnumTaskStatus.TASK_STATUS_ENV_PREPARE.getValue());
            task.setAppId(appId);
            testMapper.addTestTask(task);
            Set<SubTaskBean> set = TaskUtil.startXnfTestCaseMock(testMap, taskId);
            saveSubTasks(set);
            TaskRunningHandler.getInstance().addTaskIdAndStatus(taskId);
            TaskRunningHandler.getInstance().addTaskAndSubTaskStatus(taskId, set);
            return TASK_START_SUCCESS;
        } catch (PersistenceException e) {
            log.error("save subtask occur xception,{}", e.getMessage());
            return TASK_START_FAILED;
        }
    }

    /**
     * getXnfTestCases.
     *
     * @return
     */
    public static Map<TestCase, VnfParas> getXnfTestCases(String appAddress) {
        Map<TestCase, VnfParas> map = new HashMap<>();
        Set<TestCase> testCaseSet = TaskRunningHandler.getInstance().getTestCaseSet();
        for (TestCase tc : testCaseSet) {
            VnfParas paras = new VnfParas();
            String inputs = tc.getInputs();
            JsonArray jsonArray = new JsonParser().parse(inputs).getAsJsonArray();
            Map<String, String> keyValue = new HashMap<>();
            for (int j = 0; j < jsonArray.size(); j++) {
                JsonElement inputJson = jsonArray.get(j);
                String inputName = inputJson.getAsJsonObject().get("name").getAsString();
                keyValue.put(inputName, appAddress);
            }
            paras.setKeyValue(keyValue);
            map.put(tc, paras);
        }
        return map;
    }

    /**
     * getTaskNo.
     *
     * @return
     */
    private String getTaskNo(String taskNo, String dateStr) {
        StringBuilder stringBuilder = new StringBuilder();
        if (taskNo == null || taskNo.equals("")) {
            stringBuilder.append("MEC").append(dateStr).append("001");
            return stringBuilder.toString();
        }

        // the format of taskNo is MECyyyyMMDD001, the No start from index 11
        int intNumber = Integer.parseInt(taskNo.substring(11));
        intNumber++;
        String number = String.valueOf(intNumber);
        for (int i = 0; i < 3; i++) {
            number = number.length() < 3 ? "0" + number : number;
        }
        stringBuilder.append("MEC").append(dateStr).append(number);
        return stringBuilder.toString();
    }

    /**
     * saveSubTasks.
     */
    public void saveSubTasks(Set<SubTaskBean> set) {
        try {
            for (SubTaskBean bean : set) {
                testCaseMapper.saveSubTask(bean);
            }
        } catch (PersistenceException e) {
            log.error("save SubTasks occur mybatis Exception,{}", e.getMessage());
        }
    }

    /**
     * getSubtasks.
     *
     * @return
     */
    public Either<FormatRespDto, SubTaskListResponse> getSubTasks(String appId, String taskId) {

        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(taskId)) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "appId or taskId is empty."));
        }

        if (!appId.matches(REGEX_UUID) || !taskId.matches(REGEX_UUID)) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "appId or taskId must be UUID Format."));
        }
        TestApp app = testMapper.getAppById(appId);
        TestTask task = testMapper.getTestTaskById(taskId);
        if (app == null || task == null) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "appId or taskId invalid"));
        }
        if (!app.getAppId().equals(task.getAppId())) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST,
                "appid queryed by taskid and input appid, the two appids are inconsistent"));
        }
        try {
            SubTaskListResponse subs = new SubTaskListResponse();
            List<SubTaskBean> subTasks = testCaseMapper.getSubTaskList(taskId);
            subs.setSubTasks(subTasks);
            return Either.right(subs);
        } catch (PersistenceException e) {
            log.error(e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR, "internal exception"));
        }

    }

    /**
     * scanRunningTask.
     *
     * @return
     */
    public void scanRunningTask() {
        if (TaskRunningHandler.getInstance().isTaskNeedLoaded()) {
            doLoadTask();
        } else {
            doScheduled();
        }

    }

    /**
     * load task.
     */
    public void doLoadTask() {
        Map<String, String> taskIdAndStatusMap = new HashMap<>();
        Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap = new HashMap<>();
        loadTask(taskIdAndStatusMap, taskIdAndSubTaskMap);
        TaskRunningHandler.getInstance().setRunningData(taskIdAndStatusMap, taskIdAndSubTaskMap);

    }

    private void doScheduled() {
        Map<String, String> taskIdAndStatusMap = TaskRunningHandler.getInstance().getAppTestTaskIdAndStatusMap();
        Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap = TaskRunningHandler.getInstance().getTaskIdAndSubTaskMap();
        refreshTaskStatusMock(taskIdAndStatusMap, taskIdAndSubTaskMap);
    }

    /**
     * taskIdAndStatusMap.
     */
    public void loadTask(Map<String, String> taskIdAndStatusMap, Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap) {
        try {
            List<String> taskList = testMapper.getAllTaskId();
            for (String taskId : taskList) {
                List<SubTaskBean> runningSubTaskList = testCaseMapper.getSubTaskList(taskId);
                taskIdAndStatusMap.put(taskId, EnumTaskStatus.TASK_STATUS_ENV_PREPARE.getValue());
                taskIdAndSubTaskMap.put(taskId, new HashSet<>(runningSubTaskList));
            }
        } catch (PersistenceException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * refreshTaskStatusMock.
     */
    @Transactional
    public void refreshTaskStatusMock(Map<String, String> taskIdAndStatusMap,
        Map<String, Set<SubTaskBean>> taskIdAndSubTaskMap) {
        Iterator<Entry<String, String>> entries = taskIdAndStatusMap.entrySet().iterator();
        try {
            while (entries.hasNext()) {
                Entry<String, String> entry = entries.next();
                String taskId = entry.getKey();
                if (EnumTaskStatus.TASK_STATUS_COMPLETED.getValue().equals(entry.getValue())) {
                    entries.remove();
                    continue;
                }
                Set<SubTaskBean> subTaskStatusSet = taskIdAndSubTaskMap.get(taskId);
                for (SubTaskBean subTask : subTaskStatusSet) {
                    String status = SubTaskRunningHandler.getInstance().getStatusMock(subTask.getExecutionid());
                    Map<String, Object> map = new HashMap<>();
                    map.put("executionid", subTask.getExecutionid());
                    map.put("status", status);
                    testCaseMapper.updateSubTask(map);
                }
                String taskStatus = SubTaskRunningHandler.getInstance().getTaskStatus(taskId);
                entry.setValue(taskStatus);
                Map<String, Object> taskMap = new HashMap<>();
                if (taskStatus.equals(EnumTaskStatus.TASK_STATUS_COMPLETED.getValue())) {
                    taskMap.put("endtime", new Date());
                }
                taskMap.put("taskid", taskId);
                taskMap.put("status", taskStatus);
                testMapper.updateTestTask(taskMap);

            }
        } catch (PersistenceException e) {
            log.error(e.getMessage());
        }
    }

}
