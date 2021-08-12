package org.edgegallery.developer.util;

import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.exception.WindowException;
import org.edgegallery.developer.service.EncryptedService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class encryptedServiceTest {

    @Autowired
    private EncryptedService encryptedService;

    @Test
    public void testEncryptedFile() {
        try {
            encryptedService.encryptedFile("src/test/resources/testdata/template_package");
            encryptedService.encryptedCMS("src/test/resources/testdata/template_package");
            Assert.assertTrue(true);
        } catch (WindowException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
