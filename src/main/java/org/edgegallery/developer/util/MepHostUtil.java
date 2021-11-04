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

package org.edgegallery.developer.util;

import com.google.gson.Gson;
import java.io.File;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.edgegallery.developer.common.Consts;
import org.edgegallery.developer.exception.CustomException;
import org.edgegallery.developer.model.lcm.MecHostBody;
import org.edgegallery.developer.model.resource.mephost.EnumVimType;
import org.edgegallery.developer.model.resource.mephost.MepHost;
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

public final class MepHostUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MepHostUtil.class);

    private static CookieStore cookieStore = new BasicCookieStore();

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * addMecHostToLcm.
     *
     * @param host request body
     * @return
     */
    public static boolean addMecHostToLcm(MepHost host) {
        MecHostBody body = new MecHostBody();
        body.setAffinity(host.getArchitecture());
        body.setCity(host.getAddress());
        body.setMechostIp(host.getMecHostIp());
        body.setMechostName(host.getName());
        if (host.getVimType().equals(EnumVimType.OpenStack) || host.getVimType().equals(EnumVimType.FusionSphere)) {
            body.setVim("OpenStack");
        } else {
            body.setVim("K8s");
        }
        body.setOrigin("developer");
        // add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(body), headers);
        String url = getUrlPrefix(host.getLcmProtocol(), host.getLcmIp(), host.getLcmPort())
            + Consts.APP_LCM_ADD_MECHOST;
        LOGGER.info("add mec host url:{}", url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("add mec host to lcm log:{}", response);
        } catch (CustomException e) {
            LOGGER.error("Failed add mec host to lcm exception {}", e.getBody());
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed add mec host to lcm exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed add mec host to lcm");
        return false;
    }

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }

    public static boolean uploadFileToLcm(String protocol, String lcmIp, int port, String filePath, String mecHost,
        String token) {
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + filePath);
        RestTemplate restTemplate = RestTemplateBuilder.create();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("configFile", new FileSystemResource(file));
        body.add("hostIp", mecHost);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            String url = getUrlPrefix(protocol, lcmIp, port) + Consts.APP_LCM_UPLOAD_FILE;
            LOGGER.info(" upload file url is {}", url);
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("upload file lcm log:{}", response);
        } catch (Exception e) {
            LOGGER.error("Failed to upload file lcm, exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to upload file lcm, filePath is {}", filePath);
        return false;
    }

}
