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

package org.edgegallery.developer.test.service.apppackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.application.EnumApplicationType;
import org.edgegallery.developer.model.application.container.ContainerApplication;
import org.edgegallery.developer.model.application.vm.EnumVMStatus;
import org.edgegallery.developer.model.application.vm.Network;
import org.edgegallery.developer.model.application.vm.VMApplication;
import org.edgegallery.developer.model.application.vm.VirtualMachine;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.apppackage.AppPackageService;
import org.edgegallery.developer.service.apppackage.csar.creater.ContainerPackageFileCreator;
import org.edgegallery.developer.service.apppackage.csar.creater.PackageFileCreator;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.test.service.application.ApplicationServiceTest;
import org.edgegallery.developer.util.SpringContextUtil;
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
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class AppPackageServiceTest extends AbstractJUnit4SpringContextTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPackageServiceTest.class);

    @Autowired
    private AppPackageService appPackageService;

    @Autowired
    private VMAppNetworkService networkService;

    @Autowired
    private VMAppVmService vmAppVmService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UploadFileService uploadFileService;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        SpringContextUtil.setApplicationContext(applicationContext);
    }

    @Test
    public void testGetAppPackageSuccess() throws IOException {
        AppPackage appPackage = appPackageService.getAppPackage("f2759fcb-bb4b-42f5-bc6c-8e1635348fda");
        Assert.assertNotNull(appPackage);
    }

    @Test
    public void testGetAppPackageByAppIdSuccess() throws IOException {
        AppPackage appPackage = appPackageService.getAppPackageByAppId("6a75a2bd-9811-432f-bbe8-2813aa97d365");
        Assert.assertNotNull(appPackage);
    }

    @Test
    public void testGetAppPackageStructureBadWithEmptyId() throws IOException {
        try {
            appPackageService.getAppPackageStructure("");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is empty!", e.getMessage());
        }
    }

    @Test
    public void testGetAppPackageStructureBadWithErrId() throws IOException {
        try {
            appPackageService.getAppPackageStructure("err-id");
        } catch (DataBaseException e) {
            Assert.assertEquals("packageId is error", e.getMessage());
        }
    }

    @Test
    public void testGetAppPackageStructureBadWithErrFileName() throws IOException {
        try {
            appPackageService.getAppPackageStructure("f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("app pkg not found!", e.getMessage());
        }
    }

    @Test
    public void testGetAppPackageFileContentBadWithNullPkgId() throws IOException {
        try {
            ReleasedPkgFileContentReqDto structureReqDto = new ReleasedPkgFileContentReqDto();
            appPackageService.getAppPackageFileContent(structureReqDto, "");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is null", e.getMessage());
        }
    }

    @Test
    public void testGetAppPackageFileContentBadWithNullReqDto() throws IOException {
        try {
            ReleasedPkgFileContentReqDto structureReqDto = new ReleasedPkgFileContentReqDto();
            appPackageService.getAppPackageFileContent(null, "zzz");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("structureReqDto is null", e.getMessage());
        }
    }

    @Test
    public void testGetAppPackageFileContentBadWithNullReturn() throws IOException {
        try {
            ReleasedPkgFileContentReqDto structureReqDto = new ReleasedPkgFileContentReqDto();
            appPackageService.getAppPackageFileContent(structureReqDto, "zzz");
        } catch (DataBaseException e) {
            Assert.assertEquals("packageId is error", e.getMessage());
        }
    }

    @Test
    public void testGetAppPackageFileContentBadWithNullFile() throws IOException {
        try {
            ReleasedPkgFileContentReqDto structureReqDto = new ReleasedPkgFileContentReqDto();
            appPackageService.getAppPackageFileContent(structureReqDto, "f2759fcb-bb4b-42f5-bc6c-8e1635348fda");
        } catch (Exception e) {
            Assert.assertNull(e.getMessage());
        }
    }

    @Test
    public void testUpdateAppPackageStructureBadWithEmptyId() throws IOException {
        try {
            appPackageService.updateAppPackageFileContent(new ReleasedPkgFileContent(), "");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is null", e.getMessage());
        }
    }

    @Test
    public void testUpdateAppPackageStructureBadWithErrId() throws IOException {
        try {
            appPackageService.getAppPackageStructure("err-id");
        } catch (DataBaseException e) {
            Assert.assertEquals("packageId is error", e.getMessage());
        }
    }

    @Test
    public void testUpdateAppPackageFileContentBadWithNullReqDto() throws IOException {
        try {
            appPackageService.updateAppPackageFileContent(null, "f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("releasedPkgFileContent is null", e.getMessage());
        }
    }

    @Test
    public void testUpdateAppPackageStructureBadWithNullFile() throws IOException {
        try {
            appPackageService
                .updateAppPackageFileContent(new ReleasedPkgFileContent(), "f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("app pkg decompress dir was not found", e.getMessage());
        }
    }

    @Test
    public void testGenerateAppPackageBadWithNullReturn() throws IOException {
        try {
            VMApplication vmApplication = createVmApp();
            Assert.assertEquals("vmApp", vmApplication.getName());
            appPackageService.generateAppPackage(vmApplication);
        } catch (NullPointerException e) {
            Assert.assertNull(e.getMessage());
        }
    }

    @Test
    public void testDeletePackageBadWithErrId() throws IOException {
        boolean res = appPackageService.deletePackage("vmApplication");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testGenerateAppPackageFail() throws IOException {
        try {
            new MockUp<ContainerPackageFileCreator>() {
                @Mock
                public String generateAppPackageFile() {
                    return null;
                }
            };
            ContainerApplication application = new ContainerApplication();
            application.setId(UUID.randomUUID().toString());
            appPackageService.generateAppPackage(application);
        } catch (FileOperateException e) {
            Assert.assertEquals("Generation app package error.", e.getMessage());
        }
    }

    @Test
    public void testGenerateAppPackageFail1() throws IOException {
        try {
            new MockUp<ContainerPackageFileCreator>() {
                @Mock
                public String generateAppPackageFile() {
                    return "";
                }
            };
            ContainerApplication application = new ContainerApplication();
            application.setId(UUID.randomUUID().toString());
            appPackageService.generateAppPackage(application);
        } catch (FileOperateException e) {
            Assert.assertEquals("Generation app package error.", e.getMessage());
        }
    }

    @Test
    public void testZipPackageFail1() throws IOException {
        try {
            appPackageService.zipPackage("application");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("package does not exist!", e.getMessage());
        }
    }

    @Test
    public void testZipPackageFail2() throws IOException {
        try {
            new MockUp<PackageFileCreator>() {
                @Mock
                public String PackageFileCompress() {
                    return "";
                }
            };
            appPackageService.zipPackage("f2759fcb-bb4b-42f5-bc6c-8e1635348fdd");
        } catch (FileOperateException e) {
            Assert.assertEquals("zip package error.", e.getMessage());
        }
    }

    @Test
    public void testZipPackageFail3() throws IOException {
        try {
            new MockUp<PackageFileCreator>() {
                @Mock
                public String PackageFileCompress() {
                    return null;
                }
            };
            appPackageService.zipPackage("f2759fcb-bb4b-42f5-bc6c-8e1635348fdd");
        } catch (FileOperateException e) {
            Assert.assertEquals("zip package error.", e.getMessage());
        }
    }

    @Test
    public void testZipPackageFail4() throws IOException {
        try {
            new MockUp<PackageFileCreator>() {
                @Mock
                public String PackageFileCompress() {
                    return null;
                }
            };
            appPackageService.zipPackage("f2759fcb-bb4b-42f5-bc6c-8e1635348fda");
        } catch (FileOperateException e) {
            Assert.assertEquals("zip package error.", e.getMessage());
        }
    }

    @Test
    public void testZipPackageFail5() throws IOException {
        try {
            new MockUp<PackageFileCreator>() {
                @Mock
                public String PackageFileCompress() {
                    return "";
                }
            };
            appPackageService.zipPackage("f2759fcb-bb4b-42f5-bc6c-8e1635348fda");
        } catch (FileOperateException e) {
            Assert.assertEquals("zip package error.", e.getMessage());
        }
    }

    @Test
    public void testDeletePackageRecordFail() throws IOException {
        boolean ret = appPackageService.deletePackageRecord("appPackage");
        Assert.assertEquals(true, ret);

    }

    @Test
    public void testDeletePackageSuccess() throws IOException {
        boolean res = appPackageService.deletePackage("f2759fcb-bb4b-42f5-bc6c-8e1635348fdb");
        Assert.assertEquals(true, res);
    }

    private VMApplication createVmApp() throws IOException {
        AccessUserUtil.setUser("d59d459c-f07e-4a44-a4e6-989752038c06", "admin");
        VMApplication application = new VMApplication();
        application.setId("328c4fcf-1581-40d1-afc1-6ae280489e8f");
        application.setName("vmApp");
        application.setDescription("test create vm app");
        application.setVersion("v1.0");
        application.setProvider("edgegallery");
        application.setArchitecture("X86");
        application.setAppClass(EnumAppClass.VM);
        application.setType("Video Application");
        application.setIndustry("Smart Park");
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            ApplicationServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadFile result = uploadFileService.uploadFile("b27d72b5-93a6-4db4-8268-7ec502331ade", "icon", uploadFile);
        application.setIconFileId(result.getFileId());
        application.setAppCreateType(EnumApplicationType.DEVELOP);
        application.setCreateTime(String.valueOf(new Date().getTime()));
        //vm app set network list
        List<Network> networkList = new ArrayList<>();
        Network network = new Network();
        network.setId("85818e6e-9cbc-4c04-ac6f-300df0fee295");
        network.setName("Network_MEP");
        network.setDescription(
            "The network with the edge computing platform is required when the application has service dependency or needs to publish services");
        networkList.add(network);
        application.setNetworkList(networkList);
        //vm app set VirtualMachine list
        VirtualMachine vm = new VirtualMachine();
        // id, app_id, name, flavor_id, image_id, user_data, status, area_zone, flavor_extra_specs
        List<VirtualMachine> vmList = new ArrayList<>();
        vm.setId("068fa7b9-e1bd-4eee-a7e8-2532889910a3");
        vm.setName("test-vm");
        vm.setFlavorId(UUID.randomUUID().toString());
        vm.setImageId(1);
        vm.setUserData("user data");
        vm.setStatus(EnumVMStatus.NOT_DEPLOY);
        vm.setAreaZone("xi'an");
        vm.setFlavorExtraSpecs("FlavorExtraSpecs");
        vmList.add(vm);
        application.setVmList(vmList);
        return application;
    }

}
