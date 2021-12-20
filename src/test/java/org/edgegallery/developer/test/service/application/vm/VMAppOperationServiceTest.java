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

package org.edgegallery.developer.test.service.application.vm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.mapper.application.vm.VMInstantiateInfoMapper;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.filesystem.FileSystemResponse;
import org.edgegallery.developer.model.instantiate.EnumAppInstantiateStatus;
import org.edgegallery.developer.model.instantiate.vm.ImageExportInfo;
import org.edgegallery.developer.model.instantiate.vm.VMInstantiateInfo;
import org.edgegallery.developer.model.lcm.LcmLog;
import org.edgegallery.developer.model.operation.EnumActionStatus;
import org.edgegallery.developer.model.operation.OperationStatus;
import org.edgegallery.developer.model.restful.OperationInfoRep;
import org.edgegallery.developer.model.reverseproxy.ScpConnectEntity;
import org.edgegallery.developer.model.uploadfile.FileUploadEntity;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.OperationStatusService;
import org.edgegallery.developer.service.application.impl.vm.VMAppOperationServiceImpl;
import org.edgegallery.developer.service.application.vm.VMAppVmService;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.HttpClientUtil;
import org.edgegallery.developer.util.ShhFileUploadUtil;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class VMAppOperationServiceTest extends AbstractJUnit4SpringContextTests {

    private final String PRESET_APPLICATION_ID = "4cbbab9d-c48f-4adb-ae82-d1816d8edd7c";

    private final String PRESET_VM_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d757";

    private final String PRESET_VM1_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d758";

    private static final Logger LOGGER = LoggerFactory.getLogger(VMAppOperationServiceTest.class);

    private static final int MAX_TRY_NUMBER = 50;

    private static final int CHUNK_SIZE = 102400;

    private User user;

    @Autowired
    private VMAppVmService vmAppVmService;

    @Autowired
    private VMAppOperationServiceImpl vmAppOperationService;

    @Autowired
    private OperationStatusService operationStatusService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    VMInstantiateInfoMapper vmInstantiateInfoMapper;

    private enum LcmReturnMockTypeEnum {
        UPLOAD_PKG_FAILED,
        DISTRIBUTE_PKG_FAILED,
        GET_DISTRIBUTE_RES_FAILED,
        INSTANTIATE_APP_FAILED,
        GET_WORKLOAD_STATUS_FAILED,
        SUCCESS
    }

    @Before
    public void prepare() {
        user = new User("testId", "testUser", "testAuth", "testToken");
        SpringContextUtil.setApplicationContext(applicationContext);
        prepareFilesForTestApplication();
    }

    private void prepareFilesForTestApplication() {
        try {
            MultipartFile iconFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
                VMAppOperationServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
            UploadFile iconFileInfo = uploadFileService.uploadFile("db22dc00-8f44-408c-a106-402e60c643de", "icon",
                iconFile);
            LOGGER.info("Icon file id {}", iconFileInfo.getFileId());
            MultipartFile mdFile = new MockMultipartFile("template-zoneminder.md", "template-zoneminder.md", null,
                VMAppOperationServiceTest.class.getClassLoader()
                    .getResourceAsStream("testdata/template-zoneminder.md"));
            UploadFile mdFileInfo = uploadFileService.uploadFile("db22dc00-8f44-408c-a106-402e60c643df", "md", mdFile);
            LOGGER.info("Md file id {}", mdFileInfo.getFileId());
            Application application = applicationService.getApplication(PRESET_APPLICATION_ID);
            application.setIconFileId(iconFileInfo.getFileId());
            application.setGuideFileId(mdFileInfo.getFileId());
            applicationService.modifyApplication(PRESET_APPLICATION_ID, application);
        } catch (IOException e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
    }

    @Test
    public void testInstantiateVMSuccess() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.SUCCESS);
        try {
            OperationStatus status = callInstantiateVM();
            Assert.assertEquals(EnumActionStatus.SUCCESS, status.getStatus());
            Assert.assertEquals(100, status.getProgress());
            //InstantiateInfo created check.
            VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getVmInstanceId()));
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getVncUrl()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateVMUploadPkgFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.UPLOAD_PKG_FAILED);
        try {
            OperationStatus status = callInstantiateVM();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getAppPackageId()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getMepmPackageId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateVMDistributePkgFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.DISTRIBUTE_PKG_FAILED);
        try {
            OperationStatus status = callInstantiateVM();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getMepmPackageId()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateVMGetDistributeResFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.GET_DISTRIBUTE_RES_FAILED);
        try {
            OperationStatus status = callInstantiateVM();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getMepmPackageId()));
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getDistributedMecHost()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateVMInstantiateAppFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.INSTANTIATE_APP_FAILED);
        try {
            OperationStatus status = callInstantiateVM();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
            Assert.assertFalse(StringUtils.isEmpty(instantiateInfo.getDistributedMecHost()));
            Assert.assertTrue(StringUtils.isEmpty(instantiateInfo.getAppInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testInstantiateVMGetWorkloadFailed() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.GET_WORKLOAD_STATUS_FAILED);
        try {
            OperationStatus status = callInstantiateVM();
            Assert.assertEquals(EnumActionStatus.FAILED, status.getStatus());
            //InstantiateInfo created check.
            VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
            Assert.assertNotNull(instantiateInfo.getAppInstanceId());
            Assert.assertNull(instantiateInfo.getVmInstanceId());
            Assert.assertNull(instantiateInfo.getVncUrl());
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    @Test
    public void testUploadFileToVmSuccess() {
        new MockUp<ShhFileUploadUtil>(ShhFileUploadUtil.class) {
            @Mock
            public FileUploadEntity uploadFile(File file, String remoteFileName, ScpConnectEntity scpConnectEntity) {
                return new FileUploadEntity("ok", "Success to upload file.", null);
            }
        };
        try {
            File file = Resources.getResourceAsFile("testdata/IDEAPluginDev.zip");
            InputStream stream = new FileInputStream(file);
            int length = stream.available();

            byte[] chunkData = new byte[CHUNK_SIZE];
            int chunkNumber = 1;
            int totalSize = length / CHUNK_SIZE;
            int currentSize = 0;
            while ((currentSize = stream.read(chunkData, 0, CHUNK_SIZE)) != -1) {
                MultipartFile chunkMultipart = new MockMultipartFile("IDEAPluginDev.zip", "IDEAPluginDev.zip", null,
                    new ByteArrayInputStream(chunkData, 0, currentSize));
                MockHttpServletRequest mockRequest = new MockHttpServletRequest("POST", "uri");
                mockRequest.setContentType("multipart/form-data");
                Chunk chunk = new Chunk();
                chunk.setChunkNumber(chunkNumber);
                chunk.setChunkSize((long) CHUNK_SIZE);
                chunk.setFile(chunkMultipart);
                chunk.setCurrentChunkSize((long) currentSize);
                chunk.setFilename("IDEAPluginDev.zip");
                chunk.setId((long) chunkNumber);
                chunk.setTotalChunks(totalSize);
                chunk.setIdentifier("IDEAPluginDev");
                Boolean res = vmAppOperationService.uploadFileToVm(PRESET_APPLICATION_ID, PRESET_VM1_ID, mockRequest,
                    chunk);
                Assert.assertTrue(res);
                chunkNumber++;
            }

            Boolean mergeResult = vmAppOperationService.mergeAppFile(PRESET_APPLICATION_ID, PRESET_VM1_ID,
                "IDEAPluginDev.zip", "IDEAPluginDev");
            Assert.assertTrue(mergeResult);
        } catch (IOException e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
    }

    @Test
    public void testCreateVmImageSuccess() {
        MockUp mockup = mockLcmReturnInfo(LcmReturnMockTypeEnum.SUCCESS);
        try {
            OperationStatus status = callCreateVMImage();
            Assert.assertEquals(EnumActionStatus.SUCCESS, status.getStatus());
            Assert.assertEquals(100, status.getProgress());
            //imageExport info check.
            ImageExportInfo imageExportInfo = vmAppOperationService.getImageExportInfo(PRESET_VM_ID);
            Assert.assertFalse(StringUtils.isEmpty(imageExportInfo.getImageInstanceId()));
        } catch (Exception e) {
            LOGGER.error("Exception happens", e);
            Assert.fail();
        }
        mockup.tearDown();
    }

    private OperationStatus callCreateVMImage() {
        //mock as instantiated.
        VMInstantiateInfo instantiateInfo = vmAppOperationService.getInstantiateInfo(PRESET_VM_ID);
        if (null == instantiateInfo) {
            instantiateInfo = new VMInstantiateInfo();
            instantiateInfo.setOperationId(UUID.randomUUID().toString());
            instantiateInfo.setStatus(EnumAppInstantiateStatus.SUCCESS);
            vmAppOperationService.createInstantiateInfo(PRESET_VM_ID, instantiateInfo);
        } else if (EnumAppInstantiateStatus.SUCCESS.equals(instantiateInfo.getStatus())) {
            instantiateInfo.setStatus(EnumAppInstantiateStatus.SUCCESS);
            vmAppOperationService.updateInstantiateInfo(PRESET_VM_ID, instantiateInfo);
        }
        //Sent instantiate request.
        OperationInfoRep operationInfo = vmAppOperationService.createVmImage(PRESET_APPLICATION_ID, PRESET_VM_ID, user);
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

    private OperationStatus callInstantiateVM() {
        //Clean instantiate data in db.
        vmInstantiateInfoMapper.deleteVMInstantiateInfo(PRESET_VM_ID);
        //Sent instantiate request.
        OperationInfoRep operationInfo = vmAppOperationService.instantiateVM(PRESET_APPLICATION_ID, PRESET_VM_ID, user);
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

    private MockUp mockLcmReturnInfo(final LcmReturnMockTypeEnum lcmReturnType) {
        return new MockUp<HttpClientUtil>() {

            @Mock
            public FileSystemResponse queryImageCheck(String url) {
                FileSystemResponse response = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/vm_export_image_filesystem_rsp.json");
                    response = new ObjectMapper().readValue(file, FileSystemResponse.class);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getDistributeRes failed.");
                }

                return response;
            }

            @Mock
            public String getImageStatus(String basePath, String hostIp, String userId, String imageId,
                String lcmToken) {
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/vm_export_image_status_rsp.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getDistributeRes failed.");
                }
                return jsonStr;
            }

            @Mock
            public String vmInstantiateImage(String basePath, String userId, String lcmToken, String vmId,
                String hostIp, String imageName, LcmLog lcmLog) {
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/vm_export_image_rsp.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getDistributeRes failed.");
                }
                return jsonStr;
            }

            @Mock
            public String uploadPkg(String basePath, String filePath, String userId, String token, LcmLog lcmLog) {
                if (lcmReturnType.equals(LcmReturnMockTypeEnum.UPLOAD_PKG_FAILED)) {
                    return null;
                }
                return "{\"appId\": \"" + PRESET_APPLICATION_ID + "\", \"packageId\": \"test_mepm_package_id\" }";
            }

            @Mock
            public String distributePkg(String basePath, String userId, String token, String packageId, String mecHost,
                LcmLog lcmLog) {
                if (lcmReturnType.equals(LcmReturnMockTypeEnum.DISTRIBUTE_PKG_FAILED)) {
                    return null;
                }
                return "success";
            }

            @Mock
            public String getDistributeRes(String basePath, String userId, String token, String pkgId) {
                if (lcmReturnType.equals(LcmReturnMockTypeEnum.GET_DISTRIBUTE_RES_FAILED)) {
                    return null;
                }
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/vm_package_distribute_status.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getDistributeRes failed.");
                }
                return jsonStr;
            }

            @Mock
            public boolean instantiateApplication(String basePath, String appInstanceId, String userId, String token,
                LcmLog lcmLog, String pkgId, String mecHost, Map<String, String> inputParams) {
                if (lcmReturnType.equals(LcmReturnMockTypeEnum.INSTANTIATE_APP_FAILED)) {
                    return false;
                }
                return true;
            }

            @Mock
            public String getWorkloadStatus(String basePath, String appInstanceId, String userId,
                String token) {
                if (lcmReturnType.equals(LcmReturnMockTypeEnum.GET_WORKLOAD_STATUS_FAILED)) {
                    return null;
                }
                String jsonStr = null;
                try {
                    File file = Resources.getResourceAsFile("testdata/json/vm_workload_status.json");
                    jsonStr = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

                } catch (IOException e) {
                    LOGGER.error("Load the mock json data for getWorkloadStatus failed.");
                }
                return jsonStr;
            }
        };
    }
}
