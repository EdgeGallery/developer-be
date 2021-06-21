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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.CustomException;
import org.edgegallery.developer.exception.DomainException;
import org.edgegallery.developer.model.LcmLog;
import org.edgegallery.developer.model.lcm.DistributeBody;
import org.edgegallery.developer.model.lcm.DistributeResponse;
import org.edgegallery.developer.model.lcm.InstantRequest;
import org.edgegallery.developer.model.vm.VmCreateConfig;
import org.edgegallery.developer.model.vm.VmImageRequest;
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
import org.springframework.util.StringUtils;
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
    public static boolean instantiateApplication(String basePath, String appInstanceId, String userId, String token,
        LcmLog lcmLog, String pkgId, String mecHost, Map<String, String> inputParams) {
        //before instantiate ,call distribute result interface
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        String disRes = getDistributeRes(basePath, userId, token, pkgId);
        if (StringUtils.isEmpty(disRes)) {
            LOGGER.error("instantiateApplication get pkg distribute res failed!");
            return false;
        }
        //parse dis res
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<List<DistributeResponse>>() { }.getType();
        List<DistributeResponse> list = gson.fromJson(disRes, typeEvents);
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
        String url = basePath + Consts.APP_LCM_INSTANTIATE_APP_URL.replaceAll("appInstanceId", appInstanceId)
            .replaceAll("tenantId", userId);
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
        String url = basePath + Consts.APP_LCM_UPLOAD_APPPKG_URL.replaceAll("tenantId", userId);
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
            return response.getBody();
        }
        LOGGER.error("Failed to upload pkg!");
        return null;
    }

    /**
     * distribute pkg.
     */
    public static boolean distributePkg(String basePath, String userId, String token, String packageId, String mecHost,
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
        String url = basePath + Consts.APP_LCM_DISTRIBUTE_APPPKG_URL.replaceAll("tenantId", userId)
            .replaceAll("packageId", packageId);
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
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed distribute pkg packageId is {} exception {}", packageId, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to distribute pkg which packageId is {}", packageId);
        return false;
    }

    /**
     * delete host.
     */
    public static boolean deleteHost(String basePath, String userId, String token, String pkgId, String hostIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = basePath + Consts.APP_LCM_DELETE_HOST_URL.replaceAll("tenantId", userId)
            .replaceAll("packageId", pkgId).replaceAll("hostIp", hostIp);
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
        String url = basePath + Consts.APP_LCM_DELETE_APPPKG_URL.replaceAll("tenantId", userId)
            .replaceAll("packageId", pkgId);
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
        String url = basePath + Consts.APP_LCM_DISTRIBUTE_APPPKG_URL.replaceAll("tenantId", userId)
            .replaceAll("packageId", pkgId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
            LOGGER.info("APPlCM get distribute res log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed get distribute res pkgId is {} exception {}", pkgId, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
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
        String url = basePath + Consts.APP_LCM_TERMINATE_APP_URL.replaceAll("appInstanceId", appInstanceId)
            .replaceAll("tenantId", userId);
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
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getWorkloadEvents(String protocol, String ip, int port, String appInstanceId, String userId,
        String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_WORKLOAD_EVENTS_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId);
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
    public static String getHealth(String protocol, String ip, int port) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_HEALTH;
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

    /**
     * vmInstantiateImage.
     */
    public static String vmInstantiateImage(String basePath, String userId, String lcmToken, String vmId,
        String appInstanceId, LcmLog lcmLog) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, lcmToken);
        VmImageRequest ins = new VmImageRequest();
        ins.setVmId(vmId);
        Gson gson = new Gson();
        LOGGER.warn(gson.toJson(ins));
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(ins), headers);
        String url = basePath + Consts.APP_LCM_INSTANTIATE_IMAGE_URL.replaceAll("appInstanceId", appInstanceId)
            .replaceAll("tenantId", userId);
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to create vm image  which appInstanceId is {} exception {}", appInstanceId, errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Failed to create vm image  which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to create vm image  which appInstanceId is {}", appInstanceId);
        return null;

    }

    /**
     * getImageStatus.
     */
    public static String getImageStatus(String basePath, String appInstanceId, String userId, String imageId,
        String lcmToken) {
        String url = basePath + Consts.APP_LCM_GET_IMAGE_STATUS_URL.replaceAll("appInstanceId", appInstanceId)
            .replaceAll("tenantId", userId).replaceAll("imageId", imageId);
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
            return response.getBody();
        }
        LOGGER.error("Failed to get image status which imageId is {}", imageId);
        return null;

    }

    /**
     * downloadVmImage.
     */
    public static boolean downloadVmImage(String basePath, String userId, String packagePath, String appInstanceId,
        String imageId, String imageName, String chunkNum, String token) {

        String url = basePath + Consts.APP_LCM_GET_IMAGE_DOWNLOAD_URL.replaceAll("appInstanceId", appInstanceId)
            .replaceAll("tenantId", userId).replaceAll("imageId", imageId);
        LOGGER.info("url is {}", url);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setConnection("close");
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        headers.set("chunk_num", chunkNum);
        // download images
        ResponseEntity<byte[]> response;
        try {

            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            //            LOGGER.warn(response.getBody());
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("download file error, response is {}", response.getBody());
                throw new DomainException("download file exception");
            }
            byte[] result = response.getBody();
            if (result == null) {
                throw new DomainException("download response is null");
            }
            String fileName = "temp_" + chunkNum;
            String outPath = packagePath + File.separator + imageName;
            LOGGER.info("output image path:{}", outPath);
            File imageDir = new File(outPath);
            if (!imageDir.exists()) {
                boolean isMk = imageDir.mkdirs();
                if (!isMk) {
                    LOGGER.error("create upload path failed");
                    return false;
                }
            }
            File file = new File(outPath + File.separator + fileName);
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.error("create download file error");
                throw new DomainException("create download file error");
            }
            try (InputStream inputStream = new ByteArrayInputStream(result);
                 OutputStream outputStream = new FileOutputStream(file)) {
                int len = 0;
                byte[] buf = new byte[1024];
                while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
            }
        } catch (RestClientException | IOException e) {

            LOGGER.error("Failed to get image  which chunkNum is {} exception {}", chunkNum, e.getMessage());
            return false;
        }

        return true;

    }

    /**
     * deleteVmImage.
     */
    public static boolean deleteVmImage(String protocol, String ip, int port, String userId, String appInstanceId,
        String imageId, String token) {

        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_IMAGE_DELETE_URL
            .replaceAll("appInstanceId", appInstanceId).replaceAll("tenantId", userId).replaceAll("imageId", imageId);
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
     * upload system image.
     */
    public static String uploadSystemImage(String fileServerAddr, String filePath, String userId) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", new FileSystemResource(filePath));
        formData.add("userId", userId);
        formData.add("priority", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);
        String url = fileServerAddr + Consts.SYSTEM_IMAGE_UPLOAD_URL;

        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("upload system image file success, resp = {}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("Failed upload system image exception {}", errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Failed upload system image exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to upload system image!");
        return null;
    }

    /**
     * delete system image.
     */
    public static Boolean deleteSystemImage(String url) {
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

    /**
     * checkImageInfo.
     *
     * @param systemPath systemPath
     * @return
     */
    public static Boolean checkImageInfo(String systemPath) {
        String url = systemPath.substring(0, systemPath.length() - 16);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
        } catch (RestClientException e) {
            LOGGER.error("get system image exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("get image file success, resp = {}", response);
            return true;
        }
        LOGGER.error("get system image fail!");
        return false;

    }
}
