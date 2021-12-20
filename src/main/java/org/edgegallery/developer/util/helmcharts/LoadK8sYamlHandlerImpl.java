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
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.exception.DeveloperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadK8sYamlHandlerImpl extends AbstractContainerFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadK8sYamlHandlerImpl.class);

    private static final String MEP_TEMPLATES_PATH = System.getProperty("user.dir")
        + "/configs/chart_template/templates/eg_template";

    @Override
    public void load(String... filePaths) throws IOException {
        if (filePaths.length < 1) {
            LOGGER.error("Please upload one HelmCharts file.");
            throw new DeveloperException("Failed to read k8s config.");
        }
        boolean ret = verify(filePaths);
        if (!ret) {
            LOGGER.error("Can not parse this files by kubernetes-client.");
            return;
        }

        String helmChartsName = getHelmChartName(filePaths[0]);

        // create helm-charts temp dir
        genTempWorkspace(helmChartsName);

        genDefaultValuesYaml();

        genDefaultChartYaml(helmChartsName);

        try {
            Files.createDirectory(Paths.get(helmChartsDir, "templates"));
            createMepTemplates();
            for (String filePath : filePaths) {
                addTemplate(filePath);
            }
        } catch (IOException e) {
            FileUtils.deleteDirectory(new File(workspace));
            workspace = null;
            throw new DeveloperException("Failed to read k8s config. msg:" + e.getMessage());
        }

        replaceAllImageUrlToValues();
    }

    private void genDefaultChartYaml(String helmChartsName) throws IOException {
        // create charts.yaml
        EgChartsYaml defaultCharts = this.getDefaultChart(helmChartsName);
        Path chartsYaml = Files.createFile(Paths.get(helmChartsDir, "Chart.yaml"));
        FileUtils.writeByteArrayToFile(chartsYaml.toFile(), defaultCharts.getContent().getBytes(), false);
    }

    private void genDefaultValuesYaml() throws IOException {
        // create values.yaml
        EgValuesYaml defaultValues = this.getDefaultValues();
        Path valuesYaml = Files.createFile(Paths.get(helmChartsDir, "values.yaml"));
        FileUtils.writeByteArrayToFile(valuesYaml.toFile(), defaultValues.getContent().getBytes(), false);
    }

    private String getHelmChartName(String inputFile) {
        File orgFile = new File(inputFile);
        String fileName = orgFile.getName();
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
    }

    private void genTempWorkspace(String helmChartsName) throws IOException {
        Path tempDir = Files.createTempDirectory("eg-helmcharts-");
        workspace = tempDir.toString();
        Path helmChartPath = Files.createDirectory(Paths.get(workspace, helmChartsName));
        helmChartsDir = helmChartPath.toString();
    }

    private void createMepTemplates() throws IOException {
        File egMepTemplatePath = new File(MEP_TEMPLATES_PATH);
        if (egMepTemplatePath.exists() && egMepTemplatePath.isDirectory()) {
            Path k8sYaml = Files.createDirectory(Paths.get(helmChartsDir, "templates", egMepTemplatePath.getName()));
            FileUtils.copyDirectory(egMepTemplatePath, k8sYaml.toFile());
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
        } catch (IOException e) {
            LOGGER.error("Can not parse this yaml to k8s configs. msg:{}", e.getMessage());
            return false;
        }
        return true;
    }
}
