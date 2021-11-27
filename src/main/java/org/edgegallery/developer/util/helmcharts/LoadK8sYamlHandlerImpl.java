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
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.exception.DeveloperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadK8sYamlHandlerImpl extends AbstractContainerFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadK8sYamlHandlerImpl.class);

    @Setter
    private boolean hasMep = true;

    @Setter
    private Map<String, Object> values;

    @Override
    public void load(String... filePaths) throws IOException {
        if (filePaths.length < 1) {
            LOGGER.error("Please upload one HelmCharts file.");
            throw new DeveloperException("Failed to read k8s config.");
        }
        if (!verify(filePaths)) {
            LOGGER.error("Can not parse this files by kubernetes-client.");
            return;
        }

        // create helm-charts temp dir
        String firstFile = filePaths[0];
        Path tempDir = Files.createTempDirectory("eg-helmcharts-");
        workspace = tempDir.toString();
        File orgFile = new File(firstFile);
        String fileName = orgFile.getName();
        Path helmChartPath = Files.createDirectory(
            Paths.get(workspace, fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName));
        helmChartsDir = helmChartPath.toString();

        // create values.yaml
        EgValuesYaml defaultValues = EgValuesYaml.createDefaultEgValues();
        Path valuesYaml = Files.createFile(Paths.get(helmChartsDir, "values.yaml"));
        FileUtils.writeByteArrayToFile(valuesYaml.toFile(), defaultValues.getContent().getBytes(), false);

        // create charts.yaml
        EgChartsYaml defaultCharts = EgChartsYaml.createDefaultCharts();
        Path chartsYaml = Files.createFile(Paths.get(helmChartsDir, "Chart.yaml"));
        FileUtils.writeByteArrayToFile(chartsYaml.toFile(), defaultCharts.getContent().getBytes(), false);

        try {
            Files.createDirectory(Paths.get(helmChartsDir, "templates"));
            for (String filePath : filePaths) {
                addTemplate(filePath);
            }
        } catch (IOException e) {
            FileUtils.deleteDirectory(new File(workspace));
            workspace = null;
            throw new DeveloperException("Failed to read k8s config. msg:" + e.getMessage());
        }
    }

    private void setValues(String valuesFilePath) throws IOException {
        values = new HashMap<>();
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        HashMap<String, Object> valuesMap = yaml.load(FileUtils.readFileToString(new File(valuesFilePath), "utf-8"));
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
            Object val = entry.getValue();
            // if (val instanceof Map)
        }
    }

    private void addTemplate(String filePath) throws IOException {
        File orgFile = new File(filePath);
        // create template dir and copy k8s yaml to template
        Path k8sYaml = Files.createFile(Paths.get(helmChartsDir, "templates", orgFile.getName()));
        FileUtils.copyFile(orgFile, k8sYaml.toFile());
    }

    // try to parse k8s file with kubernetes.client
    private boolean verify(String... filePaths) {
        try {
            for (String filePath : filePaths) {
                Yaml.loadAll(filePath);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Can not parse this yaml to k8s configs. msg:{}", e.getMessage());
            return false;
        }
    }
}
