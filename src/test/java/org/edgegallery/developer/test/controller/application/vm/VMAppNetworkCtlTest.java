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

package org.edgegallery.developer.test.controller.application.vm;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
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
import com.google.gson.Gson;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class VMAppNetworkCtlTest {

    @MockBean
    private VMAppNetworkService vmAppNetworkService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateNetworkSuccess() throws Exception {
        Network network = new Network();
        String url = String.format("/mec/developer/v2/applications/%s/networks", UUID.randomUUID().toString());
        Mockito.when(vmAppNetworkService.createNetwork(Mockito.anyString(), Mockito.any())).thenReturn(network);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new Network()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllNetworkSuccess() throws Exception {
        List<Network> networks = new ArrayList<>();
        String url = String.format("/mec/developer/v2/applications/%s/networks", UUID.randomUUID().toString());
        Mockito.when(vmAppNetworkService.getAllNetwork(Mockito.anyString())).thenReturn(networks);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.get(url).with((csrf())).content(new Gson().toJson(new Network()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetNetworkSuccess() throws Exception {
        Network network = new Network();
        String url = String.format("/mec/developer/v2/applications/%s/networks", UUID.randomUUID().toString());
        Mockito.when(vmAppNetworkService.getNetwork(Mockito.anyString(), Mockito.any())).thenReturn(network);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.get(url).with((csrf())).content(new Gson().toJson(new Network()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyNetworkSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/networks/%s",UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(vmAppNetworkService.modifyNetwork(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new Network()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteNetworkSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/networks/%s", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(vmAppNetworkService.deleteNetwork(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with((csrf())).content(new Gson().toJson(new Network()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
