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
        String appInstanceId, String token) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));
        body.add("hostIp", ip);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_INSTANTIATE_APP_URL
            .replaceAll("appInstanceId", appInstanceId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
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
        String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_TERMINATE_APP_URL
            .replaceAll("appInstanceId", appInstanceId);
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
    public static String getWorkloadStatus(String protocol, String ip, int port, String appInstanceId, String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_WORKLOAD_STATUS_URL
            .replaceAll("appInstanceId", appInstanceId);
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
            return response.toString();
        }
        LOGGER.error("Failed to get workload status which appInstanceId is {}", appInstanceId);
        return null;
    }

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }
}
