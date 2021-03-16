package org.edgegallery.developer.controller;


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
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.vm.VmResource;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.UploadedFile;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.spencerwi.either.Either;

@Controller
@RestSchema(schemaId = "vm")
@RequestMapping("/mec/developer/v1")
@Api(tags = "vm")
@Validated
public class VmController {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

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
    public ResponseEntity<VmResource> getVirtualResource( ){
        Either<FormatRespDto, VmResource> either = vmService
            .getVirtualResource();
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
    @RequestMapping(value = "/projects/{projectId}/vm-create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<VmCreateConfig> createVm(
        @NotNull @ApiParam(value = "VmConfig", required = true) @RequestBody VmCreateConfig vmCreateConfig,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, VmCreateConfig> either = vmService.createVm(userId, projectId, vmCreateConfig,token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get vm create config by projectId.
     */
    @ApiOperation(value = "get vm create config by projectId", response = VmCreateConfig.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmCreateConfig.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId") @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId") @PathVariable("vmId") String vmId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId") @RequestParam("userId") String userId,
        HttpServletRequest request) {
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
    @RequestMapping(value = "/projects/{projectId}/vm/{vmId}/files", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> uploadFile(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile uploadFile,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId", required = true) @PathVariable("vmId") String vmId) throws Exception {
        Either<FormatRespDto, Boolean> either = vmService.uploadFileToVm(userId, projectId, vmId, uploadFile);
        return ResponseDataUtil.buildResponse(either);

    }

    /**
     * download vm csar package.
     */
    @ApiOperation(value = "download vm csar package.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/projects/{projectId}/vm/{vmId}/package", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<byte[]> getSampleCode(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId") @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "vmId must be in UUID format")
        @ApiParam(value = "vmId") @PathVariable("vmId") String vmId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId") @RequestParam("userId") String userId) {
        Either<FormatRespDto, ResponseEntity<byte[]>> either = vmService.downloadVmCsar(userId, projectId,vmId);
        if (either.isRight()) {
            return either.getRight();
        } else {
            return null;
        }
    }

    /**
     * import vm image
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
     * get  vm image  config
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
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId") @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId") @RequestParam("userId") String userId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = vmService.deleteVmImage(userId, projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }





}

