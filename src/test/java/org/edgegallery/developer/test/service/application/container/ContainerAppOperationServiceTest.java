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

package org.edgegallery.developer.test.service.application.container;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.instantiate.container.ContainerAppInstantiateInfo;
import org.edgegallery.developer.model.lcm.LcmLog;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.test.service.application.vm.VMAppOperationServiceTest;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ContainerAppOperationServiceTest extends AbstractJUnit4SpringContextTests {

    private static String APPLICATION_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d365";

    private final String PRESET_APPLICATION_ID = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppOperationServiceTest.class);

    private static final int MAX_TRY_NUMBER = 50;

    @Autowired
    private ContainerAppOperationServiceImpl containerAppOperationService;

    @Autowired
    private ContainerAppHelmChartService containerAppHelmChartService;

    @Autowired
    private OperationStatusService operationStatusService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ApplicationService applicationService;

    private User user;

    private MockUp mockup;

    private enum LcmReturnMockTypeEnum {
        UPLOAD_PKG_FAILED,
        DISTRIBUTE_PKG_FAILED,
        GET_DISTRIBUTE_RES_FAILED,
        INSTANTIATE_APP_FAILED,
        GET_WORKLOAD_STATUS_FAILED,
        SUCCESS
    }

    @Before
    public void prepare() throws IOException {
        user = new User("testId", "testUser", "testAuth", "testToken");
        SpringContextUtil.setApplicationContext(applicationContext);
        prepareFilesForTestApplication();
        mockup = new MockUp<ContainerAppHelmChartUtil>() {
            @Mock
            public boolean checkImageExist(List<String> imageList) {
                return true;
            }
        };
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        containerAppHelmChartService
            .uploadHelmChartFile(APPLICATION_ID, uploadFile);
    }

    @After
    public void shutdown(){
        mockup.tearDown();
    }

    @Test
    public void generatePackageTest() throws IOException {
        MockUp mockup = new MockUp<ContainerAppHelmChartUtil>() {
            @Mock
            public boolean checkImageExist(List<String> imageList) {
                return true;
            }
        };
        File file = Resources.getResourceAsFile("testdata/demo.yaml");
        MultipartFile multipartFile = new MockMultipartFile("file", "demo.yaml", MediaType.TEXT_PLAIN_VALUE,
            FileUtils.openInputStream(file));
        containerAppHelmChartService.uploadHelmChartFile(APPLICATION_ID, multipartFile);
        containerAppOperationService.generatePackage(APPLICATION_ID);
        mockup.tearDown();
    }

    private void prepareFilesForTestApplication() {
        try {
            MultipartFile iconFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
                VMAppOperationServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
            UploadFile iconFileInfo = uploadFileService
                .uploadFile("db22dc00-8f44-408c-a106-402e60c643de", "icon", iconFile);
            MultipartFile mdFile = new MockMultipartFile("template-zoneminder.md", "template-zoneminder.md", null,
                VMAppOperationServiceTest.class.getClassLoader()
                    .getResourceAsStream("testdata/template-zoneminder.md"));
            UploadFile mdFileInfo = uploadFileService.uploadFile("db22dc00-8f44-408c-a106-402e60c643df", "md", mdFile);
            Application application = applicationService.getApplication(APPLICATION_ID);
            application.setIconFileId(iconFileInfo.getFileId());
            application.setGuideFileId(mdFileInfo.getFileId());
            applicationService.modifyApplication(APPLICATION_ID, application);
        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testInstantiateContainerSuccess() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.SUCCESS);
        try {
            OperationStatus status = callInstantiateContainer();
            Assert.assertEquals(EnumActionStatus.SUCCESS, status.getStatus());
            Assert.assertEquals(100, status.getProgress());
            //InstantiateInfo created check.
            ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService
                .getInstantiateInfo(APPLICATION_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppPackageId()));
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getPods()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateContainerUploadPkgFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.UPLOAD_PKG_FAILED);
        try {
            OperationStatus status = callInstantiateContainer();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService
                .getInstantiateInfo(APPLICATION_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppPackageId()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getMepmPackageId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateContainerDistributePkgFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.DISTRIBUTE_PKG_FAILED);
        try {
            OperationStatus status = callInstantiateContainer();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService
                .getInstantiateInfo(APPLICATION_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppPackageId()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateContainerGetDistributeResFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.DISTRIBUTE_PKG_FAILED);
        try {
            OperationStatus status = callInstantiateContainer();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService
                .getInstantiateInfo(APPLICATION_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppPackageId()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateContainerInstantiateAppFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.INSTANTIATE_APP_FAILED);
        try {
            OperationStatus status = callInstantiateContainer();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            ContainerAppInstantiateInfo instantiateInfo = containerAppOperationService
                .getInstantiateInfo(APPLICATION_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppPackageId()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    private OperationStatus callInstantiateContainer() {
        //Clean instantiate data in db.
        containerAppOperationService.deleteInstantiateInfo(APPLICATION_ID);
        //Sent instantiate request.
        OperationInfoRep operationInfo = containerAppOperationService.instantiateContainerApp(APPLICATION_ID, user);
        OperationStatus status = null;
        for (int i = 0; i < MAX_TRY_NUMBER; i++) {
            status = operationStatusService.getOperationStatusById(operationInfo.getOperationId());
            if (EnumActionStatus.SUCCESS.equals(status.getStatus()) || EnumActionStatus.FAILED.equals(
                status.getStatus())) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("thread sleep error.");
            }
        }
        return status;
    }

    private MockUp mockLcmReturnInfo(final ContainerAppOperationServiceTest.LcmReturnMockTypeEnum lcmReturnType) {
        return new MockUp<HttpClientUtil>() {

            @Mock
            public String uploadPkg(String basePath, String filePath, String userId, String token, LcmLog lcmLog) {
                if (lcmReturnType.equals(ContainerAppOperationServiceTest.LcmReturnMockTypeEnum.UPLOAD_PKG_FAILED)) {
                    return null;
                }
                return "{\"appId\": \"" + PRESET_APPLICATION_ID + "\", \"packageId\": \"test_mepm_package_id\" }";
            }

            @Mock
            public String distributePkg(String basePath, String userId, String token, String packageId, String mecHost,
                LcmLog lcmLog) {
                if (lcmReturnType
                    .equals(ContainerAppOperationServiceTest.LcmReturnMockTypeEnum.DISTRIBUTE_PKG_FAILED)) {
                    return null;
                }
                return "success";
            }

            @Mock
            public String getDistributeRes(String basePath, String userId, String token, String pkgId) {
                if (lcmReturnType
                    .equals(ContainerAppOperationServiceTest.LcmReturnMockTypeEnum.GET_DISTRIBUTE_RES_FAILED)) {
                    return null;
                }
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/container_package_distribute_status.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getDistributeRes failed.");
                }
                return jsonStr;
            }

            @Mock
            public boolean instantiateApplication(String basePath, String appInstanceId, String userId, String token,
                LcmLog lcmLog, String pkgId, String mecHost, Map<String, String> inputParams) {
                if (lcmReturnType
                    .equals(ContainerAppOperationServiceTest.LcmReturnMockTypeEnum.INSTANTIATE_APP_FAILED)) {
                    return false;
                }
                return true;
            }

            @Mock
            public String getWorkloadStatus(String basePath, String appInstanceId, String userId, String token) {
                if (lcmReturnType
                    .equals(ContainerAppOperationServiceTest.LcmReturnMockTypeEnum.GET_WORKLOAD_STATUS_FAILED)) {
                    return null;
                }
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/container_workload_status.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getWorkloadStatus failed.");
                }
                return jsonStr;
            }

            @Mock
            public String getWorkloadEvents(String basePath, String appInstanceId, String userId,
                String token) {
                if (lcmReturnType
                    .equals(ContainerAppOperationServiceTest.LcmReturnMockTypeEnum.GET_WORKLOAD_STATUS_FAILED)) {
                    return null;
                }
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/container_event_status.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getWorkloadStatus failed.");
                }
                return jsonStr;
            }
        };
    }

}
