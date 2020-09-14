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

package org.edgegallery.developer.config.security;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.developer.common.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerTokenServicesConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Import({ResourceServerTokenServicesConfiguration.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AccessTokenFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenFilter.class);
    @Autowired
    TokenStore jwtTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String accessTokenStr = request.getHeader(Consts.ACCESS_TOKEN_STR);
        if (StringUtils.isEmpty(accessTokenStr)) {
            LOGGER.error("Access token is empty");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access token is empty");
            return;
        }
        OAuth2AccessToken accessToken = jwtTokenStore.readAccessToken(accessTokenStr);
        if (accessToken == null) {
            LOGGER.error("Invalid access token, token string is null");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token, token string is null.");
            return;
        }
        if (accessToken.isExpired()) {
            LOGGER.error("Access token expired");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access token expired");
            return;
        }
        Map<String, Object> additionalInfoMap = accessToken.getAdditionalInformation();
        if (additionalInfoMap == null) {
            LOGGER.error("Invalid access token, additional info map is null.");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token, additional info map is null.");
            return;
        }
        String userIdFromToken = additionalInfoMap.get("userId").toString();
        String userNameFromToken = additionalInfoMap.get("userName").toString();
        AccessUserUtil.setUser(userIdFromToken, userNameFromToken);
        String userIdFromRequest = request.getParameter("userId");
        if (!StringUtils.isEmpty(userIdFromRequest) && !userIdFromRequest.equals(userIdFromToken)) {
            LOGGER.error("Illegal userId");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Illegal userId");
            return;
        }
        String userNameFromRequest = request.getParameter("userName");

        if (!StringUtils.isEmpty(userNameFromRequest) && !userNameFromRequest.equals(userNameFromToken)) {
            LOGGER.error("Illegal userName");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Illegal userName");
            return;
        }
        OAuth2Authentication auth = jwtTokenStore.readAuthentication(accessToken);
        if (auth == null) {
            LOGGER.error("Invalid access token, authentication info is null.");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token, authentication info is null.");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
