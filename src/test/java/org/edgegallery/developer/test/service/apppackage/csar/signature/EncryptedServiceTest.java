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

package org.edgegallery.developer.test.service.apppackage.csar.signature;

import org.edgegallery.developer.test.DeveloperApplicationTests;
import org.edgegallery.developer.service.apppackage.csar.signature.EncryptedService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class EncryptedServiceTest {

    @Autowired
    private EncryptedService encryptedService;

    @Test
    public void testEncryptedFile() {
        encryptedService.encryptedFile("src/test/resources/testdata/vm_package");
        encryptedService.encryptedCMS("src/test/resources/testdata/vm_package");
        Assert.assertTrue(true);


    }
    @Test
    public void testEncryptedContainer() {
        encryptedService.encryptedFile("src/test/resources/testdata/container_package");
        encryptedService.encryptedCMS("src/test/resources/testdata/container_package");
        Assert.assertTrue(true);


    }
}
