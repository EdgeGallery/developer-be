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

package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.TestApp;
import org.edgegallery.developer.request.AppRequestParam;
import org.edgegallery.developer.response.AppTagsResponse;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.SubTaskListResponse;
import org.edgegallery.developer.response.TestTaskListResponse;
import org.edgegallery.developer.service.TestAppService;
import org.edgegallery.developer.service.TestCaseService;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "testapp")
@RequestMapping(value = "/mec/developer/v1/apps")
@CrossOrigin(allowedHeaders = "*")
@Api(tags = "App")
public class TestAppController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    private static final int MAX_SHORT_STRING_LENGTH = 32;

    private static final int MAX_COMMON_STRING_LENGTH = 255;

    private static final int MAX_DETAILS_STRING_LENGTH = 1024;

    @Autowired
    private TestAppService testAppService;

    @Autowired
    private TestCaseService testCaseService;

    /**
     * upload app package and apply test.
     *
     * @return
     */
    @ApiOperation(value = "upload app to server", response = TestApp.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TestApp.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<TestApp> upload(
        @ApiParam(value = "appfile", required = true) @RequestPart("appFile") MultipartFile appFile,
        @ApiParam(value = "logoFile", required = true) @RequestPart("logoFile") MultipartFile logoFile,
        @ApiParam(value = "app main function", required = true)
        @RequestParam("affinity") String affinity,
        @ApiParam(value = "industry", required = true)
        @RequestParam("industry") String industry,
        @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of type cannot exceed 255")
        @ApiParam(value = "app type", required = true) @RequestParam("type") String type,
        @Length(max = MAX_DETAILS_STRING_LENGTH, message = "Length of appDesc cannot exceed 1024")
        @ApiParam(value = "app description", required = true) @RequestParam("appDesc") String appDesc,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "app author", required = true) @RequestParam("userId") String userId) {
        // AppRequestParam: all the params of upload
        AppRequestParam app = new AppRequestParam();
        app.setAppId(UUID.randomUUID().toString());
        app.setAppFile(appFile);
        app.setLogoFile(logoFile);
        app.setAffinity(affinity);
        app.setIndustry(industry);
        app.setType(type);
        app.setAppDesc(appDesc);
        app.setUserId(userId);
        // upload app
        Either<FormatRespDto, TestApp> either = testAppService.upload(app);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all test task.
     *
     * @return
     */
    @ApiOperation(value = "query test task list", response = TestTaskListResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TestTaskListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<TestTaskListResponse> getTaskByParam(
        @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of app name cannot exceed 255")
        @ApiParam(value = "app name", required = false) @RequestParam(value = "appName", required = false)
            String appName, @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of app name cannot exceed 255")
        @ApiParam(value = "test status", required = false) @RequestParam(value = "status", required = false) String
        status,
        @Length(max = MAX_SHORT_STRING_LENGTH, message = "Length of beginTime cannot exceed 32")
        @ApiParam(value = "test begin Time", required = false) @RequestParam(value = "beginTime", required = false)
            String beginTime, @Length(max = MAX_SHORT_STRING_LENGTH, message = "Length of endTime cannot exceed 32")
        @ApiParam(value = "test end Time", required = false) @RequestParam(value = "endTime", required = false)
        String endTime, @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "app author", required = true) @RequestParam(value = "userId", required = true) String userId
    ) {
        // get all tasks
        Either<FormatRespDto, TestTaskListResponse> either = testAppService.getTaskByParam(appName, status, beginTime,
            endTime, userId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get app function tag list.
     *
     * @return
     */
    @ApiOperation(value = "query apptag list", response = AppTagsResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppTagsResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/tags", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<AppTagsResponse> getTagList() {
        // get all app tags
        Either<FormatRespDto, AppTagsResponse> either = testAppService.getTagList();
        return ResponseDataUtil.buildResponse(either);

    }

    /**
     * upload app to appstore.
     *
     * @return
     */
    @ApiOperation(value = "upload the app that completed the test task to the appstore", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{appId}/action/upload", method = RequestMethod.POST,
        produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> uploadToAppStore(
        @Pattern(regexp = REGEX_UUID, message = "appId must be in UUID format") @PathVariable("appId") String appId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @RequestParam(value = "userId", required = true) String userId,
        @RequestParam(value = "userName", required = true) String userName,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, String> either = testAppService.uploadToAppStore(appId, userId, userName, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * start test task.
     *
     * @return
     */
    @ApiOperation(value = "start to run testcase(task and subtask)")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "appId", paramType = "path", value = "appId", dataType = "string", required = true),
        @ApiImplicitParam(name = "userId", paramType = "query", value = "userId", dataType = "string", required = true)
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{appId}/action/start-test", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> startTest(
        @Pattern(regexp = REGEX_UUID, message = "appId must be in UUID format") @PathVariable("appId") String appId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @RequestParam(value = "userId", required = true) String userId) {
        Either<FormatRespDto, Boolean> either = testCaseService.startToTest(appId, userId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get subtask list.
     */
    @ApiOperation(value = "query all subtask", response = SubTaskListResponse.class)
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "appId", paramType = "path", value = "appId", dataType = "string", required = true),
        @ApiImplicitParam(name = "taskId", paramType = "path", value = "taskId", dataType = "string", required = true)
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = SubTaskListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{appId}/task/{taskId}/subtasks", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<SubTaskListResponse> subtasks(
        @Pattern(regexp = REGEX_UUID, message = "appId must be in UUID format") @PathVariable("appId") String appId,
        @Pattern(regexp = REGEX_UUID, message = "taskId must be in UUID format") @PathVariable("taskId")
            String taskId) {
        // get all subtasks
        Either<FormatRespDto, SubTaskListResponse> either = testCaseService.getSubTasks(appId, taskId);
        return ResponseDataUtil.buildResponse(either);
    }
}
