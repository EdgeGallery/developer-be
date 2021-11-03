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

package org.edgegallery.developer.test.service.apppackage.csar;

import java.io.IOException;
import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.service.apppackage.csar.PackageFileCreator;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DeveloperApplicationTests.class)
public class PackageFileCreatorTest extends AbstractJUnit4SpringContextTests {

    @Before
    public void setApplicationContext() {
        SpringContextUtil.setApplicationContext(applicationContext);

    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testPackageFileCreator() throws IOException {
        Application application = new Application();
        application.setId("cda40588-7ace-4fe0-a2a4-cc7d2d845fda");
        application.setAppClass(EnumAppClass.VM);
        application.setCreateTime("2021-10-25");
        application.setDescription("测试");
        application.setName("vmTest");
        application.setVersion("v1.0");
        application.setProvider("edgegallery");
        application.setArchitecture("X86");
        PackageFileCreator packageFileCreator =new PackageFileCreator(application, "ef874bc2-b32f-4295-8489-5409f9742242");

        boolean res = packageFileCreator.copyPackageTemplateFile();
        Assert.assertTrue(res);
        packageFileCreator.configMfFile();
        packageFileCreator.configMetaFile();
        packageFileCreator.configVnfdMeta();
        String compressPath = packageFileCreator.PackageFileCompress();
        Assert.assertNotNull(compressPath);
    }


}
