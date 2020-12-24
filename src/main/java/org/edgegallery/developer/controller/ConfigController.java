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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.DeployPlatformConfig;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ConfigService;
import org.edgegallery.developer.service.ProjectService;
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
@RestSchema(schemaId = "config")
@RequestMapping("/mec/developer/v1/config")
public class ConfigController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    @Autowired
    private ConfigService configService;


    /**
     * modify config of deployPlatform virtual machine
     */
    @ApiOperation(value = "modify config of deployPlatform", response = DeployPlatformConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = DeployPlatformConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/deploy-platform", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<DeployPlatformConfig> deployPlatformConfig(


        @NotNull @ApiParam(value = "DeployPlatformConfig", required = true) @RequestBody
            DeployPlatformConfig deployPlatformConfig) {
        Either<FormatRespDto, DeployPlatformConfig> either = configService.configDeployPlatform(deployPlatformConfig);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modify config of deployPlatform virtual machine
     */
    @ApiOperation(value = "modify config of deployPlatform", response = DeployPlatformConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = DeployPlatformConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/deploy-platform", method = RequestMethod.GET,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<DeployPlatformConfig> getDeployPlatformConfig( ) {
        Either<FormatRespDto, DeployPlatformConfig> either = configService.getConfigDeployPlatform();
        return ResponseDataUtil.buildResponse(either);
    }
}