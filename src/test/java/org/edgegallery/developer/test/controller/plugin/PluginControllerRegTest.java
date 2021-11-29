/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.developer.test.controller.plugin;

import com.google.gson.Gson;
import junit.framework.TestCase;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.model.plugin.PluginDto;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class PluginControllerRegTest extends TestCase {
    @Autowired
    MockMvc mvc;

    @SneakyThrows
    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void registerAppSuccess() throws Exception {
        String userId = "test001";
        String userName = "testUserName";
        File iconFile = Resources.getResourceAsFile("testdata/idea.png");
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/developer/v1/plugins/")
                .file(new MockMultipartFile("pluginFile", "IDEAPluginDev.zip", "text/plain", Resources.getResourceAsStream("testdata/IDEAPluginDev.zip")))
                .file(new MockMultipartFile("logoFile", "idea.png", "text/plain", FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("apiFile", "template-zoneminder.md", "text/plain",Resources.getResourceAsStream("testdata/template-zoneminder.md")))
                .with(csrf())
                .param("pluginName", "pluginName")
                .param("codeLanguage", "JAVA")
                .param("pluginType", "1")
                .param("version", "1")
                .param("introduction", "introduction")
                .param("userId", userId)
                .param("userName", userName));
        MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        PluginDto pluginDto = new Gson().fromJson(result,PluginDto.class);
        Assert.assertNotNull(pluginDto);
    }

}