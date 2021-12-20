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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.util.CompressFileUtilsJava;
import org.edgegallery.developer.util.helmcharts.k8sobject.EnumKubernetesObject;
import org.edgegallery.developer.util.helmcharts.k8sobject.IContainerImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractContainerFileHandler extends AbstractReadHelmChartsFileHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerFileHandler.class);

    private static final String VALUES_FORMAT_IMAGE_URL = String
        .format("{{ %s }}/{{ %s }}", EgValuesYaml.VALUES_KEY_IMAGE_DOMAIN_NAME, EgValuesYaml.VALUES_KEY_IMAGE_PROJECT)
        + "/%s:%s";

    @Override
    public abstract void load(String... filePaths) throws IOException;

    // replace all of the image url to Values
    void replaceAllImageUrlToValues() {
        Map<HelmChartFile, List<String>> imagesMap = getAllImagesFromFile();

        List<HelmChartFile> needUpdateFile = new ArrayList<>();

        // check image-location values
        for (Map.Entry<HelmChartFile, List<String>> entry : imagesMap.entrySet()) {
            List<String> images = entry.getValue();
            for (String imageUrl : images) {
                ImageInfo imageInfo = new ImageInfo(imageUrl);
                String targetImageUrl = String.format(VALUES_FORMAT_IMAGE_URL, imageInfo.name, imageInfo.version);
                String content = entry.getKey().getContent();
                content = StringUtils.replace(content, imageInfo.imageUrl, targetImageUrl);
                entry.getKey().setContent(content);
            }
            needUpdateFile.add(entry.getKey());
        }

        // to update inner-file
        needUpdateFile.forEach(item -> this.modifyFileByPath(item.getInnerPath(), item.getContent()));
    }

    private Map<HelmChartFile, List<String>> getAllImagesFromFile() {
        List<HelmChartFile> k8sTemplates = this.getTemplatesFile();
        Map<HelmChartFile, List<String>> imagesMap = new HashMap<>();
        for (HelmChartFile k8sTemplate : k8sTemplates) {
            List<Object> k8s = this.getK8sTemplateObject(k8sTemplate);
            List<String> images = new ArrayList<>();
            for (Object obj : k8s) {
                IContainerImage containerImage = EnumKubernetesObject.of(obj);
                List<String> findImages = containerImage.getImages();
                if (findImages != null) {
                    images.addAll(containerImage.getImages());
                }
            }
            if (!images.isEmpty()) {
                imagesMap.put(k8sTemplate, images);
            }
        }
        return imagesMap;
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
            return FileUtils.readFileToString(Paths.get(helmChartsDir, innerPath).toFile(), "UTF-8");
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
    public void close() throws IOException {
        clean();
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

    @Setter
    static class ImageInfo {
        private final String name;

        private final String version;

        private final String domain;

        private final String imageUrl;

        // support format: nginx; nginx:latest; example.host/nginx:latest; example.host:9033/nginx:latest
        ImageInfo(String url) {
            this.imageUrl = url;
            int index = imageUrl.lastIndexOf("/");
            if (index > -1) {
                this.domain = imageUrl.substring(0, index);
            } else {
                this.domain = "";
            }
            String[] imageNameAndVersion = imageUrl.substring(index + 1).split(":");
            this.name = imageNameAndVersion[0];
            if (imageNameAndVersion.length > 1) {
                this.version = imageNameAndVersion[1];
            } else {
                this.version = "latest";
            }
        }
    }
}
