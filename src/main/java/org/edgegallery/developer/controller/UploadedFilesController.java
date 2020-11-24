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
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.service.UploadFileService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "files")
@RequestMapping("/mec/developer/v1/files")
@Api(tags = "File")
public class UploadedFilesController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private UploadFileService uploadFileService;

    /**
     * get a file stream.
     */
    @ApiOperation(value = "get a file", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getFile(@Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId, @ApiParam(value = "type") @RequestParam("type") String type) {
        Either<FormatRespDto, ResponseEntity<byte[]>> either = uploadFileService.getFile(fileId, userId, type);
        if (either.isRight()) {
            return either.getRight();
        } else {
            return null;
        }
    }

    /**
     * get a api file .
     */
    @ApiOperation(value = "get a file", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/api-info/{fileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<UploadedFile> getApiFile(
        @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId) {
        Either<FormatRespDto, UploadedFile> either = uploadFileService.getApiFile(fileId, userId);
        return ResponseDataUtil.buildResponse(either);
    }


    /**
     * upload file.
     */
    @ApiOperation(value = "upload file", response = UploadedFile.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = UploadedFile.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<UploadedFile> uploadFile(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile uploadFile,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, UploadedFile> either = uploadFileService.uploadFile(userId, uploadFile);
        return ResponseDataUtil.buildResponse(either);

    }

    /**
     * upload helm template yaml.
     */
    @ApiOperation(value = "upload helm template yaml", response = HelmTemplateYamlRespDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = HelmTemplateYamlRespDto.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/helm-template-yaml", method = RequestMethod.POST,
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<HelmTemplateYamlRespDto> uploadHelmTemplateYaml(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile helmTemplateYaml,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @RequestParam("projectId") String projectId) {
        Either<FormatRespDto, HelmTemplateYamlRespDto> either = uploadFileService
            .uploadHelmTemplateYaml(helmTemplateYaml, userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get helm template yaml list.
     */
    @ApiOperation(value = "get helm template yaml list", response = List.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/helm-template-yaml", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<List<HelmTemplateYamlRespDto>> getHelmTemplateYamlList(
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @RequestParam("projectId") String projectId) {
        Either<FormatRespDto, List<HelmTemplateYamlRespDto>> either = uploadFileService
            .getHelmTemplateYamlList(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete helm template yaml.
     */
    @ApiOperation(value = "delete helm template yaml", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/helm-template-yaml", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<String> deleteHelmTemplateYamlByFileId(
        @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @RequestParam("fileId") String fileId) {
        Either<FormatRespDto, String> either = uploadFileService.deleteHelmTemplateYamlByFileId(fileId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get sample code.
     */
    @ApiOperation(value = "get sample code", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/samplecode", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getSampleCode(
        @ApiParam(value = "apiFileIds", required = true) @RequestBody List<String> apiFileIds) throws IOException {
        Either<FormatRespDto, ResponseEntity<byte[]>> either = uploadFileService.downloadSampleCode(apiFileIds);
        if (either.isRight()) {
            return either.getRight();
        } else {
            return null;
        }
    }


    @ApiOperation(value = "get sdk code", response = File.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/sdk/{fileId}/download/{lan}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
            )
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<byte[]> getSDKProject(
            @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
            @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
            @Pattern(regexp = REGEX_UUID, message = "lan must be in UUID format")
            @ApiParam(value = "lan", required = true) @PathVariable("lan") String lan
           )throws IOException {
        Either<FormatRespDto, ResponseEntity<byte[]>> either = uploadFileService.getSDKProject(fileId,lan);
        if (either.isRight()) {
            return either.getRight();
        } else {
            return null;
        }
    }

}
