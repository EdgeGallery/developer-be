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
import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.controller.HostController;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.HostService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

@RunWith(MockitoJUnitRunner.class)
public class HostApiTest {

    private Gson gson = new Gson();

    @InjectMocks
    private HostController hostController;

    @Mock
    private HostService hostService;

    private MockMvc mvc;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(hostController).build();
        MockitoAnnotations.initMocks(this);
    }

    private MepHost createHost() throws Exception {
        MepHost host = new MepHost();
        host.setUserId("admin");
        host.setName("Node_Test");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
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
        MepHost host = new MepHost();
        host.setUserId("admin");
        host.setName("Node_Test");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        Either<FormatRespDto, MepHost> either = Either.right(new MepHost());
        Mockito.when(hostService.createHost(Mockito.any())).thenReturn(either);
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/developer/v1/hosts/").content(gson.toJson(host)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createHostFailed2() throws Exception {
        MepHost host = new MepHost();
        host.setUserId("admin");
        host.setName("1234");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("is not ip format");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        // String msg = "health check faild,current ip or port cann't be used!";
        // FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
        // Either<FormatRespDto, MepHost> either = Either.left(dto);
        // Mockito.when(hostService.createHost(Mockito.any())).thenReturn(either);
        mvc.perform(MockMvcRequestBuilders.post("/mec/developer/v1/hosts/").content(gson.toJson(host)).with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest()).andDo(MockMvcResultHandlers.print());

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void createHostFailed() throws Exception {
        MepHost host = new MepHost();
        host.setUserId("admin");
        host.setName("1234");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("is not ip format");
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
        Either<FormatRespDto, List<MepHost>> res = Either.right(new ArrayList<>());
        Mockito.when(hostService.getAllHosts(Mockito.anyString())).thenReturn(res);
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/hosts?userId=aa").contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHostById() throws Exception {
        Either<FormatRespDto, MepHost> res = Either.right(new MepHost());
        Mockito.when(hostService.getHost(Mockito.anyString())).thenReturn(res);
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/hosts/c8aac2b2-4162-40fe-9d99-0630e3245cf7")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getHostByIdFailed() throws Exception {
        FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, "Can not find the host.");
        Either<FormatRespDto, MepHost> res = Either.left(dto);
        Mockito.when(hostService.getHost(Mockito.anyString())).thenReturn(res);
        mvc.perform(MockMvcRequestBuilders.get("/mec/developer/v1/hosts/can-not-be-find")
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void deleteHostFailed() throws Exception {
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete failed.");
        Either<FormatRespDto, Boolean> res = Either.left(error);
        Mockito.when(hostService.deleteHost(Mockito.anyString())).thenReturn(res);
        mvc.perform(MockMvcRequestBuilders.delete("/mec/developer/v1/hosts/can-not-be-find").with(csrf())
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void updateHostById() throws Exception {
        MepHost host = new MepHost();
        host.setUserId("admin");
        host.setName("123435");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("is not ip format");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        Either<FormatRespDto, MepHost> either = Either.right(new MepHost());
        Mockito.when(hostService.updateHost(Mockito.anyString(), Mockito.any())).thenReturn(either);
        mvc.perform(
            MockMvcRequestBuilders.put("/mec/developer/v1/hosts/aa").with(csrf()).content(gson.toJson(host))
                .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test(expected = NestedServletException.class)
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void updateHostByErrorId() throws Exception {
        MepHost host = new MepHost();
        host.setUserId("admin");
        host.setName("123435");
        host.setAddress("xi'an");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("is not ip format");
        host.setPort(30101);
        host.setOs("Ubuntu");
        host.setPortRangeMin(30000);
        host.setPortRangeMax(32767);
        FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Can not find the host.");
        Either<FormatRespDto, MepHost> either = Either.left(error);
        Mockito.when(hostService.updateHost(Mockito.anyString(), Mockito.any())).thenReturn(either);
        mvc.perform(
            MockMvcRequestBuilders.put("/mec/developer/v1/hosts/can-not-be-find").with(csrf())
                .content(gson.toJson(host)).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
