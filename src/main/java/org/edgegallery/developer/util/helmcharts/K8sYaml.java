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

package org.edgegallery.developer.util.helmcharts;

import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class K8sYaml implements IContainerFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sYaml.class);

    private final String filePath;

    @Setter
    private boolean hasMep = true;

    K8sYaml(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("D:\\test\\face2\\templates\\face_recognition_with_mepagent4_1.yaml");
        List v1Service = (ArrayList) Yaml.loadAll(file);
        for (Object service : v1Service) {
            System.out.println(service.getClass());
        }

        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();

    }

    @Override
    public void load(String filePath) {
        if (!verify(filePath)) {
            return;
        }
        EgValuesYaml defaultValues = EgValuesYaml.createDefaultEgValues();

        String content = defaultValues.getContent();
    }

    @Override
    public void getCatalog() {

    }

    @Override
    public String exportHelmCharts(String outPath) {
        return null;
    }

    @Override
    public void modifyFileByPath(String filePath, String content) {

    }

    @Override
    public void addFile(String filePath, String content) {

    }

    boolean verify(String filePath) {
        try {
            List v1Service = (ArrayList) Yaml.loadAll(filePath);
            for (Object service : v1Service) {
                System.out.println(service.getClass());
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Can not parse this yaml to k8s configs. filePath:{}, msg:{}", filePath, e.getMessage());
        }
        return false;
    }
}
