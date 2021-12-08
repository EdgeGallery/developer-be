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

package org.edgegallery.developer.test.controller.resource.container;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.resource.container.ContainerImage;
import org.edgegallery.developer.model.resource.container.ContainerImageReq;
import org.edgegallery.developer.service.recource.container.ContainerImageService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class ContainerImageCtlTest {

    @MockBean
    private ContainerImageService containerImageService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/containerimages/%s/action/upload",
            "4c22f069-e489-47cd-9c3c-e21741c857df");
        Mockito.when(containerImageService.uploadContainerImage(Mockito.any(), Mockito.any(),Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with((csrf())))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testMergeImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/containerimages/%s/action/merge",
            "4c22f069-e489-47cd-9c3c-e21741c857df");
        Mockito.when(containerImageService.mergeContainerImage(Mockito.anyString(), Mockito.any(),Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetAllImageSuccess() throws Exception {
        ContainerImageReq containerImage = new ContainerImageReq();
        List<ContainerImage> list = new ArrayList<>();
        Page<ContainerImage> page = new Page<>(list,1,2,3);
        String url = String.format("/mec/developer/v2/containerimages/action/get-all-images");
        Mockito.when(containerImageService.getAllImage(Mockito.any())).thenReturn(page);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with(csrf())
            .content(new Gson().toJson(containerImage)).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyImageSuccess() throws Exception {
        ContainerImage containerImage = new ContainerImage();
        String url = String.format("/mec/developer/v2/containerimages/%s","4c22f069-e489-47cd-9c3c-e21741c857df");
        Mockito.when(containerImageService.updateContainerImage(Mockito.anyString(),Mockito.any())).thenReturn(containerImage);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.put(url).with(csrf())
            .content(new Gson().toJson(containerImage)).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteImageSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/containerimages/%s","4c22f069-e489-47cd-9c3c-e21741c857dg");
        Mockito.when(containerImageService.deleteContainerImage(Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.delete(url).with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/containerimages/%s/action/download","4c22f069-e489-47cd-9c3c-e21741c857do");
        Mockito.when(containerImageService.downloadHarborImage(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url)
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCancelUploadImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/containerimages/%s/action/upload","4c22f069-e489-47cd-9c3c-e21741c857do");
        Mockito.when(containerImageService.cancelUploadHarborImage(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.delete(url).with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testSynchronizeImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok("synchronized successfully!");
        String url = String.format("/mec/developer/v2/containerimages/action/synchronize");
        Mockito.when(containerImageService.synchronizeHarborImage()).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}
