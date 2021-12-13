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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import javax.ws.rs.core.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
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

public class AtpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AtpUtil.class);

    private static final String ATP_ADDRESS = "atp_address";

    private static final RestTemplate restTemplate = new RestTemplate();

    private AtpUtil() {
        throw new IllegalStateException("AtpUtil class");
    }

    /**
     * send request to atp to create test task.
     *
     * @param filePath csar file path
     * @param token request token
     * @return response from atp
     */
    public static String sendCreatTask2Atp(String filePath, String token) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        LOGGER.info("filePath: {}", filePath);
        body.add("file", new FileSystemResource(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = InitConfigUtil.getProperties(ATP_ADDRESS).concat(Consts.CREATE_TASK_FROM_ATP);
        LOGGER.info("url: {}", url);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return response.getBody();
            }
            LOGGER.error("Create instance from atp failed,  status is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("Failed to create instance from atp,  exception {}", e.getMessage());
        }

        throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR, "Create instance from atp failed.");
    }

    /**
     * get task status by taskId from atp.
     *
     * @param taskId taskId
     * @return task status
     */
    public static String getTaskStatusFromAtp(String taskId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = InitConfigUtil.getProperties(ATP_ADDRESS).concat(String.format(Consts.GET_TASK_FROM_ATP, taskId));
        LOGGER.info("get task status frm atp, url: {}", url);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            LOGGER.error("Get task status from atp reponse failed, the taskId is {}, The status code is {}",
                taskId, response.getStatusCode());
            throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR,
                "Get task status from atp reponse failed.");
        }

        JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
        String status = jsonObject.get("status").getAsString();
        LOGGER.info("status: {}", status);
        return status;
    }

}
