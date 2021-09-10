package org.edgegallery.developer.controller.mephost;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.mephost.MepHost;
import org.edgegallery.developer.model.mephost.MepHostLog;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.mephost.MepHostService;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "mepHosts")
@RequestMapping("/mec/developer/v2/mephosts")
@Api(tags = "mepHosts")
public class MepHostCtl {
    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private MepHostService mepHostService;

    /**
     * getAllHosts.
     *
     * @return
     */
    @ApiOperation(value = "get all server(build and test app)", response = MepHost.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Page<MepHost>> getAllHosts(
        @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
        @ApiParam(value = "vimType", required = true) @RequestParam(value = "vimType") String vimType,
        @ApiParam(value = "architecture", required = true) @RequestParam(value = "architecture") String architecture,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity.ok(mepHostService.getAllHosts(name, vimType, architecture, limit, offset));
    }

    /**
     * createHost.
     *
     * @return
     */
    @ApiOperation(value = "create one server", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> createHost(
        @ApiParam(value = "MepHost", required = true) @Validated @RequestBody MepHost host,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = mepHostService.createHost(host, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modifyHost.
     *
     * @return
     */
    @ApiOperation(value = "update one server by hostId",
        response = org.edgegallery.developer.model.workspace.MepCreateHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK",
            response = org.edgegallery.developer.model.workspace.MepCreateHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{mephostId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyHost(@PathVariable("mephostId")
    @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String mephostId,
        @Validated @RequestBody MepHost host, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = mepHostService.updateHost(mephostId, host, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * deleteHost.
     *
     * @return
     */
    @ApiOperation(value = "delete one server by hostId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{mephostId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteHost(@ApiParam(value = "mephostId", required = true) @PathVariable("mephostId")
    @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String mephostId) {
        Either<FormatRespDto, Boolean> either = mepHostService.deleteHost(mephostId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * getHost.
     *
     * @return
     */
    @ApiOperation(value = "get one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{mephostId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<MepHost> getHost(@ApiParam(value = "mephostId", required = true) @PathVariable("mephostId")
    @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String mephostId) {
        Either<FormatRespDto, MepHost> either = mepHostService.getHost(mephostId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * getHostLogByHostId.
     *
     * @return
     */
    @ApiOperation(value = "get all server(build and test app)", response = MepHost.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{hostId}/logs", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<MepHostLog>> getHostLogByHostId(
        @ApiParam(value = "hostId", required = true) @PathVariable String hostId) {
        Either<FormatRespDto, List<MepHostLog>> either = mepHostService.getHostLogByHostId(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

}
