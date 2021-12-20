package org.edgegallery.developer.test.controller.application.vm;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.service.application.impl.vm.VMAppVmServiceImpl;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
public class VMAppVmCtlTest {

    @MockBean
    private VMAppVmServiceImpl vmAppVmService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateVmSuccess() throws Exception {
        VirtualMachine virtualMachine = new VirtualMachine();
        String url = String.format("/mec/developer/v2/applications/%s/vms", UUID.randomUUID().toString());
        Mockito.when(vmAppVmService.createVm(Mockito.anyString(), Mockito.any())).thenReturn(virtualMachine);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new VirtualMachine()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllVmSuccess() throws Exception {
        List<VirtualMachine> virtualMachines = new ArrayList<>();
        String url = String.format("/mec/developer/v2/applications/%s/vms", UUID.randomUUID().toString());
        Mockito.when(vmAppVmService.getAllVm(Mockito.anyString())).thenReturn(virtualMachines);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.get(url).with((csrf())).content(new Gson().toJson(new VirtualMachine()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetVmSuccess() throws Exception {
        VirtualMachine virtualMachine = new VirtualMachine();
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(vmAppVmService.getVm(Mockito.anyString(), Mockito.anyString())).thenReturn(virtualMachine);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.get(url).with((csrf())).content(new Gson().toJson(new VirtualMachine()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyVmSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(vmAppVmService.modifyVm(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new VirtualMachine()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteVmSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(vmAppVmService.deleteVm(Mockito.anyString(), Mockito.anyString(),  Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.delete(url).with((csrf())).content(new Gson().toJson(new VirtualMachine()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
