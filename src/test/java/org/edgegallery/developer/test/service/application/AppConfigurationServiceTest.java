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

package org.edgegallery.developer.test.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.model.application.configuration.AppCertificate;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.configuration.AppServiceProduced;
import org.edgegallery.developer.model.application.configuration.AppServiceRequired;
import org.edgegallery.developer.model.application.configuration.DnsRule;
import org.edgegallery.developer.model.application.configuration.TrafficRule;
import org.edgegallery.developer.service.application.AppConfigurationService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class AppConfigurationServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationServiceTest.class);

    @Autowired
    private AppConfigurationService configurationService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private VMAppNetworkService networkService;

    @Autowired
    private VMAppVmService vmAppVmService;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");

    }

    @Test
    public void testGetAppConfigurationSuccess() {
        AppConfiguration appConfiguration = configurationService
            .getAppConfiguration("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertNotNull(appConfiguration);
    }

    @Test
    public void testModifyAppConfigurationSuccess() {
        AppConfiguration appConfiguration = configurationService
            .getAppConfiguration("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertNotNull(appConfiguration);
        //set AppCertificate
        AppCertificate appCertificate = new AppCertificate();
        appCertificate.setAk("ak");
        appCertificate.setSk("sk");
        appConfiguration.setAppCertificate(appCertificate);
        //set AppServiceProduced
        List<AppServiceProduced> appServiceProduceds = new ArrayList<>();
        AppServiceProduced appServiceProduced = createNewAppServiceProduced();
        appServiceProduceds.add(appServiceProduced);
        appConfiguration.setAppServiceProducedList(appServiceProduceds);
        //set AppServiceRequired
        List<AppServiceRequired> appServiceRequireds = new ArrayList<>();
        AppServiceRequired appServiceRequired = createNewAppServiceRequired();
        appServiceRequireds.add(appServiceRequired);
        appConfiguration.setAppServiceRequiredList(appServiceRequireds);
        //set TrafficRule
        List<TrafficRule> trafficRules = new ArrayList<>();
        TrafficRule trafficRule = createNewTrafficRule();
        trafficRules.add(trafficRule);
        appConfiguration.setTrafficRuleList(trafficRules);
        //set DnsRule
        List<DnsRule> dnsRules = new ArrayList<>();
        DnsRule dnsRule = createNewDnsRule();
        dnsRules.add(dnsRule);
        appConfiguration.setDnsRuleList(dnsRules);
        boolean res = configurationService
            .modifyAppConfiguration("6a75a2bd-9811-432f-bbe8-2813aa97d364", appConfiguration);
        Assert.assertEquals(res, true);
    }

    @Test
    public void testGetAllTrafficRulesSuccess() {
        List<TrafficRule> trafficRuleList = configurationService
            .getAllTrafficRules("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertEquals(0, trafficRuleList.size());
    }

    @Test
    public void testCreateTrafficRulesBadWithErrId() {
        try {
            configurationService.createTrafficRules("test-id", null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("application does not exist, create traffic rule failed!", e.getMessage());
        }
    }

    @Test
    public void testCreateTrafficRulesSuccess() {
        TrafficRule trafficRule = createNewTrafficRule();
        TrafficRule createdTrafficRule = configurationService
            .createTrafficRules("6a75a2bd-9811-432f-bbe8-2813aa97d364", trafficRule);
        Assert.assertNotNull(createdTrafficRule);
    }

    @Test
    public void testCreateTrafficRulesBadWithExistRule() {
        try {
            TrafficRule trafficRule = createNewTrafficRule();
            TrafficRule createdTrafficRule = configurationService
                .createTrafficRules("6a75a2bd-9811-432f-bbe8-2813aa97d365", trafficRule);
        } catch (DeveloperException e) {
            Assert.assertEquals("create trafficRule failed: ruleId have exit", e.getMessage());
        }
    }

    @Test
    public void testModifyTrafficRuleSuccess() {
        TrafficRule trafficRule = createNewTrafficRule();
        boolean res = configurationService.modifyTrafficRule("6a75a2bd-9811-432f-bbe8-2813aa97d365", trafficRule);
        Assert.assertEquals(res, true);
    }

    @Test
    public void testDeleteTrafficRuleSuccess() {
        boolean res = configurationService
            .deleteTrafficRule("6a75a2bd-9811-432f-bbe8-2813aa97d366", "e7bb85d1-a461-465a-b335-7189d1e527d5");
        Assert.assertEquals(res, true);
    }

    @Test
    public void testCreateDnsRuleBadWithErrId() {
        try {
            configurationService.createDnsRule("test-id", null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("application does not exist, create dns rule failed!", e.getMessage());
        }
    }

    @Test
    public void testCreateDnsRuleBadWithExistRule() {
        try {
            DnsRule dnsRule = createNewDnsRule();
            configurationService.createDnsRule("6a75a2bd-9811-432f-bbe8-2813aa97d365", dnsRule);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("create DnsRule failed: ruleId have exit", e.getMessage());
        }
    }

    @Test
    public void testCreateDnsRuleSuccess() {
        DnsRule dnsRule = createNewDnsRule();
        DnsRule createdDnsRule = configurationService.createDnsRule("6a75a2bd-9811-432f-bbe8-2813aa97d364", dnsRule);
        Assert.assertNotNull(createdDnsRule);
    }

    @Test
    public void testGetAllDnsRuleSuccess() {
        List<DnsRule> dnsRuleList = configurationService.getAllDnsRules("6a75a2bd-9811-432f-bbe8-2813aa97d365");
        Assert.assertEquals(1, dnsRuleList.size());
    }

    @Test
    public void testDeleteDnsRuleSuccess() {
        boolean res = configurationService
            .deleteDnsRule("6a75a2bd-9811-432f-bbe8-2813aa97d366", "aeeb627d-a377-42bb-acb9-1f076682b206");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testModifyDnsRuleSuccess() {
        DnsRule dnsRule = createNewDnsRule();
        boolean res = configurationService.modifyDnsRule("6a75a2bd-9811-432f-bbe8-2813aa97d365", dnsRule);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testGetAllAppServiceProducedSuccess() {
        List<AppServiceProduced> res = configurationService
            .getAllServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d365");
        Assert.assertEquals(1, res.size());
    }

    @Test
    public void testCreateAppServiceProducedBadWithErrId() {
        try {
            configurationService.createServiceProduced("test-id", null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("application does not exist!", e.getMessage());
        }
    }

    @Test
    public void testCreateAppServiceProducedBadWithExistRule() {
        try {
            AppServiceProduced appServiceProduced = createNewAppServiceProduced();
            configurationService.createServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d365", appServiceProduced);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("create serviceProduced failed,serviceName have exit", e.getMessage());
        }
    }

    @Test
    public void testCreateAppServiceProducedSuccess() {
        AppServiceProduced appServiceProduced = createNewAppServiceProduced();
        AppServiceProduced appServiceProducedCreated = configurationService
            .createServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d364", appServiceProduced);
        Assert.assertNotNull(appServiceProducedCreated);
    }

    @Test
    public void testDeleteAppServiceProducedSuccess() {
        boolean res = configurationService
            .deleteServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d366", "2e334f90-53ca-4d4c-a644-e90a44fa73c9");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testModifyAppServiceProducedBadWithErrId() {
        try {
            configurationService
                .modifyServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d366", "2e334f90-53ca-4d4c-a644-e90a44fa73aa",
                    null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("appServiceProducedId is incorrect!", e.getMessage());
        }
    }

    @Test
    public void testModifyAppServiceProducedSuccess() {
        AppServiceProduced appServiceProduced = createNewAppServiceProduced();
        boolean res = configurationService
            .modifyServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d365", "2e334f90-53ca-4d4c-a644-e90a44fa73c8",
                appServiceProduced);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testModifyAppServiceProducedSuccessWithNullId() {
        AppServiceProduced appServiceProduced = createNewAppServiceProducedWithNullId();
        boolean res = configurationService
            .modifyServiceProduced("6a75a2bd-9811-432f-bbe8-2813aa97d365", "2e334f90-53ca-4d4c-a644-e90a44fa73c8",
                appServiceProduced);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testGetAllServiceRequiredSuccess() {
        List<AppServiceRequired> appServiceRequireds = configurationService
            .getAllServiceRequired("6a75a2bd-9811-432f-bbe8-2813aa97d365");
        Assert.assertEquals(1, appServiceRequireds.size());
    }

    @Test
    public void testCreateAppServiceRequiredBadWithId() {
        try {
            configurationService.createServiceRequired("test-id", null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("application does not exist!", e.getMessage());
        }
    }

    @Test
    public void testCreateAppServiceRequiredBadWithExistSerName() {
        try {
            AppServiceRequired appServiceRequired = createNewAppServiceRequired();
            configurationService.createServiceRequired("6a75a2bd-9811-432f-bbe8-2813aa97d365", appServiceRequired);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("create serviceRequired failed: serName does exist", e.getMessage());
        }
    }

    @Test
    public void testCreateAppServiceRequiredSuccess() {
        AppServiceRequired appServiceRequired = createAnotherAppServiceRequired();
        AppServiceRequired appServiceRequiredCreated = configurationService
            .createServiceRequired("6a75a2bd-9811-432f-bbe8-2813aa97d364", appServiceRequired);
        Assert.assertNotNull(appServiceRequiredCreated);
    }

    @Test
    public void testCreateAppServiceRequiredBad() {
        try {
            AppServiceRequired appServiceRequired = createNewAppServiceRequired();
            AppServiceRequired appServiceRequiredCreated = configurationService
                .createServiceRequired("6a75a2bd-9811-432f-bbe8-2813aa97d364", appServiceRequired);
        } catch (DataBaseException e) {
            Assert.assertEquals("update selectCount failed", e.getMessage());
        }
    }

    @Test
    public void testModifyAppServiceRequiredSuccess() {
        AppServiceRequired appServiceRequired = createNewAppServiceRequired();
        boolean res = configurationService
            .modifyServiceRequired("6a75a2bd-9811-432f-bbe8-2813aa97d365", appServiceRequired);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testDeleteAppServiceRequiredSuccess() {
        boolean res = configurationService
            .deleteServiceRequired("6a75a2bd-9811-432f-bbe8-2813aa97d366", "serName-test-001");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testGetAppCertificateSuccess() {
        AppCertificate appCertificate = configurationService.getAppCertificate("test-id");
        Assert.assertNull(appCertificate);
    }

    @Test
    public void testCreateAppCertificateBadWithErrId() {
        try {
            configurationService.createAppCertificate("test-id", null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("application does not exist!", e.getMessage());
        }
    }

    @Test
    public void testCreateAppCertificateSuccess() {
        AppCertificate appCertificate = new AppCertificate();
        appCertificate.setSk("sk");
        appCertificate.setAk("ak");
        AppCertificate appCertificateCreated = configurationService
            .createAppCertificate("6a75a2bd-9811-432f-bbe8-2813aa97d364", appCertificate);
        Assert.assertNotNull(appCertificateCreated);
    }

    @Test
    public void testModifyAppCertificateSuccess() {
        AppCertificate appCertificate = new AppCertificate();
        appCertificate.setSk("sk1");
        appCertificate.setAk("ak1");
        boolean res = configurationService.modifyAppCertificate("6a75a2bd-9811-432f-bbe8-2813aa97d365", appCertificate);
        Assert.assertEquals(res, true);
    }

    @Test
    public void testDeleteAppCertificateSuccess() {
        AppCertificate appCertificate = new AppCertificate();
        appCertificate.setSk("sk1");
        appCertificate.setAk("ak1");
        boolean res = configurationService.deleteAppCertificate("6a75a2bd-9811-432f-bbe8-2813aa97d366");
        Assert.assertEquals(res, true);
    }

    private AppServiceProduced createNewAppServiceProduced() {
        AppServiceProduced appServiceProduced = new AppServiceProduced();
        appServiceProduced.setAppServiceProducedId(UUID.randomUUID().toString());
        appServiceProduced.setOneLevelName("oneLevelName");
        appServiceProduced.setOneLevelNameEn("oneLevelNameEn");
        appServiceProduced.setTwoLevelName("twoLevelName");
        appServiceProduced.setDescription("desc");
        appServiceProduced.setApiFileId(UUID.randomUUID().toString());
        appServiceProduced.setGuideFileId(UUID.randomUUID().toString());
        appServiceProduced.setIconFileId(UUID.randomUUID().toString());
        appServiceProduced.setServiceName("serviceName-000");
        appServiceProduced.setInternalPort(22222);
        appServiceProduced.setVersion("v1.0");
        appServiceProduced.setProtocol("https");
        appServiceProduced.setAuthor("admin");
        appServiceProduced.setExperienceUrl("experienceUrl");
        return appServiceProduced;
    }

    private AppServiceProduced createNewAppServiceProducedWithNullId() {
        AppServiceProduced appServiceProduced = new AppServiceProduced();
        appServiceProduced.setAppServiceProducedId(null);
        appServiceProduced.setOneLevelName("oneLevelName");
        appServiceProduced.setOneLevelNameEn("oneLevelNameEn");
        appServiceProduced.setTwoLevelName("twoLevelName");
        appServiceProduced.setDescription("desc");
        appServiceProduced.setApiFileId(UUID.randomUUID().toString());
        appServiceProduced.setGuideFileId(UUID.randomUUID().toString());
        appServiceProduced.setIconFileId(UUID.randomUUID().toString());
        appServiceProduced.setServiceName("serviceName-000");
        appServiceProduced.setInternalPort(22222);
        appServiceProduced.setVersion("v1.0");
        appServiceProduced.setProtocol("https");
        appServiceProduced.setAuthor("admin");
        appServiceProduced.setExperienceUrl("experienceUrl");
        return appServiceProduced;
    }

    private AppServiceRequired createNewAppServiceRequired() {
        AppServiceRequired appServiceRequired = new AppServiceRequired();
        appServiceRequired.setId("143e8608-7304-4932-9d99-4bd6b115dac8");
        appServiceRequired.setOneLevelName("平台基础服务");
        appServiceRequired.setOneLevelNameEn("Platform services");
        appServiceRequired.setTwoLevelName("服务发现");
        appServiceRequired.setTwoLevelNameEn("service discovery");
        appServiceRequired.setSerName("serName-test");
        appServiceRequired.setVersion("v1.0");
        appServiceRequired.setAppId("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        appServiceRequired.setPackageId("pkgId");
        appServiceRequired.setRequestedPermissions(true);
        return appServiceRequired;
    }

    private AppServiceRequired createAnotherAppServiceRequired() {
        AppServiceRequired appServiceRequired = new AppServiceRequired();
        appServiceRequired.setId("e111f3e7-90d8-4a39-9874-ea6ea6752ef0");
        appServiceRequired.setOneLevelName("平台基础服务");
        appServiceRequired.setOneLevelNameEn("Platform services");
        appServiceRequired.setTwoLevelName("服务发现");
        appServiceRequired.setTwoLevelNameEn("service discovery");
        appServiceRequired.setSerName("serName-test");
        appServiceRequired.setVersion("v1.0");
        appServiceRequired.setAppId("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        appServiceRequired.setPackageId("pkgId");
        appServiceRequired.setRequestedPermissions(true);
        return appServiceRequired;
    }

    private TrafficRule createNewTrafficRule() {
        TrafficRule trafficRule = new TrafficRule();
        trafficRule.setTrafficRuleId("e7bb85d1-a461-465a-b335-7189d1e527d4");
        trafficRule.setAction("setAction");
        trafficRule.setDstInterface(new ArrayList<>());
        trafficRule.setFilterType("setFilterType");
        trafficRule.setPriority(0);
        trafficRule.setTrafficFilter(new ArrayList<>());
        return trafficRule;
    }

    private DnsRule createNewDnsRule() {
        DnsRule dnsRule = new DnsRule();
        dnsRule.setDnsRuleId("aeeb627d-a377-42bb-acb9-1f076682b205");
        dnsRule.setDomainName("domainName");
        dnsRule.setIpAddress("1.1.1.1");
        dnsRule.setIpAddressType("setIpAddressType");
        dnsRule.setTtl("ttl");
        return dnsRule;
    }

}
