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
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.system.MepGetSystemImageReq;
import org.edgegallery.developer.model.system.MepGetSystemImageRes;
import org.edgegallery.developer.model.system.VmSystem;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SystemImageMgmtServiceV2;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@Controller
@RestSchema(schemaId = "systemImageMgmtV2")
@RequestMapping("/mec/developer/v2/system")
@Api(tags = "systemImageV2")
public class SystemImageMgmtControllerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemImageMgmtControllerV2.class);

    @Autowired
    private SystemImageMgmtServiceV2 systemImageMgmtServiceV2;

    /**
     * getSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "get systemImage)", response = MepGetSystemImageRes.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepGetSystemImageRes.class)
    })
    @RequestMapping(value = "/images/list", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepGetSystemImageRes> getSystemImages(
            @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
            @ApiParam(value = "MepGetImages", required = true) @RequestBody MepGetSystemImageReq mepGetSystemImageReq) {
        LOGGER.info("query system image file, userId = {}", userId);
        Either<FormatRespDto, MepGetSystemImageRes> either = systemImageMgmtServiceV2
                .getSystemImages(mepGetSystemImageReq);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * createSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "create systemImage", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class)
    })
    @RequestMapping(value = "/images", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> createSystemImage(
            @ApiParam(value = "MepSystemImage", required = true) @Validated @RequestBody VmSystem vmImage) {
        LOGGER.info("create system image file");
        Either<FormatRespDto, Boolean> either = systemImageMgmtServiceV2.createSystemImage(vmImage);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * updateSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "update systemImage by systemId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class)
    })
    @RequestMapping(value = "/images/{systemId}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> modifySystemImage(@PathVariable("systemId") Integer systemId,
                                                     @Validated @RequestBody VmSystem vmImage) {
        LOGGER.info("update system image file, systemId = {}", systemId);
        Either<FormatRespDto, Boolean> either = systemImageMgmtServiceV2.updateSystemImage(vmImage, systemId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * deleteSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "delete systemImage by systemId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class)
    })
    @RequestMapping(value = "/images/{systemId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteSystemImage(@PathVariable("systemId") Integer systemId) throws Exception {
        LOGGER.info("delete system image file, systemId = {}", systemId);
        Either<FormatRespDto, Boolean> either = systemImageMgmtServiceV2.deleteSystemImage(systemId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * publishSystemImage.
     *
     * @return
     */
    @ApiOperation(value = "publish systemImage by systemId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class)
    })
    @RequestMapping(value = "/images/{systemId}/publish", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> publishSystemImage(@PathVariable("systemId") Integer systemId) throws Exception {
        LOGGER.info("publish system image file, systemId = {}", systemId);
        Either<FormatRespDto, Boolean> either = systemImageMgmtServiceV2.publishSystemImage(systemId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * upload system image.
     */
    @ApiOperation(value = "upload system image", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class)
    })
    @RequestMapping(value = "/images/{systemId}/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity uploadSystemImage(HttpServletRequest request, Chunk chunk,
                                            @ApiParam(value = "systemId", required = true) @PathVariable("systemId") Integer systemId) throws IOException {
        LOGGER.info("upload system image file, systemId = {}", systemId);
        return systemImageMgmtServiceV2.uploadSystemImage(request, chunk, systemId);
    }

    /**
     * cancel upload system image.
     */
    @ApiOperation(value = "cancel upload system image", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class)
    })
    @RequestMapping(value = "/images/{systemId}/upload", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity cancelUploadSystemImage(
            @RequestParam(value = "identifier", required = false) String identifier,
            @ApiParam(value = "systemId", required = true) @PathVariable("systemId") Integer systemId) throws IOException {
        LOGGER.info("cancel upload system image file, systemId = {}", systemId);
        return systemImageMgmtServiceV2.cancelUploadSystemImage(systemId, identifier);
    }

    /**
     * merge system image.
     */
    @ApiOperation(value = "merge system image", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class)
    })
    @RequestMapping(value = "/images/{systemId}/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity mergeSystemImage(@RequestParam(value = "fileName", required = false) String fileName,
                                           @RequestParam(value = "identifier", required = false) String identifier,
                                           @ApiParam(value = "systemId", required = true) @PathVariable("systemId") Integer systemId) throws IOException {
        LOGGER.info("merge system image file, systemId = {}, fileName = {}, identifier = {}", systemId, fileName,
                identifier);
        return systemImageMgmtServiceV2.mergeSystemImage(fileName, identifier, systemId);
    }

    /**
     * download system image.
     */
    @ApiOperation(value = "download system image", response = File.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class)
    })
    @RequestMapping(value = "/images/{systemId}/download", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<byte[]> downloadSystemImage(
            @ApiParam(value = "systemId", required = true) @PathVariable("systemId") Integer systemId) {
        LOGGER.info("download system image file, systemId = {}", systemId);
        return systemImageMgmtServiceV2.downloadSystemImage(systemId);
    }
}
