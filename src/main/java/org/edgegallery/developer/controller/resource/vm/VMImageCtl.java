/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.controller.resource.vm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.restful.VMImageReq;
import org.edgegallery.developer.model.restful.VMImageRes;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.recource.vm.VMImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
@RestSchema(schemaId = "vmImage")
@RequestMapping("/mec/developer/v2/vmimages")
@Api(tags = "vmImage")
public class VMImageCtl {

    private static final Logger LOGGER = LoggerFactory.getLogger(VMImageCtl.class);

    @Autowired
    private VMImageService vmImageService;

    /**
     * get vm image.
     *
     * @return
     */
    @ApiOperation(value = "get vm image)", response = VMImageRes.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VMImageRes.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/action/get-list", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<VMImageRes> getVmImages(
        @ApiParam(value = "VmImageReq", required = true) @RequestBody VMImageReq vmImageReq) {
        VMImageRes either = vmImageService.getVmImages(vmImageReq);
        return ResponseEntity.ok(either);
    }

    /**
     * get vm image by id.
     *
     * @return
     */
    @ApiOperation(value = "get vm image by id.", response = VMImage.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VMImage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<VMImage> getVmImagesById(@PathVariable("imageId") Integer imageId) {
        return ResponseEntity.ok(vmImageService.getVmImageById(imageId));
    }

    /**
     * create vm image.
     *
     * @return
     */
    @ApiOperation(value = "create vm image.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> createVmImage(
        @ApiParam(value = "VmImage", required = true) @Validated @RequestBody VMImage vmImage) {
        LOGGER.info("create vm image file");
        return ResponseEntity.ok(vmImageService.createVmImage(vmImage));
    }

    /**
     * modify vm image.
     *
     * @return
     */
    @ApiOperation(value = "modify vm image. by imageId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> modifyVmImage(@PathVariable("imageId") Integer imageId,
        @Validated @RequestBody VMImage vmImage) {
        LOGGER.info("update vm image file, imageId = {}", imageId);
        return ResponseEntity.ok(vmImageService.updateVmImage(vmImage, imageId));
    }

    /**
     * delete vm image.
     *
     * @return
     */
    @ApiOperation(value = "delete vm image. by imageId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteVmImage(@PathVariable("imageId") Integer imageId) {
        LOGGER.info("delete vm image file, imageId = {}", imageId);
        return ResponseEntity.ok(vmImageService.deleteVmImage(imageId));
    }

    /**
     * publish vm image.
     *
     * @return
     */
    @ApiOperation(value = "publish vm image. by imageId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/publish", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> publishVmImage(@PathVariable("imageId") Integer imageId) {
        LOGGER.info("publish vm image file, imageId = {}", imageId);
        return ResponseEntity.ok(vmImageService.publishVmImage(imageId));
    }

    /**
     * reset image status.
     *
     * @return
     */
    @ApiOperation(value = "reset image status", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/reset", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> resetImageStatus(@PathVariable("imageId") Integer imageId) {
        LOGGER.info("reset vm image status, imageId = {}", imageId);
        return ResponseEntity.ok(vmImageService.resetImageStatus(imageId));
    }

    /**
     * upload vm image.
     */
    @ApiOperation(value = "upload vm image.", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity uploadVmImage(HttpServletRequest request, Chunk chunk,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") Integer imageId) {
        LOGGER.info("upload vm image file, imageId = {}", imageId);
        return vmImageService.uploadVmImage(request, chunk, imageId);
    }

    /**
     * check chunk for upload vm image.
     */
    @ApiOperation(value = "check chunk for upload vm image.", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/upload", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity checkChunkForUploadVmImage(
        @RequestParam(value = "identifier", required = false) String identifier,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") Integer imageId) {
        LOGGER.info("check chunk for upload vm image file, imageId = {}, identifier = {}", imageId, identifier);
        return ResponseEntity.ok(vmImageService.checkUploadedChunks(imageId, identifier));
    }

    /**
     * cancel upload vm image.
     */
    @ApiOperation(value = "cancel upload vm image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/upload", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity cancelUploadVmImage(@RequestParam(value = "identifier", required = false) String identifier,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") Integer imageId) {
        LOGGER.info("cancel upload vm image file, imageId = {}", imageId);
        return vmImageService.cancelUploadVmImage(imageId, identifier);
    }

    /**
     * merge vm image.
     */
    @ApiOperation(value = "merge vm image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity mergeVmImage(@RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "identifier", required = false) String identifier,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") Integer imageId) {
        LOGGER.info("merge vm image file, imageId = {}, fileName = {}, identifier = {}", imageId, fileName, identifier);
        return vmImageService.mergeVmImage(fileName, identifier, imageId);
    }

    /**
     * download vm image.
     */
    @ApiOperation(value = "download vm image", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = File.class)
    })
    @RequestMapping(value = "/{imageId}/action/download", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<byte[]> downloadVmImage(
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") Integer imageId) {
        LOGGER.info("download vm image file, systemId = {}", imageId);
        VMImage vmImage = vmImageService.getVmImageById(imageId);
        if (vmImage == null) {
            LOGGER.error("can not find vm image {} in db.", imageId);
            throw new EntityNotFoundException("can not find vm image in db!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + vmImage.getImageFileName());
        byte[] fileData = vmImageService.downloadVmImage(imageId);
        return ResponseEntity.ok().headers(headers).body(fileData);
    }

    /**
     * image slim.
     *
     * @return
     */
    @ApiOperation(value = "image slim.", response = OperationInfoRep.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OperationInfoRep.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/slim", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<OperationInfoRep> imageSlim(@PathVariable("imageId") Integer imageId) {
        LOGGER.info("image slim, imageId = {}", imageId);
        return ResponseEntity.ok(vmImageService.imageSlim(imageId));
    }

}
