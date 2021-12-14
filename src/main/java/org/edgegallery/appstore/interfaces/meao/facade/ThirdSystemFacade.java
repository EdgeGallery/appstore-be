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

package org.edgegallery.appstore.interfaces.meao.facade;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("ThirdSystemFacade")
public class ThirdSystemFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThirdSystemFacade.class);

    private static final String QUERY_THIRD_SYSTEM_ERR_MESSAGES = "get third system fail.";

    @Value("${thirdSystem.url}")
    private String thirdSystemHost;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * query thirdSystem by type.
     *
     * @param type type
     * @return ThirdSystem
     */
    public ResponseEntity<List<ThirdSystem>> getThirdSystemByType(String type, String token) {
        String url = thirdSystemHost + Consts.THIRD_SYSTEM_URL + "/systemType/" + type;
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.GET, request, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(response.getStatusCode())) {
                List<ThirdSystem> ret = new Gson()
                    .fromJson(response.getBody(), new TypeToken<List<ThirdSystem>>() { }.getType());
                if (ret != null) {
                    return ResponseEntity.ok(ret);
                }
            }
            LOGGER.error("Failed to query meao info from third system, code is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("Failed to query meao info from third system, exception {}", e.getMessage());
        }
        throw new AppException(QUERY_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_QUERY_THIRD_SYSTEM_FAILED);
    }
}
