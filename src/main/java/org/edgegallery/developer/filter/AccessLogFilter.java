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

package org.edgegallery.developer.filter;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLogFilter implements HttpServerFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogFilter.class);

    private ThreadLocal<String> localAccessId = new ThreadLocal<>();

    private static final int MAX_RESPONSE_BODY = 100;

    @Override
    public int getOrder() {
        return 1;
    }

    /**
     * Do log after receive request.
     */
    @Override
    public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx httpServletRequestEx) {
        String accessId = UUID.randomUUID().toString();
        localAccessId.set(accessId);
        //mark log
        HttpRequestTraceLog traceLog = new HttpRequestTraceLog();
        traceLog.setAccessId(accessId);
        traceLog.setPath(httpServletRequestEx.getRequestURI());
        traceLog.setMethod(httpServletRequestEx.getMethod());
        traceLog.setTime(LocalDateTime.now().toString());
        traceLog.setParameterMap(new Gson().toJson(httpServletRequestEx.getParameterMap()));

        // Need to check whether sensitive data needs to be filtered
        traceLog.setRequestBody(httpServletRequestEx.getBodyBuffer().toString());
        Enumeration<String> headerNames = httpServletRequestEx.getHeaderNames();
        Map<String, String> headerMap = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = httpServletRequestEx.getHeader(name);
            headerMap.put(name, value);
        }
        traceLog.setHeaders(headerMap);
        LOGGER.info("Http request trace log: {}", new Gson().toJson(traceLog));
        return null;
    }

    /**
     * Do log before send response.
     */
    public void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
        HttpResponseTraceLog responseLog = new HttpResponseTraceLog();
        responseLog.setAccessId(localAccessId.get());
        responseLog.setStatus(responseEx.getStatus());
        responseLog.setTime(LocalDateTime.now().toString());
        String body = responseEx.getBodyBuffer().toString();
        if (body.length() > MAX_RESPONSE_BODY) {
            responseLog.setBody(body.substring(0, MAX_RESPONSE_BODY) + "...");
        } else {
            responseLog.setBody(body);
        }
        LOGGER.info("Http response trace log: {}", new Gson().toJson(responseLog));
    }

    @Setter
    @Getter
    private static class HttpRequestTraceLog {
        private String accessId;
        private String path;

        private String userId;

        private Map<String, String> headers;

        private String parameterMap;

        private String method;

        private String time;

        private String requestBody;
    }

    @Setter
    @Getter
    private static class HttpResponseTraceLog {
        private String accessId;
        private Integer status;
        private String time;
        private String body;
    }

    public void unload() {
        localAccessId.remove();
    }
}
