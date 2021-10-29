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

package org.edgegallery.developer.controller.uploadfile;

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
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.uploadfile.UploadService;
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
@RestSchema(schemaId = "filesv2")
@RequestMapping("/mec/developer/v2/files")
@Api(tags = "Filev2")
public class UploadFileController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private UploadService uploadFileService;

    /**
     * get file stream.
     */
    @ApiOperation(value = "get a file", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getFile(@Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
    @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId, @ApiParam(value = "type") @RequestParam("type") String type) {
        return uploadFileService.getFile(fileId, userId, type);
    }

    /**
     * get file Echo use.
     */
    @ApiOperation(value = "get a file", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}/api-info", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<UploadedFile> getApiFile(
        @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format") @ApiParam(value = "userId")
        @RequestParam("userId") String userId) {
        return ResponseEntity.ok(uploadFileService.getApiFile(fileId, userId));
    }

    /**
     * upload file.
     */
    @ApiOperation(value = "upload pic file", response = UploadedFile.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = UploadedFile.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<UploadedFile> uploadPicture(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile uploadFile,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "fileType", required = true) @RequestParam("fileType") String fileType) {
        return ResponseEntity.ok(uploadFileService.uploadFile(fileType, uploadFile));
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
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getSampleCode(
        @ApiParam(value = "apiFileIds", required = true) @RequestBody List<String> apiFileIds) {
        return uploadFileService.downloadSampleCode(apiFileIds);
    }

    /**
     * get sample code struc.
     */
    @ApiOperation(value = "get sample code struc", response = AppPkgStructure.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppPkgStructure.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/samplecode/get-pkg-structure", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<AppPkgStructure> getSampleCodeStru(
        @ApiParam(value = "apiFileIds", required = true) @RequestBody List<String> apiFileIds) {
        return ResponseEntity.ok(uploadFileService.getSampleCodeStru(apiFileIds));
    }

    /**
     * +
     * get sample code content.
     */
    @ApiOperation(value = "get sample code content", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/samplecode/get-file-content", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<String> getSampleCodeContent(
        @ApiParam(value = "fileName", required = true) @RequestParam String fileName) {
        return ResponseEntity.ok(uploadFileService.getSampleCodeContent(fileName));
    }

    /**
     * get sdk code.
     */
    @ApiOperation(value = "get sdk code", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/sdk/{fileId}/download/{lan}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getSdkProject(@Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
    @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REGEX_UUID, message = "lan must be in UUID format") @ApiParam(value = "lan", required = true)
        @PathVariable("lan") String lan) throws IOException {
        return uploadFileService.getSdkProject(fileId, lan);
    }

}
