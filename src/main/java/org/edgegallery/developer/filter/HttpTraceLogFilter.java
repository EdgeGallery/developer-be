/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

@Component
public class HttpTraceLogFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpTraceLogFilter.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        String accessId = UUID.randomUUID().toString();
        if (!(request.getRequestURI().equals("/health"))) {
            try {
                logForRequest(accessId, request);
                filterChain.doFilter(request, response);
            } finally {
                logForResponse(accessId, response);
                updateResponse(response);
            }
        }

    }

    private void logForRequest(String accessId, HttpServletRequest request) {
        HttpRequestTraceLog requestTraceLog = new HttpRequestTraceLog();
        requestTraceLog.setAccessId(accessId);
        requestTraceLog.setTime(LocalDateTime.now().toString());
        requestTraceLog.setPath(request.getRequestURI());
        requestTraceLog.setMethod(request.getMethod());
        requestTraceLog.setParameterMap(new Gson().toJson(request.getParameterMap()));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Http request trace log: {}", new Gson().toJson(requestTraceLog));
        }
    }

    private void logForResponse(String accessId, HttpServletResponse response) {
        HttpResponseTraceLog responseTraceLog = new HttpResponseTraceLog();
        responseTraceLog.setAccessId(accessId);
        responseTraceLog.setStatus(response.getStatus());
        responseTraceLog.setTime(LocalDateTime.now().toString());
        if (!responseTraceLog.isGoodResponse()) {
            responseTraceLog.body = getErrorMessage((ContentCachingResponseWrapper) response);
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Http response trace log: {}", new Gson().toJson(responseTraceLog));
        }
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils
            .getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getErrorMessage(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = WebUtils
            .getNativeResponse(response, ContentCachingResponseWrapper.class);
        String result = "";
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    String payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                    logger.error("read paylod is" + payload);
                    JsonElement element = JsonParser.parseString(payload).getAsJsonObject().get("message");
                    if (element != null && !element.isJsonNull()) {
                        result = element.getAsString();
                    }
                } catch (UnsupportedEncodingException e) {
                    result = "read response body exception";
                }
            }
        }
        return result;
    }

    @Setter
    @Getter
    private static class HttpRequestTraceLog {
        private String accessId;

        private String path;

        private String userId;

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

        /**
         * bad or good response.
         *
         * @return ture of false
         */
        public boolean isGoodResponse() {
            return status == HttpStatus.OK.value();
        }
    }
}
