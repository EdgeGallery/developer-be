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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.workspace.UploadedFile;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.UploadFileService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.edgegallery.developer.util.InitConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class UploadFilesServiceTest {

    @Autowired
    private UploadFileService uploadFileService;

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
        String filePath = result.getRight().getFilePath();
        String realPath = InitConfigUtil.getWorkSpaceBaseDir() + filePath;
        System.out.println(realPath);
        File file = new File(realPath);
        Assert.assertTrue(file.exists() && file.isFile());
        Assert.assertEquals("file-name", result.getRight().getFileName());
        byte[] md5new = DigestUtils.md5(new FileInputStream(file));
        Assert.assertEquals(new String(md5, "utf-8"), new String(md5new, "utf-8"));
        toDeleteTempFile(result.getRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetFile() throws IOException, URISyntaxException {
        String sourceData = FileUtils.readFileToString(new File(UploadFilesServiceTest.class.getClassLoader().getResource("testdata/test-icon.png").toURI()), "UTF-8");
        MultipartFile uploadFile = new MockMultipartFile("file-name", "file-name", null,
            UploadFilesServiceTest.class.getClassLoader().getResourceAsStream("testdata/test-icon.png"));
        Either<FormatRespDto, UploadedFile> result = uploadFileService.uploadFile("test-user", uploadFile);
        Assert.assertTrue(result.isRight());
        Either<FormatRespDto, ResponseEntity<byte[]>> fileStream = uploadFileService
            .getFile(result.getRight().getFileId(), "test-user", "OPENMEP_ECO");
        Assert.assertTrue(fileStream.isRight());
        byte[] input = fileStream.getRight().getBody();
        Assert.assertEquals(sourceData, new String(input,"UTF-8"));
    }
}
