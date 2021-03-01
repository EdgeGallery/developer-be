package org.edgegallery.developer.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.*;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SystemService;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.List;

@Controller
@RestSchema(schemaId = "system")
@RequestMapping("/mec/developer/v1/system")
@Api(tags = "system")
public class SystemController {
    /**
     * todo
     * 沙箱管理接口
     * 能力中心管理接口
     * 项目管理接口
     */



    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private SystemService systemService;

    @ApiOperation(value = "get all server(build and test app)", response = MepHost.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepHost.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Page<MepHost>> getAllHosts(
            @ApiParam(value = "userId", required = false) @RequestParam(value = "userId", required = false) String userId,
            @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
            @ApiParam(value = "ip", required = false) @RequestParam(value = "ip", required = false) String ip,
            @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
            @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity.ok(systemService.getAllHosts(userId, name, ip, limit, offset));

    }

    @ApiOperation(value = "get one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepHost.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepHost> getHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
                                           @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, MepHost> either = systemService.getHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "create one server", response = MepHost.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepHost.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepHost> createHost(
            @ApiParam(value = "MepHost", required = true) @Validated @RequestBody MepHost host) {
        Either<FormatRespDto, MepHost> either = systemService.createHost(host);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "delete one server by hostId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
                                              @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, Boolean> either = systemService.deleteHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "update one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepHost.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepHost> modifyHost(@PathVariable("hostId") @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId,
                                              @Validated @RequestBody MepHost host) {
        Either<FormatRespDto, MepHost> either = systemService.updateHost(hostId, host);
        return ResponseDataUtil.buildResponse(either);
    }


    @ApiOperation(value = "get all server(build and test app)", response = MepHost.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MepHost.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}/log", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<List<MepHostLog>> getHostLogByHostId(
            @ApiParam(value = "hostId", required = true) @PathVariable String hostId) {
        Either<FormatRespDto, List<MepHostLog>> either = systemService.getHostLogByHostId(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * create capability group by group.
     *
     * @return
     */
    @ApiOperation(value = "create one EdgeGalleryCapabilityGroup", response = OpenMepCapabilityGroup.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/capability", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<OpenMepCapabilityGroup> createGroup(
            @ApiParam(value = "EdgeGalleryCapabilityGroup", required = true) @RequestBody OpenMepCapabilityGroup group) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = systemService.createCapabilityGroup(group);
        return ResponseDataUtil.buildResponse(either);
    }


    /**
     * delete capability by userId and groupId.
     *
     * @return
     */
    @ApiOperation(value = "delete one EdgeGalleryCapability by userId and groupId", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/capability", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteCapabilityByUserIdAndGroupId(
            @ApiParam(value = "groupId", required = true) @RequestParam("groupId")
            @Pattern(regexp = REG_UUID, message = "groupId must be in UUID format") String groupId,
            @Pattern(regexp = REG_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId", required = true)
            @RequestParam("userId") String userId) {
        Either<FormatRespDto, Boolean> either = systemService.deleteCapabilityByUserIdAndGroupId(groupId, userId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all EdgeGallery capability.
     *
     * @return
     */
    @ApiOperation(value = "get all EdgeGalleryCapability", response = OpenMepCapabilityGroup.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/capability", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<Page<OpenMepCapabilityGroup>> getAllCapability(
            @ApiParam(value = "userId", required = false) @RequestParam(value = "userId", required = false) String userId,
            @ApiParam(value = "twoLevelName", required = false) @RequestParam(value = "twoLevelName", required = false) String twoLevelName,
            @ApiParam(value = "twoLevelNameEn", required = false) @RequestParam(value = "twoLevelNameEn", required = false) String twoLevelNameEn,
            @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
            @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity.ok(systemService.getAllCapabilityGroups(userId,twoLevelName,twoLevelNameEn, limit, offset));
    }

    /**
     * get EdgeGalleryCapability detail.
     *
     * @return
     */
    @ApiOperation(value = "get EdgeGalleryCapability detail", response = OpenMepCapabilityGroup.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/capability/{groupId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<OpenMepCapabilityGroup> getCapalitiesByGroupId(
            @ApiParam(value = "groupId", required = true) @PathVariable("groupId")
            @Pattern(regexp = REG_UUID) String groupId) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = systemService.getCapabilityByGroupId(groupId);
        return ResponseDataUtil.buildResponse(either);
    }
}
