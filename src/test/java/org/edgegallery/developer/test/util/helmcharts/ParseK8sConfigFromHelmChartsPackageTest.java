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
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
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
        List<HelmChartFile> files = handler.getCatalog();
        List<Object> k8s = handler.getK8sTemplateObject("/templates/face_recognition_with_mepagent4_1.yaml");
        for (Object obj : k8s) {
            Assert.assertTrue(obj instanceof KubernetesObject);
            KubernetesObject k8sObj = (KubernetesObject) obj;
            k8sObj.getMetadata().getName();
            System.out.println(obj.getClass() + ":" + k8sObj.getKind());
        }
        Assert.assertEquals(4, k8s.size());
    }
}
