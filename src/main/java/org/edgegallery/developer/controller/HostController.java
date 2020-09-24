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
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.HostService;
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

@Controller
@RestSchema(schemaId = "hosts")
@RequestMapping("/mec/developer/v1/hosts")
@Api(tags = "Host")
public class HostController {

    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private HostService hostService;

    @ApiOperation(value = "get all server(build and test app)", response = MepHost.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<List<MepHost>> getAllHosts() {
        Either<FormatRespDto, List<MepHost>> either = hostService.getALlHosts();
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "get one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{hostId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepHost> getHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
        @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, MepHost> either = hostService.getHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "create one server", response = MepHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepHost> createHost(
        @ApiParam(value = "MepHost", required = true) @Validated @RequestBody MepHost host) {
        Either<FormatRespDto, MepHost> either = hostService.createHost(host);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "delete one server by hostId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{hostId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
        @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, Boolean> either = hostService.deleteHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "update one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{hostId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<MepHost> modifyHost(@PathVariable("hostId") @Pattern(regexp = REG_UUID) String hostId,
        @Validated @RequestBody MepHost host) {
        Either<FormatRespDto, MepHost> either = hostService.updateHost(hostId, host);
        return ResponseDataUtil.buildResponse(either);
    }
}
