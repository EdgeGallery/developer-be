/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;

import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.CapabilityService;
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

import com.google.gson.Gson;
import com.spencerwi.either.Either;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class CapabilityControllerTest {
    @MockBean
    private CapabilityService systemService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateGroup() throws Exception {
        Either<FormatRespDto, OpenMepCapabilityGroup> response = Either.right(new OpenMepCapabilityGroup());
        Mockito.when(systemService.createCapabilityGroup(Mockito.any())).thenReturn(response);
        String url = String.format("/mec/developer/v1/capabilities");
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(new OpenMepCapabilityGroup()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteCapabilityByUserIdAndGroupId() throws Exception {
        Either<FormatRespDto, Boolean> response = Either.right(true);
        Mockito.when(systemService.deleteCapabilityByUserIdAndGroupId(Mockito.anyString())).thenReturn(response);
        String url = String.format("/mec/developer/v1/capabilities?groupId=%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllCapability() throws Exception {
        String url = String
            .format("/mec/developer/v1/capabilities?userId=%s&twoLevelName=%s&twoLevelNameEn=%s&limit=1&offset=0",
                "admin", "group-2", "group-2-en");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void tesGetCapalitiesByGroupId() throws Exception {
        Either<FormatRespDto, OpenMepCapabilityGroup> response = Either.right(new OpenMepCapabilityGroup());
        Mockito.when(systemService.getCapabilityByGroupId(Mockito.anyString())).thenReturn(response);
        String url = String.format("/mec/developer/v1/capabilities/%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}
