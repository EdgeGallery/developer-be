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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.exception.DeveloperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class K8sYaml extends ContainerFileHandlerImp {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sYaml.class);

    @Setter
    private boolean hasMep = true;

    @Override
    public void load(String filePath) throws IOException {
        if (!verify(filePath)) {
            return;
        }
        try {
            // create helm-charts temp dir
            Path tempDir = Files.createTempDirectory("eg-helmcharts-");
            workspace = tempDir.toString();
            File orgFile = new File(filePath);
            String fileName = orgFile.getName();
            Path helmChartPath = Files.createDirectory(Paths.get(workspace,
                fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName));
            helmChartsDir = helmChartPath.toString();

            // create template dir and copy k8s yaml to template
            Files.createDirectory(Paths.get(helmChartsDir, "templates"));
            Path k8sYaml = Files.createFile(Paths.get(helmChartsDir, "templates", orgFile.getName()));
            FileUtils.copyFile(orgFile, k8sYaml.toFile());

            // create values.yaml
            EgValuesYaml defaultValues = EgValuesYaml.createDefaultEgValues();
            Path valuesYaml = Files.createFile(Paths.get(helmChartsDir, "values.yaml"));
            FileUtils.writeByteArrayToFile(valuesYaml.toFile(), defaultValues.getContent().getBytes(), false);

            // create charts.yaml
            EgChartsYaml defaultCharts = EgChartsYaml.createDefaultCharts();
            Path chartsYaml = Files.createFile(Paths.get(helmChartsDir, "charts.yaml"));
            FileUtils.writeByteArrayToFile(chartsYaml.toFile(), defaultCharts.getContent().getBytes(), false);

        } catch (IOException e) {
            FileUtils.deleteDirectory(new File(workspace));
            workspace = null;
            throw new DeveloperException("Failed to read k8s config. config:" + filePath);
        }
    }

    // try to parse k8s file with kubernetes.client
    boolean verify(String filePath) {
        try {
            Yaml.loadAll(filePath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Can not parse this yaml to k8s configs. filePath:{}, msg:{}", filePath, e.getMessage());
            throw new DeveloperException("Failed to read k8s config. error message:" + e.getMessage());
        }
    }
}
