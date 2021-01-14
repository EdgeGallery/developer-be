package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.ReleaseConfig;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ReleaseConfigService;
import org.edgegallery.developer.util.ResponseDataUtil;
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
@RestSchema(schemaId = "releaseconfig")
@RequestMapping("/mec/developer/v1/releaseconfig")
public class ReleaseConfigController {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private ReleaseConfigService releaseService;

    @ApiOperation(value = "save release config", response = ReleaseConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ReleaseConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/release-config", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ReleaseConfig> saveReleaseConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable(value = "projectId", required = true)
            String projectId, @RequestBody ReleaseConfig config) {
        Either<FormatRespDto, ReleaseConfig> either = releaseService.saveConfig(projectId, config);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "modify release config", response = ReleaseConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ReleaseConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/release-config", method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ReleaseConfig> updateReleaseConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable(value = "projectId", required = true)
            String projectId, @RequestBody ReleaseConfig config) {
        Either<FormatRespDto, ReleaseConfig> either = releaseService.modifyConfig(projectId, config);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "get release config", response = ReleaseConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ReleaseConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/release-config", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ReleaseConfig> getReleaseConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable(value = "projectId", required = true)
            String projectId) {
        Either<FormatRespDto, ReleaseConfig> either = releaseService.getConfigById(projectId);
        return ResponseDataUtil.buildResponse(either);
    }

}
