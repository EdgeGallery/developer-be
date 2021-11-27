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

package org.edgegallery.developer.test.service.uploadfile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.FileFoundFailException;
import org.edgegallery.developer.exception.FileOperateException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.mapper.capability.CapabilityMapper;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.capability.Capability;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class UploadServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceTest.class);

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private CapabilityMapper capabilityMapper;

    @Test
    public void testUploadFileBadWithErrFileType() throws Exception {
        try {
            MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
                UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
            uploadFileService.uploadFile(null, "text", uploadFile);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("fileType is error,must be one of [icon,md,api]", e.getMessage());
        }
    }

    @Test
    public void testUploadFileBadWithErrSuffix() throws Exception {
        try {
            MultipartFile uploadFile = new MockMultipartFile("config", "config", null,
                UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/config"));
            uploadFileService.uploadFile(null, "icon", uploadFile);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("file suffix is error.", e.getMessage());
        }
    }

    @Test
    public void testUploadFileBadWithErrFileLength() throws Exception {
        try {
            MultipartFile uploadFile = new MockMultipartFile("scenery.jpg", "scenery.jpg", null,
                UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/scenery.jpg"));
            System.out.println(uploadFile.getSize());
            uploadFileService.uploadFile(null, "icon", uploadFile);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("icon file size can not be greater than 2m", e.getMessage());
        }
    }

    @Test
    public void testUploadFileSuccess() throws Exception {
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168754", "icon", uploadFile);
        Assert.assertNotNull(file);
    }

    @Test
    public void testGetFileStreamBadWithNullFile() throws Exception {
        try {
            UploadFile uploadFile = new UploadFile();
            uploadFileService.getFileStream(uploadFile, "f1127092-ecc7-4e94-9247-b5f74d168754");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("can not find file in repository!", e.getMessage());
        }
    }

    @Test
    public void testGetFileStreamSuccess() throws Exception {
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168754", "icon", uploadFile);
        Assert.assertNotNull(file);
        byte[] bytes = uploadFileService.getFileStream(file, "f1127092-ecc7-4e94-9247-b5f74d168754");
        Assert.assertNotNull(bytes);
    }

    @Test
    public void testGetFileStreamSuccessWithNullUserId() throws Exception {
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168754", "icon", uploadFile);
        Assert.assertNotNull(file);
        byte[] bytes = uploadFileService.getFileStream(file, "");
        Assert.assertNotNull(bytes);
    }

    @Test
    public void testGetFileBadWithErrId() throws Exception {
        try {
            uploadFileService.getFile("file");
        } catch (FileFoundFailException e) {
            Assert.assertEquals("file does not exist!", e.getMessage());
        }
    }

    @Test
    public void testGetFileSuccess() throws Exception {
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168754", "icon", uploadFile);
        Assert.assertNotNull(file);
        UploadFile bytes = uploadFileService.getFile(file.getFileId());
        Assert.assertNotNull(bytes);
    }

    @Test
    public void testDeleteFileBadWithEmptyId() throws Exception {
        try {
            uploadFileService.deleteFile("");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("fileId does not exist.", e.getMessage());
        }
    }

    @Test
    public void testDeleteFileBadWithErrId() throws Exception {
        boolean res = uploadFileService.deleteFile("text");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testDeleteFileBadWithExistId() throws Exception {
        boolean res = uploadFileService.deleteFile("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        Assert.assertEquals(true, res);
    }

    @Test
    public void testDeleteFileSuccess() throws Exception {
        MultipartFile uploadFile = new MockMultipartFile("face.png", "face.png", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/face.png"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "icon", uploadFile);
        Assert.assertNotNull(file);
        boolean res = uploadFileService.deleteFile(file.getFileId());
        Assert.assertEquals(true, res);
    }

    @Test
    public void testDownloadSampleCodeBadWithErrApiFile() throws Exception {
        try {
            List<String> apis = new ArrayList<>();
            MultipartFile uploadFile = new MockMultipartFile("health.yaml", "health.yaml", null,
                UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/health.yaml"));
            UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "api", uploadFile);
            Assert.assertNotNull(file);
            apis.add(file.getFileId());
            uploadFileService.downloadSampleCode(apis);
        } catch (DeveloperException e) {
            Assert.assertEquals("Yaml deserialization failed.", e.getMessage());
        }
    }

    @Test
    public void testDownloadSampleCodeBadWithErrFileId() throws Exception {
        try {
            List<String> apis = new ArrayList<>();
            apis.add("file.getFileId()");
            uploadFileService.downloadSampleCode(apis);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("The input is not in UUID format.", e.getMessage());
        }
    }

    @Test
    public void testDownloadSampleCodeBadWithNotExistId() throws Exception {
        try {
            List<String> apis = new ArrayList<>();
            apis.add(UUID.randomUUID().toString());
            uploadFileService.downloadSampleCode(apis);
        } catch (FileFoundFailException e) {
            Assert.assertEquals("can not find api file.", e.getMessage());
        }
    }

    @Test
    public void testDownloadSampleCodeSuccess() throws Exception {
        List<String> apis = new ArrayList<>();
        MultipartFile uploadFile = new MockMultipartFile("test.json", "test.json", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test.json"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "api", uploadFile);
        Assert.assertNotNull(file);
        apis.add(file.getFileId());
        byte[] bytes = uploadFileService.downloadSampleCode(apis);
        Assert.assertNotNull(bytes.length);
    }

    @Test
    public void testGetSampleCodeStruSuccess() throws Exception {
        List<String> apis = new ArrayList<>();
        MultipartFile uploadFile = new MockMultipartFile("test.json", "test.json", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test.json"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "api", uploadFile);
        Assert.assertNotNull(file);
        apis.add(file.getFileId());
        AppPkgStructure appPkgStructure = uploadFileService.getSampleCodeStru(apis);
        Assert.assertNotNull(appPkgStructure);
    }

    @Test
    public void testGetSampleCodeContentBadWithErrFileName() throws Exception {
        try {
            List<String> apis = new ArrayList<>();
            MultipartFile uploadFile = new MockMultipartFile("test.json", "test.json", null,
                UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test.json"));
            UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "api", uploadFile);
            Assert.assertNotNull(file);
            apis.add(file.getFileId());
            AppPkgStructure appPkgStructure = uploadFileService.getSampleCodeStru(apis);
            Assert.assertNotNull(appPkgStructure);
            uploadFileService.getSampleCodeContent("test");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("file has not any content!", e.getMessage());
        }
    }

    @Test
    public void testGetSampleCodeContentSuccess() throws Exception {
        List<String> apis = new ArrayList<>();
        MultipartFile uploadFile = new MockMultipartFile("test.json", "test.json", null,
            UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test.json"));
        UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "api", uploadFile);
        Assert.assertNotNull(file);
        apis.add(file.getFileId());
        AppPkgStructure appPkgStructure = uploadFileService.getSampleCodeStru(apis);
        Assert.assertNotNull(appPkgStructure);
        String content = uploadFileService.getSampleCodeContent("ApiSampleCode001.java");
        Assert.assertNotNull(content);
    }

    @Test
    public void testGetSdkProjectBadWithErrId() throws Exception {
        try {
            uploadFileService.getSdkProject("fileId", "lan", null);
        } catch (FileFoundFailException e) {
            Assert.assertEquals("can not find file in db", e.getMessage());
        }
    }

    @Test
    public void testGetSdkProjectBad() throws Exception {
        try {
            MultipartFile uploadFile = new MockMultipartFile("test.json", "test.json", null,
                UploadServiceTest.class.getClassLoader().getResourceAsStream("testdata/test.json"));
            UploadFile file = uploadFileService.uploadFile("f1127092-ecc7-4e94-9247-b5f74d168755", "api", uploadFile);
            Assert.assertNotNull(file);
            Capability capability = capabilityMapper.selectById("e111f3e7-90d8-4a39-9874-ea6ea6752efgd");
            Assert.assertNotNull(capability);
            List<Capability> capabilities = new ArrayList<>();
            capabilities.add(capability);
            uploadFileService.getSdkProject(file.getFileId(), "java", capabilities);
        }catch (FileOperateException e){
            Assert.assertEquals("Failed to compress project", e.getMessage());
        }

    }

}
