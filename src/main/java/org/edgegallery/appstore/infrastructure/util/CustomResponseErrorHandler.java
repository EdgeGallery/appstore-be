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

package org.edgegallery.appstore.infrastructure.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

public class CustomResponseErrorHandler implements ResponseErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomResponseErrorHandler.class);

    private final ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return errorHandler.hasError(response);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        String body = convertStreamToString(response.getBody());

        try {
            errorHandler.handleError(response);
        } catch (RestClientException scx) {
            throw new CustomException(scx.getMessage(), scx, body);
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = null;
        if (is != null) {
            reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        }
        StringBuilder sb = new StringBuilder();
        if (reader != null) {
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                LOGGER.error("read input stream to string exception: {}", e.getMessage());
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("close stream exception: {}", e.getMessage());
                }
            }
        }

        if (!StringUtils.isEmpty(sb)) {
            return sb.toString();
        }

        return "";
    }
}
