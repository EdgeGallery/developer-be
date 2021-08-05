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

package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmPackageConfig;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.virtual.VmService;
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
@RestSchema(schemaId = "vm")
@RequestMapping("/mec/developer/v1")
@Api(tags = "vm")
@Validated
public class VmController {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private VmService vmService;

    /**
     * get vm resources information.
     */
    @ApiOperation(value = "get vm resources information", response = VmResource.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmResource.class)
    })
    @RequestMapping(value = "/vmconfig", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VmResource> getVirtualResource() {
        String currUserId = AccessUserUtil.getUserId();
        Either<FormatRespDto, VmResource> either = vmService.getVirtualResource(currUserId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * generate vm package.
     */
    @ApiOperation(value = "create one vm", response = VmPackageConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmPackageConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm-package", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VmPackageConfig> vmPackage(
        @NotNull @ApiParam(value = "VmPackage", required = true) @RequestBody VmPackageConfig vmPackageConfig,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, VmPackageConfig> either = vmService.vmPackage(userId, projectId, vmPackageConfig);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete vm package.
     */
    @ApiOperation(value = "create one vm", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm-package", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteVmPackage(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, Boolean> either = vmService.deleteVmPackage(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get vm package config.
     */
    @ApiOperation(value = "get vm package config", response = VmPackageConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmPackageConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm-package", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VmPackageConfig> getVmPackage(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, VmPackageConfig> either = vmService.getVmPackage(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * create vm.
     */
    @ApiOperation(value = "create one vm", response = VmCreateConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmCreateConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm-create", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VmCreateConfig> createVm(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, VmCreateConfig> either = vmService.createVm(userId, projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get vm create config by projectId.
     */
    @ApiOperation(value = "get vm create config by projectId", response = VmCreateConfig.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmCreateConfig.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<VmCreateConfig>> getCreateVmConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, List<VmCreateConfig>> either = vmService.getCreateVm(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete vm create config by projectId and vmId.
     */
    @ApiOperation(value = "delete vm create config by projectId and vmId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/{vmId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteCreateVmConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format") @ApiParam(value = "projectId")
        @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format") @ApiParam(value = "vmId")
        @PathVariable("vmId") String vmId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = vmService.deleteCreateVm(userId, projectId, vmId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * upload file.
     */
    @ApiOperation(value = "upload file", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/{vmId}/files", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> uploadFile(HttpServletRequest request, Chunk chunk,
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) throws Exception {
        Either<FormatRespDto, Boolean> either = vmService.uploadFileToVm(userId, projectId, vmId, request, chunk);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * merge file.
     */
    @ApiOperation(value = "merge app file", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/{vmId}/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity mergeAppFile(@RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "identifier", required = false) String identifier,
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) throws IOException {

        return vmService.mergeAppFile(userId, projectId, vmId, fileName, identifier);
    }

    /**
     * download vm csar package.
     */
    @ApiOperation(value = "download vm csar package.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/{vmId}/package", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<byte[]> getSampleCode(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format") @ApiParam(value = "projectId")
        @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format") @ApiParam(value = "vmId")
        @PathVariable("vmId") String vmId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId) {
        Either<FormatRespDto, ResponseEntity<byte[]>> either = vmService.downloadVmCsar(userId, projectId, vmId);
        if (either.isRight()) {
            return either.getRight();
        } else {
            return null;
        }
    }

    /**
     * import vm image.
     */
    @ApiOperation(value = "import vm image", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/image", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> importVmImage(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = vmService.importVmImage(userId, projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get  vm image  config.
     */
    @ApiOperation(value = "get  vm image  config by vmId, projectId", response = VmImageConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmImageConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/image", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VmImageConfig> getVmImage(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, VmImageConfig> either = vmService.getVmImage(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete vm image config by projectId and vmId.
     */
    @ApiOperation(value = "delete vm create config by projectId and vmId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/image", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteVmImage(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format") @ApiParam(value = "projectId")
        @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = vmService.deleteVmImage(userId, projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * clean vm deploy env by projectId .
     */
    @ApiOperation(value = "clean vm deploy env by projectId .", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/clean", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> cleanVmDeploy(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format") @ApiParam(value = "projectId")
        @PathVariable("projectId") String projectId, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = vmService.cleanVmDeploy(projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }

}

