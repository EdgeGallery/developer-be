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

package org.edgegallery.developer.test.controller.application;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.UUID;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.edgegallery.developer.service.application.ApplicationService;
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
public class AppConfigurationCtlTest {

    @MockBean
    private ApplicationService applicationService;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AppConfigurationService appConfigurationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetOneAppConfigSuccess() throws Exception {
        AppConfiguration configuration = new AppConfiguration();
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.getAppConfiguration(Mockito.anyString())).thenReturn(configuration);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyOneAppConfigSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration", UUID.randomUUID().toString());
        Mockito.when(applicationService.modifyApplication(Mockito.anyString(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new AppConfiguration()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllTrafficRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/trafficrules", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.getAllTrafficRules(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateTrafficRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/trafficrules", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.createTrafficRules(Mockito.anyString(), Mockito.any()))
            .thenReturn(new TrafficRule());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new TrafficRule()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyTrafficRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/trafficrules/%s", UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.modifyTrafficRule(Mockito.anyString(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new TrafficRule()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteTrafficRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/trafficrules/%s", UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.deleteTrafficRule(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllDnsRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/dnsrules", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.getAllDnsRules(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateDnsRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/dnsrules", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.createDnsRule(Mockito.anyString(), Mockito.any()))
            .thenReturn(new DnsRule());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new DnsRule()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyDnsRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/dnsrules/%s", UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.modifyDnsRule(Mockito.anyString(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new DnsRule()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteDnsRuleSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/dnsrules/%s", UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.deleteDnsRule(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllServiceProducedSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/serviceproduceds",
            UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.getAllServiceProduced(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateServiceProducedSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/serviceproduceds",
            UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.createServiceProduced(Mockito.anyString(), Mockito.any()))
            .thenReturn(new AppServiceProduced());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new AppServiceProduced()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyServiceProducedSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/serviceproduceds/%s",
            UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(
            appConfigurationService.modifyServiceProduced(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new AppServiceProduced()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteServiceProducedSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/serviceproduceds/%s",
            UUID.randomUUID().toString(), UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.deleteServiceProduced(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAllAppServiceRequiredSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/servicerequireds",
            UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.getAllServiceRequired(Mockito.anyString())).thenReturn(new ArrayList<>());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateAppServiceRequiredSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/servicerequireds",
            UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.createServiceRequired(Mockito.anyString(), Mockito.any()))
            .thenReturn(new AppServiceRequired());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new AppServiceRequired()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyAppServiceRequiredSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/servicerequireds/%s",
            UUID.randomUUID().toString(), "sername");
        Mockito.when(appConfigurationService.modifyServiceRequired(Mockito.anyString(), Mockito.any()))
            .thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new AppServiceRequired()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteAppServiceRequiredSuccess() throws Exception {
        String url = String.format("/mec/developer/v2/applications/%s/appconfiguration/servicerequireds/%s",
            UUID.randomUUID().toString(), "sername");
        Mockito.when(appConfigurationService.deleteServiceRequired(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testGetAppCertificateSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/appcertificate", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.getAppCertificate(Mockito.anyString())).thenReturn(new AppCertificate());
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testCreateAppCertificateSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/appcertificate", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.createAppCertificate(Mockito.anyString(), Mockito.any()))
            .thenReturn(new AppCertificate());
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.post(url).with((csrf())).content(new Gson().toJson(new AppCertificate()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testModifyAppCertificateSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/appcertificate", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.modifyAppCertificate(Mockito.anyString(), Mockito.any())).thenReturn(true);
        ResultActions actions = mvc.perform(
            MockMvcRequestBuilders.put(url).with((csrf())).content(new Gson().toJson(new AppCertificate()))
                .contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_ADMIN")
    public void testDeleteAppCertificateSuccess() throws Exception {
        String url = String
            .format("/mec/developer/v2/applications/%s/appconfiguration/appcertificate", UUID.randomUUID().toString());
        Mockito.when(appConfigurationService.deleteAppCertificate(Mockito.anyString())).thenReturn(true);
        ResultActions actions = mvc
            .perform(MockMvcRequestBuilders.delete(url).with((csrf())).contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(MockMvcResultMatchers.status().isOk());
        Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

}
