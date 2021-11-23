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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.util.internal.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpHead;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.application.external.mecm.dto.MecmInfo;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("mecmService")
public class MecmService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MecmService.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String MECM_URL_GET_MECHOSTS = "/inventory/v1/mechosts";

    private static final String APM_DELETE_EDGE_PACKAGE = "/apm/v1/tenants/%s/packages/%s/hosts/%s";

    private static final String APM_DELETE_APM_PACKAGE = "/apm/v1/tenants/%s/packages/%s";

    private static final String APPO_INSTANTIATE_APP = "/appo/v1/tenants/%s/app_instances/%s";

    //mirnada
    private static final String MECM_UPLOAD_GET_APPINFO = "/apm/v1/tenants/%s/packages/upload";

    private static final String MECM_GET_DEPLOYMENT_STATUS = "/appo/v1/tenants/{tenant_id}/apps/{appId}/packages/{appPackageId}/status";



    @Value("${mecm.urls.apm:}")
    private String apmUrl;

    @Value("${mecm.urls.appo:}")
    private String appoUrl;

    @Value("${mecm.urls.inventory:}")
    private String inventoryUrl;

    /**
     * get all mecm hosts.
     *
     * @param token access token
     * @return mecm host list
     * Release: String appPkgName, String appPkgVersion, File file
     */

    public MecmInfo upLoadPackageToApm(String token, Release release, String hostList, String tenantId){
        String appPackageName = release.getPackageFile().getOriginalFileName();
        String appPackageVersion = release.getAppBasicInfo().getVersion();
        String filePath = release.getPackageFile().getStorageAddress();
        if(StringUtils.isEmpty(appPackageName) && StringUtils.isEmpty(appPackageVersion) && StringUtils.isEmpty(filePath)){
            LOGGER.error("Failed to validate input parameters of MECM task creation, status is {}", ResponseConst.RET_PARAM_INVALID);
            return null;
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("appPackageName", appPackageName);
        body.add("appPackageVersion", appPackageVersion);
        LOGGER.info("filePath: {}", filePath);
        body.add("file", new FileSystemResource(filePath)); // file body
        body.add("hostList", hostList);

        LOGGER.error("[Upload to MECM], before http request.");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = apmUrl.concat(String.format(MECM_UPLOAD_GET_APPINFO, tenantId));
        try{
            LOGGER.error("[Upload to MECM], starting http request.");

            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(response.getStatusCode())) {
                LOGGER.error("[Upload to MECM], successfully get http request.");

                JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
                String mecm_appId = jsonObject.get("appId").getAsString();
                String mecm_appPackageId = jsonObject.get("appPackageId").getAsString();
                LOGGER.error("Successfully created test task {}, status is {}", mecm_appId, mecm_appPackageId);
                return new MecmInfo(mecm_appId, mecm_appPackageId);
            }
            LOGGER.error("Failed to create instance of MecmInfo,  status is {}", response.getStatusCode());
            throw new AppException("Failed HTTP Request to MECM inventory.",
                ResponseConst.FAIL_HTTP_REQUEST_TO_MECM_INVENTORY);
        }catch(Exception e){
            LOGGER.error("Failed to create instance of MecmInfo,  exception {}", e.getMessage());
        }

        return null;
    }

    public MecmDeploymentInfo getMecmDepolymentStatus(String token, String mecmAppId, String mecmAppPackageId, String tenantId){
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String url = appoUrl.concat(String.format(MECM_GET_DEPLOYMENT_STATUS, tenantId, mecmAppId, mecmAppPackageId));
        LOGGER.error("[Get MECM status], before http request.");

        try {
            ResponseEntity<String> response = REST_TEMPLATE
                .exchange(url, HttpMethod.GET, request, String.class);

            LOGGER.error("[Get MECM status], starting http request.");

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get MECM Deployment Status from MECM, The status code is {}",
                    response.getStatusCode());
                throw new AppException("Failed HTTP Request to MECM inventory.",
                    ResponseConst.FAIL_HTTP_REQUEST_TO_MECM_INVENTORY);
            }
            LOGGER.error("[Get MECM status], after http request.");

            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            String mecmAppInstanceId = jsonObject.get("appInstanceId").getAsString();
            String mecmOperationalStatus = jsonObject.get("operationalStatus").getAsString();
            LOGGER.error("Successfully created test task {}, status is {}", mecmAppInstanceId, mecmAppInstanceId);

            return new MecmDeploymentInfo(mecmAppInstanceId, mecmOperationalStatus, mecmAppId, mecmAppPackageId);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get MECM Deployment Status from MECM", e.getMessage());
        }

        return null;
    }


    public List<Map<String, Object>> getAllMecmHosts(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = REST_TEMPLATE
                .exchange(inventoryUrl.concat(MECM_URL_GET_MECHOSTS), HttpMethod.GET, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Failed to get mechosts from mecm inventory, The status code is {}",
                    response.getStatusCode());
                throw new AppException("Failed to get mechosts from mecm inventory.",
                    ResponseConst.RET_GET_MECMHOST_FAILED);
            }

            return new Gson().fromJson(response.getBody(), List.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get mechosts, RestClientException is {}", e.getMessage());
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
            mecHost.setCity((String) mecHostInfoMap.get("city"));
            mecHostMap.put(mecHostIp, mecHost);
        }

        return mecHostMap;
    }

    /**
     * delete app instance from appo.
     *
     * @param appInstanceId app instance id
     * @param tenantId tenant id
     * @param token  access token
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
