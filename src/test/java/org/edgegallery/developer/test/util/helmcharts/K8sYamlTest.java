package org.edgegallery.developer.test.util.helmcharts;

import java.io.File;
import java.io.IOException;
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

public class K8sYamlTest {

    private IContainerFileHandler handler;

    @Before
    public void before() {
        handler = LoadContainerFileFactory.createLoader("xxxx.yaml");
    }

    @After
    public void clean() {
        handler.clean();
    }

    @Test
    public void should_successfully_when_load_k8syaml() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> fileList = handler.getCatalog();
        Assert.assertFalse(fileList.isEmpty());
        Assert.assertEquals(1, fileList.size());
        Assert.assertEquals("helm_charts", fileList.get(0).getName());
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
        String outFile = handler.exportHelmCharts("test");
        Assert.assertNotNull(outFile);
        Assert.assertEquals("test.tgz", new File(outFile).getName());
        FileUtils.deleteDirectory(new File("./helmCharts"));
    }

    @Test
    public void should_successfully_when_get_file_content() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> fileList = handler.getCatalog();
        String content = handler.getContentByInnerPath("/charts.yaml");
        Assert.assertNotNull(content);
        content = handler.getContentByInnerPath("\\templates");
        Assert.assertNull(content);
        content = handler.getContentByInnerPath("\\values.yaml");
        Assert.assertNotNull(content);
        content = handler.getContentByInnerPath("\\values-no.yaml");
        Assert.assertNull(content);
    }

    @Test
    public void should_successfully_when_modify_content() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());

        String content = handler.getContentByInnerPath("\\values.yaml");
        Assert.assertNotNull(content);

        Assert.assertTrue(handler.modifyFileByPath("\\values.yaml", "modified data."));
        String modifiedContent = handler.getContentByInnerPath("\\values.yaml");

        Assert.assertEquals("modified data.", modifiedContent);
    }
}
