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

package org.edgegallery.developer.test.util.helmcharts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.service.apppackage.csar.appdconverter.CustomRepresenter;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class HelmChartsFileTest {

    private IContainerFileHandler handler;

    @Before
    public void before() {
        handler = LoadContainerFileFactory.createLoader("xxxx.tgz");
    }

    @After
    public void clean() {
        handler.clean();
    }

    @Test
    public void should_successfully_when_load_helm_charts_file() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> files = handler.getCatalog();
        Assert.assertEquals(1, files.size());
    }

    @Test
    public void should_successfully_when_export_tgz() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo.getCanonicalPath());
        String outFile = handler.exportHelmChartsPackage();
        Assert.assertNotNull(outFile);
    }

    @Test
    public void should_successfully_when_modify_content() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo.getCanonicalPath());

        String content = handler.getContentByInnerPath("/values.yaml");
        Assert.assertNotNull(content);

        Assert.assertTrue(handler.modifyFileByPath("/values.yaml", "modified data."));
        String modifiedContent = handler.getContentByInnerPath("/values.yaml");

        Assert.assertEquals("modified data.", modifiedContent);
    }

    @Test
    public void should_successfully_when_get_file_content() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> fileList = handler.getCatalog();
        List<String> files = new ArrayList<>();
        List<String> dirs = new ArrayList<>();
        deep(fileList, files, dirs);

        for (String file : files) {
            String content = handler.getContentByInnerPath(file);
            Assert.assertNotNull(content);
        }
        for (String dir : dirs) {
            String content = handler.getContentByInnerPath(dir);
            Assert.assertNull(content);
        }
        String content = handler.getContentByInnerPath("/templates/eg_template/namespace-config.yaml");
        Assert.assertNotNull(content);
    }

    private void deep(List<HelmChartFile> root, List<String> files, List<String> dirs) {
        for (HelmChartFile file : root) {
            if (file.getChildren() != null) {
                dirs.add(file.getInnerPath());
                deep(file.getChildren(), files, dirs);
            } else {
                files.add(file.getInnerPath());
            }
        }
    }

    @Test
    public void should_failed_when_upload_more_then_one_helmcharts() throws IOException {
        File demo1 = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        File demo2 = Resources.getResourceAsFile("testdata/helmcharts/namespacetest2.tgz");
        try {
            handler.load(demo1.getCanonicalPath(), demo2.getCanonicalPath());
            Assert.fail();
        } catch (DeveloperException e) {
            Assert.assertEquals("Just support to upload one HelmCharts.", e.getMessage());
        }
    }

    @Test
    public void should_successfully_when_merge_input_values_and_default() throws IOException {
        File demo1 = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo1.getCanonicalPath());
        String content = handler.getContentByInnerPath("/values.yaml");
        Yaml yaml = new Yaml(new SafeConstructor(), new CustomRepresenter());
        Map<String, Object> map = (Map) yaml.load(content);
        Assert.assertTrue(map.containsKey("appconfig"));
        Assert.assertTrue(map.containsKey("replicaCount"));
    }

    @Test
    public void should_successfully_when_get_all_k8s_objects() throws IOException {
        File demo1 = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo1.getCanonicalPath());
        List<Object> objects = handler.getAllK8sObject();
        Assert.assertEquals(7, objects.size());
    }
}
