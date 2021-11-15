package org.edgegallery.developer.test.util.helmcharts;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class K8sYamlTest {

    private IContainerFileHandler handler;

    @Before
    public void before() {
        handler = LoadContainerFileFactory.createLoader("xxxx.yaml");
    }

    @Test
    public void should_successfully_when_load_k8syaml() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        System.out.println(demo.getPath());
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> fileList = handler.getCatalog();
        Assert.assertFalse(fileList.isEmpty());
        Assert.assertEquals(1, fileList.size());
        Assert.assertEquals("helm_charts", fileList.get(0).getName());
        Assert.assertEquals("", fileList.get(0).getPath());
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
}
