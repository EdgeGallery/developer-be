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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.MepCreateHost;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "system")
@RequestMapping("/mec/developer/v1/system")
@Api(tags = "system")
public class SystemController {

    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private SystemService systemService;

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
    @RequestMapping(value = "/hosts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Page<MepHost>> getAllHosts(
        @ApiParam(value = "userId", required = false) @RequestParam(value = "userId", required = false) String userId,
        @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
        @ApiParam(value = "ip", required = false) @RequestParam(value = "ip", required = false) String ip,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity.ok(systemService.getAllHosts(userId, name, ip, limit, offset));

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
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<MepHost> getHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
        @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, MepHost> either = systemService.getHost(hostId);
        return ResponseDataUtil.buildResponse(either);
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
    @RequestMapping(value = "/hosts", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> createHost(
        @ApiParam(value = "MepHost", required = true) @Validated @RequestBody MepCreateHost host,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = systemService.createHost(host, token);
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
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
        @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, Boolean> either = systemService.deleteHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modifyHost.
     *
     * @return
     */
    @ApiOperation(value = "update one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<MepHost> modifyHost(
        @PathVariable("hostId") @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId,
        @Validated @RequestBody MepHost host, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, MepHost> either = systemService.updateHost(hostId, host, token);
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
    @RequestMapping(value = "/hosts/{hostId}/log", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
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
    @RequestMapping(value = "/capability", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
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
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteCapabilityByUserIdAndGroupId(
        @ApiParam(value = "groupId", required = true) @RequestParam("groupId")
        @Pattern(regexp = REG_UUID, message = "groupId must be in UUID format") String groupId) {
        Either<FormatRespDto, Boolean> either = systemService.deleteCapabilityByUserIdAndGroupId(groupId);
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
    @PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<Page<OpenMepCapabilityGroup>> getAllCapability(
        @ApiParam(value = "userId", required = false) @RequestParam(value = "userId", required = false) String userId,
        @ApiParam(value = "twoLevelName", required = false) @RequestParam(value = "twoLevelName", required = false)
            String twoLevelName,
        @ApiParam(value = "twoLevelNameEn", required = false) @RequestParam(value = "twoLevelNameEn", required = false)
            String twoLevelNameEn,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity
            .ok(systemService.getAllCapabilityGroups(userId, twoLevelName, twoLevelNameEn, limit, offset));
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
    @RequestMapping(value = "/capability/{groupId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<OpenMepCapabilityGroup> getCapalitiesByGroupId(
        @ApiParam(value = "groupId", required = true) @PathVariable("groupId")
        @Pattern(regexp = REG_UUID) String groupId) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = systemService.getCapabilityByGroupId(groupId);
        return ResponseDataUtil.buildResponse(either);
    }
}
