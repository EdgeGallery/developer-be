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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.ibatis.io.Resources;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumDeployPlatform;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumProjectType;
import org.edgegallery.developer.model.workspace.EnumTestConfigStatus;
import org.edgegallery.developer.model.workspace.MepAgentConfig;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfigStageStatus;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.UploadFileService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
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

    private String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";

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
        project.setType("new");
        project.setDeployPlatform(EnumDeployPlatform.KUBERNETES);

        List<OpenMepCapabilityGroup> capabilities = new ArrayList<>();
        OpenMepCapabilityGroup capability = new OpenMepCapabilityGroup("3", "Location", "","", "",
            EnumOpenMepType.OPENMEP, "","");
        List<OpenMepCapabilityDetail> capabilitiesDetail = new ArrayList<>();
        OpenMepCapabilityDetail detail = new OpenMepCapabilityDetail("3", "3", "LocationService", "","version",
            "description");
        capabilitiesDetail.add(detail);
        capability.setCapabilityDetailList(capabilitiesDetail);
        capabilities.add(capability);

        OpenMepCapabilityGroup capabilityGPU = new OpenMepCapabilityGroup("10", "GPU", "","", "", EnumOpenMepType.OPENMEP,"","");
        capabilitiesDetail = new ArrayList<>();
        detail = new OpenMepCapabilityDetail("2", "10", "GPUService-CMCC", "","1.2", "Sample GPU Service");
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

    private Either<FormatRespDto, ApplicationProject> createNewProjectWithNullIcon() throws IOException {
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
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        project.setCreateDate(time.format(new Date()));
        project.setType("new");
        return projectService.createProject(userId, project);
    }

    private Either<FormatRespDto, ApplicationProject> createNewProjectWithErrorIcon() throws IOException {
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
        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        project.setCreateDate(time.format(new Date()));
        project.setType("new");
        project.setIconFileId("1111");
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
    public void testCreateProjectWithNullIcon() throws IOException {
        Either<FormatRespDto, ApplicationProject> response = createNewProjectWithNullIcon();
        Assert.assertEquals(true, response.isLeft());
        Assert.assertEquals("icon file is null", response.getLeft().getErrorRespDto().getDetail());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateProjectWithErrorIcon() throws IOException {
        Either<FormatRespDto, ApplicationProject> response = createNewProjectWithErrorIcon();
        Assert.assertEquals(true, response.isLeft());
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
    public void testDeleteProjectError() throws IOException {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "aaaaa";
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
        //Assert.assertEquals(result.getRight().getTestId(), "00001");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyProject() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        Either<FormatRespDto, ApplicationProject> result = projectService
            .modifyProject(userId, "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e", project.getRight());
        Assert.assertTrue(result.isRight());
        Assert.assertEquals(result.getRight().getUserId(), userId);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyProjectError() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        Either<FormatRespDto, ApplicationProject> result = projectService
            .modifyProject(userId, "aaaa", project.getRight());
        Assert.assertTrue(result.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployProjectError() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        Either<FormatRespDto, ApplicationProject> result = projectService.deployProject(userId, "aaaa", "");
        Assert.assertTrue(result.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateTestConfig() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        ProjectTestConfig test = new ProjectTestConfig();
        test.setProjectId(project.getRight().getId());
        // MEPAgentConfig
        MepAgentConfig agent = new MepAgentConfig();
        agent.setServiceName("codelab2223");
        agent.setHref("codelab2223");
        agent.setPort(32119);
        test.setAgentConfig(agent);
        List<String> imageFileIds = new ArrayList<String>();
        imageFileIds.add("78055873-58cf-4712-8f12-cfdd4e19f268");
        test.setImageFileIds(imageFileIds);

        List<MepHost> hosts = new ArrayList<MepHost>();
        MepHost host = new MepHost();
        host.setHostId("4eb3503e-f546-4580-b946-4fd35e4c727d");
        host.setName("Node2");
        host.setAddress("XIAN");
        host.setArchitecture("ARM");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("192.168.0.5");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        hosts.add(host);
        test.setHosts(hosts);

        UploadedFile apiFile = uploadOneFile("/testdata/plugin.json", "api-file");
        test.setAppApiFileId(apiFile.getFileId());

        test.setAppInstanceId("app-instance-id");

        Either<FormatRespDto, ProjectTestConfig> result = projectService
            .createTestConfig(project.getRight().getUserId(), project.getRight().getId(), test);
        Assert.assertNotNull(result.getRight());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyTestConfig() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        ProjectTestConfig test = new ProjectTestConfig();
        test.setProjectId(project.getRight().getId());
        // MEPAgentConfig
        MepAgentConfig agent = new MepAgentConfig();
        agent.setServiceName("codelab2223");
        agent.setHref("codelab2223");
        agent.setPort(32119);
        test.setAgentConfig(agent);
        List<String> imageFileIds = new ArrayList<String>();
        imageFileIds.add("78055873-58cf-4712-8f12-cfdd4e19f268");
        test.setImageFileIds(imageFileIds);

        List<MepHost> hosts = new ArrayList<MepHost>();
        MepHost host = new MepHost();
        host.setHostId("4eb3503e-f546-4580-b946-4fd35e4c727d");
        host.setName("Node2");
        host.setAddress("XIAN");
        host.setArchitecture("ARM");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("192.168.0.5");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        hosts.add(host);
        test.setHosts(hosts);

        UploadedFile apiFile = uploadOneFile("/testdata/plugin.json", "api-file");
        test.setAppApiFileId(apiFile.getFileId());

        test.setAppInstanceId("app-instance-id");

        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        AccessUserUtil.setUser(project.getRight().getUserId(), "hhh");
        Either<FormatRespDto, ProjectTestConfig> result = projectService
            .modifyTestConfig(project.getRight().getId(), test);
        Assert.assertNotNull(result.getLeft());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetTestConfigError() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        Either<FormatRespDto, ProjectTestConfig> result = projectService.getTestConfig(project.getRight().getId());
        Assert.assertNull(result.getRight());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadProToStoreError1() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        String userId = project.getRight().getUserId();
        String userName = "mec";
        String projectId = project.getRight().getId();
        String token = "";
        Either<FormatRespDto, Boolean> result = projectService.uploadToAppStore(userId, projectId, userName, token);
        Assert.assertTrue(result.isLeft());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadProToStoreError2() throws IOException {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        String userId = "hello";
        String userName = "mec";
        String projectId = project.getRight().getId();
        String token = "";
        Either<FormatRespDto, Boolean> result = projectService.uploadToAppStore(userId, projectId, userName, token);
        Assert.assertTrue(result.isLeft());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testOpenToMecEco() throws Exception {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        Either<FormatRespDto, OpenMepCapabilityGroup> result = projectService
            .openToMecEco(project.getRight().getUserId(), project.getRight().getId());
        Assert.assertTrue(result.isLeft());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCleanEnv() throws Exception {
        Either<FormatRespDto, ApplicationProject> project = createNewProject();
        String userId = project.getRight().getUserId();
        String projectId = project.getRight().getId();
        String token = "test";
        Either<FormatRespDto, Boolean> result = projectService.cleanTestEnv(userId, projectId, token);
        Assert.assertTrue(result.isRight());

    }

    private UploadedFile uploadOneFile(String resourceFile, String fileName) throws IOException {
        MultipartFile uploadFile = new MockMultipartFile(fileName, fileName, null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream(resourceFile));
        Either<FormatRespDto, UploadedFile> uloadFile = uploadFileService.uploadFile(userId, uploadFile);
        return uloadFile.getRight();
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testProcessDeploy() throws Exception {
        projectService.processDeploy();
        Assert.assertTrue(true);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testTerminateProjectNoConfig() throws Exception {
        Either<FormatRespDto, Boolean> res = projectService.terminateProject("userId", "proId", "test");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testTerminateProjectWithNullWorkStatu() throws Exception {
        Either<FormatRespDto, Boolean> res = projectService
            .terminateProject("userId", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85f", "test");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testTerminateProjectWithTerFail() throws Exception {
        Either<FormatRespDto, Boolean> res = projectService
            .terminateProject("userId", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85d", "test");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployProjectWithNoConfig() throws Exception {
        Either<FormatRespDto, ApplicationProject> res = projectService.deployProject("userId", "test", "test");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployProjectWithBadStatus() throws Exception {
        Either<FormatRespDto, ApplicationProject> res = projectService
            .deployProject("userId", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85f", "test");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployProjectSuccess() throws Exception {
        Either<FormatRespDto, ApplicationProject> res = projectService
            .deployProject("f24ea0a2-d8e6-467c-8039-94f0d29bac43", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e", "test");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateCsarPkg() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85d";
        AccessUserUtil.setUser(userId,"test-user");
        Either<FormatRespDto, ApplicationProject> res = projectService.getProject(userId,projectId);
        Assert.assertTrue(res.isRight());
        res.getRight().setDescription("hello");
        Either<FormatRespDto, ProjectTestConfig> resConfig = projectService.getTestConfig(projectId);
        Assert.assertTrue(resConfig.isRight());
        File file = projectService
            .createCsarPkg(userId, res.getRight(), resConfig.getRight());
        Assert.assertNotNull(file);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployTestConfigToAppLcm() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85g";
        AccessUserUtil.setUser(userId,"test-user");
        Either<FormatRespDto, ApplicationProject> res = projectService.getProject(userId,projectId);
      //  Assert.assertTrue(res.isRight());
        Either<FormatRespDto, ProjectTestConfig> resConfig = projectService.getTestConfig(projectId);
       // Assert.assertTrue(resConfig.isRight());
        File file = projectService
            .createCsarPkg(userId, res.getRight(), resConfig.getRight());
        Assert.assertNotNull(file);
        boolean isSuccess = projectService
            .deployTestConfigToAppLcm(file, res.getRight(), resConfig.getRight(),userId,"token");
        Assert.assertEquals(false,isSuccess);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateTestConfigSuccess() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        AccessUserUtil.setUser(userId,"test-user");
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, ProjectTestConfig> resConfig = projectService.getTestConfig(projectId);
        Assert.assertTrue(resConfig.isRight());
        resConfig.getRight().setPrivateHost(true);
        Either<FormatRespDto, ProjectTestConfig> res= projectService
            .createTestConfig(userId,projectId,resConfig.getRight());
        Assert.assertTrue(res.isRight());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadToAppStoreFail() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85d";
        Either<FormatRespDto, Boolean> res= projectService
            .uploadToAppStore(userId,projectId,"hello","hello");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadToAppStoreFail1() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, Boolean> res= projectService
            .uploadToAppStore(userId,projectId,"hello","hello");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadToAppStoreFail2() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85d";
        Either<FormatRespDto, Boolean> res= projectService
            .uploadToAppStore(userId,projectId,"hello","hello");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testOpenToMecFail() throws Exception {
        Either<FormatRespDto, OpenMepCapabilityGroup> res= projectService
            .openToMecEco("test","test1");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testOpenToMecFail1() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85d";
        Either<FormatRespDto, OpenMepCapabilityGroup> res= projectService
            .openToMecEco(userId,projectId);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testOpenToMecFail2() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85f";
        Either<FormatRespDto, OpenMepCapabilityGroup> res= projectService
            .openToMecEco(userId,projectId);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testOpenToMecSuccess() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85h";
        Either<FormatRespDto, OpenMepCapabilityGroup> res= projectService
            .openToMecEco(userId,projectId);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCleanEnvBad() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        Either<FormatRespDto, Boolean> res= projectService
            .cleanTestEnv(userId,"aa","aa");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCleanEnvWithNoConfig() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85f";
        Either<FormatRespDto, Boolean> res= projectService
            .cleanTestEnv(userId,projectId,"aa");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCleanEnvSuccess() throws Exception {
        String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85h";
        Either<FormatRespDto, Boolean> res= projectService
            .cleanTestEnv(userId,projectId,"aa");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateDeployResult() throws Exception {
        ApplicationProject project = new ApplicationProject();
        ProjectTestConfig testConfig = new ProjectTestConfig();
        ProjectTestConfigStageStatus stageStatus = new ProjectTestConfigStageStatus();
        testConfig.setStageStatus(stageStatus);
        projectService.updateDeployResult(testConfig,project,"workStatus", EnumTestConfigStatus.Success);
        Assert.assertTrue(true);
    }

    @Test(expected= InvocationException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateATPTestTask() throws Exception {
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        String token = "";
        Either<FormatRespDto, Boolean> res = projectService.createAtpTestTask(projectId, token,"EnumTestConfigStatus.Success");
    }





















}
