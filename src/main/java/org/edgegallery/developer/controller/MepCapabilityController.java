/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.OpenMepApiResponse;
import org.edgegallery.developer.response.OpenMepEcoApiResponse;
import org.edgegallery.developer.service.OpenMepCapabilityService;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "capability-groups")
@RequestMapping("/mec/developer/v1/capability-groups")
@Api(tags = "MEPCapability")
public class MepCapabilityController {

    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private OpenMepCapabilityService openService;

    /**
     * create group by group.
     *
     * @return
     */
    @ApiOperation(value = "create one EdgeGalleryCapabilityGroup", response = OpenMepCapabilityGroup.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<OpenMepCapabilityGroup> createGroup(
        @ApiParam(value = "EdgeGalleryCapabilityGroup", required = true) @RequestBody OpenMepCapabilityGroup group) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = openService.createGroup(group);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * Delete group by groupId.
     *
     * @param groupId groupId
     * @return
     */
    @ApiOperation(value = "delete one EdgeGalleryCapabilityGroup by groupId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{groupId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteGroup(@ApiParam(value = "groupId", required = true) @PathVariable("groupId")
        @Pattern(regexp = REG_UUID) String groupId) {
        Either<FormatRespDto, Boolean> either = openService.deleteGroup(groupId);
        return ResponseDataUtil.buildResponse(either);

    }

    /**
     * create capability by groupId and capability.
     *
     * @return
     */
    @ApiOperation(value = "create one EdgeGalleryCapability", response = OpenMepCapabilityDetail.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityDetail.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{groupId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<OpenMepCapabilityDetail> createCapability(
        @ApiParam(value = "groupId", required = true) @PathVariable("groupId")
        @Pattern(regexp = REG_UUID) String groupId,
        @ApiParam(value = "EdgeGalleryCapabilityDetail", required = true) @RequestBody
            OpenMepCapabilityDetail capability) {
        Either<FormatRespDto, OpenMepCapabilityDetail> either = openService.createCapability(groupId, capability);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete capability by userId.
     *
     * @return
     */
    @ApiOperation(value = "delete one EdgeGalleryCapability by userId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/capabilities/{capabilityId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteCapabilityByUserId(
        @ApiParam(value = "capabilityId", required = true) @PathVariable("capabilityId")
        @Pattern(regexp = REG_UUID) String capabilityId,
        @Pattern(regexp = REG_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId) {
        Either<FormatRespDto, Boolean> either = openService.deleteCapabilityByUserId(capabilityId, userId);
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
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<List<OpenMepCapabilityGroup>> getAllCapalities() {
        Either<FormatRespDto, List<OpenMepCapabilityGroup>> either = openService.getAllCapabilityGroups();
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all EdgeGalleryCapability.
     *
     * @return
     */
    @ApiOperation(value = "get all EdgeGalleryCapability", response = OpenMepCapabilityGroup.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{groupId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<OpenMepCapabilityGroup> getCapalitiesByGroupId(
        @ApiParam(value = "groupId", required = true) @PathVariable("groupId")
        @Pattern(regexp = REG_UUID) String groupId) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = openService.getCapabilitiesByGroupId(groupId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all EdgeGallery API.
     *
     * @return
     */
    @ApiOperation(value = "get all EdgeGallery API", response = OpenMepApiResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OpenMepApiResponse.class)})
    @RequestMapping(value = "/openmep-api", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<OpenMepApiResponse> getOpenMepApi() {
        Either<FormatRespDto, OpenMepApiResponse> either = openService.getOpenMepList();
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get all EdgeGallery ECO API.
     *
     * @return
     */
    @ApiOperation(value = "get all EdgeGallery ECO API", response = OpenMepEcoApiResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OpenMepEcoApiResponse.class)})
    @RequestMapping(value = "/openmepeco-api", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<OpenMepEcoApiResponse> getOpenMepEcoApi() {
        Either<FormatRespDto, OpenMepEcoApiResponse> either = openService.getOpenMepEcoList();
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get EdgeGallery API detail by file id.
     *
     * @return
     */
    @ApiOperation(value = "get EdgeGallery API by file id", response = OpenMepCapabilityDetail.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = OpenMepApiResponse.class)})
    @RequestMapping(value = "/openmep-api/{fileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<OpenMepCapabilityDetail> getOpenMepApiByFileId(
        @Pattern(regexp = REG_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REG_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId) {
        Either<FormatRespDto, OpenMepCapabilityDetail> either = openService.getOpenMepByFileId(fileId, userId);
        return ResponseDataUtil.buildResponse(either);
    }

}
