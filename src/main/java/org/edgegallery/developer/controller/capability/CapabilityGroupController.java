/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

import java.util.List;

import javax.validation.constraints.Pattern;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.capability.CapabilityGroup;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.capability.CapabilityGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RestSchema(schemaId = "capability-groups-v2")
@RequestMapping("/mec/developer/v2/capability-groups")
@Api(tags = "capability-groups")
public class CapabilityGroupController {
	private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

	@Autowired
	private CapabilityGroupService capabilityGroupService;

	@ApiOperation(value = "Create one CapabilityGroup", response = CapabilityGroup.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = CapabilityGroup.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
	public ResponseEntity<CapabilityGroup> createCapabilityGroup(
			@ApiParam(value = "CapabilityGroup", required = true) @RequestBody CapabilityGroup group) {
		return ResponseEntity.ok(capabilityGroupService.create(group));
	}

	@ApiOperation(value = "Delete one CapabilityGroup by id", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
	public ResponseEntity<Boolean> deleteCapabilityGroupById(
			@ApiParam(value = "id", required = true) @PathVariable("id") @Pattern(regexp = REG_UUID) String id) {
		return ResponseEntity.ok(capabilityGroupService.deleteById(id));

	}

	@ApiOperation(value = "Get All CapabilityGroup", response = CapabilityGroup.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = CapabilityGroup.class, responseContainer = "List") })
	@GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
	public ResponseEntity<List<CapabilityGroup>> getAllCapabilityGroup() {
		return ResponseEntity.ok(capabilityGroupService.findAll());
	}

	@ApiOperation(value = "Get CapabilityGroup by id", response = CapabilityGroup.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = CapabilityGroup.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
	public ResponseEntity<CapabilityGroup> getCapabilityGroupById(
			@ApiParam(value = "id", required = true) @PathVariable("id") @Pattern(regexp = REG_UUID) String id) {
		return ResponseEntity.ok(capabilityGroupService.findById(id));
	}
}
