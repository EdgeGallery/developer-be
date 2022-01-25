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

package org.edgegallery.developer.test.controller.application.container;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.application.container.ModifyFileContentDto;
import org.edgegallery.developer.service.application.impl.container.ContainerAppHelmChartServiceImpl;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ContainerAppHelmChartCtlTest {

    @MockBean
    private ContainerAppHelmChartServiceImpl containerAppHelmChartService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUploadHelmChartFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts", UUID.randomUUID().toString());
        File file = Resources.getResourceAsFile("testdata/config");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.multipart(url)
            .file(new MockMultipartFile("file", "config", MediaType.TEXT_PLAIN_VALUE, FileUtils.openInputStream(file)))
            .with(csrf())).andReturn();
        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetHelmChartListSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts", UUID.randomUUID().toString());
        Mockito.when(containerAppHelmChartService.getHelmChartList(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetOneHelmChartListSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts/%s", UUID.randomUUID().toString(),
            UUID.randomUUID().toString());
        Mockito.when(containerAppHelmChartService.getHelmChartById(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new HelmChart());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteOneHelmChartListSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts/%s", UUID.randomUUID().toString(),
            UUID.randomUUID().toString());
        Mockito.when(containerAppHelmChartService.deleteHelmChartById(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDownloadHelmChartsPackageSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/helmcharts/%s/action/download", UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Mockito.when(containerAppHelmChartService.getHelmChartById(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new HelmChart());
        Mockito.when(containerAppHelmChartService.downloadHelmChart(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new byte[1000]);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetFileContentByFilePathSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts/%s/action/get-inner-file?filePath=%s",
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), "testPath");
        Mockito.when(containerAppHelmChartService
            .getFileContentByFilePath(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new String());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyFileContentByFilePathSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts/%s/action/modify-inner-file",
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), "testPath");
        Mockito.when(
            containerAppHelmChartService.modifyFileContent(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(new Gson().toJson(new ModifyFileContentDto()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}
