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

import java.util.List;

import javax.validation.constraints.Min;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.capability.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RestSchema(schemaId = "capability-query-v2")
@RequestMapping("/mec/developer/v2/query/capabilities")
@Api(tags = "capability-query-v2")
public class CapabilityQueryController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CapabilityQueryController.class);
	private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

	@Autowired
	private CapabilityService capabilityService;

	@ApiOperation(value = "get Capability by groupId", response = Capability.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = Capability.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@GetMapping(value = "/group-id/{groupId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
	public ResponseEntity<List<Capability>> getCapabilityByGroupId(
			@ApiParam(value = "groupId", required = true) @PathVariable(value = "groupId") String groupId) {
		return ResponseEntity.ok(capabilityService.findByGroupId(groupId));
	}
	
	@ApiOperation(value = "get Capability by projectId", response = Capability.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = Capability.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@GetMapping(value = "/project-id/{projectId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
	public ResponseEntity<List<Capability>> getCapabilityByProjectId(
			@ApiParam(value = "projectId", required = true) @PathVariable(value = "projectId") String projectId) {
		return ResponseEntity.ok(capabilityService.findByProjectId(projectId));
	}	

	@ApiOperation(value = "get Capability by name with fuzzy", response = Capability.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = Capability.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@GetMapping(value = { "/name" }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
	public ResponseEntity<Page<Capability>> getCapabilityByNameWithFuzzy(
			@ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
			@ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
			@ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
		PageHelper.offsetPage(offset, limit);
		PageInfo<Capability> pageInfo = new PageInfo<>(capabilityService.findByNameWithFuzzy(name));
		return ResponseEntity.ok(new Page<Capability>(pageInfo.getList(), limit, offset, pageInfo.getTotal()));
	}

	@ApiOperation(value = "get Capability by nameEn with fuzzy", response = Capability.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = Capability.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class) })
	@GetMapping(value = { "/name-en" }, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@PreAuthorize("hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
	public ResponseEntity<Page<Capability>> getCapabilityByNameEnWithFuzzy(
			@ApiParam(value = "nameEn", required = false) @RequestParam(value = "nameEn", required = false) String nameEn,
			@ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
			@ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
		PageHelper.offsetPage(offset, limit);
		PageInfo<Capability> pageInfo = new PageInfo<>(capabilityService.findByNameEnWithFuzzy(nameEn));
		return ResponseEntity.ok(new Page<Capability>(pageInfo.getList(), limit, offset, pageInfo.getTotal()));
	}
}
