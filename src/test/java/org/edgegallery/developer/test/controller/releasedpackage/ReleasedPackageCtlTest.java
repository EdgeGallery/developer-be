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

package org.edgegallery.developer.test.controller.releasedpackage;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.UUID;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.releasedpackage.ReleasedPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.service.releasedpackage.ReleasedPackageService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
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
public class ReleasedPackageCtlTest {

    @MockBean
    private ReleasedPackageService packageService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testSynchronizeAppPkgSuccess() throws Exception {
        AppPackage appPackage = new AppPackage();
        String url = String.format("/mec/developer/v2/released-packages");
        Mockito.when(packageService.synchronizePackage(Mockito.any(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(new Gson().toJson(new ArrayList<>()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllAppPkgSuccess() throws Exception {
        Page<ReleasedPackage> packagePage = new Page<>(new ArrayList<>(), 1, 0, 1);
        String url = String.format("/mec/developer/v2/released-packages?name=%s&limit=%s&offset=%s", "test", 1, 0);
        Mockito.when(packageService.getAllPackages(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(packagePage);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAppPackageStructureSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/released-packages/%s/action/get-pkg-structure", UUID.randomUUID().toString());
        Mockito.when(packageService.getAppPkgStructure(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAppPkgFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/released-packages/%s/action/get-file-content", "aa");
        Mockito.when(packageService.getAppPkgFileContent(Mockito.any(), Mockito.anyString()))
            .thenReturn(new ReleasedPkgFileContent());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with((csrf()))
            .content(new Gson().toJson(new ReleasedPkgFileContentReqDto()))
            .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testEditAppPkgFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/released-packages/%s/action/edit-file-content", "aa");
        Mockito.when(packageService.editAppPkgFileContent(Mockito.any(), Mockito.anyString()))
            .thenReturn(new ReleasedPkgFileContent());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new ReleasedPkgFileContent()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteAppPkgFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/released-packages/%s", "aa1");
        Mockito.when(packageService.deleteAppPkg(Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testReleaseAppPkgFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/released-packages/%s/action/release", "aa1");
        Mockito.when(packageService.releaseAppPkg(Mockito.any(),Mockito.any(),Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new PublishAppReqDto()))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
