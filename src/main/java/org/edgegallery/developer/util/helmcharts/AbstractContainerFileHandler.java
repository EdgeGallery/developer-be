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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractContainerFileHandler implements IContainerFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerFileHandler.class);

    protected String workspace;

    protected String helmChartsDir;

    private Map<String, Object> valuesMap;

    @Override
    public abstract void load(String... filePaths) throws IOException;

    @Override
    public List<HelmChartFile> getCatalog() {
        if (helmChartsDir == null) {
            return null;
        }
        File root = new File(helmChartsDir);
        return deepReadDir(new ArrayList<>(), root);
    }

    public Map<String, HelmChartFile> getHelmChartFileMap(List<HelmChartFile> catalog, Map<String, HelmChartFile> map) {
        for (HelmChartFile file : catalog) {
            map.put(file.getInnerPath(), file);
            if (file.getChildren() != null) {
                getHelmChartFileMap(file.getChildren(), map);
            }
        }
        return map;
    }

    private List<HelmChartFile> deepReadDir(List<HelmChartFile> files, File root) {
        if (root.isFile()) {
            HelmChartFile file = HelmChartFile.builder().name(root.getName()).isFile(true)
                .innerPath(root.getPath().replace(helmChartsDir, "")).index(files.size() + 1).build();
            files.add(file);
        }

        if (root.isDirectory()) {
            HelmChartFile file = HelmChartFile.builder().name(root.getName()).isFile(false)
                .innerPath(root.getPath().replace(helmChartsDir, "")).index(files.size() + 1).build();
            List<HelmChartFile> children = new ArrayList<>();
            file.setChildren(children);
            files.add(file);
            for (File childrenFile : Objects.requireNonNull(root.listFiles())) {
                deepReadDir(children, childrenFile);
            }
        }
        return files;
    }

    public List<HelmChartFile> getTemplatesFile() {
        List<HelmChartFile> files = this.getCatalog();
        if (files.isEmpty()) {
            return null;
        }
        List<HelmChartFile> result = new ArrayList<>();
        for (Map.Entry<String, HelmChartFile> entry : this.getHelmChartFileMap(files, new HashMap<>()).entrySet()) {
            if (entry.getKey().startsWith(File.separator + "templates") && entry.getValue().isFile() && StringUtils
                .endsWithAny(entry.getValue().getName().toLowerCase(), ".yaml", ".yml")) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    public List getKubernetesConfigs(String innerFilePath) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get(helmChartsDir, innerFilePath));
            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);
                if (line.trim().startsWith("{{")) {
                    allLines.set(i, line.replace("{{", "##"));
                }
            }
            return Yaml.loadAll(StringUtils.join(allLines, "\n"));
        } catch (IOException e) {
            LOGGER.error("Failed to parse k8s config by kubernetes-client.");
        }
        return null;
    }

    @Override
    public String exportHelmChartsPackage() {
        try {
            String fileName = new File(helmChartsDir).getName();
            File file = CompressFileUtilsJava.compressToTgz(helmChartsDir, workspace, fileName);
            assert file != null;
            if (file.exists()) {
                return file.getCanonicalPath();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to export helm charts package.");
        }
        return null;
    }

    @Override
    public String getContentByInnerPath(String innerPath) {
        try {
            List<String> allLines = Files.readAllLines(Paths.get(helmChartsDir, innerPath));
            return StringUtils.join(allLines, "\n");
        } catch (IOException e) {
            LOGGER.error("Failed to read the inner file. innerPath:{}", innerPath);
        }
        return null;
    }

    @Override
    public boolean modifyFileByPath(String innerPath, String content) {
        try {
            Path realPath = Paths.get(helmChartsDir, innerPath);
            if (realPath.toFile().exists() && realPath.toFile().isFile()) {
                Files.write(realPath, content.getBytes(StandardCharsets.UTF_8));
                return true;
            }
            LOGGER.warn("Can not find file by the innerPath: {}", innerPath);
        } catch (IOException e) {
            LOGGER.error("Failed to modify the innerFile. innerPath:{}", innerPath);
        }
        return false;
    }

    @Override
    public void addFile(String filePath, String content) {

    }

    @Override
    public void clean() {
        try {
            if (workspace != null) {
                FileUtils.deleteDirectory(new File(workspace));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to clean workspace.");
        } finally {
            workspace = null;
            helmChartsDir = null;
        }
    }

    public List<Object> getK8sTemplateObject(HelmChartFile innerFile) {
        if (valuesMap == null) {
            this.valuesMap = getValuesMapFromYaml();
        }
        String content = this.getContentByInnerPath(innerFile.getInnerPath());

        // replace values
        content = replaceValuesInTemplateFile(content, this.valuesMap);

        String[] lines = StringUtils.split(content, "\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().startsWith("{{")) {
                lines[i] = line.replace("{{", "##");
            }
        }
        content = StringUtils.join(lines, "\n");
        try {
            return Yaml.loadAll(content);
        } catch (IOException e) {
            LOGGER.error("Failed to parse k8s file.");
        }
        return null;
    }

    private String replaceValuesInTemplateFile(String templateContent, Map<String, Object> valuesMap) {
        Pattern pattern = Pattern.compile("\\{\\{[\\s]*([\\w\\.\\s\\[\\]|]+)[\\s]*}}");
        Matcher matcher = pattern.matcher(templateContent);
        String result = templateContent;
        while (matcher.find()) {
            String find = matcher.group(0);
            String key = matcher.group(1);
            Object value = valuesMap.getOrDefault(key, String.format("unknown(%s)", find).replaceAll(" ", "-"));
            result = StringUtils.replace(result, find, value + "");
        }
        return result;
    }

    private Map<String, Object> getValuesMapFromYaml() {
        String valueContent = getContentByInnerPath("/Values.yaml");
        assert valueContent != null;
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        HashMap<String, Object> valuesYamlMap = yaml.load(valueContent);
        List<String> route = new ArrayList<>();
        route.add(".Values");
        return readMap(valuesYamlMap, new LinkedHashMap<>(), route);
    }

    private Map<String, Object> readMap(Map<String, Object> valuesYamlMap, Map<String, Object> valuesResultMap,
        List<String> route) {
        for (Map.Entry<String, Object> entry : valuesYamlMap.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof ArrayList) {
                List list = (ArrayList) val;
                int i = 0;
                for (Object subList : list) {
                    route.add(entry.getKey() + "[" + i + "]");
                    readObject(subList, valuesResultMap, route);
                    route.remove(route.size() - 1);
                    i++;
                }
            } else if (val instanceof Map) {
                route.add(entry.getKey());
                readMap((Map<String, Object>) entry.getValue(), valuesResultMap, route);
                route.remove(route.size() - 1);
            } else {
                route.add(entry.getKey());
                valuesResultMap.put(StringUtils.join(route, "."), entry.getValue());
                route.remove(route.size() - 1);
            }
        }
        return valuesResultMap;
    }

    private void readObject(Object obj, Map<String, Object> valuesResultMap, List<String> route) {
        if (obj instanceof String || obj instanceof Boolean || obj instanceof Number) {
            valuesResultMap.put(StringUtils.join(route, "."), obj);
        } else if (obj instanceof ArrayList) {
            System.out.println("array---------" + obj);
            List list = (ArrayList) obj;
            int i = 0;
            for (Object subList : list) {
                String lastKey = route.get(route.size() - 1);
                route.set(route.size() - 1, lastKey + "[" + i + "]");
                readObject(subList, valuesResultMap, route);
                route.set(route.size() - 1, lastKey);
                i++;
            }
        } else if (obj instanceof Map) {
            readMap((Map<String, Object>) obj, valuesResultMap, route);
        }
    }
}
