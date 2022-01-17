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

package org.edgegallery.developer.test.controller.resource.vm;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.restful.VMImageReq;
import org.edgegallery.developer.model.restful.VMImageRes;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.recource.vm.VMImageService;
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
public class VMImageCtlTest {

    @MockBean
    private VMImageService vmImageService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetVmImagesSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/action/get-list");
        Mockito.when(vmImageService.getVmImages(Mockito.any())).thenReturn(new VMImageRes());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(new Gson().toJson(new VMImageReq()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetVmImageByIdSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/%s", 1);
        Mockito.when(vmImageService.getVmImageById(Mockito.anyInt())).thenReturn(new VMImage());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testCreateVmImageSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages");
        Mockito.when(vmImageService.createVmImage(Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(new Gson().toJson(new VMImage()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testModifyVmImageSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/%s", 1);
        Mockito.when(vmImageService.updateVmImage(Mockito.any(), Mockito.anyInt())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with(csrf()).content(new Gson().toJson(new VMImage()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteVmImageSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/%s", 1);
        Mockito.when(vmImageService.deleteVmImage(Mockito.anyInt())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testPublishVmImageSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/%s/action/publish", 1);
        Mockito.when(vmImageService.publishVmImage(Mockito.anyInt())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.put(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testResetImageStatusSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/%s/action/reset", 1);
        Mockito.when(vmImageService.resetImageStatus(Mockito.anyInt())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.put(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUploadVmImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/vmimages/%s/action/upload", 1);
        Mockito.when(vmImageService.uploadVmImage(Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(response);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCheckChunkForUploadVmImageSuccess() throws Exception {
        List<Integer> list = new ArrayList<>();
        String url = String.format("/mec/developer/v2/vmimages/%s/action/upload?identifier=%s", 1, "test");
        Mockito.when(vmImageService.checkUploadedChunks(Mockito.anyInt(), Mockito.anyString())).thenReturn(list);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCancelUploadVmImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/vmimages/%s/action/upload?identifier=%s", 1, "test");
        Mockito.when(vmImageService.cancelUploadVmImage(Mockito.anyInt(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testMergeVmImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String
            .format("/mec/developer/v2/vmimages/%s/action/merge?fileName=%s&identifier=%s", 1, "test", "test");
        Mockito.when(vmImageService.mergeVmImage(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
            .thenReturn(response);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDownloadVmImageSuccess() throws Exception {
        ResponseEntity response = ResponseEntity.ok().build();
        String url = String.format("/mec/developer/v2/vmimages/%s/action/download", 1);
        Mockito.when(vmImageService.downloadVmImage(Mockito.anyInt())).thenReturn(response);
        Mockito.when(vmImageService.getVmImageById(Mockito.anyInt())).thenReturn(new VMImage());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testImageSlimSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/vmimages/%s/action/slim", 1);
        Mockito.when(vmImageService.imageSlim(Mockito.anyInt())).thenReturn(new OperationInfoRep(""));
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
