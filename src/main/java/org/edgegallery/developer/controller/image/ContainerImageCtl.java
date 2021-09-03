package org.edgegallery.developer.controller.image;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.Chunk;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.image.ContainerImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
    @RequestMapping(value = "/{imageId}/upload", method = RequestMethod.POST)
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
    @RequestMapping(value = "/{imageId}/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity mergeContainerImage(@RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "guid", required = false) String guid,
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") String imageId) throws IOException {
        return containerImageService.mergeContainerImage(fileName, guid, imageId);
    }

}
