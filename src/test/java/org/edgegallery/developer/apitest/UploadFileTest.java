/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.controller.UploadedFilesController;
import org.edgegallery.developer.interfaces.plugin.facade.dto.PluginDto;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.service.UploadFileService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.NestedServletException;

@RunWith(MockitoJUnitRunner.class)
public class UploadFileTest {
    private Gson gson = new Gson();

    @InjectMocks
    private UploadedFilesController uploadedFilesController;

    @Mock
    private UploadFileService uploadFileService;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(uploadedFilesController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getFile() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> response = Either
            .right(ResponseEntity.status(HttpStatus.OK).build());
        Mockito.when(uploadFileService
            .getFile("ad66d1b6-5d29-487b-9769-be48b62aec2e", "f24ea0a2-d8e6-467c-8039-94f0d29bac43", "OPENMEP"))
            .thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/files/ad66d1b6-5d29-487b-9769-be48b62aec2e?userId=f24ea0a2-d8e6-467c-8039-94f0d29bac43&type=OPENMEP")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test(expected = AssertionError.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getFileBad() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> response = Either
            .left(new FormatRespDto(Status.BAD_REQUEST, "can not find file in db."));
        Mockito.when(uploadFileService
            .getFile("ad66d1b6-5d29-487b-9769-be48b62aec2e222", "f24ea0a2-d8e6-467c-8039-94f0d29bac43", "OPENMEP"))
            .thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/files/ad66d1b6-5d29-487b-9769-be48b62aec2e222?userId=f24ea0a2-d8e6-467c-8039-94f0d29bac43&type=OPENMEP")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHelmYamlFail() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(
            "/mec/developer/v1/files/helm-template-yaml?userId=ad66d1b6-5d29-487b-9769-be48b62aec2e2&projectId=f24ea0a2-d8e6-467c-8039-94f0d29bac43")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteHelmYaml() throws Exception {
        Either<FormatRespDto, String> response = Either.right("Failed to delete helm template yaml");
        Mockito.when(uploadFileService.deleteHelmTemplateYamlByFileId("e111f3e7-90d8-4a39-9874-ea6ea6752ef4"))
            .thenReturn(response);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .delete("/mec/developer/v1/files/helm-template-yaml/?fileId=e111f3e7-90d8-4a39-9874-ea6ea6752ef4");
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test(expected = NestedServletException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadHelmYaml() throws Exception {

        File logoFile = Resources.getResourceAsFile("testdata/yaml/projects-v1.yaml");
        InputStream logoInputStream = new FileInputStream(logoFile);

        MultipartFile logoMultiFile = new MockMultipartFile(logoFile.getName(), logoFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), logoInputStream);
        System.out.println(logoMultiFile.getSize());
        HelmTemplateYamlRespDto helmTemplateYamlRespDto = new HelmTemplateYamlRespDto();
        mvc.perform(MockMvcRequestBuilders.multipart("/mec/developer/v1/files/helm-template-yaml")
            .file("file", logoMultiFile.getBytes()).param("userId", "e111f3e7-90d8-4a39-9874-ea6ea6752ef4")
            .param("projectId", "e111f3e7-90d8-4a39-9874-ea6ea6752ef5").param("configType","upload"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSampleCode() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("ad66d1b6-5d29-487b-9769-be48b62aec2e");
        Either<FormatRespDto, ResponseEntity<byte[]>> either= Either.right(new ResponseEntity<>(HttpStatus.OK));
        Mockito.when(uploadFileService.downloadSampleCode(Mockito.any())).thenReturn(either);
        mvc.perform(
            MockMvcRequestBuilders.post("/mec/developer/v1/files/samplecode").content(gson.toJson(list).getBytes())
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetApiInfo() throws Exception {
        Either<FormatRespDto, UploadedFile> response = Either.right(new UploadedFile());
        Mockito.when(uploadFileService.getApiFile(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(response);
        String url = String.format("/mec/developer/v1/files/api-info/%s?userId=%s","aa","bb");
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(url);
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadFile() throws Exception {
        Either<FormatRespDto, UploadedFile> either= Either.right(new UploadedFile());
        Mockito.when(uploadFileService.uploadFile(Mockito.anyString(),Mockito.any())).thenReturn(either);
        File iconFile = Resources.getResourceAsFile("testdata/face.png");
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/developer/v1/files?userId=aaaa").file(
            new MockMultipartFile("file", "face.png", "text/plain", Resources.getResourceAsStream("testdata/face.png"))));
        MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        UploadedFile uploadedFile = new Gson().fromJson(result, UploadedFile.class);
        Assert.assertNotNull(uploadedFile);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSdkProject() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> either= Either.right(new ResponseEntity<>(HttpStatus.OK));
        Mockito.when(uploadFileService.getSdkProject(Mockito.anyString(),Mockito.any())).thenReturn(either);
        String url = String.format("/mec/developer/v1/files/sdk/%s/download/%s","a","b");
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(url));
        MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Assert.assertEquals(mvcResult.getResponse().getStatus(),200);
    }
}
