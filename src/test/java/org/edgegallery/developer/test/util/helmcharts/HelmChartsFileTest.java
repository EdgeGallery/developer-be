package org.edgegallery.developer.test.util.helmcharts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    }

    @Test
    public void should_successfully_when_export_tgz() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/namespacetest.tgz");
        handler.load(demo.getCanonicalPath());
        String outFile = handler.exportHelmCharts();
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

        String content = handler.getContentByInnerPath("/values-no.yaml");
        Assert.assertNull(content);
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