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

package org.edgegallery.developer.test.service.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.apache.http.entity.ContentType;
import org.apache.ibatis.io.Resources;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.exception.EntityNotFoundException;
import org.edgegallery.developer.filter.security.AccessUserUtil;
import org.edgegallery.developer.model.common.Page;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.plugin.AFile;
import org.edgegallery.developer.model.plugin.Plugin;
import org.edgegallery.developer.model.plugin.PluginDto;
import org.edgegallery.developer.service.plugin.PluginFileService;
import org.edgegallery.developer.service.plugin.impl.PluginService;
import org.edgegallery.developer.service.plugin.impl.PluginServiceFacade;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.util.filechecker.ApiChecker;
import org.edgegallery.developer.util.filechecker.FileChecker;
import org.edgegallery.developer.util.filechecker.IconChecker;
import org.edgegallery.developer.util.filechecker.PluginChecker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class PluginServiceTest {

    @Autowired
    private PluginServiceFacade pluginServiceFacade;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private PluginFileService pluginFileService;

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
    public void testGetPluginNameSuccess() {
        String name = pluginServiceFacade.getPluginName("586224da-e1a2-4893-a5b5-bf766fdfb8c8");
        Assert.assertNotNull(name);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdatePluginBad() throws Exception {
        AccessUserUtil.setUser("123", "test-user");
        Plugin plugin = new Plugin();
        plugin.setPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8c8");
        plugin.setUser(new User("234", "test-111"));
        try {
            pluginServiceFacade.updatePlugin(plugin, null, null, null);
        } catch (InvocationException e) {
            Assert.assertEquals(400, e.getStatusCode());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void getPluginList() {
        Page<PluginDto> page1 = pluginServiceFacade.query("1", "", "", 10, 0);

        Assert.assertNotNull(page1);
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void delPlugin() {
        pluginServiceFacade
            .deleteByPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8c7", "f24ea0a2-d8e6-467c-8039-94f0d29bac43");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void delPluginByBadUserId() {
        pluginServiceFacade
            .deleteByPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8c7", "d24ea0a2-d8e6-467c-8039-94f0d29bac43");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetPluginName() {
        try {
            pluginServiceFacade.getPluginName("586224da-e1a2-4893-a5b5-bf766fdfb8c9");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("cannot find the plugin with id 586224da-e1a2-4893-a5b5-bf766fdfb8c9", e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void delPluginByBadId() {
        try {
            pluginServiceFacade
                .deleteByPluginId("586224da-e1a2-4893-a5b5-bf766fdfb8ss", "586224da-e1a2-4893-a5b5-bf766fdfb8c9");
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("cannot find the plugin with id 586224da-e1a2-4893-a5b5-bf766fdfb8ss", e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void downloadPlugin() throws Exception {
        upload();
        InputStream stream = pluginServiceFacade.downloadFile("b7370981-f199-4bf1-9814-30c3ea48b1d9");
        Assert.assertNotNull(stream);
        pluginServiceFacade
            .deleteByPluginId("b7370981-f199-4bf1-9814-30c3ea48b1d9", "b7370981-f199-4bf1-9814-30c3ea48b1d8");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void downloadPluginLogo() throws Exception {
        upload();
        InputStream stream = pluginServiceFacade.downloadLogo("b7370981-f199-4bf1-9814-30c3ea48b1d9");
        Assert.assertNotNull(stream);
        pluginServiceFacade
            .deleteByPluginId("b7370981-f199-4bf1-9814-30c3ea48b1d9", "b7370981-f199-4bf1-9814-30c3ea48b1d8");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void downloadPluginApi() throws Exception {
        upload();
        InputStream stream = pluginServiceFacade.downloadApiFile("b7370981-f199-4bf1-9814-30c3ea48b1d9");
        Assert.assertNotNull(stream);
        pluginServiceFacade
            .deleteByPluginId("b7370981-f199-4bf1-9814-30c3ea48b1d9", "b7370981-f199-4bf1-9814-30c3ea48b1d8");
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void updatePluginScore() throws Exception {
        upload();
        User user = new User("b7370981-f199-4bf1-9814-30c3ea48b1d8", "hello");
        Plugin either = pluginServiceFacade.mark("b7370981-f199-4bf1-9814-30c3ea48b1d9", 5, user);
        Assert.assertEquals(Float.toString(5f), Float.toString(either.getSatisfaction()));
        pluginServiceFacade.deleteByPluginId(either.getPluginId(), user.getUserId());
    }

    private void upload() throws Exception {
        Plugin param = new Plugin();
        param.setPluginId("b7370981-f199-4bf1-9814-30c3ea48b1d9");
        File pluginFile = Resources.getResourceAsFile("testdata/IDEAPluginDev.zip");
        File logoFile = Resources.getResourceAsFile("testdata/idea.png");
        File apiFile = Resources.getResourceAsFile("testdata/template-zoneminder.md");
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
        param.setUser(new User("b7370981-f199-4bf1-9814-30c3ea48b1d8", "hello"));
        pluginServiceFacade.publish(param, pluginMultiFile, logoMultiFile, apiMultiFile);

    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker) throws IOException {
        String fileAddress = pluginFileService.saveTo(file, fileChecker);
        return new AFile(file.getOriginalFilename(), fileAddress, file.getSize());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testUpdatePlugin() throws Exception {
        Plugin plugin = new Plugin();
        plugin.setPluginId("test");
        try {
            pluginServiceFacade.updatePlugin(plugin, null, null, null);
        } catch (EntityNotFoundException e) {
            Assert.assertEquals("cannot find the plugin with id test", e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetFileContentFail() throws Exception {
        try {
            File file = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
            pluginFileService.get(file.getCanonicalPath(), "TOSCA.meta");
        } catch (FileNotFoundException e) {
            Assert.assertEquals("TOSCA.meta not found", e.getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetFileContentSuccess() throws Exception {
        File file = Resources.getResourceAsFile("testdata/face_recognition1.4.csar");
        String content = pluginFileService
            .get(file.getCanonicalPath(), "face_reconigition_app/Artifacts/Docs/face_recognition.md");
        Assert.assertNotNull(content);

    }

}
