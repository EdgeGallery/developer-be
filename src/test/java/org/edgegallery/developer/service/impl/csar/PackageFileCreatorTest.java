package org.edgegallery.developer.service.impl.csar;

import java.io.IOException;
import org.edgegallery.developer.DeveloperApplicationTests;
import org.edgegallery.developer.config.ApplicationContext;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.service.apppackage.csar.PackageFileCreator;
import org.edgegallery.developer.util.SpringContextUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextAware;
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
