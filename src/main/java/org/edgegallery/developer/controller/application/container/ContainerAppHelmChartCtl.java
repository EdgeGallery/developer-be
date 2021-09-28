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
import java.util.List;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
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

@Controller
@RestSchema(schemaId = "helmCharts")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "helmCharts")
@Validated
public class ContainerAppHelmChartCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private ContainerAppHelmChartService containerAppHelmChartService;

    /**
     * upload helm template yaml.
     */
    @ApiOperation(value = "upload helm template yaml", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/helmcharts", method = RequestMethod.POST,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> uploadHelmChartYaml(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile helmTemplateYaml,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @RequestParam("applicationId") String applicationId) {
        return ResponseEntity.ok(containerAppHelmChartService.uploadHelmChartYaml(helmTemplateYaml, applicationId));
    }

    /**
     * get helm chart yaml list.
     */
    @ApiOperation(value = "get helm chart yaml list", response = HelmChart.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmChart.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/helmcharts", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<HelmChart>> getHelmChartList(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @RequestParam("applicationId") String applicationId) {
        return ResponseEntity.ok(containerAppHelmChartService.getHelmChartList(applicationId));
    }

    /**
     * get a helm chart yaml.
     */
    @ApiOperation(value = "get a helm chart yaml.", response = HelmChart.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmChart.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/helmcharts/{id}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<HelmChart> getHelmChartById(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @RequestParam("applicationId") String applicationId,
        @ApiParam(value = "id", required = true) @RequestParam("id") String id) {
        return ResponseEntity.ok(containerAppHelmChartService.getHelmChartById(applicationId, id));
    }

    /**
     * delete helm chart yaml.
     */
    @ApiOperation(value = "delete helm chart yaml.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/helmcharts/{id}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteHelmChartById(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @RequestParam("applicationId") String applicationId,
        @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "id", required = true) @RequestParam("id") String id) {
        return ResponseEntity.ok(containerAppHelmChartService.deleteHelmChartById(applicationId, id));
    }

}
