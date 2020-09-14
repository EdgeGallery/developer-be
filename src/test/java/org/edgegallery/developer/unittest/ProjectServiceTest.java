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

package org.edgegallery.developer.unittest;

import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.ibatis.io.Resources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumProjectType;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.UploadFileService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ProjectServiceTest {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UploadFileService uploadFileService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    private Either<FormatRespDto, ApplicationProject> createNewProject() throws IOException {
        String userId = "test-user";
        ApplicationProject project = new ApplicationProject();
        project.setStatus(EnumProjectStatus.ONLINE);
        project.setName("test_app_1");
        project.setVersion("1.0.1");
        project.setProvider("huawei");
        List<String> platforms = new ArrayList<>();
        platforms.add("KunPeng");
        project.setPlatform(platforms);
        project.setUserId("8595621b-d567-4331-b964-c3288815bd7b");
        project.setProjectType(EnumProjectType.CREATE_NEW);
        project.setCreateDate(new Date());
        project.setType("new");

        List<OpenMepCapabilityGroup> capabilities = new ArrayList<>();
        OpenMepCapabilityGroup capability = new OpenMepCapabilityGroup("3", "Location", EnumOpenMepType.OPENMEP);
        List<OpenMepCapabilityDetail> capabilitiesDetail = new ArrayList<>();
        OpenMepCapabilityDetail detail = new OpenMepCapabilityDetail("3", "3", "LocationService", "version",
            "description");
        capabilitiesDetail.add(detail);
        capability.setCapabilityDetailList(capabilitiesDetail);
        capabilities.add(capability);

        OpenMepCapabilityGroup capabilityGPU = new OpenMepCapabilityGroup("10", "GPU", EnumOpenMepType.OPENMEP);
        capabilitiesDetail = new ArrayList<>();
        detail = new OpenMepCapabilityDetail("2", "10", "GPUService-CMCC", "1.2", "Sample GPU Service");
        capabilitiesDetail.add(detail);
        capabilityGPU.setCapabilityDetailList(capabilitiesDetail);
        capabilities.add(capabilityGPU);

        project.setCapabilityList(capabilities);

        File icon = Resources.getResourceAsFile("testdata/test-icon.png");
        FileItem item = DeveloperFileUtils.createFileItem(icon, icon.getName());
        MultipartFile mockFile = new CommonsMultipartFile(item);

        Either<FormatRespDto, UploadedFile> either = uploadFileService.uploadFile(userId, mockFile);
        UploadedFile result = either.getRight();
        project.setIconFileId(result.getFileId());

        return projectService.createProject(userId, project);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateProject() throws IOException {
        Either<FormatRespDto, ApplicationProject> response = createNewProject();
        Assert.assertEquals(true, response.isRight());
        Assert.assertNotNull(response.getRight().getId());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetProject() throws IOException {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, ApplicationProject> response = projectService.getProject(userId, projectId);
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAllProject() throws IOException {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        Either<FormatRespDto, List<ApplicationProject>> response = projectService.getAllProjects(userId);
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteProject() throws IOException {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, Boolean> response = projectService.deleteProject(userId, projectId);
        Assert.assertTrue(response.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateTestConfigError() {
        Either<FormatRespDto, ProjectTestConfig> response = projectService
            .createTestConfig("18db0288-3c67-4042-a708-a8e4a10c6b3", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e", null);
        Assert.assertEquals(400, response.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyTestConfigError() {
        Either<FormatRespDto, ProjectTestConfig> response = projectService
            .modifyTestConfig("71481045-1344-4073-98b1-cec155470273", null);
        Assert.assertEquals(400, response.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetTestConfig() throws IOException {
        Either<FormatRespDto, ProjectTestConfig> result = projectService
            .getTestConfig("200dfab1-3c30-4fc7-a6ca-ed6f0620a85e");
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(result.getRight().getTestId(), "00001");
    }
}
