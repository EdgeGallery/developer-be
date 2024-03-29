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

package org.edgegallery.developer.controller.resource.container;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.resource.container.ContainerImage;
import org.edgegallery.developer.model.resource.container.ContainerImageReq;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.recource.container.ContainerImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "ContainerImage")
@RequestMapping("/mec/developer/v2/containerimages")
@Api(tags = "ContainerImage")
public class ContainerImageCtl {

    @Autowired
    private ContainerImageService containerImageService;

    /**
     * upload container image.
     */
    @ApiOperation(value = "upload container image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity uploadContainerImage(HttpServletRequest request, Chunk chunk,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") String imageId) {
        return containerImageService.uploadContainerImage(request, chunk, imageId);
    }

    /**
     * merge image.
     */
    @ApiOperation(value = "merge image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{imageId}/action/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity mergeContainerImage(@RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "guid", required = false) String guid,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") String imageId) {
        return containerImageService.mergeContainerImage(fileName, guid, imageId);
    }

    /**
     * getAllContainerImage.
     *
     * @return
     */
    @ApiOperation(value = "get all ContainerImage", response = ContainerImage.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ContainerImage.class)
    })
    @RequestMapping(value = "/action/get-all-images", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Page<ContainerImage>> getAllContainerImage(
        @ApiParam(value = "ContainerImages", required = true) @RequestBody ContainerImageReq containerImageReq) {
        Page<ContainerImage> either = containerImageService.getAllImage(containerImageReq);
        return ResponseEntity.ok(either);
    }

    /**
     * modifyContainerImage.
     *
     * @return
     */
    @ApiOperation(value = "modify ContainerImage", response = ContainerImage.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ContainerImage.class)
    })
    @RequestMapping(value = "/{imageId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ContainerImage> modifyContainerImage(
        @ApiParam(value = "imageId", required = true) @PathVariable String imageId,
        @ApiParam(value = "ContainerImage", required = true) @RequestBody ContainerImage containerImage) {
        ContainerImage either = containerImageService.updateContainerImage(imageId, containerImage);
        return ResponseEntity.ok(either);
    }

    /**
     * deleteContainerImage.
     *
     * @return
     */
    @ApiOperation(value = "delete ContainerImage", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class)
    })
    @RequestMapping(value = "/{imageId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteContainerImage(
        @ApiParam(value = "imageId", required = true) @PathVariable String imageId) {
        Boolean either = containerImageService.deleteContainerImage(imageId);
        return ResponseEntity.ok(either);
    }

    /**
     * download harbor image.
     */
    @ApiOperation(value = "download system image", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class)
    })
    @RequestMapping(value = "/{imageId}/action/download", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<InputStreamResource> downloadSystemImage(
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") String imageId) {
        return containerImageService.downloadHarborImage(imageId);
    }

    /**
     * cancel upload harbor image.
     */
    @ApiOperation(value = "cancel upload harbor image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class)
    })
    @RequestMapping(value = "/{imageId}/action/upload", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity cancelUploadHarborImage(
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") String imageId) {
        return containerImageService.cancelUploadHarborImage(imageId);
    }

    /**
     * synchronize image from harbor repo.
     */
    @ApiOperation(value = "synchronize image from harbor repo", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class)
    })
    @RequestMapping(value = "/action/synchronize", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity synchronizeHarborImage() {
        return containerImageService.synchronizeHarborImage();

    }
}
