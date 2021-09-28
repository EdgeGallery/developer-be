/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.AppReleaseService;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "apprelease")
@RequestMapping("/mec/developer/v1/apprelease")
public class AppReleaseController {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private AppReleaseService releaseService;

    @ApiOperation(value = "get csar pkg structure", response = AppPkgStructure.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppPkgStructure.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/{csarId}/action/get-pkg-structure", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<AppPkgStructure> getCsarPkgStructure(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable(value = "projectId", required = true)
            String projectId, @Pattern(regexp = REGEX_UUID, message = "csarId must be in UUID format")
        @ApiParam(value = "csarId", required = true) @PathVariable(value = "csarId", required = true) String csarId) {
        Either<FormatRespDto, AppPkgStructure> either = releaseService.getPkgStruById(projectId, csarId);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "get csar pkg file content", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/get-pkg-content", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> getPkgContent(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable(value = "projectId", required = true)
            String projectId,
        @ApiParam(value = "fileName", required = true) @RequestParam(value = "fileName", required = true)
            String fileName) {
        Either<FormatRespDto, String> either = releaseService.getPkgContentByFileName(projectId, fileName);
        return ResponseDataUtil.buildResponse(either);
    }

}
