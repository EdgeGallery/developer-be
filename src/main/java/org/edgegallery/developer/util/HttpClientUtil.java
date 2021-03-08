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

import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.CustomException;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageConfig;
import org.edgegallery.developer.model.workspace.ProjectTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class HttpClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private HttpClientUtil() {

    }

    /**
     * instantiateApplication.
     *
     * @return InstantiateAppResult
     */
    public static boolean instantiateApplication(String protocol, String ip, int port, String filePath,
                                                 String appInstanceId, String userId, String token, String projectName, ProjectTestConfig testConfig) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));
        body.add("hostIp", ip);
        body.add("appName", projectName);
        body.add("packageId", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_INSTANTIATE_APP_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}", appInstanceId,
                    errorLog);
            testConfig.setErrorLog(errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to instantiate application which appInstanceId is {}", appInstanceId);
        return false;
    }

    /**
     * terminateAppInstance.
     *
     * @return boolean
     */
    public static boolean terminateAppInstance(String protocol, String ip, int port, String appInstanceId,
        String userId, String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_TERMINATE_APP_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to terminate application which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to terminate application which appInstanceId is {}", appInstanceId);
        return false;
    }

    /**
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getWorkloadStatus(String protocol, String ip, int port, String appInstanceId, String userId,
        String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_WORKLOAD_STATUS_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId);
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get workload status which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get workload status which appInstanceId is {}", appInstanceId);
        return null;
    }

    /**
     * getHealth.
     */
    public static String getHealth(String ip, int port) {
        String url = getUrlPrefix("https", ip, port) + Consts.APP_LCM_GET_HEALTH;
        LOGGER.info(" health url is {}", url);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, null, String.class);
        } catch (RestClientException e) {
            LOGGER.error("call app lcm health api occur exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("call app lcm health api failed");
        return null;
    }

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }

    /**
     * vm instantiate Application.
     *
     * @return InstantiateAppResult
     */
    public static boolean vmInstantiateApplication(String protocol, String ip, int port, String filePath,
        String appInstanceId, String userId, String projectName, VmCreateConfig vmConfig) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));
        body.add("hostIp", ip);
        body.add("appName", projectName);
        body.add("packageId", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, vmConfig.getLcmToken());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_INSTANTIATE_APP_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to vm instantiate application which packageId is {} exception {}", appInstanceId,
                errorLog);
            vmConfig.setLog(errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to instantiate application which appInstanceId is {}", appInstanceId);
        return false;
    }

    public static boolean vmInstantiateImage(String protocol, String ip, int port, String userId, VmImageConfig imageConfig) {
        String appInstanceId = imageConfig.getAppInstanceId();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("vmId", imageConfig.getVmId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, imageConfig.getLcmToken());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_INSTANTIATE_IMAGE_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            JsonElement imageId = jsonObject.get("imageId");
            imageConfig.setImageId(imageId.getAsString());
            LOGGER.info("APPlCM log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to create vm image  which appInstanceId is {} exception {}", appInstanceId,
                errorLog);
            imageConfig.setLog(errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed to create vm image  which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to create vm image  which appInstanceId is {}", appInstanceId);
        return false;

    }

    public static String getImageStatus(String protocol, String ip, int port, String appInstanceId, String userId,
        String imageId, String lcmToken) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_IMAGE_STATUS_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId)
            .replaceAll("imageId", imageId);
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, lcmToken);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get image status which imageId is {} exception {}", imageId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get image status which imageId is {}", imageId);
        return null;

    }

    public static boolean downloadVmImage(String protocol, String ip, int port, String userId, String packagePath, VmImageConfig config) {

        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_IMAGE_DOWNLOAD_URL
            .replaceAll("appInstanceId", config.getAppInstanceId()).replaceAll("tenantId", userId)
            .replaceAll("imageId", config.getImageId());
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, config.getLcmToken());
//        headers.set(Consts.CHUNK_NUM, config.getSumChunkNum());
        // download images
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get image status which imageId is {} exception {}", config.getImageId(),
                e.getMessage());
            return false;
        }
        return true;


    }
}
