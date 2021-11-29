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
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.atp.AtpTestTaskMapper;
import org.edgegallery.developer.mapper.application.AppConfigurationMapper;
import org.edgegallery.developer.mapper.application.ApplicationMapper;
import org.edgegallery.developer.mapper.capability.CapabilityGroupMapper;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.mapper.uploadfile.UploadFileMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumApplicationStatus;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.atp.AtpTest;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.model.restful.SelectMepHostReq;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.service.application.AppOperationService;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.util.AppStoreUtil;
import org.edgegallery.developer.util.AtpUtil;
import org.edgegallery.developer.util.FileUtil;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("AppOperationService")
public class AppOperationServiceImpl implements AppOperationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppOperationServiceImpl.class);

    private static final String TEST_TASK_STATUS_WAITING = "waiting";

    private static final String TEST_TASK_STATUS_RUNNING = "running";

    private static final String TEST_TASK_STATUS_SUCCESS = "success";

    private static final String TEST_TASK_STATUS_CREATED = "created";

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private AtpTestTaskMapper atpTestTaskMapper;

    @Autowired
    private UploadFileMapper uploadMapper;

    @Autowired
    private AppConfigurationMapper appConfigurationMapper;

    @Autowired
    private CapabilityGroupMapper capabilityGroupMapper;

    @Autowired
    private CapabilityMapper capabilityMapper;

    @Autowired
    private AppPackageService appPackageService;

    @Override
    public Boolean cleanEnv(String applicationId, User user) {
        return true;
    }

    @Override
    public AppPackage generatePackage(String applicationId) {
        return null;
    }

    @Override
    public Boolean createAtpTest(String applicationId, User user) {
        Application app = applicationMapper.getApplicationById(applicationId);
        checkParamNull(app, "application is empty. applicationId: ".concat(applicationId));

        AppPackage appPkg = appPackageService.getAppPackageByAppId(applicationId);
        checkParamNull(appPkg.getId(), "app package content is empty. applicationId: ".concat(applicationId));

        String filePath = appPkg.queryPkgPath();
        ResponseEntity<String> response = AtpUtil.sendCreatTask2Atp(filePath, user.getToken());
        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();

        AtpTest atpTest = new AtpTest();
        atpTest.setId(jsonObject.get("id").getAsString());
        atpTest.setAppName(null != jsonObject.get("appName") ? jsonObject.get("appName").getAsString() : null);
        atpTest.setStatus(null != jsonObject.get("status") ? jsonObject.get("status").getAsString() : null);
        atpTest.setCreateTime(null != jsonObject.get("createTime") ? jsonObject.get("createTime").getAsString() : null);
        atpTestTaskMapper.createAtpTest(applicationId, atpTest);
        applicationMapper.updateApplicationStatus(applicationId, EnumApplicationStatus.TESTED.toString());
        LOGGER.info("atp status:{}", atpTest.getStatus());
        applicationMapper.updateApplicationStatus(applicationId, EnumApplicationStatus.TESTED.toString());
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
            || TEST_TASK_STATUS_RUNNING.equalsIgnoreCase(atpTestTask.getStatus())
            || TEST_TASK_STATUS_CREATED.equalsIgnoreCase(atpTestTask.getStatus()))
            .forEach(task -> queryAndUpdateTestStatus(task));
        return atpTests;
    }

    @Override
    public AtpTest getAtpTestById(String atpTestId) {
        AtpTest atpTest = atpTestTaskMapper.getAtpTestById(atpTestId);
        checkParamNull(atpTest, "atpTest does not exit. atpTestId: ".concat(atpTestId));
        if (TEST_TASK_STATUS_WAITING.equalsIgnoreCase(atpTest.getStatus()) || TEST_TASK_STATUS_RUNNING
            .equalsIgnoreCase(atpTest.getStatus()) || TEST_TASK_STATUS_CREATED.equalsIgnoreCase(atpTest.getStatus())) {
            queryAndUpdateTestStatus(atpTest);
        }
        return atpTest;
    }

    public void sentTerminateRequestToLcm(String basePath, String userId, String accessToken, String appInstanceId,
        String mepmPackageId, String mecHostIp) {
        // delete Instance
        if (StringUtils.isNotEmpty(appInstanceId)) {
            HttpClientUtil.terminateAppInstance(basePath, appInstanceId, userId, accessToken);
        }
        if (StringUtils.isNotEmpty(mepmPackageId)) {
            // delete hosts
            HttpClientUtil.deleteHost(basePath, userId, accessToken, mepmPackageId, mecHostIp);
            // delete package
            HttpClientUtil.deletePkg(basePath, userId, accessToken, mepmPackageId);
        }
    }

    @Override
    public Boolean releaseApp(String applicationId, User user, PublishAppReqDto publishAppDto) {
        Application app = applicationMapper.getApplicationById(applicationId);
        checkParamNull(app, "application is empty. applicationId: ".concat(applicationId));
        AppPackage appPkg = appPackageService.getAppPackageByAppId(applicationId);
        checkParamNull(appPkg.getId(), "app package content is empty. applicationId: ".concat(applicationId));
        UploadFile iconFile = uploadMapper.getFileById(app.getIconFileId());
        checkParamNull(iconFile, "file icon is empty. iconFileId: ".concat(app.getIconFileId()));
        List<AtpTest> testList = getAtpTests(applicationId);
        checkAtpTestStatus(testList);

        Map<String, Object> map = new HashMap<>();
        map.put("file", new FileSystemResource(new File(appPkg.queryPkgPath())));
        File icon = new File(InitConfigUtil.getWorkSpaceBaseDir() + iconFile.getFilePath());
        File copyIcon = FileUtil.copyFile(icon,iconFile.getFileName());
        LOGGER.info("copy file Name:{}", copyIcon.getName());
        checkFileNull(copyIcon, "copy file failed!");
        map.put("icon", new FileSystemResource(copyIcon));
        map.put("type", app.getType());
        map.put("shortDesc", app.getDescription());
        map.put("affinity", app.getArchitecture());
        map.put("industry", app.getIndustry());
        map.put("testTaskId", testList.get(0).getId());
        ResponseEntity<String> uploadReslut = AppStoreUtil.storeToAppStore(map, user);
        checkInnerParamNull(uploadReslut, "upload app to appstore fail!");

        LOGGER.info("upload appstore result:{}", uploadReslut);
        JsonObject jsonObject = new JsonParser().parse(uploadReslut.getBody()).getAsJsonObject();
        JsonElement appStoreAppId = jsonObject.get("appId");
        JsonElement appStorePackageId = jsonObject.get("packageId");

        checkInnerParamNull(appStoreAppId, "response from upload to appstore does not contain appId");
        checkInnerParamNull(appStorePackageId, "response from upload to appstore does not contain packageId");

        ResponseEntity<String> publishRes = AppStoreUtil
            .publishToAppStore(appStoreAppId.getAsString(), appStorePackageId.getAsString(), user.getToken(),
                publishAppDto);
        checkInnerParamNull(publishRes, "publish app to appstore fail!");
        //release service
        releaseServiceProduced(applicationId, jsonObject);
        applicationMapper.updateApplicationStatus(applicationId, EnumApplicationStatus.RELEASED.toString());
        return true;
    }

    private boolean releaseServiceProduced(String applicationId, JsonObject jsonObject) {
        List<AppServiceProduced> serviceProducedList = appConfigurationMapper.getAllServiceProduced(applicationId);
        if (CollectionUtils.isEmpty(serviceProducedList)) {
            LOGGER.warn("This project is not configured with any services and does not need to be published!");
            return true;
        }
        for (AppServiceProduced serviceProduced : serviceProducedList) {
            Capability capability = new Capability();
            CapabilityGroup group = capabilityGroupMapper.selectByName(serviceProduced.getOneLevelName());
            if (group == null) {
                LOGGER.error("Can not get group {}.", serviceProduced.getOneLevelName());
                throw new DataBaseException("Can not find selected group", ResponseConsts.RET_QUERY_DATA_FAIL);
            }
            fillCapability(serviceProduced, capability, jsonObject, group);
            saveCapability(capability);
        }
        int ret = applicationMapper.updateApplicationStatus(applicationId, EnumApplicationStatus.RELEASED.toString());
        if (ret < 1) {
            LOGGER.error("update application {} status RELEASE failed.", applicationId);
            throw new DataBaseException("update application status failed!", ResponseConsts.RET_UPDATE_DATA_FAIL);
        }
        return true;
    }

    private boolean saveCapability(Capability capability) {
        List<Capability> findedCapabilities = capabilityMapper
            .selectByNameOrNameEn(capability.getName(), capability.getNameEn());
        if (!CollectionUtils.isEmpty(findedCapabilities)) {
            LOGGER.error("The capability name {} has exist.", capability.getName());
            throw new DataBaseException("The capability is exist", ResponseConsts.RET_QUERY_DATA_FAIL);
        }
        int res = capabilityMapper.insert(capability);
        if (res < 1) {
            LOGGER.error("store db to tbl_capability fail!");
            throw new DataBaseException("save capability db fail!", ResponseConsts.RET_CERATE_DATA_FAIL);
        }
        return true;
    }

    private void fillCapability(AppServiceProduced serviceProduced, Capability capability, JsonObject obj,
        CapabilityGroup group) {
        capability.setId(UUID.randomUUID().toString());
        capability.setGroupId(group.getId());
        capability.setName(serviceProduced.getTwoLevelName());
        capability.setNameEn(serviceProduced.getTwoLevelName());
        capability.setVersion(serviceProduced.getVersion());
        capability.setDescription(serviceProduced.getDescription());
        capability.setDescriptionEn(serviceProduced.getDescription());
        JsonElement provider = obj.get("provider");
        if (provider != null) {
            capability.setProvider(provider.getAsString());
        } else {
            capability.setProvider("");
        }
        capability.setProvider("");
        capability.setApiFileId(serviceProduced.getApiFileId());
        capability.setGuideFileId(serviceProduced.getGuideFileId());
        capability.setGuideFileIdEn(serviceProduced.getGuideFileId());
        capability.setUploadTime(new Date().getTime());
        capability.setPort(serviceProduced.getInternalPort());
        capability.setHost(serviceProduced.getServiceName());
        capability.setProtocol(serviceProduced.getProtocol());
        capability.setAppId(obj.get("appId").getAsString());
        capability.setPackageId(obj.get("packageId").getAsString());
        capability.setUserId(AccessUserUtil.getUserId());
        capability.setSelectCount(0);
        capability.setIconFileId(serviceProduced.getIconFileId());
        capability.setAuthor(serviceProduced.getAuthor());
        capability.setExperienceUrl(serviceProduced.getExperienceUrl());
    }

    private void checkAtpTestStatus(List<AtpTest> atpTests) {
        checkParamNull(atpTests, "atpTest field is null in application.");
        AtpTest atpTest = atpTests.get(0);
        if (!TEST_TASK_STATUS_SUCCESS.equalsIgnoreCase(atpTest.getStatus())) {
            String msg = "atp test status is ".concat(atpTest.getStatus()).concat(", can not be released.");
            LOGGER.error(msg);
            throw new IllegalRequestException(msg, ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
    }

    private <T> void checkInnerParamNull(T innerParam, String msg) {
        if (null == innerParam) {
            LOGGER.error(msg);
            throw new IllegalRequestException(msg, ResponseConsts.RET_REQUEST_PARAM_EMPTY);
        }
    }

    private <T> void checkParamNull(T param, String msg) {
        if (null == param) {
            LOGGER.error(msg);
            throw new EntityNotFoundException(msg, ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
    }

    private <T> void checkFileNull(T param, String msg) {
        if (null == param) {
            LOGGER.error(msg);
            throw new FileOperateException(msg, ResponseConsts.RET_COPY_FILE_FAIL);
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
