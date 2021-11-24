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

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.helmcharts.k8sObject.EnumKubernetesObject;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.edgegallery.developer.util.helmcharts.k8sObject.IContainerImage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParseK8sConfigFromHelmChartsPackageTest {

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
    public void should_successfully_when_read_k8s_config_from_helmcharts() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/face2.tgz");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> k8sTemplates = handler.getTemplatesFile();
        for (HelmChartFile k8sTemplate : k8sTemplates) {
            List<Object> k8s = handler.getK8sTemplateObject(k8sTemplate);
            for (Object obj : k8s) {
                Assert.assertTrue(obj instanceof KubernetesObject);
                if (obj instanceof V1Pod) {
                    V1Pod pod = (V1Pod) obj;
                    Objects.requireNonNull(pod.getSpec()).getContainers().get(0).getImage();
                }
                KubernetesObject k8sObj = (KubernetesObject) obj;
                Assert.assertNotNull(k8sObj.getKind());
            }
        }
    }

    @Test
    public void should_successfully_when_get_images_from_pod() throws IOException {
        File demo = Resources.getResourceAsFile("testdata/helmcharts/face2.tgz");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> k8sTemplates = handler.getTemplatesFile();
        for (HelmChartFile k8sTemplate : k8sTemplates) {
            List<Object> k8s = handler.getK8sTemplateObject(k8sTemplate);
            for (Object obj : k8s) {
                Assert.assertTrue(obj instanceof KubernetesObject);
                IContainerImage containerImage = EnumKubernetesObject.of(obj);
                List<String> images = containerImage.getImages();
                if (obj instanceof V1Pod) {
                    Assert.assertNotNull(images);
                    Assert.assertEquals(4, images.size());
                    Assert.assertEquals("192.168.102.132/appstore/face_recognition_app:v1.4", images.get(0));
                }
            }
        }
    }

    @Test
    public void should_successfully_when_get_images_from_deployment() throws IOException {
        handler = LoadContainerFileFactory.createLoader("xxx.yaml");
        File demo = Resources.getResourceAsFile("testdata/helmcharts/deployment.yaml");
        handler.load(demo.getCanonicalPath());
        List<HelmChartFile> k8sTemplates = handler.getTemplatesFile();
        for (HelmChartFile k8sTemplate : k8sTemplates) {
            List<Object> k8s = handler.getK8sTemplateObject(k8sTemplate);
            for (Object obj : k8s) {
                Assert.assertTrue(obj instanceof KubernetesObject);
                IContainerImage containerImage = EnumKubernetesObject.of(obj);
                List<String> images = containerImage.getImages();
                if (obj instanceof V1Deployment) {
                    Assert.assertNotNull(images);
                    Assert.assertEquals(1, images.size());
                    Assert.assertEquals("nginx:1.14.2", images.get(0));
                }
            }
        }
    }
}
