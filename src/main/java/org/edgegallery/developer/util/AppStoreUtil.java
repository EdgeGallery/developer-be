/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

import java.util.Map;
import org.edgegallery.developer.config.security.AccessUserUtil;
import org.edgegallery.developer.domain.model.user.User;
import org.edgegallery.developer.model.workspace.PublishAppReqDto;
import org.edgegallery.developer.service.UtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AppStoreUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilsService.class);

    private static final String APPSTORE_ADDRESS = "appstore.address";

    private AppStoreUtil() {
        throw new IllegalStateException("AppStoreUtil class");
    }

    /**
     * upload app to appstore.
     */
    public static ResponseEntity<String> storeToAppStore(Map<String, Object> params, User user) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(600000);// 设置超时
        requestFactory.setReadTimeout(600000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        params.forEach(map::add);
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", user.getToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String url = String
            .format("%s/mec/appstore/v1/apps?userId=%s&userName=%s", InitConfigUtil.getProperties(APPSTORE_ADDRESS),
                user.getUserId(), user.getUserName());
        LOGGER.warn(url);
        try {
            ResponseEntity<String> responses = restTemplate
                .exchange(url, HttpMethod.POST, new HttpEntity<>(map, headers), String.class);
            LOGGER.info("upload appstore response:{}", responses);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(responses.getStatusCode())) {
                return responses;
            }
            LOGGER.error("Upload appstore failed,  status is {}", responses.getStatusCode());
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Failed to upload appstore,  exception {}", e.getMessage());
            return null;
        }
    }

    /**
     * publish app to appstore.
     */
    public static ResponseEntity<String> publishToAppStore(String appId, String pkgId, String token,
        PublishAppReqDto pubAppReqDto) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(600000);// 设置超时
        requestFactory.setReadTimeout(600000);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = String.format("%s/mec/appstore/v1/apps/%s/packages/%s/action/publish",
            InitConfigUtil.getProperties(APPSTORE_ADDRESS), appId, pkgId);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("isFree", pubAppReqDto.isFree());
        body.add("price", pubAppReqDto.getPrice());
        LOGGER.info("isFree: {}, price: {}", pubAppReqDto.isFree(),pubAppReqDto.getPrice());
        LOGGER.info("url: {}", url);
        try {
            ResponseEntity<String> responses = restTemplate
                .exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            LOGGER.info("res: {}", responses);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(responses.getStatusCode())) {
                return responses;
            }
            LOGGER.error("publish app failed: the app have exist,  status is {}", responses.getStatusCode());
            return null;
        } catch (RestClientException e) {
            LOGGER.error("publish app  failed,  exception {}", e.getMessage());
            return null;

        }
    }

}
