package org.edgegallery.developer.test.util.helmcharts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoadK8SYamlHandlerIpmTest {

    private IContainerFileHandler handler;

    @Before
    public void before() {
        handler = LoadContainerFileFactory.createLoader("xxxx.yaml");
    }

    @After
    public void clean() {
        // handler.clean();
    }

    @Test
    public void should_successfully_when_load_k8syaml() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> fileList = handler.getCatalog();
        Assert.assertFalse(fileList.isEmpty());
        Assert.assertEquals(1, fileList.size());
        // demo is input file name.
        Assert.assertEquals("demo", fileList.get(0).getName());
        Assert.assertEquals("", fileList.get(0).getInnerPath());
        Assert.assertEquals(3, fileList.get(0).getChildren().size());

        int count = 0;
        for (HelmChartFile file : fileList.get(0).getChildren()) {
            switch (file.getName()) {
                case "charts.yaml":
                case "values.yaml":
                    count++;
                    break;
                case "templates":
                    Assert.assertEquals(1, file.getChildren().size());
                    count++;
                    break;
            }
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void should_successfully_when_export_tgz() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());
        String outFile = handler.exportHelmCharts();
        Assert.assertNotNull(outFile);
    }

    @Test
    public void should_successfully_when_get_file_content() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
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

        String content = handler.getContentByInnerPath("/values-no.yaml");
        Assert.assertNull(content);
    }

    @Test
    public void should_successfully_when_modify_content() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());

        String content = handler.getContentByInnerPath("/values.yaml");
        Assert.assertNotNull(content);

        Assert.assertTrue(handler.modifyFileByPath("/values.yaml", "modified data."));
        String modifiedContent = handler.getContentByInnerPath("/values.yaml");

        Assert.assertEquals("modified data.", modifiedContent);
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
}
