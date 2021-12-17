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

package org.edgegallery.developer.test.util.releasedpackage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.model.apppackage.AppPackage;
import org.edgegallery.developer.model.releasedpackage.AppPkgFile;
import org.edgegallery.developer.util.releasedpackage.ReleasedPackageUtil;
import org.junit.Assert;
import org.junit.Test;

public class ReleasedPackageUtilTest {

    @Test
    public void testDecompressAppPkgSuccess() throws IOException {
        File pluginFile = Resources.getResourceAsFile("testdata/bonita.zip");
        AppPackage appPackage = new AppPackage();
        String pkgId = UUID.randomUUID().toString();
        appPackage.setId(UUID.randomUUID().toString());
        appPackage.setPackageFileName("bonita.zip");
        String pkgFilePath = pluginFile.getCanonicalPath();
        if (pkgFilePath.contains("D:")) {
            pkgFilePath = pkgFilePath.substring(2);
        } else {
            pkgFilePath = pkgFilePath.substring(8);
        }
        appPackage.setPackageFilePath(pkgFilePath);
        String zipDir = ReleasedPackageUtil.decompressAppPkg(appPackage, pluginFile.getCanonicalPath(), pkgId);
        Assert.assertNotNull(zipDir);
    }

    @Test
    public void testGetCatalogueSuccess() throws IOException {
        File pluginFile = Resources.getResourceAsFile("testdata/container_package");
        List<AppPkgFile> zipDir = ReleasedPackageUtil.getCatalogue(pluginFile.getCanonicalPath());
        Assert.assertNotNull(zipDir);
    }

    @Test
    public void testGetContentByInnerPathSuccess() throws IOException {
        File pluginFile = Resources.getResourceAsFile("testdata/container_package");
        String content = ReleasedPackageUtil.getContentByInnerPath("/namespacetest.mf", pluginFile.getCanonicalPath());
        Assert.assertNotNull(content);
    }

    @Test
    public void testEditContentByInnerPathSuccess() throws IOException {
        File pluginFile = Resources.getResourceAsFile("testdata/container_package");
        boolean content = ReleasedPackageUtil
            .modifyFileByPath("/namespacetest.mf", "test", pluginFile.getCanonicalPath());
        Assert.assertTrue(content);
    }

    @Test
    public void testGetReleasedPkgDecompressPathSuccess() throws IOException {
        Assert.assertNotNull(ReleasedPackageUtil.getReleasedPkgDecompressPath("pkgId"));
    }

    @Test
    public void testGetAppPkgDecompressPathSuccess() throws IOException {
        Assert.assertNotNull(ReleasedPackageUtil.getAppPkgDecompressPath("appId", "pkgId"));
    }

    @Test
    public void testGetAppPkgPathSuccess() throws IOException {
        Assert.assertNotNull(ReleasedPackageUtil.getAppPkgPath("pkgId"));
    }

}
