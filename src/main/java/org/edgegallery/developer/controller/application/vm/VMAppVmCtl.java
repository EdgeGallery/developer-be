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
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
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
import com.spencerwi.either.Either;
@Controller
@RestSchema(schemaId = "VMAppVm")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "VMAppVm")
@Validated
public class VMAppVmCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";
    @Autowired
    private VMAppVmService vmAppVmService;
    /**
     * create a vm .
     */
    @ApiOperation(value = "create a vm.", response = VirtualMachine.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VirtualMachine.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VirtualMachine> createVm(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "VirtualMachine", required = true) @RequestBody VirtualMachine virtualMachine) {
        Either<FormatRespDto, VirtualMachine> either = vmAppVmService.createVm(applicationId, virtualMachine);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all vms.
     */
    @ApiOperation(value = "get all vms.", response = VirtualMachine.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VirtualMachine.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<VirtualMachine>> getAllVm(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId) {
        Either<FormatRespDto, List<VirtualMachine>> either = vmAppVmService.getAllVm(applicationId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get a vm.
     */
    @ApiOperation(value = "get a vm.", response = VirtualMachine.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VirtualMachine.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VirtualMachine> getVm(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) {
        Either<FormatRespDto, VirtualMachine> either = vmAppVmService.getVm(applicationId, vmId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modify a vm .
     */
    @ApiOperation(value = "modify a vm.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyVm(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId,
        @NotNull @ApiParam(value = "VirtualMachine", required = true) @RequestBody VirtualMachine virtualMachine) {
        Either<FormatRespDto, Boolean> either = vmAppVmService.modifyVm(applicationId, vmId, virtualMachine);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete a vm.
     */
    @ApiOperation(value = "delete a vm.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}/vms/{vmId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteVm(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) {
        Either<FormatRespDto, Boolean> either = vmAppVmService.deleteVm(applicationId, vmId);
        return ResponseDataUtil.buildResponse(either);
    }

}