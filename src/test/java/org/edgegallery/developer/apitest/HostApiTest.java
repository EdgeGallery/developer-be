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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class HostApiTest {

    private Gson gson = new Gson();

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private MepHost createHost() throws Exception {
        MepHost host = new MepHost();
        host.setName("Node_Test");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setIp("127.0.0.1");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/developer/v1/hosts/").content(gson.toJson(host)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
        return gson.fromJson(result.andReturn().getResponse().getContentAsString(), MepHost.class);
    }

    private void deleteHost(MepHost host) throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/mec/developer/v1/hosts/" + host.getHostId()).with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createAndDeleteHost() throws Exception {
        MepHost host = createHost();
        deleteHost(host);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createHostFailed() throws Exception {
        MepHost host = new MepHost();
        host.setName("1234");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setIp("is not ip format");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        mvc.perform(MockMvcRequestBuilders.post("/mec/developer/v1/hosts/").content(gson.toJson(host)).with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(MockMvcResultHandlers.print());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAllHost() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/hosts/").contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHostById() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/hosts/c8aac2b2-4162-40fe-9d99-0630e3245cf7")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHostByIdFailed() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/hosts/can-not-be-find")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteHostFailed() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/mec/developer/v1/hosts/can-not-be-find").with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void updateHostById() throws Exception {
        MepHost host = createHost();
        host.setName("Node_test2");
        host.setAddress("beijing");
        host.setArchitecture("ARM");
        host.setStatus(EnumHostStatus.BUSY);
        host.setIp("192.168.1.5");
        host.setPort(30101);
        host.setOs("CentOS");
        host.setPortRangeMin(30001);
        host.setPortRangeMax(32768);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.put("/mec/developer/v1/hosts/" + host.getHostId()).with(csrf())
                .content(gson.toJson(host)).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        MepHost modifiedHost = gson.fromJson(result.andReturn().getResponse().getContentAsString(), MepHost.class);
        Assert.assertEquals(gson.toJson(host), gson.toJson(modifiedHost));
        deleteHost(host);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void updateHostByErrorId() throws Exception {
        MepHost host = createHost();
        host.setName("Node_test2");
        host.setAddress("beijing");
        host.setArchitecture("ARM");
        host.setStatus(EnumHostStatus.BUSY);
        host.setIp("192.168.1.5");
        host.setPort(30101);
        host.setOs("CentOS");
        host.setPortRangeMin(30001);
        host.setPortRangeMax(32768);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.put("/mec/developer/v1/hosts/can-not-ne-find").with(csrf())
                .content(gson.toJson(host)).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
