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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractContainerFileHandler implements IContainerFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerFileHandler.class);

    protected String workspace;

    protected String helmChartsDir;

    @Override
    public abstract void load(String filePath) throws IOException;

    @Override
    public List<HelmChartFile> getCatalog() {
        if (helmChartsDir == null) {
            return null;
        }
        File root = new File(helmChartsDir);
        return deepReadDir(new ArrayList<>(), root);
    }

    private List<HelmChartFile> deepReadDir(List<HelmChartFile> files, File root) {
        if (root.isFile()) {
            HelmChartFile file = HelmChartFile.builder().name(root.getName())
                .innerPath(root.getPath().replace(helmChartsDir, "")).index(files.size() + 1).build();
            files.add(file);
        }

        if (root.isDirectory()) {
            HelmChartFile file = HelmChartFile.builder().name(root.getName())
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

    @Override
    public String exportHelmCharts() {
        try {
            String fileName = new File(helmChartsDir).getName();
            File file = CompressFileUtilsJava
                .compressToTgz(helmChartsDir, workspace, fileName + "_" + System.currentTimeMillis());
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
}
