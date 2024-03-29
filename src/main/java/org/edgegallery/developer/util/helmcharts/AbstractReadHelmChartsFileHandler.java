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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReadHelmChartsFileHandler extends AbstractDefaultFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReadHelmChartsFileHandler.class);

    protected String workspace;

    protected String helmChartsDir;

    private Map<String, Object> valuesMap;

    @Override
    public List<HelmChartFile> getCatalog() {
        if (helmChartsDir == null) {
            return Collections.emptyList();
        }
        File root = new File(helmChartsDir);
        try {
            return deepReadDir(new ArrayList<>(), root);
        } catch (IOException e) {
            LOGGER.error("Failed to get catalog. maybe read file error.");
        }
        return Collections.emptyList();
    }

    public List<Object> getK8sTemplateObject(HelmChartFile innerFile) {
        if (valuesMap == null) {
            this.valuesMap = getValuesMapFromYaml();
        }
        String content = this.getContentByInnerPath(innerFile.getInnerPath());

        // replace values
        content = replaceValuesInTemplateFile(content, this.valuesMap);
        content = commentedLogicCodeInContent(content);
        try {
            return Yaml.loadAll(content);
        } catch (IOException e) {
            LOGGER.error("yaml file {} Failed to parse k8s file.{}", innerFile.getInnerPath(), e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<Object> getAllK8sObject() {
        List<Object> k8sObjects = new ArrayList<>();
        List<HelmChartFile> k8sTemplates = this.getTemplatesFile();
        for (HelmChartFile k8sTemplate : k8sTemplates) {
            k8sObjects.addAll(this.getK8sTemplateObject(k8sTemplate));
        }
        return k8sObjects;
    }

    HelmChartFile getValuesYaml() {
        String innerPath = "/values.yaml";
        return getInnerFileByPath(innerPath);
    }

    HelmChartFile getChartYaml() {
        String innerPath = "/chart.yaml";
        return getInnerFileByPath(innerPath);
    }

    private HelmChartFile getInnerFileByPath(String innerPath) {
        try {
            Path innerFilePath = Paths.get(helmChartsDir, innerPath);
            File file = innerFilePath.toFile();
            if (file.exists()) {
                List<HelmChartFile> files = deepReadDir(new ArrayList<>(), file);
                return files.get(0);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to get the inner-file, please to check {} is really exist.", innerPath);
        }
        return null;
    }

    private List<HelmChartFile> deepReadDir(List<HelmChartFile> files, File root) throws IOException {
        if (root.isFile()) {
            HelmChartFile file = HelmChartFile.builder().name(root.getName()).isFile(true)
                .innerPath(root.getPath().replace(helmChartsDir, "")).build();
            // when the size of file is over 1mb, will not read content to memory.
            if (root.length() < 1024 * 1024) {
                file.setContent(
                    FileUtils.readFileToString(Paths.get(helmChartsDir, file.getInnerPath()).toFile(), "UTF-8"));
            }
            files.add(file);
        }

        if (root.isDirectory()) {
            HelmChartFile file = HelmChartFile.builder().name(root.getName()).isFile(false)
                .innerPath(root.getPath().replace(helmChartsDir, "")).build();
            List<HelmChartFile> children = new ArrayList<>();
            file.setChildren(children);
            files.add(file);
            File[] fileArray = root.listFiles();
            if (fileArray != null) {
                for (File childrenFile : fileArray) {
                    deepReadDir(children, childrenFile);
                }
            }
        }
        return files;
    }

    private String commentedLogicCodeInContent(String content) {
        String[] lines = StringUtils.split(content, "\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().startsWith("{{")) {
                lines[i] = line.replace("{{", "##");
            }
        }
        content = StringUtils.join(lines, "\n");
        return content;
    }

    private String replaceValuesInTemplateFile(String templateContent, Map<String, Object> valuesMap) {
        Pattern pattern = Pattern.compile("\\{\\{[\\s]*([\\w\\.\\s\\[\\]|]+)[\\s]*}}");
        Matcher matcher = pattern.matcher(templateContent);
        String result = templateContent;
        while (matcher.find()) {
            String find = matcher.group(0);
            String key = matcher.group(1).trim();
            Object value = valuesMap.getOrDefault(key, String.format("unknown(%s)", find).replace(" ", "-"));
            result = StringUtils.replace(result, find, value + "");
        }
        return result;
    }

    private Map<String, Object> getValuesMapFromYaml() {
        String valueContent = getContentByInnerPath("/values.yaml");
        assert valueContent != null;
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        HashMap<String, Object> valuesYamlMap = yaml.load(valueContent);
        List<String> route = new ArrayList<>();
        route.add(".Values");
        return readValuesYamlMap(valuesYamlMap, new LinkedHashMap<>(), route);
    }

    private Map<String, Object> readValuesYamlMap(Map<String, Object> valuesYamlMap,
        Map<String, Object> valuesResultMap, List<String> route) {
        for (Map.Entry<String, Object> entry : valuesYamlMap.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof ArrayList) {
                List<?> list = (ArrayList) val;
                int i = 0;
                for (Object subList : list) {
                    route.add(entry.getKey() + "[" + i + "]");
                    readObjectInArray(subList, valuesResultMap, route);
                    route.remove(route.size() - 1);
                    i++;
                }
            } else if (val instanceof Map) {
                route.add(entry.getKey());
                readValuesYamlMap((Map<String, Object>) entry.getValue(), valuesResultMap, route);
                route.remove(route.size() - 1);
            } else {
                route.add(entry.getKey());
                valuesResultMap.put(StringUtils.join(route, "."), entry.getValue());
                route.remove(route.size() - 1);
            }
        }
        return valuesResultMap;
    }

    private void readObjectInArray(Object obj, Map<String, Object> valuesResultMap, List<String> route) {
        if (obj instanceof String || obj instanceof Boolean || obj instanceof Number) {
            valuesResultMap.put(StringUtils.join(route, "."), obj);
        } else if (obj instanceof ArrayList) {
            List<?> list = (ArrayList) obj;
            int i = 0;
            for (Object subList : list) {
                String lastKey = route.get(route.size() - 1);
                route.set(route.size() - 1, lastKey + "[" + i + "]");
                readObjectInArray(subList, valuesResultMap, route);
                route.set(route.size() - 1, lastKey);
                i++;
            }
        } else if (obj instanceof Map) {
            readValuesYamlMap((Map<String, Object>) obj, valuesResultMap, route);
        }
    }

    private Map<String, HelmChartFile> catalogToMap(List<HelmChartFile> catalog, Map<String, HelmChartFile> map) {
        for (HelmChartFile file : catalog) {
            map.put(file.getInnerPath(), file);
            if (file.getChildren() != null) {
                catalogToMap(file.getChildren(), map);
            }
        }
        return map;
    }

    public List<HelmChartFile> getTemplatesFile() {
        List<HelmChartFile> files = this.getCatalog();
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        List<HelmChartFile> result = new ArrayList<>();
        for (Map.Entry<String, HelmChartFile> entry : this.catalogToMap(files, new HashMap<>()).entrySet()) {
            if (entry.getKey().startsWith(File.separator + "templates") && entry.getValue().isFile() && StringUtils
                .endsWithAny(entry.getValue().getName().toLowerCase(), ".yaml", ".yml")) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
