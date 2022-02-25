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

package org.edgegallery.developer.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.atp.AtpTest;
import org.edgegallery.developer.model.restful.SelectMepHostReq;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.application.AppOperationService;
import org.edgegallery.developer.service.application.factory.AppOperationServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RestSchema(schemaId = "AppOperation")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "AppOperation")
@Validated
public class AppOperationCtl {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private AppOperationServiceFactory appServiceFactory;

    /**
     * select  a hostMep.
     */
    @ApiOperation(value = "select a hostMep.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/action/sel-mephost", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> selectMepHost(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "selectSandbox", required = true) @RequestBody SelectMepHostReq selectSandbox) {
        Boolean result = getAppOperationService(applicationId).selectMepHost(applicationId, selectSandbox);
        return ResponseEntity.ok(result);
    }

    /**
     * clean a application.
     */
    @ApiOperation(value = "clean a application.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/action/clean-env", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> cleanEnv(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId) {
        User user = AccessUserUtil.getUser();
        Boolean result = getAppOperationService(applicationId).cleanEnv(applicationId, user);
        return ResponseEntity.ok(result);
    }

    /**
     * generate a package
     */
    @ApiOperation(value = "generate a package.", response = AppPackage.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppPackage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/action/generate-package", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppPackage> generatePackage(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId) {
        AppPackage appPkg = getAppOperationService(applicationId).generatePackage(applicationId);
        return ResponseEntity.ok(appPkg);
    }

    /**
     * create atp tests.
     *
     * @param applicationId applicationId
     * @return true
     */
    @ApiOperation(value = "create atp test.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/action/atp-tests", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> createAtpTest(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId) {
        User user = AccessUserUtil.getUser();
        return ResponseEntity.ok(getAppOperationService(applicationId).createAtpTest(applicationId, user));
    }

    /**
     * get atp tests by application id.
     *
     * @param applicationId application id
     * @return atp tests list
     */
    @ApiOperation(value = "Get atp test tasks by applicationId.", response = AtpTest.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AtpTest.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/action/atp-tests", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<AtpTest>> getAtpTests(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId) {
        return ResponseEntity.ok(getAppOperationService(applicationId).getAtpTests(applicationId));
    }

    /**
     * get atp test by atpTestId.
     *
     * @param applicationId applicationId
     * @param atpTestId     atpTestId
     * @return true
     */
    @ApiOperation(value = "Get atp test by atpTestId.", response = AtpTest.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AtpTest.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/atpTests/{atpTestId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AtpTest> getAtpTestById(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @Pattern(regexp = REGEX_UUID, message = "atpTestId must be in UUID format")
        @ApiParam(value = "atpTestId", required = true) @PathVariable("atpTestId") String atpTestId) {
        return ResponseEntity.ok(getAppOperationService(applicationId).getAtpTestById(atpTestId));
    }

    /**
     * release app.
     *
     * @param applicationId applicationId
     * @param request  request
     * @return true
     */
    @ApiOperation(value = "release app.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/action/release", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> releaseApp(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        HttpServletRequest request,
        @ApiParam(value = "publishAppDto", required = true) @RequestBody PublishAppReqDto publishAppDto) {
        User user = AccessUserUtil.getUser();
        return ResponseEntity.ok(getAppOperationService(applicationId).releaseApp(applicationId, user, publishAppDto));
    }

    private AppOperationService getAppOperationService(String applicationId) {
        return appServiceFactory.getAppOperationService(applicationId);
    }

}
