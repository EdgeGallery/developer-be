/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.developer.controller.MepCapabilityController;
import org.edgegallery.developer.model.AppPkgStructure;
import org.edgegallery.developer.model.workspace.EnumOpenMepType;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityDetail;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.OpenMepApiResponse;
import org.edgegallery.developer.response.OpenMepEcoApiResponse;
import org.edgegallery.developer.service.OpenMepCapabilityService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class MepCapabilityApiTest {

    private Gson gson = new Gson();

    @InjectMocks
    private MepCapabilityController mepCapabilityController;

    @Mock
    private OpenMepCapabilityService openMEPCapabilityService;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(mepCapabilityController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createGroup() throws Exception {
        OpenMepCapabilityGroup group = new OpenMepCapabilityGroup();
        group.setGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ee3");
        group.setOneLevelName("Face recognition");
        group.setType(EnumOpenMepType.OPENMEP);
        group.setDescription("face recognition");

        Either<FormatRespDto, OpenMepCapabilityGroup> response = Either.right(new OpenMepCapabilityGroup());
        Mockito.when(openMEPCapabilityService.createGroup(Mockito.any(OpenMepCapabilityGroup.class)))
            .thenReturn(response);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/mec/developer/v1/capability-groups/");
        request.content(gson.toJson(group));
        request.accept(MediaType.APPLICATION_JSON_UTF8_VALUE);
        request.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getGroup() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get("/mec/developer/v1/pability-groups/?groupId=123");
        request.accept(MediaType.APPLICATION_JSON_UTF8);
        request.contentType(MediaType.APPLICATION_JSON_UTF8);
        mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteGroup() throws Exception {

        Either<FormatRespDto, Boolean> response = Either.right(true);
        Mockito.when(openMEPCapabilityService.deleteGroup("e111f3e7-90d8-4a39-9874-ea6ea6752ef3")).thenReturn(response);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .delete("/mec/developer/v1/capability-groups/e111f3e7-90d8-4a39-9874-ea6ea6752ef3");
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createCapability() throws Exception {
        OpenMepCapabilityDetail detail = new OpenMepCapabilityDetail();
        detail.setDetailId("3857dc11-4220-46c6-8551-3a6b503b647f");
        detail.setGroupId("e111f3e7-90d8-4a39-9874-ea6ea6752ee3");
        detail.setService("Face Recognition Service New");
        detail.setVersion("v2");
        detail.setDescription("provide the face recognition capabilities for apps");
        detail.setProvider("Huawei");
        detail.setApiFileId("d0f8fa57-2f4c-4182-be33-0a508964d04a");
        detail.setUserId("d0f8fa57-2f4c-4182-be33-0a508964d0");

        Either<FormatRespDto, OpenMepCapabilityDetail> response = Either.right(new OpenMepCapabilityDetail());
        Mockito.when(
            openMEPCapabilityService.createCapability(Mockito.anyString(), Mockito.any(OpenMepCapabilityDetail.class)))
            .thenReturn(response);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .post("/mec/developer/v1/capability-groups/e111f3e7-90d8-4a39-9874-ea6ea6752ef3");
        request.content(gson.toJson(detail));
        request.accept(MediaType.APPLICATION_JSON_UTF8_VALUE);
        request.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteCapability() throws Exception {
        Either<FormatRespDto, Boolean> response = Either.right(true);
        Mockito.when(openMEPCapabilityService.deleteCapabilityByUserId("e111f3e7-90d8-4a39-9874-ea6ea6752ef4", "d0f8fa57-2f4c-4182-be33-0a508964d0"))
            .thenReturn(response);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .delete("/mec/developer/v1/capability-groups/capabilities/e111f3e7-90d8-4a39-9874-ea6ea6752ef4?userId=d0f8fa57-2f4c-4182-be33-0a508964d0");
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON);
        mvc.perform(request).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllCapalities() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/mec/developer/v1/apability-groups/");
        request.accept(MediaType.APPLICATION_JSON_UTF8);
        request.contentType(MediaType.APPLICATION_JSON_UTF8);
        mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllCapalitiesSuccess() throws Exception {
        Either<FormatRespDto, List<OpenMepCapabilityGroup>> response = Either.right(new ArrayList<>());
        String url = String.format("/mec/developer/v1/capability-groups");
        Mockito.when(openMEPCapabilityService.getAllCapabilityGroups()).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllCapalitiesByGroupIdSuccess() throws Exception {
        Either<FormatRespDto, OpenMepCapabilityGroup> response = Either.right(new OpenMepCapabilityGroup());
        String url = String.format("/mec/developer/v1/capability-groups/%s","test-group-id");
        Mockito.when(openMEPCapabilityService.getCapabilitiesByGroupId(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getOpenMepApiSuccess() throws Exception {
        Either<FormatRespDto, OpenMepApiResponse> response = Either.right(new OpenMepApiResponse());
        String url = String.format("/mec/developer/v1/capability-groups/open-api/%s","test-type");
        Mockito.when(openMEPCapabilityService.getOpenMepList(Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getOpenMepEcoApiByFileIdSuccess() throws Exception {
        Either<FormatRespDto, OpenMepCapabilityDetail> response = Either.right(new OpenMepCapabilityDetail());
        String url = String.format("/mec/developer/v1/capability-groups/openmep-api/%s?userId=%s","test-fileId","test-userId");
        Mockito.when(openMEPCapabilityService.getOpenMepByFileId(Mockito.anyString(),Mockito.anyString())).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getOpenMepEcoApiSuccess() throws Exception {
        Either<FormatRespDto, OpenMepEcoApiResponse> response = Either.right(new OpenMepEcoApiResponse());
        String url = String.format("/mec/developer/v1/capability-groups/openmepeco-api");
        Mockito.when(openMEPCapabilityService.getOpenMepEcoList()).thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getOpenMepList() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get("/mec/developer/v1/pability-groups/get-openmep-api");
        request.accept(MediaType.APPLICATION_JSON_UTF8);
        request.contentType(MediaType.APPLICATION_JSON_UTF8);
        mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getOpenMepEcoList() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
            .get("/mec/developer/v1/pability-groups/get-openmepeco-api");
        request.accept(MediaType.APPLICATION_JSON_UTF8);
        request.contentType(MediaType.APPLICATION_JSON_UTF8);
        mvc.perform(request).andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }



}
