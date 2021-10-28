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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.mapper.AtpTestTaskMapper;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.atpTestTask.AtpTest;
import org.edgegallery.developer.model.restful.SelectMepHostReq;
import org.edgegallery.developer.service.application.AppOperationService;
import org.edgegallery.developer.util.AtpUtil;
import org.edgegallery.developer.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    public Boolean cleanEnv(String applicationId, String accessToken) {
        return true;
    }

    @Override
    public AppPackage generatePackage(String applicationId) {
        return null;
    }

    @Override
    public Boolean createAtpTest(String applicationId, String accessToken) {
        Application app = applicationMapper.getApplicationById(applicationId);
        checkParamNull(app, "application is empty. applicationId: ".concat(applicationId));

        AppPackage appPkg = app.getAppPackage();
        checkParamNull(appPkg.getId(), "app package content is empty. applicationId: ".concat(applicationId));

        String filePath = appPkg.getPkgPath();
        ResponseEntity<String> response = AtpUtil.sendCreatTask2Atp(filePath, accessToken);
        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();

        AtpTest atpTest = new AtpTest();
        atpTest.setId(jsonObject.get("id").getAsString());
        atpTest.setAppName(null != jsonObject.get("appName") ? jsonObject.get("appName").getAsString() : null);
        atpTest.setStatus(null != jsonObject.get("status") ? jsonObject.get("status").getAsString() : null);
        atpTest.setCreateTime(null != jsonObject.get("createTime") ? jsonObject.get("createTime").getAsString() : null);
        atpTestTaskMapper.createAtpTest(applicationId, atpTest);
        LOGGER.info("atp status:{}", atpTest.getStatus());
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
    public List<AtpTest> getAtpTests(String applicationId) {
        List<AtpTest> atpTests = atpTestTaskMapper.getAtpTests(applicationId);
        checkParamNull(atpTests, "atpTests do not exit. applicationId: ".concat(applicationId));
        atpTests.stream().filter(atpTestTask -> TEST_TASK_STATUS_WAITING.equalsIgnoreCase(atpTestTask.getStatus())
            || TEST_TASK_STATUS_RUNNING.equalsIgnoreCase(atpTestTask.getStatus()))
            .forEach(task -> queryAndUpdateTestStatus(task));
        return atpTests;
    }

    @Override
    public AtpTest getAtpTestById(String atpTestId) {
        AtpTest atpTest = atpTestTaskMapper.getAtpTestById(atpTestId);
        checkParamNull(atpTest, "atpTest does not exit. atpTestId: ".concat(atpTestId));
        if (TEST_TASK_STATUS_WAITING.equalsIgnoreCase(atpTest.getStatus()) || TEST_TASK_STATUS_RUNNING
            .equalsIgnoreCase(atpTest.getStatus())) {
            queryAndUpdateTestStatus(atpTest);
        }
        return atpTest;
    }

    public void sentTerminateRequestToLcm(String basePath, String accessToken, String appInstanceId,
        String mepmPackageId, String mecHostIp) {
        String userId = AccessUserUtil.getUserId();
        // delete Instance
        if (StringUtils.isNotEmpty(appInstanceId)) {
            HttpClientUtil.terminateAppInstance(basePath, appInstanceId, AccessUserUtil.getUserId(), accessToken);
        }
        if (StringUtils.isNotEmpty(mepmPackageId)) {
            // delete hosts
            HttpClientUtil.deleteHost(basePath, userId, accessToken, mepmPackageId, mecHostIp);
            // delete package
            HttpClientUtil.deletePkg(basePath, userId, accessToken, mepmPackageId);
        }
    }

    private <T> void checkParamNull(T param, String msg) {
        if (null == param) {
            LOGGER.error(msg);
            throw new EntityNotFoundException(msg, ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }

    private void queryAndUpdateTestStatus(AtpTest task) {
        String newStatus = AtpUtil.getTaskStatusFromAtp(task.getId());
        if (!task.getStatus().equalsIgnoreCase(newStatus)) {
            task.setStatus(newStatus);
            atpTestTaskMapper.updateAtpTestStatus(task);
        }
    }
}
