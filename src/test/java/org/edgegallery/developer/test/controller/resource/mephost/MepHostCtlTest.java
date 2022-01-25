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

package org.edgegallery.developer.test.controller.resource.mephost;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.resource.mephost.EnumMepHostStatus;
import org.edgegallery.developer.model.resource.mephost.EnumVimType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
import org.edgegallery.developer.service.recource.mephost.MepHostService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
@AutoConfigureMockMvc
public class MepHostCtlTest {

    @MockBean
    private MepHostService mepHostService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllHostsSuccess() throws Exception {
        Page<MepHost> response = new Page<>(new ArrayList<>(), 10, 0, 1);
        String url = String
            .format("/mec/developer/v2/mephosts?name=%s&vimType=%s&architecture=%s&limit=10&offset=0", "name", "vt",
                "arch");
        Mockito.when(mepHostService
            .getAllHosts(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(response);
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateMepHostSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/mephosts");
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(createNewHost()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyMepHostSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/mephosts/%s", UUID.randomUUID().toString());
        Mockito.when(mepHostService.updateHost(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(createNewHost()))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteMepHostSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/mephosts/%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetMepHostSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/mephosts/%s", UUID.randomUUID().toString());
        Mockito.when(mepHostService.getHost(Mockito.anyString())).thenReturn(createNewHost());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetMepHostLogSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/mephosts/%s/logs", UUID.randomUUID().toString());
        Mockito.when(mepHostService.getHostLogByHostId(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUpLoadHostConfigFileSuccess() throws Exception {
        AccessUserUtil.setUser("7b53626b-135d-4e57-ae00-0111a2b05d74", "admin", Consts.ROLE_DEVELOPER_ADMIN);
        String url = String.format("/mec/developer/v2/mephosts/action/upload-config-file");
        File file = Resources.getResourceAsFile("testdata/config");
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.multipart(url)
            .file(new MockMultipartFile("file", "config", MediaType.TEXT_PLAIN_VALUE, FileUtils.openInputStream(file)))
            .param("userId", AccessUserUtil.getUserId()).with(csrf())).andReturn();
        Assert.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    private MepHost createNewHost() {
        MepHost mepHost = new MepHost();
        mepHost.setId(UUID.randomUUID().toString());
        mepHost.setName("k8s-test");
        mepHost.setLcmIp("1.1.1.1");
        mepHost.setLcmProtocol("https");
        mepHost.setLcmPort(30100);
        mepHost.setArchitecture("X86");
        mepHost.setStatus(EnumMepHostStatus.NORMAL);
        mepHost.setMecHostIp("1.1.1.1");
        mepHost.setVimType(EnumVimType.K8S);
        mepHost.setMecHostUserName("test");
        mepHost.setMecHostPassword("test");
        mepHost.setMecHostPort(20000);
        mepHost.setUserId(UUID.randomUUID().toString());
        mepHost.setConfigId(UUID.randomUUID().toString());
        mepHost.setNetworkParameter("net param");
        mepHost.setResource("resource");
        mepHost.setAddress("xi'an");
        return mepHost;
    }

}
