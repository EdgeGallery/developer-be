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

package org.edgegallery.developer.test.controller.uploadfile;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.controller.uploadfile.UploadFileController;
import org.edgegallery.developer.mapper.uploadfile.UploadFileMapper;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.capability.CapabilityService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
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

@RunWith(MockitoJUnitRunner.Silent.class)
public class UploadFileControllerTest {

    @InjectMocks
    private UploadFileController uploadFileController;

    @Mock
    private UploadFileService uploadService;

    @Mock
    private UploadFileMapper uploadedFileMapper;

    @Mock
    private CapabilityService capabilityService;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(uploadFileController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetFileStreamSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/upload-files/%s/action/get-file-stream", UUID.randomUUID().toString());
        byte[] bytes = new byte[1000];
        Mockito.when(uploadService.getFileStream(Mockito.any(), Mockito.anyString())).thenReturn(bytes);
        Mockito.when(uploadService.getFile(Mockito.anyString())).thenReturn(new UploadFile());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/upload-files/%s", UUID.randomUUID().toString());
        UploadFile uploadedFile = new UploadFile();
        Mockito.when(uploadService.getFile(Mockito.anyString())).thenReturn(uploadedFile);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUploadFileSuccess() throws Exception {
        AccessUserUtil.setUser(UUID.randomUUID().toString(), "admin");
        File iconFile = Resources.getResourceAsFile("testdata/face.png");
        InputStream configInputStream = new FileInputStream(iconFile);
        MultipartFile configMultiFile = new MockMultipartFile(iconFile.getName(), iconFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), configInputStream);
        mvc.perform(MockMvcRequestBuilders.multipart("/mec/developer/v2/upload-files").file("file", configMultiFile.getBytes())
            .param("fileType", "icon")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteFileSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/upload-files/%s", UUID.randomUUID().toString());
        Mockito.when(uploadService.deleteFile(Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDownloadSampleCodeSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/upload-files/action/download-sample-code");
        Mockito.when(uploadService.downloadSampleCode(Mockito.any())).thenReturn(new byte[1000]);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new ArrayList<>()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetSampleCodeStructureSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/upload-files/action/get-sample-code-structure");
        Mockito.when(uploadService.getSampleCodeStru(Mockito.any())).thenReturn(new AppPkgStructure());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new ArrayList<>()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetSampleCodeContentSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/upload-files/action/get-sample-code-content?fileName=%s", "test");
        Mockito.when(uploadService.getSampleCodeContent(Mockito.anyString())).thenReturn(new String());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetSdkProjectBad() {
        try {
            String url = String
                .format("/mec/developer/v2/upload-files/%s/action/download-sdk?lan=%s", UUID.randomUUID().toString(), "java");
            Mockito.when(capabilityService.findByApiFileId(Mockito.anyString())).thenReturn(null);
            mvc.perform(MockMvcRequestBuilders.get(url)).andExpect(MockMvcResultMatchers.status().isNotFound());
        } catch (Exception e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetSdkProjectSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/upload-files/%s/action/download-sdk?lan=%s", UUID.randomUUID().toString(), "java");
        Mockito.when(uploadService.getSdkProject(Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
            .thenReturn(new byte[1000]);
        List<Capability> list = new ArrayList<>();
        list.add(new Capability());
        Mockito.when(capabilityService.findByApiFileId(Mockito.anyString())).thenReturn(list);

        mvc.perform(MockMvcRequestBuilders.get(url)).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
