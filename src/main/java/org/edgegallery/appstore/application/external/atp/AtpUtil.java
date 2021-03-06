/* Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.application.external.atp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Objects;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("AtpUtil")
public class AtpUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(AtpUtil.class);

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String ATP_STATUS = "status";

    @Value("${atp.urls.create-task}")
    private String createTaskUrl;

    @Value("${atp.urls.query-task}")
    private String queryTaskUrl;

    /**
     * send request to atp to create test task.
     *
     * @param filePath csar file path
     * @param token request token
     * @return response from atp
     */
    public AtpTestDto sendCreateTask2Atp(String filePath, String token) {
        if (filePath == null || token == null) {
            LOGGER.error("Failed to validate input parameters of ATP task creation");
            return new AtpTestDto();
        }
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        LOGGER.info("filePath: {}", filePath);
        body.add("file", new FileSystemResource(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("access_token", token);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = createTaskUrl;
        LOGGER.info("url: {}", url);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(response.getStatusCode())) {
                JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
                String id = jsonObject.get("id").getAsString();
                String status = jsonObject.get(ATP_STATUS).getAsString();
                LOGGER.info("Successfully created test task {}, status is {}", id, status);
                return new AtpTestDto(id, status);
            }
            LOGGER.error("Failed to create instance from atp,  status is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("Failed to create instance from atp,  exception {}", e.getMessage());
        }

        throw new AppException("Failed to create instance from atp.", ResponseConst.RET_CREATE_TEST_TASK_FAILED);
    }

    /**
     * get task status by taskId from atp.
     *
     * @param taskId taskId
     * @param token token
     * @return task status
     */
    public String getTaskStatusFromAtp(String taskId, String token) {
        if (taskId == null || token == null) {
            LOGGER.error("Failed to validate input parameters for task status");
            return "";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = String.format(queryTaskUrl, taskId);
        LOGGER.info("Get task status frm atp, url: {}", url);
        String status = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get task status from atp response, the taskId is {}, The status code is {}",
                    taskId, response.getStatusCode());
                throw new AppException("Failed to get task status from atp response.",
                    ResponseConst.RET_GET_TEST_STATUS_FAILED);
            }

            JsonObject jsonResp = new JsonParser().parse(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            if (jsonResp.has(ATP_STATUS)) {
                status = jsonResp.get(ATP_STATUS).getAsString();
                LOGGER.info("Get task status: {}", status);
            } else {
                LOGGER.error("Failed to get task status, response does not have status info.");
            }

        } catch (RestClientException | NullPointerException e) {
            LOGGER.error("Failed to get task status from atp which taskId is {} exception {}", taskId, e.getMessage());
        }
        return status;
    }
}
