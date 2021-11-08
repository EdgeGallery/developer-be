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
import java.util.List;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.mapper.uploadfile.UploadFileMapper;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.service.uploadfile.impl.UploadFileServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "upload-file")
@RequestMapping("/mec/developer/v2/upload-file")
@Api(tags = "upload-file")
public class UploadFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileServiceImpl.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private UploadFileMapper uploadFileMapper;

    @Autowired
    private CapabilityMapper capabilityMapper;

    /**
     * get file stream.
     */
    @ApiOperation(value = "get a file", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}/action/get-file-stream", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getFileStream(@Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
    @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId) {
        String userId = AccessUserUtil.getUserId();
        UploadFile uploadFile = uploadFileService.getFile(fileId);
        if (uploadFile == null) {
            LOGGER.error("can not find file {} in db.", fileId);
            throw new EntityNotFoundException("can not find file in db!", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + uploadFile.getFileName());
        byte[] fileData = uploadFileService.getFileStream(uploadFile, userId);
        return ResponseEntity.ok().headers(headers).body(fileData);
    }

    /**
     * get file Echo use.
     */
    @ApiOperation(value = "get a file", response = UploadFile.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = UploadFile.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<UploadFile> getFile(
        @Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
        @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId) {
        return ResponseEntity.ok(uploadFileService.getFile(fileId));
    }

    /**
     * upload file.
     */
    @ApiOperation(value = "upload file", response = UploadFile.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = UploadFile.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<UploadFile> uploadFile(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile uploadFile,
        @ApiParam(value = "fileType", required = true) @RequestParam("fileType") String fileType) {
        String userId = AccessUserUtil.getUserId();
        return ResponseEntity.ok(uploadFileService.uploadFile(userId, fileType, uploadFile));
    }

    /**
     * delete file.
     */
    @ApiOperation(value = "delete file", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteFile(@Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
    @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId) {
        return ResponseEntity.ok(uploadFileService.deleteFile(fileId));
    }

    /**
     * download sample code.
     */
    @ApiOperation(value = "download sample code", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/action/download-sample-code", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getSampleCode(
        @ApiParam(value = "apiFileIds", required = true) @RequestBody List<String> apiFileIds) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add("Content-Disposition", "attachment; filename=SampleCode.tgz");
        byte[] fileData = uploadFileService.downloadSampleCode(apiFileIds);
        return ResponseEntity.ok().headers(headers).body(fileData);
    }

    /**
     * get sample code structure.
     */
    @ApiOperation(value = "get sample code structure", response = AppPkgStructure.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AppPkgStructure.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/action/get-sample-code-structure", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<AppPkgStructure> getSampleCodeStructure(
        @ApiParam(value = "apiFileIds", required = true) @RequestBody List<String> apiFileIds) {
        return ResponseEntity.ok(uploadFileService.getSampleCodeStru(apiFileIds));
    }

    /**
     * get sample code content.
     */
    @ApiOperation(value = "get sample code content", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/action/get-sample-code-content", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<String> getSampleCodeContent(
        @ApiParam(value = "fileName", required = true) @RequestParam String fileName) {
        return ResponseEntity.ok(uploadFileService.getSampleCodeContent(fileName));
    }

    /**
     * download sdk code.
     */
    @ApiOperation(value = "get sdk code", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{fileId}/action/download-sdk", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<byte[]> getSdkProject(@Pattern(regexp = REGEX_UUID, message = "fileId must be in UUID format")
    @ApiParam(value = "fileId", required = true) @PathVariable("fileId") String fileId,
        @Pattern(regexp = REGEX_UUID, message = "lan must be in UUID format") @ApiParam(value = "lan", required = true)
        @RequestParam("lan") String lan) {
        List<Capability> capabilities = capabilityMapper.selectByApiFileId(fileId);
        if (CollectionUtils.isEmpty(capabilities)) {
            LOGGER.error("can not find capability in db by api file id");
            throw new EntityNotFoundException("can not find capability in db", ResponseConsts.RET_QUERY_DATA_EMPTY);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add("Content-Disposition", "attachment; filename=" + capabilities.get(0).getHost() + ".tgz");
        byte[] fileData = uploadFileService.getSdkProject(fileId, lan, capabilities);
        return ResponseEntity.ok().headers(headers).body(fileData);
    }

}
