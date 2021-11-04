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

public class LoadK8sYaml implements ILoadContainerFile {

    private final String filePath;

    LoadK8sYaml(String filePath) {
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
}
