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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.common.ResponseConsts;
import org.edgegallery.developer.model.appstore.PublishAppErrResponse;
import org.edgegallery.developer.model.appstore.PublishAppReqDto;
import org.edgegallery.developer.model.common.User;
import org.edgegallery.developer.model.restful.AppStoreErrResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AppStoreUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppStoreUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private AppStoreUtil() {
        throw new IllegalStateException("AppStoreUtil class");
    }

    /**
     * upload app to appstore.
     */
    public static String storeToAppStore(Map<String, Object> params, User user, PublishAppErrResponse errResponse) {

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        params.forEach(map::add);

        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, user.getToken());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        List<MediaType> accept = new ArrayList<>();
        accept.add(MediaType.APPLICATION_JSON);
        headers.setAccept(accept);
        RestSvcAddressConfig svcAddressConfig = (RestSvcAddressConfig) SpringContextUtil
            .getBean(RestSvcAddressConfig.class);

        String url = svcAddressConfig.getAppstoreAddress() + String
            .format(Consts.UPLOAD_TO_APPSTORE_URL, user.getUserId(), user.getUserName());
        LOGGER.warn(url);

        try {
            ResponseEntity<String> responses = REST_TEMPLATE
                .exchange(url, HttpMethod.POST, new HttpEntity<>(map, headers), String.class);
            LOGGER.info("upload appstore response:{}", responses);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(responses.getStatusCode())) {
                return responses.getBody();
            }
        } catch (RestClientException e) {
            LOGGER.error("Failed to upload appstore,  exception {}", e.getMessage());
            if (!StringUtils.isEmpty(e.getMessage())) {
                String errMsg = e.getMessage();
                String responseBody = errMsg.substring(7, errMsg.length() - 1);
                LOGGER.info("responseBody:{}", responseBody);
                AppStoreErrResponseDto appStoreErrResponseDto = getErrRetCode(responseBody);
                errResponse.setErrCode(appStoreErrResponseDto.getRetCode());
                errResponse.setMessage(appStoreErrResponseDto.getMessage());
                return null;
            }
        }
        errResponse.setMessage("Upload app to store failed: occur unknown exception");
        errResponse.setErrCode(ResponseConsts.RET_PUBLISH_UNKNOWN_EXCEPTION);
        return null;
    }

    /**
     * publish app to appstore.
     */
    public static String publishToAppStore(String appId, String pkgId, String token, PublishAppReqDto pubAppReqDto,
        PublishAppErrResponse errResponse) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> accept = new ArrayList<>();
        accept.add(MediaType.APPLICATION_JSON);
        headers.setAccept(accept);
        RestSvcAddressConfig svcAddressConfig = (RestSvcAddressConfig) SpringContextUtil
            .getBean(RestSvcAddressConfig.class);

        String url = svcAddressConfig.getAppstoreAddress() + String
            .format(Consts.PUBLISH_TO_APPSTORE_URL, appId, pkgId);
        LOGGER.info("isFree: {}, price: {}", pubAppReqDto.isFree(), pubAppReqDto.getPrice());
        LOGGER.info("publish url: {}", url);
        try {
            ResponseEntity<String> responses = REST_TEMPLATE
                .exchange(url, HttpMethod.POST, new HttpEntity<>(new Gson().toJson(pubAppReqDto), headers),
                    String.class);
            LOGGER.info("publish res: {}", responses);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(responses.getStatusCode())) {
                return responses.getBody();
            }

        } catch (RestClientException e) {
            LOGGER.error("Failed to publish appstore,  exception {}", e.getMessage());
            if (!StringUtils.isEmpty(e.getMessage())) {
                String errMsg = e.getMessage();
                String responseBody = errMsg.substring(7, errMsg.length() - 1);
                LOGGER.info("responseBody:{}", responseBody);
                AppStoreErrResponseDto appStoreErrResponseDto = getErrRetCode(responseBody);
                errResponse.setErrCode(appStoreErrResponseDto.getRetCode());
                errResponse.setMessage(appStoreErrResponseDto.getMessage());
                return null;
            }
        }
        errResponse.setMessage("Upload app to store failed: occur unknown exception");
        errResponse.setErrCode(ResponseConsts.RET_PUBLISH_UNKNOWN_EXCEPTION);
        return null;
    }

    /**
     * get pkg info.
     */
    public static String getPkgInfo(String appId, String pkgId, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestSvcAddressConfig svcAddressConfig = (RestSvcAddressConfig) SpringContextUtil
            .getBean(RestSvcAddressConfig.class);

        String url = String.format(Consts.QUERY_APPSTORE_PKG_URL, svcAddressConfig.getAppstoreAddress(), appId, pkgId);
        LOGGER.info("get pkg url: {}", url);
        try {
            ResponseEntity<String> responses = REST_TEMPLATE
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            LOGGER.info("get pkg res: {}", responses);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.CREATED
                .equals(responses.getStatusCode())) {
                return responses.getBody();
            }
            LOGGER.error("get pkg info failed, status is {}", responses.getStatusCode());
            return null;
        } catch (RestClientException e) {
            LOGGER.error("get pkg info failed, exception {}", e.getMessage());
            return null;
        }
    }

    /**
     * download pkg .
     */
    public static byte[] downloadPkg(String appId, String pkgId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestSvcAddressConfig svcAddressConfig = (RestSvcAddressConfig) SpringContextUtil
            .getBean(RestSvcAddressConfig.class);

        String url = String
            .format(Consts.DOWNLOAD_APPSTORE_PKG_URL, svcAddressConfig.getAppstoreAddress(), appId, pkgId);
        LOGGER.info("download pkg url: {}", url);
        try {
            ResponseEntity<byte[]> responses = REST_TEMPLATE
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            LOGGER.info("download pkg res: {}", responses);
            if (HttpStatus.OK.equals(responses.getStatusCode()) || HttpStatus.CREATED
                .equals(responses.getStatusCode())) {
                return responses.getBody();
            }
            LOGGER.error("download pkg failed, status is {}", responses.getStatusCode());
            return new byte[0];
        } catch (RestClientException e) {
            LOGGER.error("download pkg failed, exception {}", e.getMessage());
            return new byte[0];
        }
    }

    private static AppStoreErrResponseDto getErrRetCode(String errBody) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<AppStoreErrResponseDto>() { }.getType();
            AppStoreErrResponseDto appStoreErrResponseDto = gson.fromJson(errBody, type);
            LOGGER.info("retCode:{}", appStoreErrResponseDto.getRetCode());
            return appStoreErrResponseDto;
        } catch (Exception e) {
            AppStoreErrResponseDto appStoreErrResponseDto = new AppStoreErrResponseDto();
            LOGGER.error("convert errBody {} to AppStoreErrResponseDto fail!", errBody);
            appStoreErrResponseDto.setCode(ResponseConsts.RET_PUBLISH_UNKNOWN_EXCEPTION);
            appStoreErrResponseDto.setMessage("Upload app to store failed: occur unknown exception");
            return appStoreErrResponseDto;
        }

    }

}
