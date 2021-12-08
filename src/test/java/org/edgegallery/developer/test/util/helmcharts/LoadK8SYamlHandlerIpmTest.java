package org.edgegallery.developer.test.util.helmcharts;

import com.google.gson.Gson;
import io.kubernetes.client.common.KubernetesObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.ImageConfig;
import org.edgegallery.developer.util.helmcharts.EgChartsYaml;
import org.edgegallery.developer.util.helmcharts.EgValuesYaml;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.edgegallery.developer.util.helmcharts.LoadK8sYamlHandlerImpl;
import org.edgegallery.developer.util.helmcharts.k8sObject.EnumKubernetesObject;
import org.edgegallery.developer.util.helmcharts.k8sObject.IContainerImage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class LoadK8SYamlHandlerIpmTest {

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
        // demo is input file name.
        Assert.assertEquals("demo", fileList.get(0).getName());
        Assert.assertEquals("", fileList.get(0).getInnerPath());
        Assert.assertEquals(3, fileList.get(0).getChildren().size());

        int count = 0;
        for (HelmChartFile file : fileList.get(0).getChildren()) {
            switch (file.getName()) {
                case "Chart.yaml":
                case "values.yaml":
                    count++;
                    break;
                case "templates":
                    for (HelmChartFile template : file.getChildren()) {
                        if (template.isFile()) {
                            Assert.assertEquals("demo.yaml", template.getName());
                            break;
                        }
                    }
                    count++;
                    break;
            }
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void should_successfully_when_export_tgz() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/battlecity_no_agent.yaml");
        handler.load(demo.getCanonicalPath());
        String outFile = handler.exportHelmChartsPackage();
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

    @Test
    public void should_successfully_when_upload_more_then_one_k8s_file() throws IOException {
        File demo1 = Resources.getResourceAsFile("testdata/helmcharts/demo.yaml");
        File demo2 = Resources.getResourceAsFile("testdata/helmcharts/demo-onlyagent.yaml");
        handler.load(demo1.getCanonicalPath(), demo2.getCanonicalPath());
        List<HelmChartFile> fileList = handler.getCatalog();
        Assert.assertEquals("demo", fileList.get(0).getName());
        int count = 0;
        for (HelmChartFile file : fileList.get(0).getChildren()) {
            switch (file.getName()) {
                case "Chart.yaml":
                case "values.yaml":
                    count++;
                    break;
                case "templates":
                    for (HelmChartFile subFile : file.getChildren()) {
                        if (subFile.isFile()) {
                            Assert.assertTrue(
                                StringUtils.containsAny(subFile.getName(), "demo.yaml", "demo-onlyagent.yaml"));
                            Assert.assertTrue(
                                StringUtils.containsAny(subFile.getName(), "demo.yaml", "demo-onlyagent.yaml"));
                        }
                    }
                    count++;
                    break;
            }
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void should_successfully_when_set_imageconfig_from_param() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        if (handler instanceof LoadK8sYamlHandlerImpl) {
            ImageConfig imageConfig = new ImageConfig();
            imageConfig.setDomainname("test_domain");
            imageConfig.setProject("test_project");
            ((LoadK8sYamlHandlerImpl) handler).setImageConfig(imageConfig);
        }
        handler.load(demo.getCanonicalPath());
        String content = handler.getContentByInnerPath("/values.yaml");
        Yaml yaml = new Yaml();
        Gson gson = new Gson();
        String json = gson.toJson(yaml.loadAs(content, Object.class));
        EgValuesYaml valuesYaml = gson.fromJson(json, EgValuesYaml.class);
        Assert.assertEquals("test_domain", valuesYaml.getImageLocation().getDomainName());
        Assert.assertEquals("test_project", valuesYaml.getImageLocation().getProject());
    }

    @Test
    public void should_successfully_when_set_chart_from_param() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getCanonicalPath());
        String content = handler.getContentByInnerPath("/Chart.yaml");
        EgChartsYaml chartsYaml = new Yaml().loadAs(content, EgChartsYaml.class);
        Assert.assertTrue(chartsYaml.getName().matches("demo-[0-9]{8}"));
    }

    @Test
    public void should_successfully_when_replace_imageurl_to_values() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/demo.yaml");
        handler.load(demo.getPath());
        List<HelmChartFile> k8sTemplates = handler.getTemplatesFile();
        EgValuesYaml defaultValues = EgValuesYaml.createDefaultEgValues(true);

        for (HelmChartFile k8sTemplate : k8sTemplates) {
            List<Object> k8s = handler.getK8sTemplateObject(k8sTemplate);
            for (Object obj : k8s) {
                Assert.assertTrue(obj instanceof KubernetesObject);
                IContainerImage containerImage = EnumKubernetesObject.of(obj);
                List<String> images = containerImage.getImages();
                if (images.size() > 0) {
                    Assert.assertEquals(String
                        .format("%s/%s/positioning_service:1.0", defaultValues.getImageLocation().getDomainName(),
                            defaultValues.getImageLocation().getProject()), images.get(0));
                }
            }
        }
    }

}
