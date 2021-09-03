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
package org.edgegallery.developer.controller.application.container;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.spencerwi.either.Either;
@Controller
@RestSchema(schemaId = "helmChart")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "helmChart")
@Validated
public class ContainerAppHelmChartCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private ContainerAppHelmChartService containerAppHelmChartService;
    /**
     * upload helm template yaml.
     */
    @ApiOperation(value = "upload helm template yaml", response = HelmTemplateYamlRespDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmTemplateYamlRespDto.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/helmchart", method = RequestMethod.POST,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<HelmTemplateYamlRespDto> uploadHelmTemplateYaml(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile helmTemplateYaml,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @RequestParam("projectId") String projectId,
        @ApiParam(value = "configType", required = true) @RequestParam("configType") String configType)
        throws IOException {
        Either<FormatRespDto, HelmTemplateYamlRespDto> either = containerAppHelmChartService
            .uploadHelmTemplateYaml(helmTemplateYaml, userId, projectId, configType);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get helm template yaml list.
     */
    @ApiOperation(value = "get helm template yaml list", response = List.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/helmchart", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<HelmTemplateYamlRespDto>> getHelmTemplateYamlList(
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @RequestParam("projectId") String projectId) {
        Either<FormatRespDto, List<HelmTemplateYamlRespDto>> either = containerAppHelmChartService
            .getHelmTemplateYamlList(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete helm template yaml.
     */
    @ApiOperation(value = "delete helm template yaml", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/helm-template-yaml", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> deleteHelmTemplateYamlByFileId(
        @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @RequestParam("fileId") String fileId) {
        Either<FormatRespDto, String> either = containerAppHelmChartService.deleteHelmTemplateYamlByFileId(fileId);
        return ResponseDataUtil.buildResponse(either);
    }

}
