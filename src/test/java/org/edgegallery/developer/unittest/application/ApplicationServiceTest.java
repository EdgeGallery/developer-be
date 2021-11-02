/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.developer.unittest.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.EnumApplicationType;
import org.edgegallery.developer.model.application.configuration.AppConfiguration;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.restful.ApplicationDetail;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.uploadfile.UploadService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ApplicationServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceTest.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UploadService uploadFileService;

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
    public void testCreateContainerAppSuccess() throws IOException {
        //upload icon
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            ApplicationServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadedFile result = uploadFileService.uploadFile("icon", uploadFile);
        Assert.assertNotNull(result);
        //create application
        AccessUserUtil.setUser("b27d72b5-93a6-4db4-8268-7ec502331ade", "admin");
        Application application = new Application();
        application.setName("containerApp");
        application.setDescription("test create container app");
        application.setVersion("v1.0");
        application.setProvider("edgegallery");
        application.setArchitecture("X86");
        application.setAppClass(EnumAppClass.CONTAINER);
        application.setType("Video Application");
        application.setIndustry("Smart Park");
        application.setIconFileId(result.getFileId());
        application.setAppCreateType(EnumApplicationType.DEVELOP);
        application.setCreateTime(String.valueOf(new Date().getTime()));
        Application retApp = applicationService.createApplication(application);
        Assert.assertNotNull(retApp);
    }

    @Test
    public void testCreateVMAppSuccess() throws IOException {
        //upload icon
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            ApplicationServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadedFile result = uploadFileService.uploadFile("icon", uploadFile);
        Assert.assertNotNull(result);
        //create application
        AccessUserUtil.setUser("b27d72b5-93a6-4db4-8268-7ec502331ade", "admin");
        Application application = new Application();
        application.setName("vmApp");
        application.setDescription("test create vm app");
        application.setVersion("v1.0");
        application.setProvider("edgegallery");
        application.setArchitecture("X86");
        application.setAppClass(EnumAppClass.VM);
        application.setType("Video Application");
        application.setIndustry("Smart Park");
        application.setIconFileId(result.getFileId());
        application.setAppCreateType(EnumApplicationType.DEVELOP);
        application.setCreateTime(String.valueOf(new Date().getTime()));
        Application retApp = applicationService.createApplication(application);
        Assert.assertNotNull(retApp);
    }

    @Test
    public void testCreateAppBadWithNullIconFileId() {
        //create application
        try {
            AccessUserUtil.setUser("b27d72b5-93a6-4db4-8268-7ec502331ade", "admin");
            Application application = new Application();
            application.setName("vmApp");
            application.setDescription("test create vm app");
            application.setVersion("v1.0");
            application.setProvider("edgegallery");
            application.setArchitecture("X86");
            application.setAppClass(EnumAppClass.VM);
            application.setType("Video Application");
            application.setIndustry("Smart Park");
            application.setIconFileId(null);
            application.setAppCreateType(EnumApplicationType.DEVELOP);
            application.setCreateTime(String.valueOf(new Date().getTime()));
            applicationService.createApplication(application);
        } catch (FileFoundFailException e) {
            Assert.assertEquals("icon file is null", e.getMessage());
        }
    }

    @Test
    public void testGetOneAppSuccess() {
        Application queryRet = applicationService.getApplication("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertNotNull(queryRet);
    }

    @Test
    public void testModifyOneAppSuccess() {
        Application queryRet = applicationService.getApplication("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertNotNull(queryRet);
        queryRet.setDescription("test update");
        boolean updateRet = applicationService.modifyApplication(queryRet.getId(), queryRet);
        Assert.assertEquals(true, updateRet);
    }

    @Test
    public void testGetAppListSuccess() {
        AccessUserUtil.setUser("b27d72b5-93a6-4db4-8268-7ec502331ade", "admin");
        Page<Application> appList = applicationService.getApplicationByNameWithFuzzy("container", 10, 0);
        Assert.assertNotNull(appList.getResults());
    }

    @Test
    public void testDeleteAppBadWithErrId() {
        try {
            applicationService.deleteApplication("appId");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("Application does not exist.", e.getMessage());
        }
    }

    @Test
    public void testDeleteAppSuccess() {
        boolean res = applicationService.deleteApplication("4cbbab9d-c48f-4adb-ae82-d1816d8edd7b");
        Assert.assertEquals(res, true);
    }

    @Test
    public void testGetContainerAppDetailSuccess() {
        ApplicationDetail detail = applicationService.getApplicationDetail("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertNotNull(detail);
    }

    @Test
    public void tesGetVmAppDetailSuccess() {
        ApplicationDetail detail = applicationService.getApplicationDetail("4cbbab9d-c48f-4adb-ae82-d1816d8edd7b");
        Assert.assertNotNull(detail);
    }

    @Test
    public void testGetAppDetailBadWithErrId() {
        try {
            applicationService.getApplicationDetail("appId");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("Application does not exist.", e.getMessage());
        }
    }

    @Test
    public void testModifyAppDetailBadWithErrId() {
        try {
            applicationService.modifyApplicationDetail("appId", null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("Application does not exist.", e.getMessage());
        }
    }

    @Test
    public void testModifyContainerAppDetailSuccess() {
        Application application = applicationService.getApplication("6a75a2bd-9811-432f-bbe8-2813aa97d364");
        Assert.assertNotNull(application);
        application.setAppConfiguration(new AppConfiguration());
        ApplicationDetail applicationDetail = new ApplicationDetail();
        ContainerApplication containerApplication = new ContainerApplication(application);
        applicationDetail.setContainerApp(containerApplication);
        boolean res = applicationService
            .modifyApplicationDetail("6a75a2bd-9811-432f-bbe8-2813aa97d364", applicationDetail);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testModifyVMAppDetailSuccess() {
        Application application = applicationService.getApplication("3f11715f-b59e-4c23-965b-b7f9c34c20d1");
        Assert.assertNotNull(application);
        application.setAppConfiguration(new AppConfiguration());
        ApplicationDetail applicationDetail = new ApplicationDetail();
        VMApplication vmApplication = new VMApplication(application);
        //vm app set network list
        List<Network> networkList = new ArrayList<>();
        Network network = new Network();
        network.setId("85818e6e-9cbc-4c04-ac6f-300df0fee294");
        network.setName("Network_MEP");
        network.setDescription(
            "The network with the edge computing platform is required when the application has service dependency or needs to publish services");
        Network createdNetWork = networkService.createNetwork("3f11715f-b59e-4c23-965b-b7f9c34c20d1", network);
        Assert.assertNotNull(createdNetWork);
        networkList.add(createdNetWork);
        vmApplication.setNetworkList(networkList);
        //vm app set VirtualMachine list
        VirtualMachine vm = new VirtualMachine();
       // id, app_id, name, flavor_id, image_id, user_data, status, area_zone, flavor_extra_specs
        List<VirtualMachine> vmList = new ArrayList<>();
        vm.setId("068fa7b9-e1bd-4eee-a7e8-2532889910a2");
        vm.setName("test-vm");
        vm.setFlavorId(UUID.randomUUID().toString());
        vm.setImageId(1);
        vm.setUserData("user data");
        vm.setStatus("NORMAL");
        vm.setAreaZone("xi'an");
        vm.setFlavorExtraSpecs("FlavorExtraSpecs");
        VirtualMachine virtualMachine = vmAppVmService.createVm("3f11715f-b59e-4c23-965b-b7f9c34c20d1",vm);
        Assert.assertNotNull(virtualMachine);
        vmList.add(virtualMachine);
        vmApplication.setVmList(vmList);
        applicationDetail.setVmApp(vmApplication);
        boolean res = applicationService
            .modifyApplicationDetail("3f11715f-b59e-4c23-965b-b7f9c34c20d1", applicationDetail);
        Assert.assertEquals(true, res);
    }

}
