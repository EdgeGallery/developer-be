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
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.response.ErrorRespDto;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.ProjectImageResponse;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.util.ApiEmulatorMgr;
import org.edgegallery.developer.util.ResponseDataUtil;
import org.hibernate.validator.constraints.Length;
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
@RestSchema(schemaId = "projects")
@RequestMapping("/mec/developer/v1/projects")
@Api(tags = "Project")
@Validated
public class ProjectController {

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REGEX_USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ApiEmulatorMgr apiEmulatorMgr;

    /**
     * get all project.
     */
    @ApiOperation(value = "get users all projects ", response = ApplicationProject.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApplicationProject.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<List<ApplicationProject>> getAllProjects(
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, List<ApplicationProject>> either = projectService.getAllProjects(userId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get one project by projectId.
     */
    @ApiOperation(value = "get one project by projectId", response = ApplicationProject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApplicationProject.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_GUEST')")
    public ResponseEntity<ApplicationProject> getProject(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, ApplicationProject> either = projectService.getProject(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * create one project.
     */
    @ApiOperation(value = "create one project", response = ApplicationProject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApplicationProject.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ApplicationProject> createProject(
        @NotNull @ApiParam(value = "ApplicationProject", required = true) @RequestBody ApplicationProject project,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId, HttpServletRequest request)
        throws IOException {

        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, ApplicationProject> either = projectService.createProject(userId, project);
        //        if (either.isRight()) {
        ////            apiEmulatorMgr.createApiEmulatorIfNotExist(userId, token);
        ////        }
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * delete one project.
     */
    @ApiOperation(value = "delete one project", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteProject(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        Either<FormatRespDto, Boolean> either = projectService.deleteProject(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modify one project.
     */
    @ApiOperation(value = "modify one project", response = ApplicationProject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApplicationProject.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ApplicationProject> modifyProject(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @NotNull @ApiParam(value = "ApplicationProject", required = true) @RequestBody ApplicationProject project,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, ApplicationProject> either = projectService.modifyProject(userId, projectId, project);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * deploy one project.
     */
    @ApiOperation(value = "deploy one project", response = ApplicationProject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApplicationProject.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/deploy", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ApplicationProject> deployProject(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, ApplicationProject> either = projectService.deployProject(userId, projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * terminate one project.
     */
    @ApiOperation(value = "terminate one project", response = ApplicationProject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ApplicationProject.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/terminate", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> terminateProject(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = projectService.terminateProject(userId, projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * clean the test environment.
     */
    @ApiOperation(value = "clean the test environment", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/clean", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> clean(@Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
    @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        HttpServletRequest request) {
        Either<FormatRespDto, Boolean> either = projectService.cleanTestEnv(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * create one test configuration.
     */
    @ApiOperation(value = "create one test configuration", response = ProjectTestConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProjectTestConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/test-config", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ProjectTestConfig> createTestConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @NotNull @ApiParam(value = "ProjectTestConfig", required = true) @RequestBody ProjectTestConfig testConfig,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, ProjectTestConfig> either = projectService
            .createTestConfig(userId, projectId, testConfig);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modify one test configuration.
     */
    @ApiOperation(value = "modify one test configuration", response = ProjectTestConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProjectTestConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/test-config", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ProjectTestConfig> modifyTestConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @NotNull @ApiParam(value = "ProjectTestConfig", required = true) @RequestBody ProjectTestConfig testConfig) {
        Either<FormatRespDto, ProjectTestConfig> either = projectService.modifyTestConfig(projectId, testConfig);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get one test configuration.
     */
    @ApiOperation(value = "get one test configuration", response = ProjectTestConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProjectTestConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/test-config", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ProjectTestConfig> getTestConfig(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId) {
        Either<FormatRespDto, ProjectTestConfig> either = projectService.getTestConfig(projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * upload this project to AppStore.
     */
    @ApiOperation(value = "upload this project to AppStore.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/upload", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> uploadToAppStore(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @NotNull @Length(min = 6, max = 30) @Pattern(regexp = REGEX_USERNAME,
            message = "username can only be a combination of letters and numbers, the length is 6 to 30")
        @ApiParam(value = "userName", required = true) @RequestParam(value = "userName", required = true)
            String userName,HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = projectService
            .uploadToAppStore(userId, projectId, userName, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * open this project to mec eco.
     */
    @ApiOperation(value = "open this project to mec eco", response = OpenMepCapabilityGroup.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OpenMepCapabilityGroup.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/open-api", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<OpenMepCapabilityGroup> openToMecEco(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId,
        @NotNull @ApiParam(value = "AbilityOpenConfig", required = true) @RequestBody
            OpenMepCapabilityDetail abilityOpenConfig) {
        Either<FormatRespDto, OpenMepCapabilityGroup> either = projectService.openToMecEco(userId, projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * addImageToProject.
     */
    @ApiOperation(value = "addImageToProject.", response = ProjectImageConfig.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProjectImageConfig.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/image", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ProjectImageConfig> addImageToProject(
        @NotNull @ApiParam(value = "ProjectImageConfig", required = true) @RequestBody ProjectImageConfig imageConfig,
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId) {
        Either<FormatRespDto, ProjectImageConfig> either = projectService.createProjectImage(projectId, imageConfig);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * ProjectImageConfig.
     */
    @ApiOperation(value = "ProjectImageConfig.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/image/{imageId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> deleteImageById(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        @Pattern(regexp = REGEX_UUID, message = "imageId must be in UUID format")
        @ApiParam(value = "imageId", required = true) @PathVariable("imageId") String imageId) {
        Either<FormatRespDto, Boolean> either = projectService.deleteImageById(projectId, imageId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * getImagesByProjectId.
     */
    @ApiOperation(value = "getImagesByProjectId.", response = ProjectImageResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ProjectImageResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/image", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<ProjectImageResponse> getImagesByProjectId(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId) {
        Either<FormatRespDto, ProjectImageResponse> either = projectService.getImagesByProjectId(projectId);
        return ResponseDataUtil.buildResponse(either);
    }

    @ApiOperation(value = "create atp test task.", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Accept", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{projectId}/action/atp", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT')")
    public ResponseEntity<Boolean> createATPTestTask(
        @Pattern(regexp = REGEX_UUID, message = "projectId must be in UUID format")
        @ApiParam(value = "projectId", required = true) @PathVariable("projectId") String projectId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = projectService.createATPTestTask(projectId, token);
        return ResponseDataUtil.buildResponse(either);
    }
}
