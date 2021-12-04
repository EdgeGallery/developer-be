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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.util.helmcharts.HelmChartFile;
import org.edgegallery.developer.util.helmcharts.IContainerFileHandler;
import org.edgegallery.developer.util.helmcharts.LoadContainerFileFactory;
import org.edgegallery.developer.util.helmcharts.k8sObject.EnumKubernetesObject;
import org.edgegallery.developer.util.helmcharts.k8sObject.IContainerImage;
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

    private static final String HARBOR_PROTOCOL = "https";

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
            List<HelmChartFile> k8sTemplates = containerFileHandler.getTemplatesFile();
            for (HelmChartFile k8sTemplate : k8sTemplates) {
                List<Object> k8sList = containerFileHandler.getK8sTemplateObject(k8sTemplate);
                for (Object obj : k8sList) {
                    IContainerImage containerImage = EnumKubernetesObject.of(obj);
                    List<String> podImages = containerImage.getImages();
                    images.addAll(podImages);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load file. file={}", helmChartsPackagePath);
        }
        return images;
    }

    /**
     * check service exist.
     *
     * @param helmChartsPackagePath helmChartsPackagePath
     * @return
     */
    public static boolean checkServiceExist(String helmChartsPackagePath) {
        boolean isExist = false;
        try (IContainerFileHandler containerFileHandler = LoadContainerFileFactory
            .createLoader(helmChartsPackagePath)) {
            assert containerFileHandler != null;
            containerFileHandler.load(helmChartsPackagePath);
            List<HelmChartFile> k8sTemplates = containerFileHandler.getTemplatesFile();
            for (HelmChartFile k8sTemplate : k8sTemplates) {
                List<Object> k8sList = containerFileHandler.getK8sTemplateObject(k8sTemplate);
                for (Object obj : k8sList) {
                    if (obj instanceof V1Service) {
                        isExist = true;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load file. file={}", helmChartsPackagePath);
        }
        return isExist;
    }

    /**
     * check image exist.
     *
     * @param imageList image list
     * @return
     */
    public static boolean checkImageExist(List<String> imageList) {
        if (CollectionUtils.isEmpty(imageList)) {
            LOGGER.error("image list is empty!");
            return false;
        }
        for (String image : imageList) {
            //judge image in format
            if (!image.contains(":") || image.endsWith(":")) {
                LOGGER.error("image {} must be in xxx:xxx format!", image);
                return false;
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
                    return false;
                }
            } else {
                LOGGER.error("image {} non-standard format domainname/project/name:version", image);
                return false;
            }
            String ret = getHarborImageInfo(project, imageName, imageVersion);
            if (StringUtils.isEmpty(ret)) {
                LOGGER.error("image {} does not exist in harbor repo", imageName);
                return false;
            }
        }
        return true;
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
            String url = String
                .format(Consts.HARBOR_IMAGE_DELETE_URL, HARBOR_PROTOCOL, imageConfig.getDomainname(), project, name,
                    version);
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
