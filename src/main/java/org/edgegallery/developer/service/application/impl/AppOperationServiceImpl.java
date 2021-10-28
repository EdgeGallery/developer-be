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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spencerwi.either.Either;
import java.util.List;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.mapper.AtpTestTaskMapper;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.atp.AtpResultInfo;
import org.edgegallery.developer.model.atpTestTask.AtpTestTask;
import org.edgegallery.developer.model.restful.SelectMepHostReq;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.AppOperationService;
import org.edgegallery.developer.util.AtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("AppOperationService")
public class AppOperationServiceImpl implements AppOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppOperationServiceImpl.class);

    private static final String TEST_TASK_STATUS_WAITING = "waiting";

    private static final String TEST_TASK_STATUS_RUNNING = "running";

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private AtpTestTaskMapper atpTestTaskMapper;

    @Override
    public Boolean cleanEnv(String applicationId) {
        return null;
    }

    @Override
    public AppPackage generatePackage(String applicationId) {
        return null;
    }

    @Override
    public Boolean commitTest(String applicationId, String accessToken) {
        Application app = applicationMapper.getApplicationById(applicationId);
        checkParamNull(app, "application is empty. applicationId: ".concat(applicationId));

        AppPackage appPkg = app.getAppPackage();
        checkParamNull(appPkg.getId(), "app package content is empty. applicationId: ".concat(applicationId));

        String filePath = appPkg.getPkgPath();
        ResponseEntity<String> response = AtpUtil.sendCreatTask2Atp(filePath, accessToken);
        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();

        AtpTestTask atpTestTask = new AtpTestTask();
        atpTestTask.setId(jsonObject.get("id").getAsString());
        atpTestTask.setAppName(null != jsonObject.get("appName") ? jsonObject.get("appName").getAsString() : null);
        atpTestTask.setStatus(null != jsonObject.get("status") ? jsonObject.get("status").getAsString() : null);
        atpTestTask
            .setCreateTime(null != jsonObject.get("createTime") ? jsonObject.get("createTime").getAsString() : null);
        atpTestTaskMapper.createAtpTestTask(applicationId, atpTestTask);
        LOGGER.info("atp status:{}", atpTestTask.getStatus());
        return true;
    }

    @Override
    public Boolean selectMepHost(String applicationId, SelectMepHostReq selectMepHostReq) {
        int res = applicationMapper.modifyMepHostById(applicationId, selectMepHostReq.getMepHostId());
        if (res < 1) {
            LOGGER.error("modify mep host  of application {} fail", applicationId);
            throw new DataBaseException("modify mep host of application fail", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    @Override
    public List<AtpTestTask> selectAtpTasks(String applicationId) {
        List<AtpTestTask> atpTestTasks = atpTestTaskMapper.getTestTasksByAppId(applicationId);
        checkParamNull(atpTestTasks, "atpTestTasks do not exit. applicationId: ".concat(applicationId));
        atpTestTasks.stream().filter(atpTestTask -> TEST_TASK_STATUS_WAITING.equalsIgnoreCase(atpTestTask.getStatus())
            || TEST_TASK_STATUS_RUNNING.equalsIgnoreCase(atpTestTask.getStatus()))
            .forEach(task -> queryAndUpdateTestStatus(task));
        return atpTestTasks;
    }

    @Override
    public AtpTestTask selectAtpTasksById(String atpTestId) {
        AtpTestTask atpTestTask = atpTestTaskMapper.getTestTaskById(atpTestId);
        checkParamNull(atpTestTask, "atpTestTask does not exit. atpTestId: ".concat(atpTestId));
        if (TEST_TASK_STATUS_WAITING.equalsIgnoreCase(atpTestTask.getStatus()) || TEST_TASK_STATUS_RUNNING
            .equalsIgnoreCase(atpTestTask.getStatus())) {
            queryAndUpdateTestStatus(atpTestTask);
        }
        return atpTestTask;
    }

    private <T> void checkParamNull(T param, String msg) {
        if (null == param) {
            LOGGER.error(msg);
            throw new EntityNotFoundException(msg, ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }

    private void queryAndUpdateTestStatus(AtpTestTask task) {
        String newStatus = AtpUtil.getTaskStatusFromAtp(task.getId());
        if (!task.getStatus().equalsIgnoreCase(newStatus)) {
            task.setStatus(newStatus);
            atpTestTaskMapper.updateAtpTaskStatus(task);
        }
    }
}
