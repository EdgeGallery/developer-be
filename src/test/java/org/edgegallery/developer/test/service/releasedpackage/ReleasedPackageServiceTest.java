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

package org.edgegallery.developer.test.service.releasedpackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.RestfulRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContent;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgFileContentReqDto;
import org.edgegallery.developer.model.releasedpackage.ReleasedPkgReqDto;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.vm.VMAppNetworkService;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.releasedpackage.ReleasedPackageService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.AppStoreUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ReleasedPackageServiceTest extends AbstractJUnit4SpringContextTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleasedPackageServiceTest.class);

    @Autowired
    private ReleasedPackageService releasedPackageService;

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
        // new MockUp<AppStoreUtil>() {
        //     @Mock
        //     public ResponseEntity<String> getPkgInfo(String appId, String pkgId, String token) {
        //         return null;
        //     }
        //
        //     @Mock
        //     public ResponseEntity<byte[]> downloadPkg(String appId, String pkgId, String token) {
        //         return null;
        //     }
        // };

    }

    @Test
    public void testSynchronizePackageFailWithNullUser() throws IOException {
        try {
            releasedPackageService.synchronizePackage(null, null);
        } catch (UnauthorizedException e) {
            Assert.assertEquals("no user info was found", e.getMessage());
        }
    }

    @Test
    public void testSynchronizePackageFailWithNullReleasedPkgReqDto() throws IOException {
        User user = new User("userId", "userName", "userAuth");
        try {
            releasedPackageService.synchronizePackage(user, null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("no request body info was found", e.getMessage());
        }
    }

    @Test
    public void testSynchronizePackageFailWithNullQueryPkgRes() throws IOException {
        User user = new User("userId", "userName", "userAuth");
        new MockUp<AppStoreUtil>() {
            @Mock
            public ResponseEntity<String> getPkgInfo(String appId, String pkgId, String token) {
                return null;
            }
        };
        try {
            List<ReleasedPkgReqDto> pkgReqDtos = new ArrayList<>();
            ReleasedPkgReqDto releasedPkgReqDto = new ReleasedPkgReqDto();
            releasedPkgReqDto.setAppId("appId");
            releasedPkgReqDto.setPackageId("pkgId");
            pkgReqDtos.add(releasedPkgReqDto);
            releasedPackageService.synchronizePackage(user, pkgReqDtos);
        } catch (RestfulRequestException e) {
            Assert.assertEquals("call app store query pkg interface failed!", e.getMessage());
        }
    }

    @Test
    public void testSynchronizePackageFailWithFalseDownloadPkgRes() throws IOException {
        User user = new User("userId", "userName", "userAuth");
        new MockUp<AppStoreUtil>() {
            @Mock
            public ResponseEntity<String> getPkgInfo(String appId, String pkgId, String token) {
                String res = "{\n" + "    \"data\": {\n"
                    + "        \"packageId\": \"fbdfee842a444c9fa2a940f240b3fcef\",\n"
                    + "        \"size\": \"50961\",\n"
                    + "        \"format\": \"{\\\"name\\\":\\\"5f631af0924d49b887408feb13258dca\\\",\\\"childs\\\":[{\\\"name\\\":\\\"Image\\\",\\\"childs\\\":[{\\\"name\\\":\\\"SwImageDesc.json\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"APPD\\\",\\\"childs\\\":[{\\\"name\\\":\\\"containerApp_eg_v1.2_X86.zip\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"containerApp_eg_v1.2_X86.mf\\\",\\\"childs\\\":[]},{\\\"name\\\":\\\"TOSCA-Metadata\\\",\\\"childs\\\":[{\\\"name\\\":\\\"TOSCA.meta\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Artifacts\\\",\\\"childs\\\":[{\\\"name\\\":\\\"Other\\\",\\\"childs\\\":[{\\\"name\\\":\\\"my_script.csh\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Informational\\\",\\\"childs\\\":[{\\\"name\\\":\\\"user_guide.txt\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Deployment\\\",\\\"childs\\\":[{\\\"name\\\":\\\"Charts\\\",\\\"childs\\\":[{\\\"name\\\":\\\"spoderfoot.tgz\\\",\\\"childs\\\":[]}]}]},{\\\"name\\\":\\\"ChangeLog.txt\\\",\\\"childs\\\":[]},{\\\"name\\\":\\\"Tests\\\",\\\"childs\\\":[{\\\"name\\\":\\\"health check.yaml\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Docs\\\",\\\"childs\\\":[{\\\"name\\\":\\\"dalulogo.png\\\",\\\"childs\\\":[]},{\\\"name\\\":\\\"DaluRobot.md\\\",\\\"childs\\\":[]}]}]}]}\",\n"
                    + "        \"createTime\": \"2021-12-07T09:39:22.252+0000\",\n"
                    + "        \"name\": \"containerApp\",\n" + "        \"version\": \"v1.2\",\n"
                    + "        \"type\": \"Video Application\",\n" + "\t\t\"details\":\"xx\",\n"
                    + "        \"affinity\": \"X86\",\n" + "        \"industry\": \"Smart Park\",\n"
                    + "        \"contact\": null,\n" + "        \"appId\": \"b6d3f961be2f41a38c90b08ea2ce1c00\",\n"
                    + "        \"userId\": \"39937079-99fe-4cd8-881f-04ca8c4fe09d\",\n"
                    + "        \"userName\": \"admin\",\n" + "        \"status\": \"Published\",\n"
                    + "        \"shortDesc\": \"asdasd\",\n" + "        \"showType\": \"public\",\n"
                    + "        \"testTaskId\": \"ee127f57-ac70-4d05-bd1d-19700014b40b\",\n"
                    + "        \"provider\": \"eg\",\n" + "        \"demoVideoName\": null,\n"
                    + "        \"deployMode\": \"container\",\n" + "        \"experienceAble\": false\n" + "    },\n"
                    + "    \"retCode\": 0,\n" + "    \"params\": null,\n"
                    + "    \"message\": \"query package by packageId success.\"\n" + "}";
                return ResponseEntity.ok(res);
            }

            @Mock
            public ResponseEntity<byte[]> downloadPkg(String appId, String pkgId, String token) {
                byte[] bytes = new byte[10];
                return ResponseEntity.ok(bytes);
            }
        };
        try {
            List<ReleasedPkgReqDto> pkgReqDtos = new ArrayList<>();
            ReleasedPkgReqDto releasedPkgReqDto = new ReleasedPkgReqDto();
            releasedPkgReqDto.setAppId("appId");
            releasedPkgReqDto.setPackageId("pkgId");
            pkgReqDtos.add(releasedPkgReqDto);
            releasedPackageService.synchronizePackage(user, pkgReqDtos);
        } catch (RestfulRequestException e) {
            Assert.assertEquals("download pkg failed!", e.getMessage());
        }
    }

    @Test
    public void testSynchronizePackageSuccess() throws IOException {
        User user = new User("userId", "userName", "userAuth");
        new MockUp<AppStoreUtil>() {
            @Mock
            public ResponseEntity<String> getPkgInfo(String appId, String pkgId, String token) {
                String res = "{\n" + "    \"data\": {\n"
                    + "        \"packageId\": \"fbdfee842a444c9fa2a940f240b3fcef\",\n"
                    + "        \"size\": \"50961\",\n"
                    + "        \"format\": \"{\\\"name\\\":\\\"5f631af0924d49b887408feb13258dca\\\",\\\"childs\\\":[{\\\"name\\\":\\\"Image\\\",\\\"childs\\\":[{\\\"name\\\":\\\"SwImageDesc.json\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"APPD\\\",\\\"childs\\\":[{\\\"name\\\":\\\"containerApp_eg_v1.2_X86.zip\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"containerApp_eg_v1.2_X86.mf\\\",\\\"childs\\\":[]},{\\\"name\\\":\\\"TOSCA-Metadata\\\",\\\"childs\\\":[{\\\"name\\\":\\\"TOSCA.meta\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Artifacts\\\",\\\"childs\\\":[{\\\"name\\\":\\\"Other\\\",\\\"childs\\\":[{\\\"name\\\":\\\"my_script.csh\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Informational\\\",\\\"childs\\\":[{\\\"name\\\":\\\"user_guide.txt\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Deployment\\\",\\\"childs\\\":[{\\\"name\\\":\\\"Charts\\\",\\\"childs\\\":[{\\\"name\\\":\\\"spoderfoot.tgz\\\",\\\"childs\\\":[]}]}]},{\\\"name\\\":\\\"ChangeLog.txt\\\",\\\"childs\\\":[]},{\\\"name\\\":\\\"Tests\\\",\\\"childs\\\":[{\\\"name\\\":\\\"health check.yaml\\\",\\\"childs\\\":[]}]},{\\\"name\\\":\\\"Docs\\\",\\\"childs\\\":[{\\\"name\\\":\\\"dalulogo.png\\\",\\\"childs\\\":[]},{\\\"name\\\":\\\"DaluRobot.md\\\",\\\"childs\\\":[]}]}]}]}\",\n"
                    + "        \"createTime\": \"2021-12-07T09:39:22.252+0000\",\n"
                    + "        \"name\": \"containerApp\",\n" + "        \"version\": \"v1.2\",\n"
                    + "        \"type\": \"Video Application\",\n" + "\t\t\"details\":\"xx\",\n"
                    + "        \"affinity\": \"X86\",\n" + "        \"industry\": \"Smart Park\",\n"
                    + "        \"contact\": null,\n" + "        \"appId\": \"b6d3f961be2f41a38c90b08ea2ce1c00\",\n"
                    + "        \"userId\": \"39937079-99fe-4cd8-881f-04ca8c4fe09d\",\n"
                    + "        \"userName\": \"admin\",\n" + "        \"status\": \"Published\",\n"
                    + "        \"shortDesc\": \"asdasd\",\n" + "        \"showType\": \"public\",\n"
                    + "        \"testTaskId\": \"ee127f57-ac70-4d05-bd1d-19700014b40b\",\n"
                    + "        \"provider\": \"eg\",\n" + "        \"demoVideoName\": null,\n"
                    + "        \"deployMode\": \"container\",\n" + "        \"experienceAble\": false\n" + "    },\n"
                    + "    \"retCode\": 0,\n" + "    \"params\": null,\n"
                    + "    \"message\": \"query package by packageId success.\"\n" + "}";
                return ResponseEntity.ok(res);
            }

            @Mock
            public ResponseEntity<byte[]> downloadPkg(String appId, String pkgId, String token) {
                byte[] bytes = "Any String you want".getBytes();
                return ResponseEntity.ok(bytes);
            }
        };
        List<ReleasedPkgReqDto> pkgReqDtos = new ArrayList<>();
        ReleasedPkgReqDto releasedPkgReqDto = new ReleasedPkgReqDto();
        releasedPkgReqDto.setAppId("appId");
        releasedPkgReqDto.setPackageId("pkgId");
        pkgReqDtos.add(releasedPkgReqDto);
        boolean ret = releasedPackageService.synchronizePackage(user, pkgReqDtos);
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testGetAllPackagesSuccess() throws IOException {
        Assert.assertNotNull(releasedPackageService.getAllPackages("", 1, 0));
    }

    @Test
    public void testGetAppPkgStructureFail() throws IOException {
        try {
            releasedPackageService.getAppPkgStructure("");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is empty!", e.getMessage());
        }
    }

    @Test
    public void testGetAppPkgStructureFail2() throws IOException {
        try {
            releasedPackageService.getAppPkgStructure("a");
        } catch (DataBaseException e) {
            Assert.assertEquals("packageId is error", e.getMessage());
        }
    }

    @Test
    public void testGetAppPkgStructureFail3() throws IOException {
        try {
            releasedPackageService.getAppPkgStructure("f2759fcb-bb4b-42f5-bc6c-8e1635348fda");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("app pkg not found!", e.getMessage());
        }
    }

    @Test
    public void testGetAppPkgFileContentFail() throws IOException {
        try {
            releasedPackageService.getAppPkgFileContent(new ReleasedPkgFileContentReqDto(), "");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is null", e.getMessage());
        }
    }

    @Test
    public void testGetAppPkgFileContentFail1() throws IOException {
        try {
            releasedPackageService.getAppPkgFileContent(null, "aa");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("structureReqDto is null", e.getMessage());
        }
    }

    @Test
    public void testGetAppPkgFileContentFail2() throws IOException {
        try {
            releasedPackageService.getAppPkgFileContent(new ReleasedPkgFileContentReqDto(), "aa");
        } catch (DataBaseException e) {
            Assert.assertEquals("packageId is error", e.getMessage());
        }
    }

    @Test
    public void testGetAppPkgFileContentFail3() throws IOException {
        try {
            ReleasedPkgFileContentReqDto reqDto = new ReleasedPkgFileContentReqDto();
            reqDto.setFilePath("/test");
            releasedPackageService.getAppPkgFileContent(reqDto, "f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("app pkg not decompress", e.getMessage());
        }
    }

    @Test
    public void testEditAppPkgFileContentFail() throws IOException {
        try {
            releasedPackageService.editAppPkgFileContent(new ReleasedPkgFileContent(), "");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is null", e.getMessage());
        }
    }

    @Test
    public void testEditAppPkgFileContentFail1() throws IOException {
        try {
            releasedPackageService.editAppPkgFileContent(new ReleasedPkgFileContent(), "aa");
        } catch (DataBaseException e) {
            Assert.assertEquals("packageId is error", e.getMessage());
        }
    }

    @Test
    public void testEditAppPkgFileContentFail2() throws IOException {
        try {
            releasedPackageService.editAppPkgFileContent(null, "f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("releasedPkgFileContent is null", e.getMessage());
        }
    }

    @Test
    public void testEditAppPkgFileContentFail3() throws IOException {
        try {
            ReleasedPkgFileContent reqDto = new ReleasedPkgFileContent();
            reqDto.setFilePath("/test");
            reqDto.setContent("ss");
            releasedPackageService.editAppPkgFileContent(reqDto, "f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("app pkg decompress dir was not found", e.getMessage());
        }
    }

    @Test
    public void testDeleteAppPkgFail() throws IOException {
        try {
            releasedPackageService.deleteAppPkg(null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is null", e.getMessage());
        }
    }

    @Test
    public void testDeleteAppPkgSuccess1() throws IOException {
        boolean ret = releasedPackageService.deleteAppPkg("aaaaa");
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testDeleteAppPkgSuccess() throws IOException {
        boolean ret = releasedPackageService.deleteAppPkg("f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testReleaseAppPkgFail() throws IOException {
        try {
            User user = new User("userId", "userName", "userAuth");
            releasedPackageService.releaseAppPkg(user, new PublishAppReqDto(), "");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("packageId is null", e.getMessage());
        }
    }

    @Test
    public void testReleaseAppPkgFail1() throws IOException {
        try {
            User user = new User("userId", "userName", "userAuth");
            releasedPackageService.releaseAppPkg(user, new PublishAppReqDto(), "1");
        } catch (DataBaseException e) {
            Assert.assertEquals("can not found app or released Package", e.getMessage());
        }
    }

    @Test
    public void testReleaseAppPkgFail2() throws IOException {
        try {
            User user = new User("userId", "userName", "userAuth");
            releasedPackageService.releaseAppPkg(user, new PublishAppReqDto(), "f2759fcb-bb4b-42f5-bc6c-8e1635348fdc");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("can not found app package(.csar)", e.getMessage());
        }
    }
}
