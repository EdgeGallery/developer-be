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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.CustomException;
import org.edgegallery.developer.model.common.Chunk;
import org.edgegallery.developer.model.filesystem.FileSystemResponse;
import org.edgegallery.developer.model.lcm.CreateConsole;
import org.edgegallery.developer.model.lcm.DistributeBody;
import org.edgegallery.developer.model.lcm.DistributeResponse;
import org.edgegallery.developer.model.lcm.InstantRequest;
import org.edgegallery.developer.model.lcm.LcmLog;
import org.edgegallery.developer.model.lcm.LcmResponseBody;
import org.edgegallery.developer.model.lcm.VmImageRequest;
import org.edgegallery.developer.model.reverseproxy.SshResponseInfo;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public final class HttpClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static Gson gson = new Gson();

    private HttpClientUtil() {

    }

    /**
     * instantiateApplication.
     *
     * @return InstantiateAppResult
     */
    public static boolean instantiateApplication(String basePath, String appInstanceId, String userId, String token,
        LcmLog lcmLog, String pkgId, String mecHost, Map<String, String> inputParams) {
        //before instantiate ,call distribute result interface
        LOGGER.info("inter instant");
        String disRes = getDistributeRes(basePath, userId, token, pkgId);
        LOGGER.info("get distribute {}", disRes);
        if (StringUtils.isEmpty(disRes)) {
            LOGGER.error("instantiateApplication get pkg distribute res failed!");
            return false;
        }
        //parse dis res
        List<DistributeResponse> list = gson.fromJson(disRes, new TypeToken<List<DistributeResponse>>() {
        }.getType());
        String appName = list.get(0).getAppPkgName();
        //set instantiate headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        //set instantiate bodys
        InstantRequest ins = new InstantRequest();
        ins.setAppName(appName);
        ins.setHostIp(mecHost);
        ins.setPackageId(pkgId);
        ins.setParameters(inputParams);
        LOGGER.warn(gson.toJson(ins));
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(ins), headers);
        String url = basePath + String.format(Consts.APP_LCM_INSTANTIATE_APP_URL, userId, appInstanceId);
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM instantiate log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}", appInstanceId,
                errorLog);
            lcmLog.setLog(errorLog);
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
     * upload pkg.
     */
    public static String uploadPkg(String basePath, String filePath, String userId, String token, LcmLog lcmLog) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("package", new FileSystemResource(filePath));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        headers.set("Origin", "mepm");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = basePath + String.format(Consts.APP_LCM_UPLOAD_APPPKG_URL, userId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPLCM upload pkg log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed upload pkg exception {}", errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Failed upload pkg exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LcmResponseBody lcmResponseBody = gson.fromJson(response.getBody(), LcmResponseBody.class);
            return lcmResponseBody.getData().toString();
        }
        LOGGER.error("Failed to upload pkg!");
        return null;
    }

    /**
     * distribute pkg.
     */
    public static String distributePkg(String basePath, String userId, String token, String packageId, String mecHost,
        LcmLog lcmLog) {
        //add body
        DistributeBody body = new DistributeBody();
        String[] bodys = new String[1];
        bodys[0] = mecHost;
        body.setHostIp(bodys);
        //add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        Gson gson = new Gson();
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(body), headers);
        String url = basePath + String.format(Consts.APP_LCM_DISTRIBUTE_APPPKG_URL, userId, packageId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPLCM distribute pkg log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed distribute pkg packageId  {} exception {}", packageId, errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Failed distribute pkg packageId is {} exception {}", packageId, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to distribute pkg which packageId is {}", packageId);
        return null;
    }

    /**
     * delete host.
     */
    public static boolean deleteHost(String basePath, String userId, String token, String pkgId, String hostIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = basePath + String.format(Consts.APP_LCM_DELETE_HOST_URL, userId, pkgId, hostIp);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.info("APPlCM delete host log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete host packageId is {} exception {}", pkgId, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to delete host which packageId is {}", pkgId);
        return false;
    }

    /**
     * delete pkg.
     */
    public static boolean deletePkg(String basePath, String userId, String token, String pkgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = basePath + String.format(Consts.APP_LCM_DELETE_APPPKG_URL, userId, pkgId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.info("APPlCM delete pkg log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete pkg pkgId is {} exception {}", pkgId, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to delete pkg which pkgId is {}", pkgId);
        return false;
    }

    /**
     * get distribute result.
     */
    public static String getDistributeRes(String basePath, String userId, String token, String pkgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = basePath + String.format(Consts.APP_LCM_DISTRIBUTE_APPPKG_URL, userId, pkgId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
            LOGGER.info("APPlCM get distribute res log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed get distribute res pkgId is {} exception {}", pkgId, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LcmResponseBody lcmResponseBody = gson.fromJson(response.getBody(), LcmResponseBody.class);
            return gson.toJson(lcmResponseBody.getData());
        }
        LOGGER.error("Failed to get distribute result!");
        return null;
    }

    /**
     * terminateAppInstance.
     *
     * @return boolean
     */
    public static boolean terminateAppInstance(String basePath, String appInstanceId, String userId, String token) {
        String url = basePath + String.format(Consts.APP_LCM_TERMINATE_APP_URL, userId, appInstanceId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);
            LOGGER.info("APPlCM terminateAppInstance log:{}", response);
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
    public static String getWorkloadStatus(String basePath, String appInstanceId, String userId, String token) {
        String url = basePath + String.format(Consts.APP_LCM_GET_WORKLOAD_STATUS_URL, userId, appInstanceId);
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
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getVncUrl(String basePath, String userId, String hostId, String vmId, String token) {
        String url = basePath + String.format(Consts.APP_LCM_GET_VNC_CONSOLE_URL, userId, hostId, vmId);
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        //set vncUrl bodys
        CreateConsole ins = new CreateConsole();
        ins.setAction("createConsole");
        LOGGER.warn(gson.toJson(ins));
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(ins), headers);
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM create console log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get vm console which vmId is {} exception {}", vmId, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LcmResponseBody lcmResponseBody = gson.fromJson(response.getBody(), LcmResponseBody.class);
            return gson.toJson(lcmResponseBody.getData());
        }
        LOGGER.error("Failed to get vm console which vmId is {}", vmId);
        return null;
    }

    /**
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getWorkloadEvents(String basePath, String appInstanceId, String userId, String token) {
        String url = basePath + String.format(Consts.APP_LCM_GET_WORKLOAD_EVENTS_URL, userId, appInstanceId);
        LOGGER.info("work event url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get workload events which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get workload events which appInstanceId is {}", appInstanceId);
        return null;
    }

    /**
     * getHealth.
     */
    public static String getHealth(String basePath) {
        String url = basePath + Consts.APP_LCM_GET_HEALTH;
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

    public static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }

    /**
     * vmInstantiateImage.
     */
    public static String vmInstantiateImage(String basePath, String userId, String lcmToken, String vmId,
        String hostIp, String imageName, LcmLog lcmLog) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, lcmToken);
        VmImageRequest ins = new VmImageRequest();
        ins.setAction("createImage");
        ins.getCreateImage().setName(imageName);
        Gson gson = new Gson();
        LOGGER.warn(gson.toJson(ins));
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(ins), headers);
        String url = basePath + String.format(Consts.APP_LCM_INSTANTIATE_IMAGE_URL, userId, hostIp, vmId);
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to create vm image  which vmId is {} exception {}", vmId, errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to create vm image  which vmId is {} exception {}", vmId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to create vm image  which vmId is {}", vmId);
        return null;

    }

    /**
     * getImageStatus.
     */
    public static String getImageStatus(String basePath, String hostIp, String userId, String imageId,
        String lcmToken) {
        String url = basePath + String.format(Consts.APP_LCM_GET_IMAGE_STATUS_URL, userId, hostIp, imageId);
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, lcmToken);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get image status which imageId is {} exception {}", imageId, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LcmResponseBody lcmResponseBody = gson.fromJson(response.getBody(), LcmResponseBody.class);
            return gson.toJson(lcmResponseBody.getData());
        }
        LOGGER.error("Failed to get image status which imageId is {}", imageId);
        return null;

    }

    /**
     * deleteVmImage.
     */
    public static boolean deleteVmImage(String basePath, String userId, String hostIp, String imageId,
        String token) {

        String url = basePath + String.format(Consts.APP_LCM_GET_IMAGE_STATUS_URL, userId, hostIp, imageId);
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        // delete images
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
            LOGGER.warn(response.getBody());
        } catch (RestClientException e) {
            LOGGER.error("Failed to delete image which imageId is {} exception {}", imageId, e.getMessage());
            return false;
        }
        return true;

    }

    /**
     * slice upload file.
     *
     * @param fileServerAddr File Server Address
     * @param chunk          File Chunk
     * @param filePath       File Path
     * @return upload result
     */
    public static boolean sliceUploadFile(String fileServerAddr, Chunk chunk, String filePath) {
        LOGGER.info("slice upload file, identifier = {}, chunkNum = {}", chunk.getIdentifier(), chunk.getChunkNumber());
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("part", new FileSystemResource(filePath));
        formData.add("priority", 0);
        formData.add("identifier", chunk.getIdentifier());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
        String url = fileServerAddr + Consts.SYSTEM_IMAGE_SLICE_UPLOAD_URL;

        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("slice upload file exception {}", errorLog);
            return false;
        } catch (Exception e) {
            LOGGER.error("slice upload file exception {}", e.getMessage());
            return false;
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("slice upload file failed!");
            return false;
        }

        return true;
    }

    /**
     * cancel slice upload file.
     *
     * @param fileServerAddr File Server Address
     * @param identifier     File Identifier
     * @return cancel result
     */
    public static boolean cancelSliceUpload(String fileServerAddr, String identifier) {
        LOGGER.info("cancel slice upload file, identifier = {}", identifier);
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("identifier", identifier);
        formData.add("priority", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
        String url = fileServerAddr + Consts.SYSTEM_IMAGE_SLICE_UPLOAD_URL;

        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("cancel slice upload file exception {}", errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("cancel slice upload file exception {}", e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("cancel slice upload file exception {}", e.getMessage());
            return false;
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("cancel slice upload file failed!");
            return false;
        }

        return true;
    }

    /**
     * slice merge file.
     *
     * @param fileServerAddr File Server Address
     * @param identifier     File Identifier
     * @param fileName       File Name
     * @param userId         User ID
     * @return merge result
     */
    public static String sliceMergeFile(String fileServerAddr, String identifier, String fileName, String userId) {
        LOGGER.info("slice merge file, identifier = {}, filename = {}", identifier, fileName);
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("userId", userId);
        formData.add("priority", 0);
        formData.add("identifier", identifier);
        formData.add("filename", fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
        String url = fileServerAddr + Consts.SYSTEM_IMAGE_SLICE_MERGE_URL;

        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("slice merge file success, resp = {}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("slice merge file exception {}", errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("slice merge file exception {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("slice merge file exception {}", e.getMessage());
            return null;
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("slice merge file failed!");
            return null;
        }

        LOGGER.info("slice merge file success, resp = {}", response);
        return response.getBody();
    }

    /**
     * delete system image.
     */
    public static boolean deleteSystemImage(String url) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete system image exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("delete system image file success, resp = {}", response);
            return true;
        }
        LOGGER.error("Failed to delete system image!");
        return false;
    }

    /**
     * downloadSystemImage.
     *
     * @param url url
     * @return
     */
    public static byte[] downloadSystemImage(String url) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
        } catch (RestClientException e) {
            LOGGER.error("Failed download system image exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("Download system image file success, resp = {}", response);
            return response.getBody();
        }
        LOGGER.error("Failed to download system image!");
        return null;
    }

    public static boolean imageSlim(String url) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (RestClientException e) {
            LOGGER.error("get image slim fail exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("get image slim  success, resp = {}", response);
            return true;
        }
        LOGGER.error("image slim fail!");
        return false;
    }

    public static FileSystemResponse queryImageCheck(String url) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
        } catch (RestClientException e) {
            LOGGER.error("get image info fail from filesystem exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("get image info success, resp = {}", response);
            try {
                return new ObjectMapper()
                    .readValue(Objects.requireNonNull(response.getBody()).getBytes(), FileSystemResponse.class);

            } catch (Exception e) {
                LOGGER.error("get image info fail from filesystem. {}", e.getMessage());
                return null;
            }
        }
        LOGGER.error("get image info fail from filesystem!");
        return null;
    }

    public static SshResponseInfo sendWebSshRequest(String basePath, String hostIp, int port, String username,
        String password, String xsrfValue) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("hostname", hostIp);
        formData.add("port", port);
        formData.add("username", username);
        formData.add("password", password);
        formData.add("_xsrf", xsrfValue);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Cookie", "_xsrf=" + xsrfValue);
        LOGGER.info("send WebSsh request url is {}", basePath);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(basePath, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("send WebSsh request, resp = {}", response);
        } catch (Exception e) {
            LOGGER.error("send WebSsh request exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("send WebSsh request success, resp = {}", response);
            try {
                return new ObjectMapper()
                    .readValue(Objects.requireNonNull(response.getBody()).getBytes(), SshResponseInfo.class);

            } catch (Exception e) {
                LOGGER.error("send WebSsh request fail from filesystem. {}", e.getMessage());
                return null;
            }
        }
        LOGGER.error("send WebSsh request fail from filesystem!");
        return null;
    }
}
