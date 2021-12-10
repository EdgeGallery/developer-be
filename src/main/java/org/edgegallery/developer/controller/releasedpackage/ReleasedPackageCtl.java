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

package org.edgegallery.developer.controller.releasedpackage;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgReqDto;
import org.edgegallery.developer.model.restful.ErrorRespDto;
import org.edgegallery.developer.service.releasedpackage.ReleasedPackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RestSchema(schemaId = "ReleasedPackage")
@RequestMapping("/mec/developer/v2/released-packages")
@Api(tags = "ReleasedPackage")
@Validated
public class ReleasedPackageCtl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleasedPackageCtl.class);

    @Autowired
    private ReleasedPackageService releasedPackageService;

    /**
     * synchronize app pkg from app store.
     *
     * @param pkgReqDtos app id and package id list
     * @return if success return true or false
     */
    @ApiOperation(value = "Get app package by packageId.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> synchronizeAppPkg(
        @NotNull @ApiParam(value = "pkgReqDtos", required = true) @RequestBody List<ReleasedPkgReqDto> pkgReqDtos) {
        LOGGER.info("enter synchronizeAppPkg method....");
        User user = AccessUserUtil.getUser();
        return ResponseEntity.ok(releasedPackageService.synchronizePackage(user, pkgReqDtos));
    }

    /**
     * get all synchronized pkg.
     *
     * @param name query condition (app package name)
     * @param limit page limit
     * @param offset page offset
     * @return return all synchronized pkg paging data
     */
    @ApiOperation(value = "Get app package by packageId.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class)
    })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Page<ReleasedPackage>> getAllAppPkg(
        @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        LOGGER.info("enter getAllAppPkg method ....");
        return ResponseEntity.ok(releasedPackageService.getAllPackages(name, limit, offset));
    }

    /**
     * get app pkg structure.
     *
     * @param packageId app package id
     * @return return AppPkgFile list
     */
    @ApiOperation(value = "Get package structure", response = List.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 400, message = "Bad Request", response = List.class)
    })
    @RequestMapping(value = "/{packageId}/action/get-pkg-structure", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<AppPkgFile>> getAppPackageStructure(
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId) {
        LOGGER.info("enter getAppPackageStructure method ....");
        return ResponseEntity.ok(releasedPackageService.getAppPkgStructure(packageId));
    }

    /**
     * get app pkg file content.
     *
     * @param structureReqDto body param(inner file path)
     * @param packageId package id
     * @return return file path and content
     */
    @ApiOperation(value = "Get app package file content", response = ReleasedPkgFileContent.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ReleasedPkgFileContent.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ReleasedPkgFileContent.class)
    })
    @RequestMapping(value = "/{packageId}/action/get-file-content", method = RequestMethod.POST)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<ReleasedPkgFileContent> getAppPkgFile(
        @NotNull @ApiParam(value = "filePath", required = true) @RequestBody
            ReleasedPkgFileContentReqDto structureReqDto,
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId) {
        LOGGER.info("enter getAppPkgFile method ....");
        return ResponseEntity.ok(releasedPackageService.getAppPkgFileContent(structureReqDto, packageId));
    }

    /**
     * edit app pkg file content.
     *
     * @param releasedPkgFileContent body param(inner file path and content)
     * @param packageId package id
     * @return return file path and content
     */
    @ApiOperation(value = "Update app package file content", response = ReleasedPkgFileContent.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ReleasedPkgFileContent.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ReleasedPkgFileContent.class)
    })
    @RequestMapping(value = "/{packageId}/action/edit-file-content", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<ReleasedPkgFileContent> updateAppPackageFileContent(
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId, @NotNull @ApiParam(value = "releasedPkgFileContent", required = true) @RequestBody
        ReleasedPkgFileContent releasedPkgFileContent) {
        LOGGER.info("enter updateAppPackageFileContent method ....");
        return ResponseEntity.ok(releasedPackageService.editAppPkgFileContent(releasedPkgFileContent, packageId));
    }

    /**
     * delete app pkg info.
     *
     * @param packageId package id
     * @return if success return true or return false
     */
    @ApiOperation(value = "delete app package info", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = Boolean.class)
    })
    @RequestMapping(value = "/{packageId}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteAppPackage(
        @ApiParam(value = "packageId", required = true) @PathVariable(value = "packageId", required = true)
            String packageId) {
        LOGGER.info("enter deleteAppPackage method ....");
        return ResponseEntity.ok(releasedPackageService.deleteAppPkg(packageId));
    }

    /**
     * release app.
     *
     * @param publishAppDto body param(is free and price)
     * @param packageId package id
     * @return if success return true or return false
     */
    @ApiOperation(value = "release app.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{packageId}/action/release", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> releaseApp(
        @ApiParam(value = "packageId", required = true) @PathVariable String packageId,
        @ApiParam(value = "publishAppDto", required = true) @RequestBody PublishAppReqDto publishAppDto) {
        LOGGER.info("enter releaseApp method ....");
        User user = AccessUserUtil.getUser();
        return ResponseEntity.ok(releasedPackageService.releaseAppPkg(user, publishAppDto, packageId));
    }

}
