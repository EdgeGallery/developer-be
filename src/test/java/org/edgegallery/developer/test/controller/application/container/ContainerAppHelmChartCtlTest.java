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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.controller.application.container.ContainerAppHelmChartCtl;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.application.container.ModifyFileContentDto;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.junit.Assert;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class ContainerAppHelmChartCtlTest {

    @Mock
    private ContainerAppHelmChartService containerAppHelmChartService;

    private MockMvc mvc;

    @InjectMocks
    private ContainerAppHelmChartCtl appHelmChartCtl;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(appHelmChartCtl).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUploadHelmChartFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/helmcharts", UUID.randomUUID().toString());
        Mockito.when(containerAppHelmChartService.uploadHelmChartFile(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(new HelmChart());
        File configFile = Resources.getResourceAsFile("testdata/config");
        InputStream configInputStream = new FileInputStream(configFile);
        MultipartFile configMultiFile = new MockMultipartFile(configFile.getName(), configFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), configInputStream);
        mvc.perform(MockMvcRequestBuilders.multipart(url).file("file", configMultiFile.getBytes()))
            .andExpect(MockMvcResultMatchers.status().isOk());
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