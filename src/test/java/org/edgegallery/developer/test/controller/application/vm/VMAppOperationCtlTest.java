package org.edgegallery.developer.test.controller.application.vm;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.UUID;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppOperationService;
import org.edgegallery.developer.service.proxy.ReverseProxyService;
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
public class VMAppOperationCtlTest {

    @MockBean
    private VMAppOperationServiceImpl VmAppOperationService;

    @MockBean
    private ReverseProxyService reverseProxyService;

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testInstantiateVmSuccess() throws Exception {
        OperationInfoRep operationInfoRep = new OperationInfoRep("");
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s/action/launch", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(VmAppOperationService.instantiateVM(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(operationInfoRep);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new OperationInfoRep("")))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testExportImageVmSuccess() throws Exception {
        OperationInfoRep operationInfoRep = new OperationInfoRep("");
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s/action/export-image", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(VmAppOperationService.createVmImage(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(operationInfoRep);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new OperationInfoRep("")))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testUploadFileVmSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s/action/upload-file", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(VmAppOperationService.uploadFileToVm(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testMergeFileVmSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/vms/%s/action/merge-file", UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(VmAppOperationService.mergeAppFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.get(url).with((csrf()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }
}