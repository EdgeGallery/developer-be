package org.edgegallery.developer.service.impl.csar;

import java.io.IOException;
import org.edgegallery.developer.domain.shared.Page;
import org.edgegallery.developer.model.application.Application;
import org.edgegallery.developer.model.application.EnumAppClass;
import org.edgegallery.developer.model.resource.MepHost;
import org.edgegallery.developer.service.apppackage.csar.PackageFileCreator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;

public class PackageFileCreatorTest {
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
        Assert.assertNotNull(packageFileCreator);
    }

}
