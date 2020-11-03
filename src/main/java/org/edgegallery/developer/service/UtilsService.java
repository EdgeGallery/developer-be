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

package org.edgegallery.developer.service;

import com.spencerwi.either.Either;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.developer.response.FormatRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component("utilsService")
public class UtilsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilsService.class);

    @Value("${appstore.address}")
    private String appstoreAddress;

    /**
     * upload app to appstore.
     */
    public Either<FormatRespDto, String> storeToAppStore(Map<String, Object> params, String userId, String userName,
        String token) {
        RestTemplate restTemplate = RestTemplateBuilder.create();
        if (params == null || params.size() == 0) {
            LOGGER.error("failed to call the apptstore microservice interface, params can not be null");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "params can not be null"));
        }
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        params.forEach(map::add);

        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        try {
            String url = String
                .format("%s/mec/appstore/v1/apps?userId=%s&userName=%s", appstoreAddress, userId, userName);
            ResponseEntity<String> responses = restTemplate
                .exchange(url, HttpMethod.POST, new HttpEntity<>(map, headers), String.class);
            return Either.right(responses.getBody());
        } catch (InvocationException e) {
            LOGGER.error("failed to call the apptstore microservice interface", e.getMessage());
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "failed to call appstore interface"));
        }
    }
}