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

package org.edgegallery.developer.test.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.HelmTemplateYamlPo;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.DeployService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class DeployYamlControllerTest {

    @MockBean
    private DeployService deployService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSaveDeploy() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> response = Either.right(new HelmTemplateYamlPo());
        String url = String
            .format("/mec/developer/v1/deploy/%s/action/save-yaml?userId=%s&configType=%s", "projectId", "userId",
                "upload");
        Mockito.when(deployService
            .saveDeployYaml(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson("hello"))
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdateDeployYaml() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> response = Either.right(new HelmTemplateYamlPo());
        String url = String.format("/mec/developer/v1/deploy/%s", "fileId");
        Mockito.when(deployService.updateDeployYaml(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content("content").contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetDeployYaml() throws Exception {
        Either<FormatRespDto, HelmTemplateYamlPo> response = Either.right(new HelmTemplateYamlPo());
        String url = String.format("/mec/developer/v1/deploy/%s", "fileId");
        Mockito.when(deployService.getDeployYamlContent(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testQueryDeployYaml() throws Exception {
        Either<FormatRespDto, List<String>> response = Either.right(new ArrayList<>());
        String url = String.format("/mec/developer/v1/deploy/%s/action/get-json", "fileId");
        Mockito.when(deployService.getDeployYamJson(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}
