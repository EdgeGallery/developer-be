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

import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.ibatis.exceptions.PersistenceException;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.mapper.AppFunctionMapper;
import org.edgegallery.developer.mapper.TestAppMapper;
import org.edgegallery.developer.model.AppPackageBasicInfo;
import org.edgegallery.developer.model.TestApp;
import org.edgegallery.developer.model.TestTask;
import org.edgegallery.developer.request.AppRequestParam;
import org.edgegallery.developer.request.TaskRequestParam;
import org.edgegallery.developer.response.AppTagsResponse;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.TestTaskListResponse;
import org.edgegallery.developer.util.AppUtil;
import org.edgegallery.developer.util.BusinessConfigUtil;
import org.edgegallery.developer.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("testAppService")
public class TestAppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAppService.class);

    @Autowired
    private TestAppMapper testAppMapper;

    @Autowired
    private AppFunctionMapper appFunctionMapper;

    @Autowired
    private UtilsService utilsService;

    /**
     * upload app.
     */
    @Transactional
    public Either<FormatRespDto, TestApp> upload(AppRequestParam app) {
        boolean result = AppRequestParam.checkApp(app);
        if (!result) {
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "app param is illegal"));
        }
        // upload appFile and logoFile
        File appDir = new File(InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getAppPath());
        if (!appDir.exists()) {
            boolean isSuccess = appDir.mkdirs();
            if (!isSuccess) {
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "make app dir failed"));
            }

        }
        MultipartFile appFile = app.getAppFile();
        MultipartFile logoFile = app.getLogoFile();
        try {
            appFile.transferTo(new File(InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getAppPath() + appFile
                .getOriginalFilename()));
            logoFile.transferTo(new File(
                InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getAppPath() + logoFile
                    .getOriginalFilename()));
        } catch (IllegalStateException | IOException e) {
            LOGGER.error("upload app or it's logo file,occur {}", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                    Consts.RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR));

        }
        return addApp(app);

    }

    private Either<FormatRespDto, TestApp> addApp(AppRequestParam appParam) {
        try {
            TestApp app = new TestApp();
            MultipartFile appFile = appParam.getAppFile();
            AppPackageBasicInfo appPkg = AppUtil.getPackageBasicInfo(
                InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getAppPath() + appFile.getOriginalFilename());
            app.setAppName(appPkg.getAppname());
            app.setAppVersion(appPkg.getVersion());

            app.setAffinity(appParam.getAffinity());
            app.setIndustry(appParam.getIndustry());
            // String fileName
            MultipartFile logoFile = appParam.getLogoFile();
            Date uploadTime = new Date();
            app.setAppId(appParam.getAppId());
            app.setAppFile(
                InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getAppPath() + appFile.getOriginalFilename());
            app.setLogoFile(InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getAppPath() + logoFile
                .getOriginalFilename());
            app.setUploadTime(uploadTime);
            app.setUserId(appParam.getUserId());
            app.setType(appParam.getType());
            app.setAppDesc(appParam.getAppDesc());
            testAppMapper.uploadApp(app);
            // query App
            TestApp queryApp = testAppMapper.getAppByUploadTime();
            return Either.right(queryApp);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("upload app occur exception", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "no enough parameters"));
        } catch (PersistenceException e) {
            LOGGER.error("upload app occur exception", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                    Consts.RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * getTaskByParam.
     *
     * @return
     */
    public Either<FormatRespDto, TestTaskListResponse> getTaskByParam(String appName, String status, String beginTime,
        String endTime, String userId) {
        TaskRequestParam task = new TaskRequestParam();
        task.setUserId(userId);
        if (appName != null && !appName.equals("")) {
            task.setAppName("%" + appName + "%");
        }
        if (status != null && !status.equals("")) {
            task.setStatus(status);
        }
        if (beginTime != null && !beginTime.equals("")) {
            task.setBeginTime(beginTime);
        }
        if (endTime != null && !endTime.equals("")) {
            task.setEndTime(endTime);
        }
        try {
            TestTaskListResponse tasks = new TestTaskListResponse();
            List<TestTask> list = testAppMapper.getTaskByParam(task);
            tasks.setTasks(list);
            return Either.right(tasks);
        } catch (PersistenceException e) {
            LOGGER.error("query test task,occur exception", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                    Consts.RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * getTagList.
     *
     * @return
     */
    public Either<FormatRespDto, AppTagsResponse> getTagList() {
        try {
            AppTagsResponse tags = new AppTagsResponse();
            List<String> appFuncList = appFunctionMapper.getAppTagList();
            tags.setTags(appFuncList);
            return Either.right(tags);
        } catch (PersistenceException e) {
            LOGGER.error("get app tag list,occur exception", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                    Consts.RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * uploadToAPPStore.
     */
    @Transactional
    public Either<FormatRespDto, String> uploadToAppStore(String appId, String userId, String userName, String token) {
        if (appId == null || appId.equals("")) {
            LOGGER.error("uplaod app to appstore,miss param appId");
            return Either
                .left(new FormatRespDto(Response.Status.BAD_REQUEST, "uplaod app to appstore,miss param taskId"));
        }
        try {
            TestApp app = testAppMapper.getAppById(appId);
            if (app == null) {
                return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "appId invalid"));
            }
            Map<String, Object> map = new HashMap<>();
            map.put("file", new FileSystemResource(app.getAppFile()));
            map.put("icon", new FileSystemResource(app.getLogoFile()));
            map.put("type", app.getType());
            map.put("shortDesc", app.getAppDesc());
            map.put("affinity", app.getAffinity());
            map.put("industry", app.getIndustry());
            return utilsService.storeToAppStore(map, userId, userName, token);
        } catch (PersistenceException e) {
            LOGGER.error("upload app to appstore with error", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.INTERNAL_SERVER_ERROR,
                    Consts.RESPONSE_MESSAGE_INTERNAL_SERVER_ERROR));
        }
    }

}
