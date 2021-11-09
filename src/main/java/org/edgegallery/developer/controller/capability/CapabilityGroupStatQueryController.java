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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.capability.CapabilityGroupStat;
import org.edgegallery.developer.service.capability.CapabilityGroupStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "capability-group-stats-query")
@RequestMapping("/mec/developer/v2/query/capability-group-stats")
@Api(tags = "capability-group-stats-query")
public class CapabilityGroupStatQueryController {
    @Autowired
    private CapabilityGroupStatService capabilityGroupStatService;

    @ApiOperation(value = "Get CapabilityGroupStat by type", response = CapabilityGroupStat.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CapabilityGroupStat.class, responseContainer = "List")
    })
    @GetMapping(value = "/type/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<List<CapabilityGroupStat>> getCapabilityGroupStat(
        @ApiParam(value = "type", required = true) @PathVariable(value = "type", required = true) String type) {
        return ResponseEntity.ok(capabilityGroupStatService.findByType(type));
    }
}
