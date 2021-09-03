package org.edgegallery.developer.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.application.ApplicationService;
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
import com.spencerwi.either.Either;

@Controller
@RestSchema(schemaId = "application")
@RequestMapping("/mec/developer/v2/applications")
@Api(tags = "application")
@Validated
public class ApplicationCtl {
    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private ApplicationService applicationService;
    /**
     * create a application.
     */
    @ApiOperation(value = "create one application.", response = Application.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Application.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Application> createApplication(
        @NotNull @ApiParam(value = "Application", required = true) @RequestBody Application application,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, Application> either = applicationService.createApplication(userId, application);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get a application.
     */
    @ApiOperation(value = "get one application.", response = Application.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Application.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Application> getApplication(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        Either<FormatRespDto, Application> either = applicationService.getApplication(applicationId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modify a application.
     */
    @ApiOperation(value = "modify one application.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyApplication(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId,
        @NotNull @ApiParam(value = "Application", required = true) @RequestBody Application application){
        Either<FormatRespDto, Boolean> either = applicationService.modifyApplication(applicationId, application);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all application.
     */
    @ApiOperation(value = "get one application.", response = Application.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Application.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Page<Application>> getAllApplication(
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false)
            String name,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity.ok(applicationService.getApplicationByNameWithFuzzy(userId, name, limit, offset));
    }

    /**
     * delete a application.
     */
    @ApiOperation(value = "delete one application.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{applicationId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteApplication(
        @Pattern(regexp = REGEX_UUID, message = "applicationId must be in UUID format")
        @ApiParam(value = "applicationId", required = true) @PathVariable("applicationId") String applicationId){
        Either<FormatRespDto, Boolean> either = applicationService.deleteApplication(applicationId);
        return ResponseDataUtil.buildResponse(either);
    }

}
