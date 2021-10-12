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

import com.spencerwi.either.Either;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.DeveloperApplication;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.apppackage.AppPkgStructure;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.HelmTemplateYamlRespDto;
import org.edgegallery.developer.service.HostService;
import org.edgegallery.developer.service.UploadFileService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.InitConfigUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ImageConfig.class)
public class UploadFilesServiceTest {

    @Autowired
    private UploadFileService uploadFileService;



    @Autowired
    private HostService hostService;

    private void toDeleteTempFile(UploadedFile uploadFile) {
        String realPath = InitConfigUtil.getWorkSpaceBaseDir() + uploadFile.getFilePath();
        File temp = new File(realPath);
        if (temp.exists()) {
            DeveloperFileUtils.deleteTempFile(temp);
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadFile() throws IOException, URISyntaxException {
        File sourceFile = new File(
            UploadFilesServiceTest.class.getClassLoader().getResource("testdata/test-icon.png").toURI());
        byte[] md5 = DigestUtils.md5(new FileInputStream(sourceFile));
        MultipartFile uploadFile = new MockMultipartFile("file-name", "file-name", null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("test-user", uploadFile);
        Assert.assertTrue(result.isRight());
        toDeleteTempFile(result.getRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadFileBad2() {
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("test-user", null);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadFileBad3() throws IOException {
        MultipartFile uploadFile = new MockMultipartFile("file-name", "file-name", null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("", uploadFile);
        Assert.assertTrue(result.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetFile() throws IOException, URISyntaxException {
        String sourceData = FileUtils.readFileToString(
            new File(UploadFilesServiceTest.class.getClassLoader().getResource("testdata/test-icon.png").toURI()),
            "UTF-8");
        MultipartFile uploadFile = new MockMultipartFile("test-icon.png", "test-icon.png", null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("test-user", uploadFile);
        Assert.assertTrue(result.isRight());
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService
            .getFile(result.getRight().getFileId(), "test-user", "OPENMEP_ECO");
        Assert.assertTrue(fileStream.isRight());
        byte[] input = fileStream.getRight().getBody();
        Assert.assertEquals(sourceData, new String(input, "UTF-8"));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetYamlFile() throws IOException, URISyntaxException {
        String sourceData = FileUtils.readFileToString(
            new File(UploadFilesServiceTest.class.getClassLoader().getResource("testdata/demo-onlyagent.yaml").toURI()),
            "UTF-8");
        MultipartFile uploadFile = new MockMultipartFile("demo-onlyagent.yaml", "demo-onlyagent.yaml", null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream("testdata/demo-onlyagent.yaml"));
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("test-user", uploadFile);
        Assert.assertTrue(result.isRight());
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService
            .getFile(result.getRight().getFileId(), "test-user", "OPENMEP");
        Assert.assertTrue(fileStream.isRight());
        byte[] input = fileStream.getRight().getBody();
        Assert.assertEquals(sourceData, new String(input, "UTF-8"));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetYamlFile1() throws IOException, URISyntaxException {
        String sourceData = FileUtils.readFileToString(
            new File(UploadFilesServiceTest.class.getClassLoader().getResource("testdata/demo-onlyagent.yaml").toURI()),
            "UTF-8");
        MultipartFile uploadFile = new MockMultipartFile("demo-onlyagent.yaml", "demo-onlyagent.yaml", null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream("testdata/demo-onlyagent.yaml"));
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("test-user", uploadFile);
        Assert.assertTrue(result.isRight());
        Either<FormatRespDto, Boolean> res = hostService.deleteHost("c8aac2b2-4162-40fe-9d99-0630e3245ct5");
        Assert.assertEquals(true, res.isRight());
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService
            .getFile(result.getRight().getFileId(), "test-user", "OPENMEP");
        Assert.assertTrue(fileStream.isRight());
        byte[] input = fileStream.getRight().getBody();
        Assert.assertEquals(sourceData, new String(input, "UTF-8"));
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetFileBad() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService
            .getFile("ss", "test-user", "OPENMEP_ECO");
        Assert.assertTrue(fileStream.isLeft());
        Assert.assertEquals(400, fileStream.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetFileBad1() throws Exception {
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService
            .getFile("ad66d1b6-5d29-487b-9769-be48b62aec2e", "test-user", "OPENMEP_ECO");
        Assert.assertTrue(fileStream.isLeft());
        Assert.assertEquals(400, fileStream.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetApiFileBad() throws Exception {
        Either<FormatRespDto, UploadedFile> fileStream = uploadFileService
            .getApiFile("ad66d1b6-5d29-487b-9769-be48b62aec2e", "test-user");
        Assert.assertTrue(fileStream.isLeft());
        Assert.assertEquals(400, fileStream.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetApiFileSuccess() throws Exception {
        Either<FormatRespDto, UploadedFile> fileStream = uploadFileService.getApiFile("test_id", "test-user");
        Assert.assertTrue(fileStream.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadFileBad() throws Exception {
        MultipartFile multipartFile = new MockMultipartFile("test", new byte[10]);
        Either<FormatRespDto, UploadedFile> fileStream = uploadFileService
            .uploadFile("ad66d1b6-5d29-487b-9769-be48b62aec2e", multipartFile);
        Assert.assertTrue(fileStream.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadSampleCodeBad() throws Exception {
        List<String> apiFileIds = new ArrayList<>();
        apiFileIds.add("test1");
        apiFileIds.add("test2");
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService.downloadSampleCode(apiFileIds);
        Assert.assertTrue(fileStream.isLeft());
        Assert.assertEquals(400, fileStream.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadSampleCodeSuccess() throws Exception {
        List<String> apiFileIds = new ArrayList<>();
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService.downloadSampleCode(apiFileIds);
        Assert.assertTrue(fileStream.isRight());
        Assert.assertEquals(200, fileStream.getRight().getStatusCode().value());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadSampleCodeBad1() throws Exception {
        List<String> apiFileIds = new ArrayList<>();
        apiFileIds.add("db8c7b17-0be3-4ca4-b22e-fa6d62e9a6e0");
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService.downloadSampleCode(apiFileIds);
        Assert.assertTrue(fileStream.isLeft());
        Assert.assertEquals(400, fileStream.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDownloadSampleCodeBad2() throws Exception {
        List<String> apiFileIds = new ArrayList<>();
        apiFileIds.add("ad66d1b6-5d29-487b-9769-be48b62aec2e");
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService.downloadSampleCode(apiFileIds);
        Assert.assertTrue(fileStream.isLeft());
        Assert.assertEquals(400, fileStream.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDelTmpFile() throws Exception {
        uploadFileService.deleteTempFile();
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadHelmYamlWithBadName() throws Exception {
        File helmYaml = Resources.getResourceAsFile("testdata/face.png");
        InputStream helmIs = new FileInputStream(helmYaml);
        MultipartFile helmMultiFile = new MockMultipartFile(helmYaml.getName(), helmYaml.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), helmIs);
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, HelmTemplateYamlRespDto> either = uploadFileService
            .uploadHelmTemplateYaml(helmMultiFile, "userId", projectId, "UPLOAD");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadHelmYaml() throws Exception {
        File helmYaml = Resources.getResourceAsFile("testdata/demo.yaml");
        InputStream helmIs = new FileInputStream(helmYaml);
        MultipartFile helmMultiFile = new MockMultipartFile(helmYaml.getName(), helmYaml.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), helmIs);
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, HelmTemplateYamlRespDto> either = uploadFileService
            .uploadHelmTemplateYaml(helmMultiFile, "userId", projectId, "UPLOAD");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUploadHelmYaml2() throws Exception {
        File helmYaml = Resources.getResourceAsFile("testdata/demo-with-agent.yaml");
        InputStream helmIs = new FileInputStream(helmYaml);
        MultipartFile helmMultiFile = new MockMultipartFile(helmYaml.getName(), helmYaml.getName(),
            ContentType.APPLICATION_OCTET_STREAM.toString(), helmIs);
        String projectId = "200dfab1-3c30-4fc7-a6ca-ed6f0620a85e";
        Either<FormatRespDto, HelmTemplateYamlRespDto> either = uploadFileService
            .uploadHelmTemplateYaml(helmMultiFile, "userId", projectId, "UPLOAD");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHelmYamlFail() throws Exception {
        Either<FormatRespDto, String> either = uploadFileService.deleteHelmTemplateYamlByFileId("aaa");
        Assert.assertTrue(either.isLeft());
        Assert.assertEquals(500, either.getLeft().getErrorRespDto().getCode());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHelmYamlSuccess() throws Exception {
        String fileId = "ad66d1b6-5d29-487b-9769-be48b62aec2e";
        Either<FormatRespDto, String> either = uploadFileService.deleteHelmTemplateYamlByFileId(fileId);
        Assert.assertTrue(either.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHelmYamlBad1() throws Exception {
        Either<FormatRespDto, String> either = uploadFileService.deleteHelmTemplateYamlByFileId("ad66d1b6-5d29-487b-9769-be48b62aec2g");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSdkProjectBad1() throws Exception {
        String fileId = "540e0817-f6ea-42e5-8c5b-cb2daf9925a3";
        Either<FormatRespDto, ResponseEntity<byte[]>> either = uploadFileService.getSdkProject(fileId, "java");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSdkProject() throws Exception {
        String fileId = "e111f3e7-90d8-4a39-9874-ea6ea6752ef5";
        Either<FormatRespDto, ResponseEntity<byte[]>> either = uploadFileService.getSdkProject(fileId, "java");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSdkProjectBad2() throws Exception {
        String fileId = "test-file-id";
        Either<FormatRespDto, ResponseEntity<byte[]>> either = uploadFileService.getSdkProject(fileId, "java");
        Assert.assertTrue(either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSampleCodeStru() throws IOException, URISyntaxException {
        List<String> apiIds = new ArrayList<>();
        apiIds.add("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        Either<FormatRespDto, AppPkgStructure> either = uploadFileService.getSampleCodeStru(apiIds);
        Assert.assertEquals(false, either.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSampleCodeStruBad1() throws IOException, URISyntaxException {
        List<String> apiIds = new ArrayList<>();
        apiIds.add("e111f3e7-90d8xxxxxxxx");
        Either<FormatRespDto, AppPkgStructure> either = uploadFileService.getSampleCodeStru(apiIds);
        Assert.assertEquals(true, either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testSampleCodeStruBad2() throws IOException, URISyntaxException {
        List<String> apiIds = new ArrayList<>();
        apiIds.add("e111f3e7-90d8-4a39-9874-ea6ea6000000");
        Either<FormatRespDto, AppPkgStructure> either = uploadFileService.getSampleCodeStru(apiIds);
        Assert.assertEquals(true, either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSampleCodeContent() throws IOException, URISyntaxException {
        List<String> apiIds = new ArrayList<>();
        apiIds.add("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        Either<FormatRespDto, AppPkgStructure> either = uploadFileService.getSampleCodeStru(apiIds);
        Assert.assertEquals(false, either.isRight());
        Either<FormatRespDto, String> either1 = uploadFileService.getSampleCodeContent("error");
        Assert.assertEquals(true, either.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSampleCodeContent1() throws IOException, URISyntaxException {
        List<String> apiIds = new ArrayList<>();
        apiIds.add("e111f3e7-90d8-4a39-9874-ea6ea6752ef5");
        Either<FormatRespDto, AppPkgStructure> either = uploadFileService.getSampleCodeStru(apiIds);
        Assert.assertEquals(false, either.isRight());
        Either<FormatRespDto, String> either1 = uploadFileService.getSampleCodeContent("Api");
        Assert.assertEquals(false, either1.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSampleCodeContent2() throws IOException, URISyntaxException {
        Either<FormatRespDto, String> either1 = uploadFileService.getSampleCodeContent("Api");
        Assert.assertEquals(false, either1.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetSampleCodeContent3() throws IOException, URISyntaxException {
        List<String> apiIds = new ArrayList<>();
        apiIds.add("e111f3e7-90d8-4a39-9874-ea6ea6752eab");
        Either<FormatRespDto, AppPkgStructure> either = uploadFileService.getSampleCodeStru(apiIds);
        Assert.assertEquals(false, either.isRight());
        Either<FormatRespDto, String> either1 = uploadFileService.getSampleCodeContent("Api");
        Assert.assertEquals(true, either1.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteTempFile() {
        uploadFileService.deleteTempFile();
    }





}
