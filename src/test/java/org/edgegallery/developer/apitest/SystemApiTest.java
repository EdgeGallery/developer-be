package org.edgegallery.developer.apitest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.EnumHostStatus;
import org.edgegallery.developer.model.workspace.MepHost;
import org.edgegallery.developer.model.workspace.MepHostLog;
import org.edgegallery.developer.model.workspace.OpenMepCapabilityGroup;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.SystemService;
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
public class SystemApiTest {

    @MockBean
    private SystemService systemService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllHosts() throws Exception {
        String url = String.format("/mec/developer/v1/system/hosts?userId=%s&name=%s&ip=%s&limit=1&offset=0",
            "e111f3e7-90d8-4a39-9874-ea6ea6752ef6", "host", "10.1.12.1");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetHost() throws Exception {
        Either<FormatRespDto, MepHost> response = Either.right(new MepHost());
        Mockito.when(systemService.getHost(Mockito.any())).thenReturn(response);
        String url = String.format("/mec/developer/v1/system/hosts/%s", "c8aac2b2-4162-40fe-9d99-0630e3245cf7");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateHost() throws Exception {
        MepHost host = new MepHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        Either<FormatRespDto, MepHost> response = Either.right(new MepHost());
        Mockito.when(systemService.createHost(Mockito.any(), Mockito.anyString())).thenReturn(response);
        String url = String.format("/mec/developer/v1/system/hosts");
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.post(url).with(csrf()).content(gson.toJson(host))
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteHost() throws Exception {
        Either<FormatRespDto, Boolean> response = Either.right(true);
        Mockito.when(systemService.deleteHost(Mockito.any())).thenReturn(response);
        String url = String.format("/mec/developer/v1/system/hosts/%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyHost() throws Exception {
        MepHost host = new MepHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        Either<FormatRespDto, MepHost> response = Either.right(new MepHost());
        Mockito.when(systemService.updateHost(Mockito.anyString(), Mockito.any())).thenReturn(response);
        String url = String.format("/mec/developer/v1/system/hosts/%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.put(url).with(csrf()).content(gson.toJson(host))
            .contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetHostLogByHostId() throws Exception {
        Either<FormatRespDto, List<MepHostLog>> response = Either.right(new ArrayList<>());
        Mockito.when(systemService.getHostLogByHostId(Mockito.anyString())).thenReturn(response);
        String url = String.format("/mec/developer/v1/system/hosts/%s/log", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateGroup() throws Exception {
        Either<FormatRespDto, OpenMepCapabilityGroup> response = Either.right(new OpenMepCapabilityGroup());
        Mockito.when(systemService.createCapabilityGroup(Mockito.any())).thenReturn(response);
        String url = String.format("/mec/developer/v1/system/capability");
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
        String url = String.format("/mec/developer/v1/system/capability?groupId=%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllCapability() throws Exception {
        String url = String
            .format("/mec/developer/v1/system/capability?userId=%s&twoLevelName=%s&twoLevelNameEn=%s&limit=1&offset=0",
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
        String url = String.format("/mec/developer/v1/system/capability/%s", UUID.randomUUID().toString());
        ResultActions actions = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON_UTF8));
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
