package org.edgegallery.developer.util;

import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.service.apppackage.signature.EncryptedService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class encryptedServiceTest {

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
