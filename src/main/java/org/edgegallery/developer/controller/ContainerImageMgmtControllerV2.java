package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.ContainerImageReq;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ContainerImageMgmtServiceV2;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RestSchema(schemaId = "containerImageMgmtV2")
@RequestMapping("/mec/developer/v2/container")
@Api(tags = "containerImageV2")
public class ContainerImageMgmtControllerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerImageMgmtControllerV2.class);

    @Autowired
    private ContainerImageMgmtServiceV2 containerImageMgmtServiceV2;

    /**
     * createContainerImage.
     *
     * @return
     */
    @ApiOperation(value = "create ContainerImage", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class)
    })
    @RequestMapping(value = "/images", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ContainerImage> createContainerImage(
        @ApiParam(value = "ContainerImage", required = true) @RequestBody ContainerImage containerImage) {
        Either<FormatRespDto, ContainerImage> either = containerImageMgmtServiceV2.createContainerImage(containerImage);
        return ResponseDataUtil.buildResponse(either);
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
    @RequestMapping(value = "/images/list", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Page<ContainerImage>> getAllContainerImage(
        @ApiParam(value = "ContainerImages", required = true) @RequestBody ContainerImageReq containerImageReq) {
        Page<ContainerImage> either = containerImageMgmtServiceV2.getAllImage(containerImageReq);
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
    @RequestMapping(value = "/images/{imageId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ContainerImage> modifyContainerImage(
        @ApiParam(value = "imageId", required = true) @PathVariable String imageId,
        @ApiParam(value = "ContainerImage", required = true) @RequestBody ContainerImage containerImage) {
        Either<FormatRespDto, ContainerImage> either = containerImageMgmtServiceV2
            .updateContainerImage(imageId, containerImage);
        return ResponseDataUtil.buildResponse(either);
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
    @RequestMapping(value = "/images/{imageId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')|| hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteContainerImage(
        @ApiParam(value = "imageId", required = true) @PathVariable String imageId) {
        Either<FormatRespDto, Boolean> either = containerImageMgmtServiceV2.deleteContainerImage(imageId);
        return ResponseDataUtil.buildResponse(either);
    }

}
