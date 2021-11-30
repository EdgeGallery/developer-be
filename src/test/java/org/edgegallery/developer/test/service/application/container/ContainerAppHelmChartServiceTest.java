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
import java.util.ArrayList;
import java.util.List;
import mockit.Mock;
import mockit.MockUp;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.exception.IllegalRequestException;
import org.edgegallery.developer.model.application.container.HelmChart;
import org.edgegallery.developer.model.application.container.ModifyFileContentDto;
import org.edgegallery.developer.service.application.container.ContainerAppHelmChartService;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.ContainerAppHelmChartUtil;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
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
public class ContainerAppHelmChartServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppHelmChartServiceTest.class);

    @Autowired
    private ContainerAppHelmChartService appHelmChartService;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");

        new MockUp<ContainerAppHelmChartUtil>() {
            @Mock
            public List<String> getImageFromHelmFile(IContainerFileHandler containerFileHandler,String helmChartsPackagePath) {
                {
                    List<String> list = new ArrayList<>();
                    list.add("1/2/3:4");
                    return list;
                }
            }

            @Mock
            public boolean checkImageExist(List<String> imageList) {
                {
                    return true;
                }
            }

        };

    }

    @Test
    public void testUploadHelmChartFileSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));

        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d364", uploadFile);
        LOGGER.info("fileList:{}", helmChart.getHelmChartFileList());
        Assert.assertNotNull(helmChart.getHelmChartFileList());

    }

    @Test
    public void should_successfully_when_upload_more_then_one_k8s_file() throws IOException {
        File yaml1 = Resources.getResourceAsFile("testdata/helmcharts/demo.yaml");
        File yaml2 = Resources.getResourceAsFile("testdata/helmcharts/demo-onlyagent.yaml");
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d364", yaml1.getCanonicalPath(),
                yaml2.getCanonicalPath());
        LOGGER.info("fileList:{}", helmChart.getHelmChartFileList());
        Assert.assertNotNull(helmChart.getHelmChartFileList());
        Assert.assertEquals("demo.tgz", helmChart.getName());
    }

    @Test
    public void should_successfully_when_upload_two_yaml_k8s_file() throws IOException {
        File yaml1 = Resources.getResourceAsFile("testdata/helmcharts/demo.yml");
        File yaml2 = Resources.getResourceAsFile("testdata/helmcharts/demo-onlyagent.yaml");
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d364", yaml1.getCanonicalPath(),
                yaml2.getCanonicalPath());
        LOGGER.info("fileList:{}", helmChart.getHelmChartFileList());
        Assert.assertNotNull(helmChart.getHelmChartFileList());
        Assert.assertEquals("demo.tgz", helmChart.getName());
    }

    @Test
    public void should_failed_when_upload_two_type_file() throws IOException {
        File yaml = Resources.getResourceAsFile("testdata/helmcharts/demo.yaml");
        File tgz = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d364", yaml.getCanonicalPath(),
                tgz.getCanonicalPath());
        Assert.assertNull(helmChart);
    }

    @Test(expected = DeveloperException.class)
    public void should_failed_when_upload_two_tgz_files() throws IOException {
        File tgz1 = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        File tgz2 = Resources.getResourceAsFile("testdata/helmcharts/namespacetest2.tgz");
        appHelmChartService.uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d364", tgz1.getCanonicalPath(),
            tgz2.getCanonicalPath());
    }

    @Test
    public void testUploadHelmChartYamlSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("demo-with-agent.yaml", "demo-with-agent.yaml", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/demo-with-agent.yaml"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("3f11715f-b59e-4c23-965b-b7f9c34c20d1", uploadFile);
        LOGGER.info("fileList:{}", helmChart.getHelmChartFileList());
        Assert.assertNotNull(helmChart.getHelmChartFileList());
    }

    @Test
    public void testGetHelmListFailed() {
        try {
            appHelmChartService.getHelmChartList(null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId is empty", e.getMessage());
        }
    }

    @Test
    public void testDeleteHelmChartByAppIdFailed() {
        try {
            appHelmChartService.deleteHelmChartByAppId(null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId is empty!", e.getMessage());
        }
    }

    @Test
    public void testDeleteHelmChartByAppIdFailed2() {
        try {
            appHelmChartService.deleteHelmChartByAppId("test");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("the query Application is empty", e.getMessage());
        }
    }

    @Test
    public void testDeleteHelmChartByAppIdFailed3() {
        boolean ret = appHelmChartService.deleteHelmChartByAppId("6a75a2bd-9811-432f-bbe8-2813aa97d366");
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testDeleteHelmChartByAppIdSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d366", uploadFile);
        LOGGER.info("fileList:{}", helmChart.getHelmChartFileList());
        Assert.assertNotNull(helmChart.getHelmChartFileList());
        boolean ret = appHelmChartService.deleteHelmChartByAppId("6a75a2bd-9811-432f-bbe8-2813aa97d366");
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testGetHelmListSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d365", uploadFile);
        LOGGER.info("fileList:{}", helmChart.getHelmChartFileList());
        Assert.assertNotNull(helmChart.getHelmChartFileList());
        List<HelmChart> list = appHelmChartService.getHelmChartList("6a75a2bd-9811-432f-bbe8-2813aa97d365");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testGetOneHelmFailedWithNullAppId() throws IOException {
        try {
            appHelmChartService.getHelmChartById(null, "test");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId is empty", e.getMessage());
        }
    }

    @Test
    public void testGetOneHelmFailedWithNullHelmId() throws IOException {
        try {
            appHelmChartService.getHelmChartById("appId", null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("helm chart id is empty", e.getMessage());
        }
    }

    @Test
    public void testGetOneHelmFailedWithNullRet() throws IOException {
        try {
            appHelmChartService.getHelmChartById("appId", "helmID");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("the query HelmChart is empty", e.getMessage());
        }
    }

    @Test
    public void testGetOneHelmSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d366", uploadFile);
        Assert.assertNotNull(helmChart);
        HelmChart chart = appHelmChartService
            .getHelmChartById("6a75a2bd-9811-432f-bbe8-2813aa97d366", helmChart.getId());
        Assert.assertNotNull(chart);
    }

    @Test
    public void testDeleteOneHelmFailedWithNullHelmId() throws IOException {
        try {
            appHelmChartService.deleteHelmChartById("appId", null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testDeleteOneHelmFailedWithNullAppId() throws IOException {
        try {
            appHelmChartService.deleteHelmChartById(null, "helmchartID");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testDeleteOneHelmFailedWithBadAppId() throws IOException {
        try {
            appHelmChartService.deleteHelmChartById("band", "helmchartID");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("the query Application is empty", e.getMessage());
        }
    }

    @Test
    public void testDeleteOneHelmFailedWithBadHelmId() throws IOException {
        try {
            appHelmChartService.deleteHelmChartById("6a75a2bd-9811-432f-bbe8-2813aa97d364", "helmchartID");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("query HelmChart is empty!", e.getMessage());
        }
    }

    @Test
    public void testDeleteOneHelmSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("4cbbab9d-c48f-4adb-ae82-d1816d8edd7b", uploadFile);
        Assert.assertNotNull(helmChart);
        boolean ret = appHelmChartService
            .deleteHelmChartById("4cbbab9d-c48f-4adb-ae82-d1816d8edd7b", helmChart.getId());
        Assert.assertEquals(true, ret);
    }

    @Test
    public void testDownloadHelmFileFailedWithNullAppId() throws IOException {
        try {
            appHelmChartService.downloadHelmChart(null, "helmchartID");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testDownloadHelmFileFailedWithNullHelmId() throws IOException {
        try {
            appHelmChartService.downloadHelmChart("test", null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testDownloadHelmFileFailedWithBadHelmId() throws IOException {
        try {
            appHelmChartService.downloadHelmChart("test", "test111");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("the query HelmChart is empty", e.getMessage());
        }
    }

    @Test
    public void testDownloadHelmFileSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d367", uploadFile);
        Assert.assertNotNull(helmChart);
        byte[] data = appHelmChartService.downloadHelmChart("6a75a2bd-9811-432f-bbe8-2813aa97d367", helmChart.getId());
        Assert.assertNotNull(data);
    }

    @Test
    public void testGetHelmFileContentFailedWithNullAppId() throws IOException {
        try {
            appHelmChartService.getFileContentByFilePath(null, "helmchartID", "test");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testGetHelmFileContentWithNullHelmId() throws IOException {
        try {
            appHelmChartService.getFileContentByFilePath("test", null, "get");
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testGetHelmFileContentWithBadHelmId() throws IOException {
        try {
            appHelmChartService.getFileContentByFilePath("test", "test111", "get");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("the query HelmChart is empty", e.getMessage());
        }
    }

    @Test
    public void testGetHelmFileContentSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d368", uploadFile);
        String content = appHelmChartService
            .getFileContentByFilePath("6a75a2bd-9811-432f-bbe8-2813aa97d368", helmChart.getId(),
                "/templates/eg_template/namespace-config.yaml");
        Assert.assertNotNull(content);
    }

    @Test
    public void testModifyHelmFileContentWithNullHelmId() throws IOException {
        try {
            appHelmChartService.modifyFileContent("test", null, null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testModifyHelmFileContentWithNullAppId() throws IOException {
        try {
            appHelmChartService.modifyFileContent(null, "test111", null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("applicationId or helmChartId is empty!", e.getMessage());
        }
    }

    @Test
    public void testModifyHelmFileContentWithNullContentDto() throws IOException {
        try {
            appHelmChartService.modifyFileContent("appId", "helmId", null);
        } catch (IllegalRequestException e) {
            Assert.assertEquals("param ModifyFileContentDto is null!", e.getMessage());
        }
    }

    @Test
    public void testModifyHelmFileContentWithBadHelmId() throws IOException {
        try {
            ModifyFileContentDto dto = new ModifyFileContentDto();
            appHelmChartService.modifyFileContent("test", "test111", dto);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("the query HelmChart is empty", e.getMessage());
        }
    }

    @Test
    public void testModifyHelmFileContentSuccess() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("namespacetest.tgz", "namespacetest.tgz", null,
            ContainerAppHelmChartServiceTest.class.getClassLoader()
                .getResourceAsStream("testdata/helmcharts/namespacetest.tgz"));
        HelmChart helmChart = appHelmChartService
            .uploadHelmChartFile("6a75a2bd-9811-432f-bbe8-2813aa97d369", uploadFile);
        ModifyFileContentDto dto = new ModifyFileContentDto();
        dto.setInnerFilePath("/templates/eg_template/namespace-config.yaml");
        dto.setContent("test");
        boolean ret = appHelmChartService
            .modifyFileContent("6a75a2bd-9811-432f-bbe8-2813aa97d369", helmChart.getId(), dto);
        Assert.assertEquals(true, ret);
    }

}
