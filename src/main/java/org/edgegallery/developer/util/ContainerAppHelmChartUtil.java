/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.developer.util;

import io.kubernetes.client.openapi.models.V1Service;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.edgegallery.developer.util.helmcharts.k8sobject.EnumKubernetesObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public final class ContainerAppHelmChartUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerAppHelmChartUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String CHECK_IMAGE_PREFIX = "image ";

    private static final String CHECK_IMAGE_SUFFIX = " is not in standard format";

    private static final String FILE_NAME_PATTERN = "^[a-z0-9][a-z0-9-\\.]+[a-z0-9]$";

    private ContainerAppHelmChartUtil() {

    }

    /**
     * get image form tgz file.
     *
     * @param helmChartsPackagePath helmChartsPackagePath
     * @return
     */
    public static List<String> getImagesFromHelmFile(String helmChartsPackagePath) {
        List<String> images = new ArrayList<>();
        try (IContainerFileHandler containerFileHandler = LoadContainerFileFactory
            .createLoader(helmChartsPackagePath)) {
            assert containerFileHandler != null;
            containerFileHandler.load(helmChartsPackagePath);
            containerFileHandler.getAllK8sObject()
                .forEach(item -> images.addAll(EnumKubernetesObject.of(item).getImages()));
        } catch (IOException e) {
            LOGGER.error("Failed to load file. file={}", helmChartsPackagePath);
        }
        return images;
    }

    /**
     * check tgz file name and deploy yaml name in format.
     *
     * @param helmChartsPackagePath helmChartsPackagePath
     * @return
     */
    public static boolean checkFileNameFormat(String helmChartsPackagePath) {
        File tgzFile = new File(helmChartsPackagePath);
        Pattern pattern = Pattern.compile(FILE_NAME_PATTERN);
        if (tgzFile.exists() && tgzFile.isFile() && pattern.matcher(tgzFile.getName()).matches()) {
            try (IContainerFileHandler containerFileHandler = LoadContainerFileFactory
                .createLoader(helmChartsPackagePath)) {
                assert containerFileHandler != null;
                containerFileHandler.load(helmChartsPackagePath);
                List<HelmChartFile> fileList = containerFileHandler.getTemplatesFile();
                if (CollectionUtils.isEmpty(fileList)) {
                    LOGGER.error("there are no files in the templates folder.");
                    return false;
                }
                for (HelmChartFile helmChartFile : fileList) {
                    if (!pattern.matcher(helmChartFile.getName()).matches()) {
                        LOGGER.error("{} name not in format.", helmChartFile.getName());
                        return false;
                    }
                }
                return true;
            } catch (IOException e) {
                LOGGER.error("Failed to load file. file is {}", helmChartsPackagePath);
                return false;
            }
        }
        LOGGER.error("tgz file name not in format.");
        return false;
    }

    /**
     * check service exist.
     *
     * @param helmChartsPackagePath helmChartsPackagePath
     * @return
     */
    public static boolean checkServiceExist(String helmChartsPackagePath) {
        try (IContainerFileHandler containerFileHandler = LoadContainerFileFactory
            .createLoader(helmChartsPackagePath)) {
            assert containerFileHandler != null;
            containerFileHandler.load(helmChartsPackagePath);
            return containerFileHandler.getAllK8sObject().stream().anyMatch(item -> item instanceof V1Service);
        } catch (IOException e) {
            LOGGER.error("Failed to load file. file={}", helmChartsPackagePath);
        }
        return false;
    }

    /**
     * check image exist.
     *
     * @param imageList image list
     * @return
     */
    public static String getImageCheckInfo(List<String> imageList) {
        for (String image : imageList) {
            //judge image in format
            if (!image.contains(":") || image.endsWith(":")) {
                LOGGER.error("image {} must be in xxx:xxx format!", image);
                return CHECK_IMAGE_PREFIX + image + CHECK_IMAGE_SUFFIX;
            }
            String project = "";
            String imageName = "";
            String imageVersion = "";
            if (image.contains("/")) {
                String[] imageInfoArr = image.split("/");
                if (imageInfoArr.length == 3) {
                    String[] images = imageInfoArr[2].split(":");
                    project = imageInfoArr[1];
                    imageName = images[0];
                    imageVersion = images[1];
                } else if (imageInfoArr.length == 4) {
                    String[] images = imageInfoArr[3].split(":");
                    project = imageInfoArr[1];
                    imageName = imageInfoArr[2] + "/" + images[0];
                    imageVersion = images[1];
                } else {
                    LOGGER.error("image {} non-standard format domainname/project/name:version", image);
                    return CHECK_IMAGE_PREFIX + image + CHECK_IMAGE_SUFFIX;
                }
            } else {
                LOGGER.error("image {} non-standard format domainname/project/name:version", image);
                return CHECK_IMAGE_PREFIX + image + CHECK_IMAGE_SUFFIX;
            }
            String ret = getHarborImageInfo(project, imageName, imageVersion);
            if (StringUtils.isEmpty(ret)) {
                LOGGER.error("image {} does not exist in harbor repo", imageName);
                return CHECK_IMAGE_PREFIX + image + " does not exist in harbor repo!";
            }
        }
        return null;
    }

    /**
     * get image info from harbor repo.
     *
     * @param project harbor project
     * @param name image name
     * @param version image version
     * @return
     */
    private static String getHarborImageInfo(String project, String name, String version) {
        HttpHeaders headers = new HttpHeaders();
        ImageConfig imageConfig = (ImageConfig) SpringContextUtil.getBean(ImageConfig.class);
        headers.set("Authorization",
            "Basic " + ContainerImageUtil.encodeUserAndPwd(imageConfig.getUsername(), imageConfig.getPassword()));
        HttpEntity requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            String url = String.format(Consts.HARBOR_IMAGE_DELETE_URL, ContainerImageUtil.getHarborProtocol(),
                imageConfig.getDomainname(), project, name, version);
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
            LOGGER.warn("get harbor image log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed get harbor image {} occur {}", name, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody();
        }
        LOGGER.error("Failed get harbor image!");
        return null;
    }

}
