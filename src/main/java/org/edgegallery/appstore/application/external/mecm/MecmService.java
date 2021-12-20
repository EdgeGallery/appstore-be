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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
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

    private static final String APM_DELETE_EDGE_PACKAGE = "/apm/v1/tenants/%s/packages/%s/hosts/%s";

    private static final String APM_DELETE_APM_PACKAGE = "/apm/v1/tenants/%s/packages/%s";

    private static final String APPO_INSTANTIATE_APP = "/appo/v1/tenants/%s/app_instances/%s";

    private static final String MECM_GET_MECHOSTS = "/north/v1/mechosts";

    private static final String MECM_UPLOAD_PACKAGE = "/north/v1/tenants/%s/package";

    private static final String MECM_GET_DEPLOYMENT_STATUS = "/north/v1/tenants/%s/packages/%s";

    private static final String MECM_DELETE_PACKAGE = "/north/v1/tenants/%s/packages/%s";

    @Value("${mecm.urls.apm}")
    private String apmUrl;

    @Value("${mecm.urls.appo}")
    private String appoUrl;

    @Value("${mecm.urls.inventory}")
    private String inventoryUrl;

    @Value("${mecm.urls.north}")
    private String northUrl;

    /**
     * upload package to mecm-apm.
     *
     * @param token access token
     * @param release app package info
     * @param hostList mec host
     * @param tenantId user id
     * @return MecmInfo
     */
    public String upLoadPackageToMecmNorth(String token, Release release, String hostList, String tenantId,
        String params) {
        String appPkgName = release.getPackageFile().getOriginalFileName();
        String appPkgVersion = release.getAppBasicInfo().getVersion();
        String appClass = release.getDeployMode();
        String filePath = release.getPackageFile().getStorageAddress();
        if (StringUtils.isEmpty(appPkgName) || StringUtils.isEmpty(appPkgVersion) || StringUtils.isEmpty(filePath)) {
            LOGGER.error("[Upload to MECM], Failed to validate input parameters of MECM task creation, status is {}",
                ResponseConst.RET_PARAM_INVALID);
            return null;
        }
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));
        body.add("hostList", hostList);
        body.add("appPkgName", appPkgName);
        body.add("appPkgVersion", appPkgVersion);
        body.add("appClass", appClass);
        body.add("params", params);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = northUrl.concat(String.format(MECM_UPLOAD_PACKAGE, tenantId));
        LOGGER.error("[Upload to MECM] The url is:{}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("[Upload to MECM] The reponse is {}", response);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error(
                    "[Upload to MECM] [Http Request Failed] Failed to get http reponse from MECM, status is {}",
                    response.getStatusCode());
                return null;
            }
            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            String message = jsonObject.get("message").getAsString();
            if (message.equalsIgnoreCase("Failed to create server")) {
                LOGGER.error("[Upload to MECM], MECM Failed to create server");
                return null;
            }
            String mecmPkgId = jsonObject.get("mecmPackageId").getAsString();
            return mecmPkgId;
        } catch (RestClientException e) {
            LOGGER.error("[Upload to MECM], Failed to upload package to MECM,  exception {}", e.getMessage());
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
    public MecmDeploymentInfo getMecmDepolymentStatus(String token, String mecmAppPackageId, String tenantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = northUrl.concat(String.format(MECM_GET_DEPLOYMENT_STATUS, tenantId, mecmAppPackageId));
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.GET, request, String.class);
            LOGGER.info("[Get MECM status], after http request, the reponse is: {}", response);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("[Get MECM status] Failed to get Deployment Status from MECM. The status code is {}",
                    response.getStatusCode());
                return null;
            }
            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            String status = jsonObject.get("data").getAsJsonArray().get(0).getAsJsonObject().get("status")
                .getAsString();
            MecmDeploymentInfo mecmDeploymentInfo = new MecmDeploymentInfo();
            if (StringUtils.isEmpty(status)) {
                LOGGER.error("[Get MECM status] Response status is null.");
                return null;
            }
            mecmDeploymentInfo.setMecmOperationalStatus(status);
            mecmDeploymentInfo.setMecmAppPackageId(mecmAppPackageId);
            return mecmDeploymentInfo;
        } catch (RestClientException e) {
            LOGGER.error("[Get MECM status], get Deployment Status from MECM exception {}", e.getMessage());
        }
        return null;
    }

    /**
     * get all mecm hosts.
     *
     * @param token access token
     * @return mecm host list
     */
    public List<Map<String, Object>> getAllMecmHosts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = northUrl.concat(MECM_GET_MECHOSTS);
        LOGGER.error("[Get All MecmHost], The url is:{}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.GET, request, String.class);
            LOGGER.error("[Get All MecmHost], The response is:{}", response);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("[Get All MecmHost], Failed to get mechosts from mecm inventory, The status code is {}",
                    response.getStatusCode());
                throw new AppException("[Get All MecmHost], Failed to get mechosts from mecm inventory.",
                    ResponseConst.RET_GET_MECMHOST_FAILED);
            }

            LOGGER.error("[Get All MecmHost]. Start to return response body");
            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            LOGGER.error("[Get All MecmHost]. JsonObject is:{}", jsonObject);
            JsonArray hostInfo = jsonObject.get("data").getAsJsonArray();
            LOGGER.error("[Get All MecmHost]. hostObject is:{}", hostInfo);
            List<Map<String, Object>> hostList = new Gson().fromJson(hostInfo, List.class);
            LOGGER.error("[Get All MecmHost]. List is:{}", hostList);
            return hostList;
        } catch (RestClientException e) {
            LOGGER.error("[Get All MecmHost], Failed to get mechosts, RestClientException is {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * get mec host by ip list.
     *
     * @param token access token
     * @param mecHostIpList mec host ip list
     * @return mec host map
     */
    public Map<String, MecHostBody> getMecHostByIpList(String token, List<String> mecHostIpList) {
        List<Map<String, Object>> allMecHosts = getAllMecmHosts(token);
        Map<String, MecHostBody> mecHostMap = new HashMap<>();
        for (String mecHostIp : mecHostIpList) {
            Optional<Map<String, Object>> mecHostInfo = allMecHosts.stream()
                .filter(mecHostInfoMap -> mecHostIp.equalsIgnoreCase((String) mecHostInfoMap.get("mechostIp")))
                .findFirst();
            if (!mecHostInfo.isPresent()) {
                continue;
            }
            Map<String, Object> mecHostInfoMap = mecHostInfo.get();
            MecHostBody mecHost = new MecHostBody();
            mecHost.setMechostIp(mecHostIp);
            mecHost.setCity((String) mecHostInfoMap.get("mechostCity"));
            mecHostMap.put(mecHostIp, mecHost);
        }
        return mecHostMap;
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
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(packageId)) {
            LOGGER.error("[Delete Server] UserId or PacakgeId is empty. ");
            return "UserId or PacageId is empty";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = northUrl.concat(String.format(MECM_DELETE_PACKAGE, userId, packageId));
        LOGGER.error("[Delete Server] The url is :{}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("[Delete Server] Failed to get response within Delete Server Interface");
                return "Failed";
            }
            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            String msg = jsonObject.get("data").getAsJsonArray().get(0).getAsJsonObject().get("message").getAsString();
            return msg;
        } catch (RestClientException e) {
            LOGGER.error("[Delete Server] Exception. Failed to get response within Delete Server Interface");
        }
        return null;
    }

    /**
     * delete app instance from appo.
     *
     * @param appInstanceId app instance id
     * @param tenantId tenant id
     * @param token access token
     * @return response success or not.
     */
    public boolean deleteAppInstance(String appInstanceId, String tenantId, String token) {
        if (StringUtils.isEmpty(appInstanceId)) {
            LOGGER.info("the app is not instantiated yet.");
            return true;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = appoUrl.concat(String.format(APPO_INSTANTIATE_APP, tenantId, appInstanceId));
        LOGGER.warn("deleteAppInstance URL: {}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, request, String.class);
            if (HttpStatus.ACCEPTED.equals(response.getStatusCode())) {
                return true;
            }
            LOGGER.error("delete app instance from appo failed. status code is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("delete app instance from appo failed, appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
        }

        return false;
    }

    /**
     * delete edge package.
     *
     * @param hostIp hostIp
     * @param tenantId tenant id
     * @param packageId package id
     * @param token access token
     * @return delete successfully
     */
    public boolean deleteEdgePackage(String hostIp, String tenantId, String packageId, String token) {
        if (StringUtils.isEmpty(hostIp) || StringUtils.isEmpty(packageId)) {
            LOGGER.info("the app is not distributed to edge yet.");
            return true;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = apmUrl.concat(String.format(APM_DELETE_EDGE_PACKAGE, tenantId, packageId, hostIp));
        LOGGER.warn("deleteEdgePkg URL: {}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, request, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return true;
            }
            LOGGER.error("deleteEdgePkg response failed. The status code is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("deleteEdgePkg failed, exception {}", e.getMessage());
        }

        return false;
    }

    /**
     * delete apm package.
     *
     * @param tenantId tenant id
     * @param packageId package id
     * @param token access token
     * @return delete successfully
     */
    public boolean deleteApmPackage(String tenantId, String packageId, String token) {
        if (StringUtils.isEmpty(packageId)) {
            LOGGER.info("the app is not uploaded to apm yet.");
            return true;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = apmUrl.concat(String.format(APM_DELETE_APM_PACKAGE, tenantId, packageId));
        LOGGER.warn("deleteApmPkg URL: {}", url);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, request, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                return true;
            }
            LOGGER.error("deleteApmPkg response failed. The status code is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("deleteApmPkg failed, exception {}", e.getMessage());
        }

        return false;
    }

}
