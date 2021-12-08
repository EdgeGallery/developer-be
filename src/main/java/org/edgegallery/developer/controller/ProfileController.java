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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.QueryParam;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.profile.ProfileInfo;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.profile.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "profiles")
@RequestMapping("/mec/developer/v2/profiles")
@Api(tags = "profiles")
public class ProfileController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private ProfileService profileService;

    /**
     * create profile.
     *
     * @param file profile zip file
     * @return profile info
     */
    @ApiOperation(value = "create profile.", response = ProfileInfo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProfileInfo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<ProfileInfo> createProfile(
        @ApiParam(value = "profile zip file", required = true) @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.createProfile(file));
    }

    /**
     * update profile.
     *
     * @param file profile zip file
     * @return profile info
     */
    @ApiOperation(value = "update profile.", response = ProfileInfo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProfileInfo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{profileId}", method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<ProfileInfo> updateProfile(
        @Pattern(regexp = REGEX_UUID, message = "profileId must be in UUID format")
        @ApiParam(value = "profileId", required = true) @PathVariable("profileId") String profileId,
        @ApiParam(value = "profile zip file", required = true) @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.updateProfile(file, profileId));
    }

    /**
     * get all profiles.
     *
     * @param limit limit
     * @param offset offset
     * @return profile info list
     */
    @ApiOperation(value = "get all profiles.", response = ProfileInfo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProfileInfo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Page<ProfileInfo>> getAllProfiles(
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @QueryParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @QueryParam("offset") int offset,
        @ApiParam(value = "profile name", required = false) @Size(max = Consts.LENGTH_64) @QueryParam("name")
            String name) {
        return ResponseEntity.ok(profileService.getAllProfiles(limit, offset, name));
    }

    /**
     * get profile by id.
     *
     * @param profileId profile id
     * @return profile info
     */
    @ApiOperation(value = "get one profile by id.", response = ProfileInfo.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProfileInfo.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{profileId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<ProfileInfo> getProfileById(
        @Pattern(regexp = REGEX_UUID, message = "profileId must be in UUID format")
        @ApiParam(value = "profileId", required = true) @PathVariable("profileId") String profileId) {
        return ResponseEntity.ok(profileService.getProfileById(profileId));
    }

    /**
     * delete profile by id.
     *
     * @param profileId profile id
     * @return true
     */
    @ApiOperation(value = "delete profile by id.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{profileId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteProfileById(
        @Pattern(regexp = REGEX_UUID, message = "profileId must be in UUID format")
        @ApiParam(value = "profileId", required = true) @PathVariable("profileId") String profileId) {
        return ResponseEntity.ok(profileService.deleteProfileById(profileId));
    }

    /**
     * download file by profile id.
     *
     * @param profileId profile id
     * @return file content
     */
    @ApiOperation(value = "download profile by id.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{profileId}/action/download", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<byte[]> downloadFileById(
        @Pattern(regexp = REGEX_UUID, message = "profileId must be in UUID format")
        @ApiParam(value = "profileId", required = true) @PathVariable("profileId") String profileId,
        @ApiParam(value = "file type", required = false) @Size(max = Consts.LENGTH_64) @QueryParam("type") String type,
        @ApiParam(value = "file name", required = false) @Size(max = Consts.LENGTH_64) @QueryParam("name")
            String name) {
        return profileService.downloadFileById(profileId, type, name);
    }

    /**
     * create application by profile id.
     *
     * @param profileId profile id
     * @param iconFile icon file
     * @return application info
     */
    @ApiOperation(value = "create application by profile id.", response = Application.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Application.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{profileId}/create-application", method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Application> createAppByProfileId(
        @Pattern(regexp = REGEX_UUID, message = "profileId must be in UUID format")
        @ApiParam(value = "profileId", required = true) @PathVariable("profileId") String profileId,
        @ApiParam(value = "icon file", required = true) @RequestPart("iconFile") MultipartFile iconFile) {
        return ResponseEntity.ok(profileService.createAppByProfileId(profileId, iconFile));
    }
}
