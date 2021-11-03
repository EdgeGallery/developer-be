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

import com.spencerwi.either.Either;
import javax.ws.rs.core.Response;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.AppReleaseService;
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
public class AppReleaseControllerTest {

    @MockBean
    private AppReleaseService appReleaseService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruSuccess() throws Exception {
        Either<FormatRespDto, AppPkgStructure> response = Either.right(new AppPkgStructure());
        String url = String.format("/mec/developer/v1/apprelease/%s/%s/action/get-pkg-structure",
            "4c22f069-e489-47cd-9c3c-e21741c857db", "4c22f069-e489-47cd-9c3c-e21741c857dd");
        Mockito.when(appReleaseService.getPkgStruById(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruFail1() throws Exception {
        Either<FormatRespDto, AppPkgStructure> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "can not find this project!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/%s/action/get-pkg-structure", "aaaa",
            "4c22f069-e489-47cd-9c3c-e21741c857dd");
        Mockito.when(appReleaseService.getPkgStruById(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruFail2() throws Exception {
        Either<FormatRespDto, AppPkgStructure> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "unzip csar file fail!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/%s/action/get-pkg-structure", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c-");
        Mockito.when(appReleaseService.getPkgStruById(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruFail3() throws Exception {
        Either<FormatRespDto, AppPkgStructure> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "get csar pkg occur io exception!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/%s/action/get-pkg-structure", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c-");
        Mockito.when(appReleaseService.getPkgStruById(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgStruFail4() throws Exception {
        Either<FormatRespDto, AppPkgStructure> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "project id can not be empty!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/%s/action/get-pkg-structure", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c-");
        Mockito.when(appReleaseService.getPkgStruById(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentSuccess() throws Exception {
        Either<FormatRespDto, String> response = Either.right(new String());
        String url = String.format("/mec/developer/v1/apprelease/%s/action/get-pkg-content?fileName=%s", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c");
        Mockito.when(appReleaseService.getPkgContentByFileName(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentFail1() throws Exception {
        Either<FormatRespDto, String> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "project id can not be empty!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/action/get-pkg-content?fileName=%s", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c");
        Mockito.when(appReleaseService.getPkgContentByFileName(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentFail2() throws Exception {
        Either<FormatRespDto, String> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "can not find any file!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/action/get-pkg-content?fileName=%s", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c");
        Mockito.when(appReleaseService.getPkgContentByFileName(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentFail3() throws Exception {
        Either<FormatRespDto, String> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "file name can not be empty!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/action/get-pkg-content?fileName=%s", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c");
        Mockito.when(appReleaseService.getPkgContentByFileName(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentFail4() throws Exception {
        Either<FormatRespDto, String> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "file is null!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/action/get-pkg-content?fileName=%s", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c");
        Mockito.when(appReleaseService.getPkgContentByFileName(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }


    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPkgContentFail5() throws Exception {
        Either<FormatRespDto, String> response = Either
            .left(new FormatRespDto(Response.Status.BAD_REQUEST, "file is not readable!"));
        String url = String.format("/mec/developer/v1/apprelease/%s/action/get-pkg-content?fileName=%s", "aaaa",
            "4c22f069-e489-47cd-bbbbbb9c3c");
        Mockito.when(appReleaseService.getPkgContentByFileName(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
    }
}
