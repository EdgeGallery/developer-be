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
package org.edgegallery.developer.controller.application.vm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.restful.VncUrlRep;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.ReverseProxyService;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
@RestSchema(schemaId = "VmAppOperation")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "VmAppOperation")
@Validated
public class VMAppOperationCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";
    @Autowired
    private VMAppOperationService VmAppOperationService;
    @Autowired
    private ReverseProxyService reverseProxyService;
    /**
     * instantiate a vm .
     */
    @ApiOperation(value = "instantiate a vm .", response = OperationInfoRep.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OperationInfoRep.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}/launch", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<OperationInfoRep> instantiateVm(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId, HttpServletRequest request) {
        String accessToken = request.getHeader(Consts.ACCESS_TOKEN_STR);
        OperationInfoRep result = VmAppOperationService.instantiateVM(applicationId, vmId, accessToken);
        return ResponseEntity.ok(result);
    }

    /**
     * export a vm image.
     */
    @ApiOperation(value = "export a vm image.", response = OperationInfoRep.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OperationInfoRep.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}/exportimage", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<OperationInfoRep> createVmImage(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId, HttpServletRequest request) {
        String accessToken = request.getHeader(Consts.ACCESS_TOKEN_STR);
        OperationInfoRep result = VmAppOperationService.createVmImage(applicationId, vmId, accessToken);
        return ResponseEntity.ok(result);
    }


    /**
     * upload file.
     */
    @ApiOperation(value = "upload file", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}/file/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> uploadFile(HttpServletRequest request, Chunk chunk,
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) {
        Boolean result = VmAppOperationService.uploadFileToVm(applicationId, vmId, request, chunk);
        return ResponseEntity.ok(result);
    }

    /**
     * merge file.
     */
    @ApiOperation(value = "merge app file", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}/file/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity mergeAppFile(@RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "identifier", required = false) String identifier,
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) {

        return VmAppOperationService.mergeAppFile(applicationId, vmId, fileName, identifier);
    }

    /**
     * get vnc url.
     */
    @ApiOperation(value = "merge app file", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}/vnc", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity getVncUrl(@ApiParam(value = "applicationId", required = true)
                                    @PathVariable("applicationId") String applicationId,
                                    @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
                                    @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId,
                                    HttpServletRequest request) {

        String accessToken = request.getHeader(Consts.ACCESS_TOKEN_STR);
        String vncUrl = reverseProxyService.getVmConsoleUrl(applicationId, vmId,
                AccessUserUtil.getUserId(), accessToken);
        return ResponseEntity.ok(new VncUrlRep(vncUrl));
    }
}
