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

package org.edgegallery.developer.test.service.resource.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.exception.RestfulRequestException;
import org.edgegallery.developer.exception.UnauthorizedException;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.resource.vm.VMImage;
import org.edgegallery.developer.model.restful.VMImageQuery;
import org.edgegallery.developer.model.restful.VMImageReq;
import org.edgegallery.developer.model.restful.VMImageRes;
import org.edgegallery.developer.service.recource.vm.VMImageService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class VMImageServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VMImageServiceTest.class);

    @Autowired
    private VMImageService vmImageService;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
    }

    @Test
    public void testGetVmImagesSuccessWithUploadTime() {
        AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
        VMImageReq vmImageReq = new VMImageReq();
        VMImageQuery query = new VMImageQuery();
        query.setSortBy("uploadTime");
        vmImageReq.setQueryCtrl(query);
        vmImageReq.setUploadTimeBegin("2020-10-10");
        vmImageReq.setUploadTimeEnd("2021-11-11");
        vmImageReq.setName("test");
        vmImageReq.setUserId(AccessUserUtil.getUserId());
        VMImageRes res = vmImageService.getVmImages(vmImageReq);
        Assert.assertNotNull(res);
    }

    @Test
    public void testGetVmImagesSuccessWithNullUploadTime() {
        AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
        VMImageReq vmImageReq = new VMImageReq();
        VMImageQuery query = new VMImageQuery();
        vmImageReq.setQueryCtrl(query);
        vmImageReq.setUploadTimeBegin("2020-10-10");
        vmImageReq.setUploadTimeEnd("2021-11-11");
        vmImageReq.setName("test");
        vmImageReq.setUserId(AccessUserUtil.getUserId());
        VMImageRes res = vmImageService.getVmImages(vmImageReq);
        Assert.assertNotNull(res);
    }

    @Test
    public void testGetVmImagesSuccessWithUserName() {
        AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
        VMImageReq vmImageReq = new VMImageReq();
        VMImageQuery query = new VMImageQuery();
        query.setSortBy("userName");
        vmImageReq.setQueryCtrl(query);
        vmImageReq.setUploadTimeBegin("2020-10-10");
        vmImageReq.setUploadTimeEnd("2021-11-11");
        vmImageReq.setName("test");
        vmImageReq.setUserId(AccessUserUtil.getUserId());
        vmImageReq.setVisibleType("public,private");
        vmImageReq.setOsType("centos,windows");
        vmImageReq.setStatus("UPLOAD_WAIT,UPLOADING,UPLOAD_SUCCEED");
        VMImageRes res = vmImageService.getVmImages(vmImageReq);
        Assert.assertNotNull(res);
    }

    @Test
    public void testGetVmImageByIdSuccess() {
        VMImage res = vmImageService.getVmImageById(1);
        Assert.assertNotNull(res);
    }

    @Test
    public void testCreateVmImageBadWithEmptyName() {
        try {
            AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", Consts.ROLE_DEVELOPER_ADMIN);
            VMImage vmImage = new VMImage();
            vmImageService.createVmImage(vmImage);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("VmImage name is empty.", e.getMessage());
        }
    }

    @Test
    public void testCreateVmImageBadWithRepeatName() {
        try {
            AccessUserUtil.setUser("39937079-99fe-4cd8-881f-04ca8c4fe09d", "admin", Consts.ROLE_DEVELOPER_ADMIN);
            VMImage vmImage = new VMImage();
            vmImage.setName("Ubuntu18.04");
            vmImageService.createVmImage(vmImage);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("image Name can not duplicate.", e.getMessage());
        }
    }

    @Test
    public void testCreateVmImageSuccess() {
        AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", Consts.ROLE_DEVELOPER_ADMIN);
        VMImage vmImage = new VMImage();
        vmImage.setName("Ubuntu16.04");
        boolean res = vmImageService.createVmImage(vmImage);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testUpdateVmImageBadWithEmptyName() {
        try {
            AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
            VMImage vmImage = new VMImage();
            vmImageService.updateVmImage(vmImage, 1);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("vmImage name or queried userId is empty.", e.getMessage());
        }
    }

    @Test
    public void testUpdateVmImageBadWithRepeatName() {
        try {
            AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
            VMImage vmImage = new VMImage();
            vmImage.setName("Ubuntu16.04");
            vmImageService.updateVmImage(vmImage, 1);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("name can not duplicate.", e.getMessage());
        }
    }

    @Test
    public void testUpdateVmImageSuccess() {
        AccessUserUtil.setUser("39937079-99fe-4cd8-881f-04ca8c4fe09d", "admin", Consts.ROLE_DEVELOPER_ADMIN);
        VMImage vmImage = vmImageService.getVmImageById(1);
        Assert.assertNotNull(vmImage);
        boolean res = vmImageService.updateVmImage(vmImage, 1);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testDeleteVmImageBadWithErrDownloadUrl() {
        try {
            AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
            vmImageService.deleteVmImage(3);
        } catch (RestfulRequestException e) {
            Assert.assertEquals("delete vm image on remote server failed.", e.getMessage());
        }
    }

    @Test
    public void testDeleteVmImageSuccessWithNullDownloadUrl() {
        AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
        boolean res = vmImageService.deleteVmImage(4);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testPublishVmImageSuccess() {
        boolean res = vmImageService.publishVmImage(2);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testResetImageStatusBadWithErrId() {
        try {
            vmImageService.resetImageStatus(6);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("vm image not found", e.getMessage());
        }
    }

    @Test
    public void testResetImageStatusBadWithErrAuth() {
        try {
            AccessUserUtil.setUser("d1bb89fe-1a9d-42e9-911e-7b038c3480b9", "developer", "common");
            vmImageService.resetImageStatus(2);
        } catch (UnauthorizedException e) {
            Assert.assertEquals("forbidden reset the image", e.getMessage());
        }
    }

    @Test
    public void testResetImageStatusSuccess() {
        AccessUserUtil.setUser("39937079-99fe-4cd8-881f-04ca8c4fe09d", "admin", Consts.ROLE_DEVELOPER_ADMIN);
        boolean res = vmImageService.resetImageStatus(2);
        Assert.assertEquals(true, res);
    }

    @Test
    public void testUploadVmImageBadWithErrRequest() {
        Chunk chunk = new Chunk();
        ResponseEntity responseEntity = vmImageService.uploadVmImage(request, chunk, null);
        Assert.assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void testUploadVmImageBadWithErrFileFormat() {
        Chunk chunk = new Chunk();
        request.setContentType("multipart/form-data");
        request.setMethod(RequestMethod.POST.name());
        ResponseEntity responseEntity = vmImageService.uploadVmImage(request, chunk, null);
        Assert.assertEquals(400, responseEntity.getStatusCode().value());
    }

    @Test
    public void testUploadVmImageBadWithNullFileNumber() throws IOException {
        File tarFile = Resources.getResourceAsFile("testdata/nginx.tar");
        InputStream helmIs = new FileInputStream(tarFile);
        MultipartFile tarMultiFile = new MockMultipartFile(tarFile.getName(), tarFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), helmIs);
        Chunk chunk = new Chunk();
        chunk.setFile(tarMultiFile);
        chunk.setChunkNumber(null);
        chunk.setCurrentChunkSize(24173056L);
        chunk.setTotalSize(24173056L);
        chunk.setFilename("nginx.tar");
        chunk.setRelativePath("nginx.tar");
        chunk.setTotalChunks(3);
        request.setContentType("multipart/form-data");
        request.setMethod(RequestMethod.POST.name());
        ResponseEntity res = vmImageService.uploadVmImage(request, chunk, null);
        Assert.assertEquals(400, res.getStatusCode().value());

    }

    @Test
    public void testUploadVmImageBadWithErrFileAddr() throws IOException {
        File tarFile = Resources.getResourceAsFile("testdata/nginx.tar");
        InputStream helmIs = new FileInputStream(tarFile);
        MultipartFile tarMultiFile = new MockMultipartFile(tarFile.getName(), tarFile.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), helmIs);
        Chunk chunk = new Chunk();
        chunk.setFile(tarMultiFile);
        chunk.setChunkNumber(4);
        chunk.setCurrentChunkSize(24173056L);
        chunk.setTotalSize(24173056L);
        chunk.setFilename("nginx.tar");
        chunk.setRelativePath("nginx.tar");
        chunk.setTotalChunks(3);
        request.setContentType("multipart/form-data");
        request.setMethod(RequestMethod.POST.name());
        ResponseEntity res = vmImageService.uploadVmImage(request, chunk, 2);
        Assert.assertEquals(500, res.getStatusCode().value());

    }

    @Test
    public void testCheckUploadedChunksBadWithErrIdentifier() {
        List<Integer> list = vmImageService.checkUploadedChunks(2, "24173056-nginxtar");
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testCancelUploadVmImageBadWithErrStatus() {
        ResponseEntity res = vmImageService.cancelUploadVmImage(5, "test");
        Assert.assertEquals(400, res.getStatusCode().value());
    }

    @Test
    public void testCancelUploadVmImageSuccess() {
        ResponseEntity responseEntity = vmImageService.cancelUploadVmImage(2, "test");
        Assert.assertEquals(responseEntity.getStatusCode().value(), 200);
    }

    @Test
    public void testMergeVmImageBadWithMergeFailed() throws IOException {
        ResponseEntity responseEntity = vmImageService.mergeVmImage("nginx.tar", "24173056-nginxtar", 2);
        Assert.assertEquals(responseEntity.getStatusCode().value(), 500);
    }

    @Test
    public void testImageSlimBadWithErrId() throws IOException {
        try {
            vmImageService.imageSlim(1000);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("vm image not found", e.getMessage());
        }
    }

    @Test
    public void testImageSlimBadWithBadAuth() throws IOException {
        try {
            AccessUserUtil.setUser("userId", "userName", "common");
            vmImageService.imageSlim(2);
        } catch (UnauthorizedException e) {
            Assert.assertEquals("forbidden slim the image", e.getMessage());
        }
    }

    @Test
    public void testImageSlimBadWithRemoteServer() throws IOException {
        try {
            AccessUserUtil.setUser("39937079-99fe-4cd8-881f-04ca8c4fe09d", "admin", Consts.ROLE_DEVELOPER_ADMIN);
            vmImageService.imageSlim(2);
        } catch (RestfulRequestException e) {
            Assert.assertEquals("image slim fail.", e.getMessage());
        }
    }

    @Test
    public void testCreateVmImageAllInfoSuccess() throws IOException {
        VMImage vmImage = new VMImage();
        vmImage.setName("Ubuntu18.0445");
        vmImage.setUserId("userId");
        VMImage createdVmImage = vmImageService.createVmImageAllInfo(vmImage);
        Assert.assertNotNull(createdVmImage.getName());
    }

    @Test
    public void testDownloadVmImageSuccess() throws IOException {
        byte[] data = vmImageService.downloadVmImage(2);
        Assert.assertNull(data);
    }


}
