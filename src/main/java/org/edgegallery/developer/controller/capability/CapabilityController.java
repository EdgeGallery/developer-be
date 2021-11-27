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

package org.edgegallery.developer.controller.capability;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.capability.CapabilityService;
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

@Controller("v2-capabilityController")
@RestSchema(schemaId = "capabilities-v2")
@RequestMapping("/mec/developer/v2/capabilities")
@Api(tags = "capabilities-v2")
public class CapabilityController {
    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private CapabilityService capabilityService;

    @ApiOperation(value = "Create one Capability", response = Capability.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Capability.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Capability> createCapability(
        @ApiParam(value = "Capability", required = true) @RequestBody Capability capability) {
        return ResponseEntity.ok(capabilityService.create(capability));
    }

    @ApiOperation(value = "Delete one Capability by id", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteCapabilityById(@ApiParam(value = "id", required = true) @PathVariable("id")
    @Pattern(regexp = REG_UUID, message = "id must be in UUID format") String id) {
        return ResponseEntity.ok(capabilityService.deleteById(id));
    }

    @ApiOperation(value = "get Capability by property", response = Capability.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Capability.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<Capability> getCapabilityById(@ApiParam(value = "id", required = true) @PathVariable("id")
    @Pattern(regexp = REG_UUID, message = "id must be in UUID format") String id, HttpServletRequest request) {
        return ResponseEntity.ok(capabilityService.findById(id));
    }

    @ApiOperation(value = "get all Capability", response = Capability.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Capability.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<List<Capability>> getAllCapability(
        @ApiParam(value = "filterType", required = false) @RequestParam(value = "filterType", required = false)
            String filterType, HttpServletRequest request) {
        return ResponseEntity.ok(capabilityService.findAll());
    }

    @ApiOperation(value = "update one Capability", response = Capability.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Capability.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Capability> modifyCapabilityById(@ApiParam(value = "id", required = true) @PathVariable
    @Pattern(regexp = REG_UUID, message = "id must be in UUID format") String id,
        @ApiParam(value = "Capability", required = true) @RequestBody Capability capability) {
        return ResponseEntity.ok(capabilityService.updateById(capability));
    }
}
