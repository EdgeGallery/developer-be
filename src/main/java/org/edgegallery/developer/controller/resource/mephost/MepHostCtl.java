/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.controller.resource.mephost;

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
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.model.resource.mephost.MepHostLog;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Page<MepHost>> getAllHosts(
        @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
        @ApiParam(value = "vimType", required = false) @RequestParam(value = "vimType") String vimType,
        @ApiParam(value = "architecture", required = false) @RequestParam(value = "architecture") String architecture,
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
        User user = AccessUserUtil.getUser();
        String accessToken = request.getHeader(Consts.ACCESS_TOKEN_STR);
        return ResponseEntity.ok(mepHostService.createHost(host, user, accessToken));
    }

    /**
     * modifyHost.
     *
     * @return
     */
    @ApiOperation(value = "update one server by hostId",
        response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK",
            response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{mephostId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyHost(@PathVariable("mephostId")
    @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String mephostId,
        @Validated @RequestBody MepHost host) {
        User user = AccessUserUtil.getUser();
        return ResponseEntity.ok(mepHostService.updateHost(mephostId, host, user));
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
    @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String mephostId,
                                              HttpServletRequest request) {
        String accessToken = request.getHeader(Consts.ACCESS_TOKEN_STR);
        return ResponseEntity.ok(mepHostService.deleteHost(mephostId, accessToken));
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
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<MepHost> getHost(@ApiParam(value = "mephostId", required = true) @PathVariable("mephostId")
    @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String mephostId) {
        return ResponseEntity.ok(mepHostService.getHost(mephostId));
    }

    /**
     * getHostLogByHostId.
     *
     * @return
     */
    @ApiOperation(value = "get all logs", response = MepHostLog.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHostLog.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{mephostId}/logs", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<MepHostLog>> getHostLogByHostId(
        @ApiParam(value = "mephostId", required = true) @PathVariable String mephostId) {
        return ResponseEntity.ok(mepHostService.getHostLogByHostId(mephostId));
    }

    /**
     * upload config file.
     */
    @ApiOperation(value = "upload file", response = UploadFile.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = UploadFile.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/action/upload-config-file", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<UploadFile> uploadFile(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile uploadFile) {
        String userId = AccessUserUtil.getUser().getUserId();
        return ResponseEntity.ok(mepHostService.uploadConfigFile(userId, uploadFile));
    }
}
