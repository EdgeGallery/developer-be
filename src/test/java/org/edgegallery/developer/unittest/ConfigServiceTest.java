/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.model.DeployPlatformConfig;
import org.edgegallery.developer.model.TestApp;
import org.edgegallery.developer.request.AppRequestParam;
import org.edgegallery.developer.response.FormatRespDto;
import org.edgegallery.developer.service.ConfigService;
import org.edgegallery.developer.util.DeveloperFileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class ConfigServiceTest {

    @Autowired
    private ConfigService configService;


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
    public void testConfigDeployPlatform() throws Exception {

        DeployPlatformConfig deployPlatformConfig = new DeployPlatformConfig();
        deployPlatformConfig.setIsVirtualMachine(false);
        deployPlatformConfig.setVirtualMachineUrl("url");
        Either<FormatRespDto, DeployPlatformConfig> res = configService.configDeployPlatform(deployPlatformConfig);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testConfigDeployPlatformError() throws Exception {
        DeployPlatformConfig deployPlatformConfig = new DeployPlatformConfig();
        deployPlatformConfig.setIsVirtualMachine(true);
        deployPlatformConfig.setVirtualMachineUrl("");
        Either<FormatRespDto, DeployPlatformConfig> res = configService.configDeployPlatform(deployPlatformConfig);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testGetConfigDeployPlatform() throws Exception {
        Either<FormatRespDto, DeployPlatformConfig> res = configService.getConfigDeployPlatform();
        Assert.assertTrue(res.isRight());
    }

}
