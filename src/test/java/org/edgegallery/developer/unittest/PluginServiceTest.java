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
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.checkerframework.checker.units.qual.A;
import org.edgegallery.developer.application.plugin.PluginService;
import org.edgegallery.developer.domain.model.plugin.ApiChecker;
import org.edgegallery.developer.domain.model.plugin.Plugin;
import org.edgegallery.developer.domain.model.plugin.PluginPageCriteria;
import org.edgegallery.developer.domain.model.plugin.PluginRepository;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.domain.service.FileService;
import org.edgegallery.developer.domain.shared.AFile;
import org.edgegallery.developer.domain.shared.FileChecker;
import org.edgegallery.developer.domain.shared.IconChecker;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.domain.shared.PluginChecker;
import org.edgegallery.developer.domain.shared.exceptions.EntityNotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.request.PluginRequestParam;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.response.PluginListResponse;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class PluginServiceTest {
   @Autowired
   private PluginService pluginService;

   @Autowired
   private PluginRepository pluginRepository;
    @Autowired
    private FileService fileService;


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
   public void getPluginList() {
       Page<Plugin> page= pluginRepository.findAllWithPagination(new PluginPageCriteria(15,0,"1"));

       Assert.assertNotNull(page);
   }

   @Test
   @WithMockUser(roles = "DEVELOPER_TENANT")
   public void delPlugin() {
       pluginService.deleteByPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8c7");
   }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void delPluginByBadId() {
       try {
           pluginService.deleteByPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8ss");
       }catch (EntityNotFoundException e){
           Assert.assertEquals("cannot find the plugin with id 586224da-e1a2-4893-a5b5-bf766fdfb8ss",e.getMessage());
       }
    }

   @Test
   @WithMockUser(roles = "DEVELOPER_TENANT")
   public void downloadPlugin() throws Exception {
       upload();
       InputStream stream = pluginService.download("b7370981-f199-4bf1-9814-30c3ea48b1d9");
       Assert.assertNotNull(stream);
   }
    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void downloadPluginLogo() throws Exception {
        upload();
        InputStream stream = pluginService.downloadLogo("b7370981-f199-4bf1-9814-30c3ea48b1d9");
        Assert.assertNotNull(stream);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void downloadPluginApi() throws Exception {
        upload();
        InputStream stream = pluginService.downloadApiFile("b7370981-f199-4bf1-9814-30c3ea48b1d9");
        Assert.assertNotNull(stream);
    }

   @Test
   @WithMockUser(roles = "DEVELOPER_TENANT")
   public void updatePluginScore() throws Exception {
       upload();
       User user = new User("b7370981-f199-4bf1-9814-30c3ea48b1d8","hello");
       Plugin either = pluginService.mark("b7370981-f199-4bf1-9814-30c3ea48b1d9",5,user);
       Assert.assertEquals(Float.toString(5f),Float.toString(either.getSatisfaction()));

   }
   private void upload() throws Exception {
       Plugin param = new Plugin();
       param.setPluginId("b7370981-f199-4bf1-9814-30c3ea48b1d9");
       File pluginFile = Resources.getResourceAsFile("testdata/IDEAPluginDev.zip");
       File logoFile = Resources.getResourceAsFile("testdata/idea.png");
       File apiFile = Resources.getResourceAsFile("testdata/plugin.json");
       InputStream pluginInputStream = new FileInputStream(pluginFile);
       InputStream logoInputStream = new FileInputStream(logoFile);
       InputStream apiInputStream = new FileInputStream(apiFile);

       MultipartFile pluginMultiFile = new MockMultipartFile(pluginFile.getName(), pluginFile.getName(),
           ContentType.APPLICATION_OCTET_STREAM.toString(), pluginInputStream);
       MultipartFile logoMultiFile = new MockMultipartFile(logoFile.getName(), logoFile.getName(),
           ContentType.APPLICATION_OCTET_STREAM.toString(), logoInputStream);
       MultipartFile apiMultiFile = new MockMultipartFile(apiFile.getName(), apiFile.getName(),
           ContentType.APPLICATION_OCTET_STREAM.toString(), apiInputStream);
       AFile plugins = getFile(pluginMultiFile, new PluginChecker());
       AFile logo = getFile(logoMultiFile, new IconChecker());
       AFile api = getFile(apiMultiFile, new ApiChecker());
       param.setPluginFile(plugins);
       param.setLogoFile(logo);
       param.setApiFile(api);
       param.setPluginName("plugintest");
       param.setCodeLanguage("JAVA");
       param.setPluginType("1");
       param.setVersion("1.0.0");
       param.setIntroduction("test");
       param.setUploadTime(new Date());
       param.setUser(new User("b7370981-f199-4bf1-9814-30c3ea48b1d8","hello"));
       pluginRepository.store(param);

   }
    private AFile getFile(MultipartFile file, FileChecker fileChecker) throws IOException {
        String fileAddress = fileService.saveTo(file, fileChecker);
        return new AFile(file.getOriginalFilename(), fileAddress, file.getSize());
    }

}
