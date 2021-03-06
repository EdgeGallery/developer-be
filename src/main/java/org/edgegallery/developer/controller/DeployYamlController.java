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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.DeployService;
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
@RestSchema(schemaId = "deploy")
@RequestMapping("/mec/developer/v1/deploy")
@Api(tags = "deploy")
@Validated
public class DeployYamlController {

    @Autowired
    private DeployService deployService;

    /**
     * genarate deploy yaml.
     */
    @ApiOperation(value = "genarate deploy yaml", response = HelmTemplateYamlPo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmTemplateYamlPo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/save-yaml", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<HelmTemplateYamlPo> saveDeploy(@RequestBody String jsonStr,
        @ApiParam(value = "userId", required = true) @RequestParam String userId,
        @ApiParam(value = "projectId", required = true) @PathVariable String projectId,
        @ApiParam(value = "configType", required = true) @RequestParam String configType) throws IOException {
        Either<FormatRespDto, HelmTemplateYamlPo> either = deployService
            .saveDeployYaml(jsonStr, projectId, userId, configType);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * update deploy yaml.
     */
    @ApiOperation(value = "modify deploy yaml", response = HelmTemplateYamlPo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmTemplateYamlPo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<HelmTemplateYamlPo> updateDeployYaml(
        @ApiParam(value = "fileId", required = true) @PathVariable String fileId,
        @ApiParam(value = "fileContent", required = true) @RequestBody String fileContent) {
        Either<FormatRespDto, HelmTemplateYamlPo> either = deployService.updateDeployYaml(fileId, fileContent);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get deploy yaml.
     */
    @ApiOperation(value = "modify deploy yaml", response = HelmTemplateYamlPo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmTemplateYamlPo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<HelmTemplateYamlPo> getDeployYaml(
        @ApiParam(value = "fileId", required = true) @PathVariable String fileId) {
        Either<FormatRespDto, HelmTemplateYamlPo> either = deployService.getDeployYamlContent(fileId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get deploy yaml.
     */
    @ApiOperation(value = "modify deploy yaml", response = List.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}/action/get-json", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<String>> queryDeployYaml(
        @ApiParam(value = "fileId", required = true) @PathVariable String fileId) throws JsonProcessingException {
        Either<FormatRespDto, List<String>> either = deployService.getDeployYamJson(fileId);
        return ResponseDataUtil.buildResponse(either);
    }

}
