package org.edgegallery.developer.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.servicecomb.provider.rest.common.RestSchema;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.spencerwi.either.Either;

@Controller
@RestSchema(schemaId = "vm")
@RequestMapping("/mec/developer/v1/vm")
@Api(tags = "vm")
@Validated
public class VmController {

    @Autowired
    private VmService vmService;

    /**
     * get vm resources information.
     */
    @ApiOperation(value = "get vm resources information", response = VmResource.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = VmResource.class)
    })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<VmResource> getVirtualResource( ){
        Either<FormatRespDto, VmResource> either = vmService
            .getVirtualResource();
        return ResponseDataUtil.buildResponse(either);
    }

}

