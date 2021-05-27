package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.*;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.vm.VmSystem;
import org.edgegallery.developer.model.workspace.MepGetSystemImageReq;
import org.edgegallery.developer.model.workspace.MepGetSystemImageRes;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SystemImageMgmtService;
import org.edgegallery.developer.util.ResponseDataUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RestSchema(schemaId = "systemImageMgmt")
@RequestMapping("/mec/developer/v1/system")
@Api(tags = "systemImage")
public class SystemImageMgmtController {


    @Autowired
    private SystemImageMgmtService systemImageMgmtService;

    /**
     * getSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "get systemImage)", response = MepGetSystemImageRes.class )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepGetSystemImageRes.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/images/list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepGetSystemImageRes> getSystemImages(
            @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
            @ApiParam(value = "MepGetImages", required = true) @RequestBody MepGetSystemImageReq mepGetSystemImageReq) {
        Either<FormatRespDto, MepGetSystemImageRes> either = systemImageMgmtService.getSystemImages(mepGetSystemImageReq);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * createSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "create systemImage", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/images", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> createSystemImage(
            @ApiParam(value = "MepSystemImage", required = true) @Validated @RequestBody VmSystem vmImage) {
        Either<FormatRespDto, Boolean> either = systemImageMgmtService.createSystemImage(vmImage);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modifySystemImage.
     *
     * @return
     */
    @ApiOperation(value = "update systemImage by systemId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/images/{systemId}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> modifySystemImage(
            @PathVariable("systemId") Integer systemId,
            @Validated @RequestBody VmSystem vmImage) {
        Either<FormatRespDto, Boolean> either = systemImageMgmtService.updateSystemImage(vmImage, systemId);
        return ResponseDataUtil.buildResponse(either);
    }


    /**
     * deleteSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "delete systemImage by systemId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/images/{systemId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteSystemImage(@PathVariable("systemId")
                                                         Integer systemId) {
        Either<FormatRespDto, Boolean> either = systemImageMgmtService.deleteSystemImage(systemId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * publishSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "publish systemImage by systemId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/images/{systemId}/publish", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> publishSystemImage(
            @PathVariable("systemId") Integer systemId) {
        Either<FormatRespDto, Boolean> either = systemImageMgmtService.publishSystemImage(systemId);
        return ResponseDataUtil.buildResponse(either);
    }
}
