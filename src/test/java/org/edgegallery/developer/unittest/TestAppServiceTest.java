/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import com.spencerwi.either.Either;
import java.io.File;
import org.apache.commons.fileupload.FileItem;
import org.apache.ibatis.io.Resources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.TestApp;
import org.edgegallery.developer.request.AppRequestParam;
import org.edgegallery.developer.response.AppTagsResponse;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.SubTaskListResponse;
import org.edgegallery.developer.response.TestTaskListResponse;
import org.edgegallery.developer.service.TestAppService;
import org.edgegallery.developer.service.TestCaseService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class TestAppServiceTest {

    /**
     * The following tests are based on the premise that both AUTH and USER_ID are correct
     */
    @Autowired
    private TestAppService testAppService;

    @Autowired
    private TestCaseService testCaseService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void upload() throws Exception {
        AppRequestParam app = new AppRequestParam();
        app.setAppId("4b9f5b7a-6a42-4a77-93ec-857bc15aa4d3");
        File appfile = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
        FileItem item = DeveloperFileUtils.createFileItem(appfile, "appFile");
        MultipartFile appFile = new CommonsMultipartFile(item);
        app.setAppFile(appFile);
        File logo = Resources.getResourceAsFile("testdata/face.png");
        FileItem logoItem = DeveloperFileUtils.createFileItem(logo, "logoFile");
        MultipartFile logoFile = new CommonsMultipartFile(logoItem);
        app.setLogoFile(logoFile);
        app.setAffinity("x86");
        app.setIndustry("industry");
        app.setType("MEP");
        app.setAppDesc("test");
        app.setUserId("9630ee98-893e-48a4-8856-1525e58d66d0");

        Either<FormatRespDto, TestApp> either = testAppService.upload(app);

        if (either.isRight()) {
            Assert.assertEquals("MEP", either.getRight().getType());
        }

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getTaskList() {
        Either<FormatRespDto, TestTaskListResponse> either = testAppService
            .getTaskByParam(null, null, null, null, null);
        if (either.isRight()) {
            Assert.assertNotNull(either.getRight());
        }

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getAppTagList() {
        Either<FormatRespDto, AppTagsResponse> either = testAppService.getTagList();
        if (either.isRight()) {
            Assert.assertNotNull(either.getRight());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void uploadToAppStore() {
        Either<FormatRespDto, String> either = testAppService.uploadToAppStore("assssddddddddd", "xxx", "username", "token");
        if (either.isLeft()) {
            Assert.assertEquals(400, either.getLeft().getErrorRespDto().getCode());
        }
        if (either.isRight()) {
            Assert.assertNotNull(either.getRight());
        }

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void startTask() throws Exception {
        Either<FormatRespDto, Boolean> either = testCaseService.startToTest("", "helongfei");
        Assert.assertEquals(400, either.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getSubtasks() {
        Either<FormatRespDto, SubTaskListResponse> either = testCaseService.getSubTasks("xxxxxxxxxx", "xxxxxxx");

        if (either.isLeft()) {
            Assert.assertEquals(400, either.getLeft().getErrorRespDto().getCode());
        }
        if (either.isRight()) {
            Assert.assertNotNull(either.getRight());
        }

    }

}
