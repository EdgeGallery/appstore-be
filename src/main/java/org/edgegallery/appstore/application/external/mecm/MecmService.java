/* Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.application.external.mecm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.Release;
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

@Service("mecmService")
public class MecmService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MecmService.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String MECM_GET_MECHOSTS = "/north/v1/mechosts";

    private static final String MECM_UPLOAD_PACKAGE = "/north/v1/tenants/%s/package";

    private static final String MECM_GET_DEPLOYMENT_STATUS = "/north/v1/tenants/%s/packages/%s";

    private static final String MECM_DELETE_PACKAGE = "/north/v1/tenants/%s/packages/%s";

    @Value("${mecm.urls.north}")
    private String northUrl;

    /**
     * upload package to mecm-apm.
     *
     * @param token access token
     * @param release app package info
     * @param hostList mec host
     * @param tenantId user id
     * @return Mecm package id
     */
    public String upLoadPackageToNorth(String token, Release release, String hostList, String tenantId,
        String params) {
        if (StringUtils.isEmpty(token) || release == null || StringUtils.isEmpty(tenantId)) {
            LOGGER.error("Failed to validate input parameters of north task creation.");
            return null;
        }
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(release.getPackageFile().getStorageAddress()));
        body.add("hostList", hostList);
        body.add("appPkgName", release.getAppBasicInfo().getAppName());
        body.add("appPkgVersion", release.getAppBasicInfo().getVersion());
        body.add("appClass", release.getDeployMode());
        body.add("parameters", params);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = northUrl.concat(String.format(MECM_UPLOAD_PACKAGE, tenantId));
        LOGGER.info("the url is:{}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get http response from MECM, status is {}", response.getStatusCode());
                return null;
            }
            JsonObject jsonBody = new JsonParser().parse(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            String message = jsonBody.get("message").getAsString();
            if (message.equalsIgnoreCase("Failed to create server")) {
                LOGGER.error("Failed to create server.");
                return null;
            }
            return jsonBody.get("mecmPackageId").getAsString();
        } catch (RestClientException | NullPointerException e) {
            LOGGER.error("Failed to upload package to north, exception {}", e.getMessage());
        }
        return null;
    }

    /**
     * query mecm deployment status of one app package.
     *
     * @param token access token
     * @param mecmAppPackageId app package info
     * @param tenantId user id
     * @return Mecm deployment info
     */
    public MecmDeploymentInfo getDeploymentStatus(String token, String mecmAppPackageId, String tenantId) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(mecmAppPackageId) || StringUtils.isEmpty(tenantId)) {
            LOGGER.error("UserId or mecmPackageId or token is empty.");
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = northUrl.concat(String.format(MECM_GET_DEPLOYMENT_STATUS, tenantId, mecmAppPackageId));
        LOGGER.info("the url is {}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.GET, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get deployment status from north. The status code is {}",
                    response.getStatusCode());
                return null;
            }
            JsonObject jsonBody = new JsonParser().parse(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            JsonArray jsonData = jsonBody.get("data").getAsJsonArray();
            if (jsonData.size() > 0) {
                String status = jsonData.get(0).getAsJsonObject().get("status").getAsString();
                MecmDeploymentInfo mecmDeploymentInfo = new MecmDeploymentInfo();
                if (StringUtils.isEmpty(status)) {
                    LOGGER.info("Mecm app deployment status is null.");
                    return null;
                }
                mecmDeploymentInfo.setMecmOperationalStatus(status);
                mecmDeploymentInfo.setMecmAppPackageId(mecmAppPackageId);
                LOGGER.info("mecmAppPackageId is {}, status is {}", mecmAppPackageId, status);
                return mecmDeploymentInfo;
            }
        } catch (RestClientException | NullPointerException e) {
            LOGGER.error("Get deployment status from north exception {}", e.getMessage());
        }
        return null;
    }

    /**
     * get all mecm hosts.
     *
     * @param token access token
     * @return mecm host list
     */
    public List<Map<String, Object>> getAllMecHosts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = northUrl.concat(MECM_GET_MECHOSTS);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.GET, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get mechosts from mecm, The status code is {}", response.getStatusCode());
                throw new AppException("Failed to get mechosts from mecm.", ResponseConst.RET_GET_MECMHOST_FAILED);
            }
            JsonObject jsonBody = new JsonParser().parse(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            JsonArray hostInfo = jsonBody.get("data").getAsJsonArray();
            return new Gson().fromJson(hostInfo, List.class);
        } catch (RestClientException | NullPointerException e) {
            LOGGER.error("Failed to get mechosts, RestClientException is {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * upload package to mecm-apm.
     *
     * @param userId user id
     * @param packageId appstore package id
     * @param token access token
     * @return delete server success or not
     */
    public String deleteServer(String userId, String packageId, String token) {
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(packageId) || StringUtils.isEmpty(token)) {
            LOGGER.error("UserId or packageId or token is empty.");
            return "UserId or packageId or token is empty";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = northUrl.concat(String.format(MECM_DELETE_PACKAGE, userId, packageId));
        LOGGER.info("The url is :{}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get response within Delete Server Interface.");
                return "Failed";
            }
            JsonObject jsonBody = new JsonParser().parse(Objects.requireNonNull(response.getBody())).getAsJsonObject();
            JsonArray jsonData = jsonBody.get("data").getAsJsonArray();
            if (jsonData.size() > 0) {
                return jsonData.get(0).getAsJsonObject().get("message").getAsString();
            }
        } catch (RestClientException | NullPointerException e) {
            LOGGER.error("Failed to get response within Delete Server Interface, exception {}", e.getMessage());
        }
        return null;
    }
}
