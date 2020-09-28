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

package org.edgegallery.developer.apitest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.ApplicationProject;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.EnumProjectImage;
import org.edgegallery.developer.model.workspace.EnumProjectStatus;
import org.edgegallery.developer.model.workspace.EnumProjectType;
import org.edgegallery.developer.model.workspace.EnumTestStatus;
import org.edgegallery.developer.model.workspace.MepAgentConfig;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.model.workspace.ProjectImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ProjectService;
import org.edgegallery.developer.service.UploadFileService;
import org.edgegallery.developer.unittest.UploadFilesServiceTest;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class CreateProjectTest {

    private Gson gson = new Gson();

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProjectService projectService;

    private UploadedFile iconFile = null;

    private ApplicationProject testProject = null;

    private String userId = "f24ea0a2-d8e6-467c-8039-94f0d29bac43";

    private String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";

    @Before
    @WithMockUser(roles = "DEVELOPER_TENENT")
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private UploadedFile uploadOneFile(String resourceFile, String fileName) throws IOException {
        MultipartFile uploadFile = new MockMultipartFile(fileName, fileName, null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream(resourceFile));
        Either<FormatRespDto, UploadedFile> uoloadFile = uploadFileService.uploadFile(userId, uploadFile);
        return uoloadFile.getRight();
    }

    @After
    public void clean() throws IOException {
    }

    private void deleteTempFile(UploadedFile uploadFile) {
        String realPath = InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath();
        File temp = new File(realPath);
        if (temp.exists()) {
            DeveloperFileUtils.deleteTempFile(temp);
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createProject() throws Exception {
        ApplicationProject project = new ApplicationProject();
        project.setStatus(EnumProjectStatus.ONLINE);
        project.setName("test_app_1");
        project.setVersion("1.0.1");
        project.setProvider("huawei");
        List<String> platforms = new ArrayList<>();
        platforms.add("KunPeng");
        project.setPlatform(platforms);
        project.setUserId(userId);
        project.setProjectType(EnumProjectType.CREATE_NEW);
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
        iconFile = uploadOneFile("/testdata/face.png", "face");
        project.setIconFileId(iconFile.getFileId());

        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/developer/v1/projects/?userId=" + userId).with(csrf())
                .content(gson.toJson(project)).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private ApplicationProject createNewProject() throws Exception {
        ApplicationProject project = new ApplicationProject();
        project.setStatus(EnumProjectStatus.ONLINE);
        project.setName("test_app_1");
        project.setVersion("1.0.1");
        project.setProvider("huawei");
        List<String> platforms = new ArrayList<>();
        platforms.add("KunPeng");
        project.setPlatform(platforms);
        project.setUserId(userId);
        project.setProjectType(EnumProjectType.CREATE_NEW);
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
        iconFile = uploadOneFile("/testdata/face.png", "face");
        project.setIconFileId(iconFile.getFileId());

        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/developer/v1/projects/?userId=" + userId).with(csrf())
                .content(gson.toJson(project)).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        return gson.fromJson(result.andReturn().getResponse().getContentAsString(), ApplicationProject.class);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createTestConfig() throws Exception {
        ApplicationProject project = createNewProject();
        ProjectTestConfig test = new ProjectTestConfig();
        test.setProjectId(project.getId());
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
        host.setIp("192.168.0.5");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        hosts.add(host);
        test.setHosts(hosts);

        UploadedFile apiFile = uploadOneFile("/testdata/plugin.json", "api-file");
        test.setAppApiFileId(apiFile.getFileId());

        test.setAppInstanceId("app-instance-id");

        String url = String
            .format("/mec/developer/v1/projects/%s/test-config?userId=%s", project.getId(), project.getUserId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url).with(csrf());
        request.content(gson.toJson(test));
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetTestConfig() throws Exception {

        String url = String.format("/mec/developer/v1/projects/?userId=%s", userId);
        mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetOneTestConfig() throws Exception {
        ApplicationProject project = createNewProject();
        String url = String.format("/mec/developer/v1/projects/" + project.getId() + "/test-config");
        mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeployProject() throws Exception {
        ApplicationProject project = createNewProject();
        String url = String
            .format("/mec/developer/v1/projects/" + project.getId() + "/action/deploy?userId=%s", project.getUserId());
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAllProjects() throws Exception {

        String url = String.format("/mec/developer/v1/projects/?userId=%s", userId);
        mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadToAppStore() throws Exception {

        ApplicationProject project = createNewProject();
        String url = String
            .format("/mec/developer/v1/projects/" + project.getId() + "/action/upload?userId=%s&userName=%s", userId,
                "lidazhao");
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testOpenApi() throws Exception {

        ApplicationProject project = createNewProject();
        String url = String
            .format("/mec/developer/v1/projects/" + project.getId() + "/action/open-api?userId=%s", userId);
        mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetProjectByIdCorrect() throws Exception {
        String url = String.format(
            "/mec/developer/v1/projects/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e?userId=f24ea0a2-d8e6-467c-8039-94f0d29bac43");
        mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetProjectByIdFailed1() throws Exception {

        String url = String.format("/mec/developer/v1/projects/%s?userId=%s", "111111111111111", userId);
        mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetProjectByIdFailed2() throws Exception {
        String url = String
            .format("/mec/developer/v1/projects/%s?userId=%s", "5cb37730-09c5-42e5-a638-6e1a3b8836b9", userId);
        mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteProjectById() throws Exception {
        String url = String.format("/mec/developer/v1/projects/%s?userId=%s", projectId, userId);
        mvc.perform(MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteProjectByIdBad() throws Exception {

        String url = String.format("/mec/developer/v1/projects/%s?userId=%s", "222222222", userId);
        mvc.perform(MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateProjectById() throws Exception {
        ApplicationProject project = new ApplicationProject();
        project.setProjectType(EnumProjectType.CREATE_NEW);
        project.setName("victor_test_new");
        project.setVersion("1.0");
        project.setProvider("Huawei");
        List<String> platforms = new ArrayList<>();
        platforms.add("KunPeng");
        project.setPlatform(platforms);
        project.setType("Video");
        project.setDescription("test");
        project.setIconFileId("d71bd9cf-17f5-4477-9248-597c68c75541");
        project.setStatus(EnumProjectStatus.ONLINE);

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
        project.setLastTestId(null);
        project.setCreateDate(null);

        String url = String.format("/mec/developer/v1/projects/%s?userId=%s", "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e",
            "f24ea0a2-d8e6-467c-8039-94f0d29bac43");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(url).with(csrf());
        request.content(gson.toJson(project));
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private void deployProject(ApplicationProject project) throws Exception {
        String url = String
            .format("/mec/developer/v1/projects/%s/action/deploy?userId=%s", project.getId(), project.getUserId());
        mvc.perform(MockMvcRequestBuilders.post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private ApplicationProject getProjectById(String projectId, String userId) throws Exception {
        String url = String.format("/mec/developer/v1/projects/%s?userId=%s", projectId, userId);
        ResultActions result = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        return gson.fromJson(result.andReturn().getResponse().getContentAsString(), ApplicationProject.class);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCleanTestEnvProjectNull() throws Exception {
        String url = String
            .format("/mec/developer/v1/projects/%s/action/clean?userId=%s&completed=%s", "can not find projectId",
                userId, false);
        mvc.perform(MockMvcRequestBuilders.post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCleanTestEnvProjectTestConfigNull() throws Exception {
        String url = String
            .format("/mec/developer/v1/projects/%s/action/clean?userId=%s&completed=%s", projectId, userId, false);
        mvc.perform(MockMvcRequestBuilders.post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateTestConfigCorrect() throws Exception {

        ProjectTestConfig test = new ProjectTestConfig();
        test.setTestId(UUID.randomUUID().toString());
        test.setProjectId("a66c4932-d254-4596-bec1-8ac7d80c2631");
        // MEPAgentConfig
        MepAgentConfig agent = new MepAgentConfig();
        agent.setServiceName("codelab2223");
        agent.setHref("codelab2223");
        agent.setPort(32119);
        test.setAgentConfig(agent);
        List<String> imageFileIds = new ArrayList<String>();
        imageFileIds.add("3c056221-65c9-4845-8e0a-84126c286142");
        imageFileIds.add("09f9d78f-93fb-47bc-88bc-1a2b805d6bf4");
        test.setImageFileIds(imageFileIds);
        // MEPHost
        List<MepHost> hosts = new ArrayList<MepHost>();
        MepHost host = new MepHost();
        host.setHostId("4eb3503e-f546-4580-b946-4fd35e4c727d");
        host.setName("Node2");
        host.setAddress("XIAN");
        host.setArchitecture("ARM");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setIp("192.168.0.5");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        hosts.add(host);
        test.setHosts(hosts);

        UploadedFile apiFile = uploadOneFile("/testdata/plugin.json", "api-file");
        test.setAppApiFileId(apiFile.getFileId());

        test.setAppInstanceId("app-instance-id");
        String url = String.format("/mec/developer/v1/projects/%s/test-config?userId=%s", projectId, userId);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url).with(csrf());
        request.content(gson.toJson(test));
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        this.deleteTempFile(apiFile);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyTestConfig() throws Exception {

        ProjectTestConfig test = new ProjectTestConfig();
        test.setProjectId("71481045-1344-4073-98b1-cec155470273");
        // MEPAgentConfig
        MepAgentConfig agent = new MepAgentConfig();
        agent.setServiceName("codelab2223");
        agent.setHref("codelab2223");
        agent.setPort(32119);
        test.setAgentConfig(agent);
        List<String> imageFileIds = new ArrayList<String>();
        imageFileIds.add("3c056221-65c9-4845-8e0a-84126c286142");
        imageFileIds.add("09f9d78f-93fb-47bc-88bc-1a2b805d6bf4");
        test.setImageFileIds(imageFileIds);
        // MEPHost
        List<MepHost> hosts = new ArrayList<MepHost>();
        MepHost host = new MepHost();
        host.setHostId("4eb3503e-f546-4580-b946-4fd35e4c727d");
        host.setName("Node2");
        host.setAddress("XIAN");
        host.setArchitecture("ARM");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setIp("159.138.53.90");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        hosts.add(host);
        test.setHosts(hosts);
        test.setAppApiFileId("f5a1a689-d606-41ed-a5f3-526b0e56004c");
        test.setStatus(EnumTestStatus.IMAGE_PULL_BACKOFF);
        test.setAccessUrl("http://159.138.53.90:30116");
        test.setErrorLog("ImagePullBackOff");
        test.setWorkLoadId("test11111579664939869");
        test.setAppInstanceId("a250418e-a805-4e18-b3d4-a5cad716cbf0");
        test.setDeployDate(new Date());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .put("/mec/developer/v1/projects/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e/test-config").with(csrf());
        request.content(gson.toJson(test));
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private ProjectImageConfig addImageToProject(ApplicationProject project) throws Exception {
        ProjectImageConfig image = new ProjectImageConfig();
        image.setName("test-image");
        image.setPort(9998);
        image.setVersion("v1.0");
        image.setProjectId("80ec733f-814e-47cc-b22c-6103d5f58c9e");
        image.setType(EnumProjectImage.DEVELOPER);
        image.setNodePort(32115);

        String url = String.format("/mec/developer/v1/projects/%s/image", project.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url).with(csrf());

        request.content(gson.toJson(image));
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        ResultActions result = mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());
        return gson.fromJson(result.andReturn().getResponse().getContentAsString(), ProjectImageConfig.class);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testAddImageToProjectCorrect() throws Exception {

        ApplicationProject applicationProject = getProject();
        addImageToProject(applicationProject);
    }

    private ApplicationProject getProject() throws Exception {
        String url = String.format(
            "/mec/developer/v1/projects/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e?userId=f24ea0a2-d8e6-467c-8039-94f0d29bac43");
        ResultActions resultActions = mvc.perform(
            MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        ApplicationProject applicationProject = gson
            .fromJson(resultActions.andReturn().getResponse().getContentAsString(), ApplicationProject.class);
        return applicationProject;
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteImageById() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete(
            "/mec/developer/v1/projects/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e/image/78055873-58cf-4712-8f12-cfdd4e19f268")
            .with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetImagesByProjectId() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/projects/200dfab1-3c30-4fc7-a6ca-ed6f0620a85e/image")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
