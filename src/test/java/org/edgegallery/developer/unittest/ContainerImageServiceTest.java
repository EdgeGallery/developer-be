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

package org.edgegallery.developer.unittest;

import java.util.Date;
import java.util.UUID;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.exception.DataBaseException;
import org.edgegallery.developer.exception.ForbiddenException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.model.containerimage.ContainerImage;
import org.edgegallery.developer.model.containerimage.ContainerImageReq;
import org.edgegallery.developer.model.containerimage.EnumContainerImageStatus;
import org.edgegallery.developer.service.image.ContainerImageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ContainerImageServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerImageServiceTest.class);

    @Autowired
    private ContainerImageService containerImageService;

    @Test
    public void testCreateContainerImageSuccess() {
        ContainerImage containerImage = new ContainerImage();
        containerImage.setImageId(UUID.randomUUID().toString());
        containerImage.setImageName("test");
        containerImage.setImageVersion("1.0");
        containerImage.setImageType("private");
        containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_SUCCEED);
        containerImage.setUserName("admin");
        containerImage.setUserId(UUID.randomUUID().toString());
        containerImage.setCreateTime(new Date());
        containerImage.setUploadTime(new Date());
        containerImage.setImagePath("xxx/xxx/test:1.0");
        containerImage.setFileName("test.tar");
        ContainerImage response = containerImageService.createContainerImage(containerImage);
        Assert.assertNotNull(response);
    }

    @Test
    public void testCreateContainerImageBad1() {
        try {
            ContainerImage containerImage = new ContainerImage();
            containerImage.setImageId(UUID.randomUUID().toString());
            containerImage.setImageName("");
            containerImage.setImageVersion("1.0");
            containerImage.setImageType("private");
            containerImage.setImageStatus(EnumContainerImageStatus.UPLOAD_SUCCEED);
            containerImage.setUserName("admin");
            containerImage.setUserId(UUID.randomUUID().toString());
            containerImage.setCreateTime(new Date());
            containerImage.setUploadTime(new Date());
            containerImage.setImagePath("xxx/xxx/test:1.0");
            containerImage.setFileName("test.tar");
            ContainerImage response = containerImageService.createContainerImage(containerImage);
        } catch (IllegalRequestException e) {
            Assert.assertEquals(e.getMessage(),
                "The required parameter is empty. pls check imageName or imageVersion or userId or userName");
        }
    }

    @Test
    public void testGetAllContainerImageSuccess() {
        AccessUserUtil.setUser("c5c7c35a-f85b-441c-9307-5516b951efd2", "author", Consts.ROLE_DEVELOPER_ADMIN);
        ContainerImageReq containerImage = new ContainerImageReq();
        containerImage.setUserId("c5c7c35a-f85b-441c-9307-5516b951efd2");
        containerImage.setImageName("test1");
        containerImage.setLimit(1);
        containerImage.setOffset(0);
        containerImage.setSortBy("image_name");
        containerImage.setSortOrder("desc");
        Page<ContainerImage> page = containerImageService.getAllImage(containerImage);
        Assert.assertNotNull(page);
    }

    @Test
    public void testGetAllContainerImageSuccess1() {
        AccessUserUtil.setUser("c5c7c35a-f85b-441c-9307-5516b951efd2", "author", "other");
        ContainerImageReq containerImage = new ContainerImageReq();
        containerImage.setUserId("c5c7c35a-f85b-441c-9307-5516b951efd2");
        containerImage.setImageName("test1");
        containerImage.setLimit(1);
        containerImage.setOffset(0);
        containerImage.setSortBy("image_name");
        containerImage.setSortOrder("desc");
        Page<ContainerImage> page = containerImageService.getAllImage(containerImage);
        Assert.assertNotNull(page);
    }

    @Test
    public void testUpdateContainerImageBad() {
        try {
            AccessUserUtil.setUser("c5c7c35a-f85b-441c-9307-5516b951efd2", "author", "other");
            ContainerImage containerImage = new ContainerImage();
            containerImage.setImageId("6ababcec-2934-43d9-afc8-d3d403ebc782");
            containerImage.setImageName("test1");
            containerImage.setImageVersion("1.0");
            containerImage.setUserName("author");
            containerImage.setUserId("c5c7c35a-f85b-441c-9307-5516b951efd2");
            ContainerImage page = containerImageService
                .updateContainerImage("6ababcec-2934-43d9-afc8-d3d403ebc782", containerImage);
        } catch (DataBaseException e) {
            Assert.assertEquals(e.getMessage(), "update ContainerImage type failed.");
        }
    }

    @Test
    public void testUpdateContainerImageBad1() {
        try {
            AccessUserUtil.setUser("c5c7c35a-f85b-441c-9307-5516b951efd3", "author", "other");
            ContainerImage containerImage = new ContainerImage();
            containerImage.setImageId("6ababcec-2934-43d9-afc8-d3d403ebc782");
            containerImage.setImageName("test1");
            containerImage.setImageVersion("1.0");
            containerImage.setUserName("author");
            containerImage.setUserId("c5c7c35a-f85b-441c-9307-5516b951efd2");
            ContainerImage page = containerImageService
                .updateContainerImage("6ababcec-2934-43d9-afc8-d3d403ebc782", containerImage);
        } catch (ForbiddenException e) {
            Assert.assertEquals(e.getMessage(), "Cannot modify data created by others");
        }
    }

    @Test
    public void testUpdateContainerImageSuccess() {
        AccessUserUtil.setUser("c5c7c35a-f85b-441c-9307-5516b951efd2", "author", "other");
        ContainerImage containerImage = new ContainerImage();
        containerImage.setImageId("6ababcec-2934-43d9-afc8-d3d403ebc782");
        containerImage.setImageName("test1");
        containerImage.setImageVersion("1.0");
        containerImage.setUserName("author");
        containerImage.setUserId("c5c7c35a-f85b-441c-9307-5516b951efd2");
        containerImage.setImageType("private");
        ContainerImage page = containerImageService
            .updateContainerImage("6ababcec-2934-43d9-afc8-d3d403ebc782", containerImage);
        Assert.assertNotNull(page);
    }

    @Test
    public void testDeleteContainerImageBad1() {
        try {
            AccessUserUtil.setUser("c5c7c35a-f85b-441c-9307-5516b951efd3", "author", "other");
            containerImageService.deleteContainerImage("6ababcec-2934-43d9-afc8-d3d403ebc782");
        } catch (ForbiddenException e) {
            Assert.assertEquals(e.getMessage(), "Cannot delete data created by others");
        }
    }

    @Test
    public void testDownloadContainerImageBad1() {
        try {
            containerImageService.downloadHarborImage("");
        } catch (IllegalRequestException e) {
            Assert.assertEquals(e.getMessage(), "imageId is null");
        }
    }

    @Test
    public void testDownloadContainerImageBad2() {
        try {
            containerImageService.downloadHarborImage("6ababcec-2934-43d9-afc8-d3d403ebc782");
        } catch (IllegalRequestException e) {
            Assert.assertEquals(e.getMessage(), "image or fileName of this record in db is empty");
        }
    }

    @Test
    public void testDownloadContainerImageBad3() {
        try {
            containerImageService.downloadHarborImage("6ababcec-2934-43d9-afc8-d3d403ebc789");
        } catch (IllegalRequestException e) {
            Assert.assertEquals(e.getMessage(), "imageId is incorrect");
        }
    }

    @Test
    public void testCancelUploadContainerImageSuccess() {
        ResponseEntity res = containerImageService.cancelUploadHarborImage("6ababcec-2934-43d9-afc8-d3d403ebc782");
        Assert.assertEquals(200, res.getStatusCode().value());
    }

}
