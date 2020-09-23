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

package org.edgegallery.developer.exception;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class RestReturnWrapper implements ResponseBodyAdvice<Object> {

    /**
     * Determine which request to execute beforeBodyWrite, return true to execute, return false to not execute.
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        //
        return true;
    }

    /**
     * Process the body, request, response and other requests before returning.
     *
     * @param body
     * @param methodParameter
     * @param mediaType
     * @param httpMessageConverter
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    @ResponseBody
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType,
        Class<? extends HttpMessageConverter<?>> httpMessageConverter, ServerHttpRequest serverHttpRequest,
        ServerHttpResponse serverHttpResponse) {
        if (body instanceof RestReturn) {
            RestReturn results = (RestReturn) body;
            HttpStatus status = HttpStatus.valueOf(results.getCode());
            serverHttpResponse.setStatusCode(status);
            return body;
        }
        return body;
    }
}