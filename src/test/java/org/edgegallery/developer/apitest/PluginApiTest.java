/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use plugin file except in compliance with the License.
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

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.application.plugin.PluginService;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.model.plugin.ApiChecker;
import org.edgegallery.developer.domain.model.plugin.Plugin;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.service.FileService;
import org.edgegallery.developer.domain.shared.AFile;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.domain.shared.IconChecker;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.domain.shared.PluginChecker;
import org.edgegallery.developer.interfaces.plugin.PluginController;
import org.edgegallery.developer.interfaces.plugin.facade.PluginServiceFacade;
import org.edgegallery.developer.interfaces.plugin.facade.dto.PluginDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PluginApiTest {
    private Gson gson = new Gson();

    @InjectMocks
    private PluginController pluginController;

    @Mock
    private PluginServiceFacade pluginServiceFacade;

    @Mock
    private PluginService pluginService;

    @Mock
    private FileService fileService;

    private MockMvc mvc;

    @Before
    public void setUp() throws IOException {
        this.mvc = MockMvcBuilders.standaloneSetup(pluginController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testQueryAllPlugin() throws Exception {
        List<PluginDto> list = new ArrayList<PluginDto>();
        Page<PluginDto> page = new Page<PluginDto>(list, 15, 0, 10);
        Mockito.when(pluginServiceFacade.qurey("1", "python", "csa", 15, 0)).thenReturn(page);

        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/plugins/?pluginType=1&limit=15&offset=0&codeLanguage=''&pluginName=''")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadPlugin() throws Exception {
        File pluginFile = Resources.getResourceAsFile("testdata/IDEAPluginDev.zip");
        File logoFile = Resources.getResourceAsFile("testdata/idea.png");
        File apiFile = Resources.getResourceAsFile("testdata/plugin.json");
        InputStream pluginInputStream = new FileInputStream(pluginFile);
        InputStream logoInputStream = new FileInputStream(logoFile);
        InputStream apiInputStream = new FileInputStream(apiFile);

        MultipartFile pluginMultiFile = new MockMultipartFile(pluginFile.getName(), pluginFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), pluginInputStream);
        MultipartFile logoMultiFile = new MockMultipartFile(logoFile.getName(), logoFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), logoInputStream);
        MultipartFile apiMultiFile = new MockMultipartFile(logoFile.getName(), logoFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), apiInputStream);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.multipart("/mec/developer/v1/plugins/")
            .file("pluginFile", pluginMultiFile.getBytes()).file("logoFile", logoMultiFile.getBytes())
            .file("apiFile", apiMultiFile.getBytes()).param("pluginId", "83891421-1338-4956-a1b5-48e29dc0539c")
            .param("pluginName", "test").param("introduction", "introduction").param("codeLanguage", "JAVA")
            .param("pluginType", "1").param("version", "1.0").param("userId", "a8622e9e-d619-4219-a7b7-49f099fe5f63")
            .param("userName", "hello")).andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeletePlugin() throws Exception {
        AccessUserUtil.setUser("f24ea0a2-d8e6-467c-8039-94f0d29bac43", "helongfei999");
        mvc.perform(MockMvcRequestBuilders.delete("/mec/developer/v1/plugins/586224da-e1a2-4893-a5b5-bf766fdfb8c7")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadPlugin() throws Exception {
        File pluginFile = Resources.getResourceAsFile("testdata/IDEAPluginDev.zip");
        InputStream pluginInputStream = new FileInputStream(pluginFile);
        Mockito.when(pluginServiceFacade.getPluginName("83891421-1338-4956-a1b5-48e29dc0539c")).thenReturn("page");
        Mockito.when(pluginServiceFacade.downloadFile("83891421-1338-4956-a1b5-48e29dc0539c"))
            .thenReturn(pluginInputStream);
        mvc.perform(
            MockMvcRequestBuilders.get("/mec/developer/v1/plugins/83891421-1338-4956-a1b5-48e29dc0539c/action/download")
                .contentType(MediaType.APPLICATION_OCTET_STREAM).accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //
    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadPluginLogo() throws Exception {
        File logoFile = Resources.getResourceAsFile("testdata/idea.png");
        InputStream logoInputStream = new FileInputStream(logoFile);
        Mockito.when(pluginServiceFacade.downloadLogo("586224da-e1a2-4893-a5b5-bf766fdfb8c7"))
            .thenReturn(logoInputStream);

        mvc.perform(MockMvcRequestBuilders
            .get("/mec/developer/v1/plugins/586224da-e1a2-4893-a5b5-bf766fdfb8c7/action/get-logofile")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadPluginApi() throws Exception {
        File apiFile = Resources.getResourceAsFile("testdata/plugin.json");
        InputStream apiInputStream = new FileInputStream(apiFile);
        Mockito.when(pluginServiceFacade.downloadApiFile("586224da-e1a2-4893-a5b5-bf766fdfb8c7"))
            .thenReturn(apiInputStream);

        mvc.perform(MockMvcRequestBuilders
            .get("/mec/developer/v1/plugins/586224da-e1a2-4893-a5b5-bf766fdfb8c7/action/get-apifile")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateScore() throws Exception {
        File pluginFile = Resources.getResourceAsFile("testdata/IDEAPluginDev.zip");
        File logoFile = Resources.getResourceAsFile("testdata/idea.png");
        File apiFile = Resources.getResourceAsFile("testdata/plugin.json");
        InputStream pluginInputStream = new FileInputStream(pluginFile);
        InputStream logoInputStream = new FileInputStream(logoFile);
        InputStream apiInputStream = new FileInputStream(apiFile);

        MultipartFile pluginMultiFile = new MockMultipartFile(pluginFile.getName(), pluginFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), pluginInputStream);
        MultipartFile logoMultiFile = new MockMultipartFile(logoFile.getName(), logoFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), logoInputStream);
        MultipartFile apiMultiFile = new MockMultipartFile(apiFile.getName(), apiFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), apiInputStream);
        AFile plugins = getFile(pluginMultiFile, new PluginChecker());
        AFile logo = getFile(logoMultiFile, new IconChecker());
        AFile api = getFile(apiMultiFile, new ApiChecker());
        Plugin plugin = new Plugin();
        plugin.setPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8c7");
        plugin.setPluginName("hello");
        plugin.setIntroduction("introduction");
        plugin.setSatisfaction(5.0f);
        plugin.setCodeLanguage("JAVA");
        plugin.setPluginType("1");
        plugin.setVersion("1.0");
        plugin.setUploadTime(new Date());
        plugin.setUser(new User("f24ea0a2-d8e6-467c-8039-94f0d29bac43", "helongfei999"));
        plugin.setPluginFile(plugins);
        plugin.setLogoFile(logo);
        plugin.setApiFile(api);
        Mockito.when(pluginServiceFacade
            .mark(Mockito.eq("586224da-e1a2-4893-a5b5-bf766fdfb8c7"), Mockito.eq(5), Mockito.any(User.class)))
            .thenReturn(plugin);

        ResultActions result = mvc.perform(MockMvcRequestBuilders.put(
            "/mec/developer/v1/plugins/586224da-e1a2-4893-a5b5-bf766fdfb8c7/action/score?score=5&userId=f24ea0a2-d8e6-467c-8039-94f0d29bac43&userName=helongfei999")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());

    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker) throws IOException {
        String fileAddress = fileService.saveTo(file, fileChecker);
        return new AFile(file.getOriginalFilename(), fileAddress, file.getSize());
    }

}
