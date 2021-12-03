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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.edgegallery.developer.exception.DeveloperException;
import org.edgegallery.developer.service.apppackage.converter.CustomRepresenter;
import org.edgegallery.developer.util.CompressFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

public class LoadHelmChartsFileHandlerImpl extends AbstractContainerFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadHelmChartsFileHandlerImpl.class);

    @Override
    public void load(String... filePaths) throws IOException {
        if (filePaths.length != 1) {
            LOGGER.error("Just support to upload one HelmCharts.");
            throw new DeveloperException("Just support to upload one HelmCharts.");
        }
        String filePath = filePaths[0];
        try {
            // create helm-charts temp dir
            Path tempDir = Files.createTempDirectory("eg-helmcharts-");
            workspace = tempDir.toString();

            File orgFile = new File(filePath);
            String fileName = orgFile.getName();
            Path targetFilePath = Files.createFile(Paths.get(workspace, fileName));
            FileUtils.copyFile(orgFile, targetFilePath.toFile());

            Path helmChartPath = Files.createDirectory(Paths
                .get(workspace, fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName));
            helmChartsDir = helmChartPath.toString();

            // unzip file
            if (!CompressFileUtils.decompress(targetFilePath.toString(), workspace)) {
                return;
            }

            mergeValuesYaml();
        } catch (IOException e) {
            throw new DeveloperException("Failed to read k8s config. config:" + filePath);
        }
    }

    private void mergeValuesYaml() throws IOException {
        // get input values.yaml
        HelmChartFile inputValues = this.getValuesYaml();
        String inputValuesYaml = null;
        if (inputValues == null) {
            createDefaultValuesYaml();
            return;
        }
        inputValuesYaml = inputValues.getContent();

        // default values.yaml
        EgValuesYaml defaultValues = this.getDefaultValues();
        String valuesYaml = defaultValues.getContent();

        // merge input values.yaml and default values.yaml
        Yaml yaml = new Yaml(new SafeConstructor(), new CustomRepresenter());
        Object mergedYamlObj = mergeObject(yaml.load(valuesYaml), yaml.load(inputValuesYaml));
        String mergedYaml = yaml.dumpAs(mergedYamlObj, Tag.MAP, DumperOptions.FlowStyle.BLOCK);

        // save to
        this.modifyFileByPath(inputValues.getInnerPath(), mergedYaml);
    }

    private void createDefaultValuesYaml() throws IOException {
        // create values.yaml
        EgValuesYaml defaultValues = this.getDefaultValues();
        Path valuesYaml = Files.createFile(Paths.get(helmChartsDir, "values.yaml"));
        FileUtils.writeByteArrayToFile(valuesYaml.toFile(), defaultValues.getContent().getBytes(), false);
    }

    // merge the object, back replaces front
    private Object mergeObject(Object arg1, Object arg2) {
        if (arg1 instanceof Map && arg2 instanceof Map) {
            // back merge to front, return front.
            Map<String, Object> map1 = (Map) arg1;
            Map<String, Object> map2 = (Map) arg2;
            for (Map.Entry<String, Object> entry : map2.entrySet()) {
                if (map1.containsKey(entry.getKey())) {
                    Object merged = mergeObject(map1.get(entry.getKey()), entry.getValue());
                    map1.put(entry.getKey(), merged);
                } else {
                    map1.put(entry.getKey(), entry.getValue());
                }
            }
            return arg1;
        } else {
            // back replaces front
            return arg2;
        }
    }

}
