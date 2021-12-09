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
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.uploadfile.UploadFile;
import org.edgegallery.developer.service.application.ApplicationService;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.service.application.impl.container.ContainerAppOperationServiceImpl;
import org.edgegallery.developer.service.uploadfile.UploadFileService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.test.service.application.vm.VMAppOperationServiceTest;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ContainerAppOperationServiceTest extends AbstractJUnit4SpringContextTests {

    private static String APPLICATION_ID = "6a75a2bd-9811-432f-bbe8-2813aa97d365";

    @Autowired
    ContainerAppOperationServiceImpl containerAppOperationService;

    @Autowired
    ContainerAppHelmChartService containerAppHelmChartService;

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ApplicationService applicationService;

    @Before
    public void prepare() {
        SpringContextUtil.setApplicationContext(applicationContext);
        prepareFilesForTestApplication();
    }

    @Test
    public void generatePackageTest() throws IOException {
        new MockUp<ContainerAppHelmChartUtil>() {
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

}
