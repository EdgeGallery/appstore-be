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

package org.edgegallery.appstore.interfaces;

import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
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

    private static final String[] NO_NEED_TOKEN_URLS = {
        "GET /health", "POST /mec/appstore/v1/messages",
        "GET /mec/appstore/v1/packages/[\\w]{0,32}/action/download-package",
        "GET /mec/appstore/v1/packages/[\\w]{0,32}/action/download-icon",
        "GET /mec/appstore/v1/packages/pullable",
        "GET /mec/appstore/v2/packages/pullable"
    };

    private static final String USERID = "userId";

    private static final String USERNAME = "userName";

    private static final String AUTHORITIES = "authorities";

    @Autowired
    TokenStore jwtTokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (shouldFilter(request)) {
            String accessTokenStr = request.getHeader("access_token");
            if (StringUtils.isEmpty(accessTokenStr)) {
                LOGGER.error("Access token is empty, url is {}",
                    String.format("%s %s", request.getMethod(), request.getRequestURI()));
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access token is empty");
                return;
            }
            OAuth2AccessToken accessToken = jwtTokenStore.readAccessToken(accessTokenStr);
            if (accessToken == null) {
                LOGGER.error("Invalid access token.");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token.");
                return;
            }
            if (accessToken.isExpired()) {
                LOGGER.error("Access token expired");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Access token expired");
                return;
            }
            Map<String, Object> additionalInfoMap = accessToken.getAdditionalInformation();
            if (additionalInfoMap == null) {
                LOGGER.error("Invalid access token, Additional is null.");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token, Additional is null.");
                return;
            }
            String authorityToken = additionalInfoMap.get(AUTHORITIES).toString();
            String userIdFromToken = additionalInfoMap.get(USERID).toString();
            String userNameFromToken = additionalInfoMap.get(USERNAME).toString();
            if (!checkUserValid(request, response, userIdFromToken, userNameFromToken)) {
                return;
            }
            OAuth2Authentication auth = jwtTokenStore.readAuthentication(accessToken);
            if (auth == null) {
                LOGGER.error("Invalid access token");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid access token");
                return;
            }
            request.setAttribute(AUTHORITIES, authorityToken);
            request.setAttribute(USERID, userIdFromToken);
            request.setAttribute(USERNAME, userNameFromToken);
            request.setAttribute("access_token", accessTokenStr);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldFilter(HttpServletRequest request) {
        if (request.getRequestURI() == null) {
            return true;
        }
        String accessUrl = String.format("%s %s", request.getMethod(), request.getRequestURI());
        for (String filter : NO_NEED_TOKEN_URLS) {
            if (accessUrl.matches(filter)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkUserValid(HttpServletRequest request, HttpServletResponse response, String userIdFromToken,
        String userNameFromToken) throws IOException {
        String userIdFromRequest = request.getParameter(USERID);
        if (!StringUtils.isEmpty(userIdFromRequest) && !userIdFromRequest.equals(userIdFromToken)) {
            LOGGER.error("Illegal userId");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Illegal userId");
            return false;
        }
        String userNameFromRequest = request.getParameter(USERNAME);
        if (!StringUtils.isEmpty(userNameFromRequest) && !userNameFromRequest.equals(userNameFromToken)) {
            LOGGER.error("Illegal userName");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Illegal userName");
            return false;
        }
        return true;
    }
}
