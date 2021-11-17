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

package org.edgegallery.developer.controller.apppackage;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.util.ResponseDataUtil;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "AppPackage")
@RequestMapping("/mec/developer/v2/apppackages")
@Api(tags = "AppPackage")
@Validated
public class AppPackageCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private AppPackageService appPackageService;

    /**
     * get an package.
     */
    @ApiOperation(value = "Get app package by packageId.", response = AppPackage.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppPackage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = AppPackage.class)
    })
    @RequestMapping(value = "/{packageId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppPackage> getAppPackage(
        @Pattern(regexp = REGEX_UUID, message = "packageId must be in UUID format")
        @ApiParam(value = "packageId", required = true) @PathVariable("packageId") String packageId) {
        return ResponseEntity.ok(appPackageService.getAppPackage(packageId));
    }

    @ApiOperation(value = "Get app package structure", response = AppPkgStructure.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppPkgStructure.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{packageId}/action/get-pkg-structure", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppPkgStructure> getAppPackageStructure(
        @Pattern(regexp = REGEX_UUID, message = "packageId must be in UUID format")
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId) {
        AppPkgStructure structure = appPackageService.getAppPackageStructure(packageId);
        return ResponseEntity.ok(structure);
    }

    @ApiOperation(value = "Get app package file content", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{packageId}/action/get-file-content", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> getAppPackageFileContent(
        @Pattern(regexp = REGEX_UUID, message = "packageId must be in UUID format")
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId,
        @ApiParam(value = "fileName", required = true) @RequestParam(value = "fileName", required = true)
            String fileName) {
        String fileContent = appPackageService.getAppPackageFileContent(packageId, fileName);
        return ResponseEntity.ok(fileContent);
    }

    @ApiOperation(value = "Update app package file content", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{packageId}/action/update-file-content", method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> updateAppPackageFileContent(
        @Pattern(regexp = REGEX_UUID, message = "packageId must be in UUID format")
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId,
        @ApiParam(value = "fileName", required = true) @RequestParam(value = "fileName", required = true)
            String fileName, @NotNull @ApiParam(value = "content", required = true) @RequestBody String content) {
        String result = appPackageService.updateAppPackageFileContent(packageId, fileName, content);
        return ResponseEntity.ok(result);
    }
}
