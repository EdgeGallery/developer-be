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

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.CapabilityService;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spencerwi.either.Either;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RestSchema(schemaId = "capabilities")
@RequestMapping("/mec/developer/v1/capabilities")
@Api(tags = "capabilities")
public class CapabilityController {
    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private CapabilityService systemService;

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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @DeleteMapping(
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
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @GetMapping(value = "/{groupId}",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<OpenMepCapabilityGroup> getCapalitiesByGroupId(
        @ApiParam(value = "groupId", required = true) @PathVariable("groupId")
        @Pattern(regexp = REG_UUID) String groupId) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = systemService.getCapabilityByGroupId(groupId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modify capability group.
     *
     * @return
     */
    @ApiOperation(value = "update one EdgeGalleryCapabilityGroup", response = OpenMepCapabilityGroup.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @PutMapping(value = "/{groupId}", 
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<OpenMepCapabilityGroup> modifyGroup(
        @ApiParam(value = "groupId", required = true) @PathVariable
        @Pattern(regexp = REG_UUID, message = "groupId must be in UUID format") String groupId,
        @ApiParam(value = "EdgeGalleryCapabilityGroup", required = true) @RequestBody OpenMepCapabilityGroup group) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = systemService.updateGroup(groupId, group);
        return ResponseDataUtil.buildResponse(either);
    }
}
