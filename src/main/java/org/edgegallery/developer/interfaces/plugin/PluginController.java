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

package org.edgegallery.developer.interfaces.plugin;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.IOException;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.application.plugin.PluginService;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.model.plugin.Plugin;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.interfaces.plugin.facade.PluginServiceFacade;
import org.edgegallery.developer.interfaces.plugin.facade.dto.PluginDto;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "plugin")
@RequestMapping("/mec/developer/v1/plugins")
@CrossOrigin(allowedHeaders = "*")
@Api(tags = "Plugin")
public class PluginController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    private static final int MAX_SHORT_STRING_LENGTH = 32;

    private static final int MAX_COMMON_STRING_LENGTH = 255;

    private static final int MAX_DETAILS_STRING_LENGTH = 2048;

    private static final int MIN_SCORE = 0;

    private static final int MAX_SCORE = 5;

    @Autowired
    private PluginServiceFacade pluginServiceFacade;

    @Autowired
    private PluginService pluginService;

    /**
     * Upload a plugin by parameters.
     */
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "upload plugin to server", response = Plugin.class,
        notes = "The API can receive the upload request from upload form, and there are nine kinds of parameters  "
            + "are needed. Noticed that the  username and userid are obtained from the login page, and"
            + "Authorization an userid are obtained from the request header, if any parameter is missing,"
            + "it will be response with state 400. If the upload is successful, then it will be response "
            + "with state 200 and Plugin object")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Plugin.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<PluginDto> upload(
        @ApiParam(value = "plugin", required = true) @RequestPart(name = "pluginFile") MultipartFile pluginFile,
        @ApiParam(value = "logo File", required = true) @RequestPart(name = "logoFile") MultipartFile logoFile,
        @ApiParam(value = "api File", required = true) @RequestPart(name = "apiFile") MultipartFile apiFile,
        @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of plugin name cannot exceed 255")
        @ApiParam(value = "plugin name", required = true) @RequestParam(name = "pluginName") String pluginName,
        @ApiParam(value = "plugin function", required = true, allowableValues = "JAVA,Python,Go,.Net,PHP")
        @Length(max = MAX_SHORT_STRING_LENGTH, message = "Length of codeLanguage cannot exceed 32")
        @RequestParam(name = "codeLanguage") String codeLanguage,
        @ApiParam(value = "plugin type,1:plugin,2:sdk", required = true, allowableValues = "1,2")
        @Length(max = MAX_SHORT_STRING_LENGTH, message = "Length of pluginType cannot exceed 32")
        @RequestParam(name = "pluginType") String pluginType,
        @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of version name cannot exceed 255")
        @ApiParam(value = "plugin version", required = true) @RequestParam(name = "version") String version,
        @Length(max = MAX_DETAILS_STRING_LENGTH, message = "Length of plugin introduction cannot exceed 1024")
        @ApiParam(value = "plugin introduction") @RequestParam(name = "introduction") String introduction,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "the author's Id of upload plugin", required = true) @RequestParam(name = "userId")
            String userId, @Pattern(regexp = REGEX_USERNAME,
        message = "username can only be a combination of letters and numbers, the length is 6 to 30")
    @ApiParam(value = "the author's name of upload plugin", required = true) @RequestParam(name = "userName")
        String userName) throws IOException {
        Plugin plugin = new Plugin(pluginName, introduction, codeLanguage, pluginType, version,
            new User(userId, userName));
        return ResponseEntity.ok(pluginServiceFacade.publish(plugin, pluginFile, logoFile, apiFile));
    }

    @ApiOperation(value = "query all plugin or sdk", response = Page.class,
        notes = "The API can receive the query plugin list request.\r\n"
            + "Noticed that the  Authorization and userid are obtained from the request header,pluginType "
            + "can be only  1 or 2,if 3,it will be response with state 400.\r\n"
            + "if any parameter is missing, it will be response with state 400. \r\n"
            + "If the query is successful, then it will be response with state 200 and List<Plugin> object")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Page.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<Page<PluginDto>> getAll(
        @ApiParam(value = "plugin type:plugin or sdk", required = true) @RequestParam("pluginType") String pluginType,
        @ApiParam(value = "codeLanguage") @RequestParam("codeLanguage") String codeLanguage,
        @ApiParam(value = "pluginName") @RequestParam("pluginName") String pluginName,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return ResponseEntity.ok(pluginServiceFacade.query(pluginType, codeLanguage, pluginName, limit, offset));
    }

    /**
     * Delete plugin by plugin id.
     *
     * @param pluginId plugin id.
     * @return
     */
    @ApiOperation(value = "delete one plugin or sdk", response = Boolean.class,
        notes = "The API can receive the delete plugin request.\r\n"
            + "Noticed that the  Authorization and userid are obtained from the request header,"
            + "pluginId can be only String type,if pluginId not exist or Incorrect,it will be"
            + " response with state 400.\r\n If the delete is successful, then it will be response"
            + " with state 200 and plugin object what be deleted")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "pluginId", paramType = "path", value = "plugin id", dataType = "string",
            required = true)
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deletePlugin(
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be in UUID format") @PathVariable("pluginId")
            String pluginId) {
        String userId = AccessUserUtil.getUser().getUserId();
        pluginServiceFacade.deleteByPluginId(pluginId, userId);
        return ResponseEntity.ok(true);
    }

    /**
     * Download plugin by plugin id.
     *
     * @param pluginId plugin id.
     * @return
     */
    @ApiOperation(value = "download one plugin or sdk", response = File.class,
        notes = "The API can receive the download plugin request.\r\n"
            + "Noticed that the pluginId can be only String type,if pluginId not exist or Incorrect,it will be "
            + "response with state 400.\r\n"
            + "if pluginId is correct,but plugin file not exist,,it will be response with state 204.\r\n"
            + "If the download plugin is successful, then it will be response with state 200 and plugin file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 404, message = "No Content", response = ErrorRespDto.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}/action/download", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<InputStreamResource> downloadFile(
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be in UUID format")
        @ApiParam(value = "pluginId", required = true) @PathVariable("pluginId") String pluginId) throws IOException {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=" + pluginServiceFacade.getPluginName(pluginId))
            .body(new InputStreamResource(pluginServiceFacade.downloadFile(pluginId)));
    }

    @ApiOperation(value = "download logo file", response = File.class,
        notes = "The API can receive the download plugin logo file request.\r\n"
            + "Noticed that the pluginId can be only String type,if pluginId not exist or Incorrect,it will be "
            + "response with state 400.\r\n"
            + "if pluginId is correct,but plugin file not exist,,it will be response with state 204.\r\n"
            + " with state 204.\r\nIf the download plugin logo is successful, then "
            + "it will be response with state 200 and plugin logo file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}/action/get-logofile", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<InputStreamResource> getLogoFile(
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be" + " in UUID format")
        @ApiParam(value = "pluginId", required = true) @PathVariable("pluginId") String pluginId) throws IOException {
        return new ResponseEntity<>(new InputStreamResource(pluginServiceFacade.downloadLogo(pluginId)), HttpStatus.OK);
    }

    /**
     * get plugin or sdk api file.
     *
     * @return
     */
    @ApiOperation(value = "download  api file", response = File.class,
        notes = "The API can receive the download plugin api file request.\r\n"
            + "Noticed that the pluginId can be only String type,if pluginId not exist or Incorrect,it will be "
            + "response with state 400.\r\n"
            + "if pluginId is correct,but plugin file not exist,,it will be response with state 204.\r\n"
            + "If the download plugin logo is successful, then it will be response with state 200 and plugin"
            + "api file")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 404, message = "Unauthorized", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}/action/get-apifile", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<InputStreamResource> getApiFile(
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be" + " in UUID format")
        @ApiParam(value = "pluginId", required = true) @PathVariable("pluginId") String pluginId) throws IOException {
        return new ResponseEntity<>(new InputStreamResource(pluginServiceFacade.downloadApiFile(pluginId)),
            HttpStatus.OK);
    }

    /**
     * update plugin.
     *
     * @return
     */
    @ApiOperation(value = "update plugin", response = Plugin.class,
        notes = "The API can receive the modify plugin  request.\r\n"
            + "Noticed that  Authorization and userid are obtained from the request header,"
            + "the pluginId can be only String type,if pluginId not exist or Incorrect,"
            + "it will be response with state 400.\r\n If the modify plugin is successful,"
            + "then it will be response with state 200 and plugin object")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "pluginId", paramType = "path", value = "plugin id", dataType = "string",
            required = true)
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Plugin.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<PluginDto> updatePlugin(
        @ApiParam(value = "plugin", required = false) @RequestPart("pluginFile") MultipartFile pluginFile,
        @ApiParam(value = "logo File", required = false) @RequestPart("logoFile") MultipartFile logoFile,
        @ApiParam(value = "api File", required = false) @RequestPart("apiFile") MultipartFile apiFile,
        @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of plugin name cannot exceed 255")
        @ApiParam(value = "plugin name", required = false) @RequestPart("pluginName") String pluginName,
        @ApiParam(value = "plugin function", required = false, allowableValues = "JAVA,Python,Go,Net,PHP")
        @Length(max = MAX_SHORT_STRING_LENGTH, message = "Length of plugin name cannot exceed 32")
        @RequestPart("codeLanguage") String codeLanguage,
        @ApiParam(value = "plugin type,1:plugin,2:SDK", required = false, allowableValues = "1,2")
        @Length(max = MAX_SHORT_STRING_LENGTH, message = "Length of plugin Type cannot exceed 32")
        @RequestPart("pluginType") String pluginType,
        @Length(max = MAX_COMMON_STRING_LENGTH, message = "Length of version name cannot exceed 255")
        @ApiParam(value = "plugin version", required = false) @RequestPart("version") String version,
        @Length(max = MAX_DETAILS_STRING_LENGTH, message = "Length of plugin introduction cannot exceed 1024")
        @ApiParam(value = "plugin introduction", required = false) @RequestPart("introduction") String introduction,
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be in UUID format") @PathVariable("pluginId")
            String pluginId) throws IOException {
        Plugin plugin = new Plugin(pluginId, pluginName, introduction, codeLanguage, pluginType, version);
        return ResponseEntity.ok(pluginServiceFacade.updatePlugin(plugin, pluginFile, logoFile, apiFile));
    }

    /**
     * update plugin satisfaction.
     *
     * @return
     */
    @ApiOperation(value = "update plugin satisfaction", response = Plugin.class,
        notes = "The API can receive the modify plugin  satisfaction request.\r\n"
            + "Noticed that  Authorization and userid are obtained from the request header,"
            + "the pluginId and score can be only String type, and score ranges from 1 to 5.\r\n"
            + "if pluginId not exist or Incorrect,it will be response with state 400.\r\n"
            + "If the modify plugin satisfaction is successful, it will be response"
            + "with state 200 and plugin object")
    @ApiImplicitParams(value = {
        @ApiImplicitParam(name = "pluginId", paramType = "path", value = "plugin Id", dataType = "string",
            required = true), @ApiImplicitParam(name = "score", paramType = "query",
        value = "plugin satisfaction score,greater than 0 and less than five, and only one decimal place",
        dataType = "string", required = true)
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Plugin.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}/action/score", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<PluginDto> updateScore(
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be in UUID format") @PathVariable("pluginId")
            String pluginId,
        @Range(min = MIN_SCORE, max = MAX_SCORE, message = "score must be 0 to 5") @RequestParam("score") Integer score,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @RequestParam(value = "userId")
            String userId, @Pattern(regexp = REGEX_USERNAME,
        message = "username can only be a combination of letters and numbers, the length is 6 to 30")
    @RequestParam(value = "userName") String userName) {
        return ResponseEntity.ok(PluginDto.of(pluginServiceFacade.mark(pluginId, score, new User(userId, userName))));
    }

    @ApiOperation(value = "get api file content", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{pluginId}/action/content", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<String> getApiContent(
        @Pattern(regexp = REGEX_UUID, message = "pluginId must be in UUID format")
        @ApiParam(value = "pluginId", required = true) @PathVariable(value = "pluginId", required = true)
            String pluginId) {
        Either<FormatRespDto, String> either = pluginService.getApiContent(pluginId);
        return ResponseDataUtil.buildResponse(either);
    }
}
