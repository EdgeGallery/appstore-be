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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeBody;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeResponse;
import org.edgegallery.appstore.domain.model.system.lcm.InstantRequest;
import org.edgegallery.appstore.domain.model.system.lcm.LcmLog;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public final class HttpClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String APP_INSTANCE_ID = "appInstanceId";

    private static final String TENANT_ID = "tenantId";

    private static final String PACKAGE_ID = "packageId";

    private HttpClientUtil() {

    }

    /**
     * instantiate Application.
     *
     * @return InstantiateAppResult
     */
    public static boolean instantiateApp(MepHost mepHost, String appInstanceId, String userId, String token,
        LcmLog lcmLog, String pkgId) {
        String protocol = mepHost.getProtocol();
        String ip = mepHost.getLcmIp();
        int port = mepHost.getPort();
        //before instantiate, call distribute result interface
        String disRes = getDistributeRes(protocol, ip, port, userId, token, pkgId);
        if (StringUtils.isEmpty(disRes)) {
            LOGGER.error("instantiateApplication get pkg distribute res failed!");
            return false;
        }
        //parse dis res
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<List<DistributeResponse>>() { }.getType();
        List<DistributeResponse> list = gson.fromJson(disRes, typeEvents);
        String appName = list.get(0).getAppPkgName();
        //set instantiate headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        //set instantiate bodys
        InstantRequest ins = new InstantRequest();
        ins.setAppName(appName);
        ins.setHostIp(mepHost.getMecHost());
        ins.setPackageId(pkgId);
        LOGGER.warn(gson.toJson(ins));
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(ins), headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_INSTANTIATE_APP_URL
            .replace(APP_INSTANCE_ID, appInstanceId).replace(TENANT_ID, userId);
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM instantiate log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}", appInstanceId,
                errorLog);
            lcmLog.setLog(errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to instantiate application which appInstanceId is {}", appInstanceId);
        return false;
    }

    /**
     * upload pkg.
     */
    public static String uploadPkg(String protocol, String ip, int port, String filePath, String userId, String token,
        LcmLog lcmLog) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("package", new FileSystemResource(filePath));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        headers.set("Origin", "mepm");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_UPLOAD_APPPKG_URL.replace(TENANT_ID, userId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPLCM upload pkg log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed upload pkg exception {}", errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Failed upload pkg exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to upload pkg!");
        return null;
    }

    /**
     * distribute pkg.
     */
    public static boolean distributePkg(MepHost mepHost, String userId, String token, String packageId,
        LcmLog lcmLog) {
        //add body
        DistributeBody body = new DistributeBody();
        String[] bodys = new String[1];
        bodys[0] = mepHost.getMecHost();
        body.setHostIp(bodys);
        //add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        Gson gson = new Gson();
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(body), headers);
        String url = getUrlPrefix(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort())
            + Consts.APP_LCM_DISTRIBUTE_APPPKG_URL.replace(TENANT_ID, userId).replace(PACKAGE_ID, packageId);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPLCM distribute pkg log:{}", response);
        } catch (CustomException e) {
            e.printStackTrace();
            String errorLog = e.getBody();
            LOGGER.error("Failed distribute pkg packageId  {} exception {}", packageId, errorLog);
            lcmLog.setLog(errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed distribute pkg packageId is {} exception {}", packageId, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to distribute pkg which packageId is {}", packageId);
        return false;
    }

    /**
     * delete host.
     */
    public static boolean deleteHost(String protocol, String ip, int port, String userId, String token, String pkgId,
        String hostIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_DELETE_HOST_URL.replace(TENANT_ID, userId)
            .replace(PACKAGE_ID, pkgId).replace("hostIp", hostIp);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.info("APPlCM delete host log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete host packageId is {} exception {}", pkgId, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to delete host which packageId is {}", pkgId);
        return false;
    }

    /**
     * delete pkg.
     */
    public static boolean deletePkg(String protocol, String ip, int port, String userId, String token, String pkgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_DELETE_APPPKG_URL.replace(TENANT_ID, userId)
            .replace(PACKAGE_ID, pkgId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.info("APPlCM delete pkg log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed delete pkg pkgId is {} exception {}", pkgId, e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to delete pkg which pkgId is {}", pkgId);
        return false;
    }

    /**
     * get distribute result.
     */
    public static String getDistributeRes(String protocol, String ip, int port, String userId, String token,
        String pkgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_DISTRIBUTE_APPPKG_URL
            .replace(TENANT_ID, userId).replace(PACKAGE_ID, pkgId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
            LOGGER.info("APPlCM get distribute res log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed get distribute res pkgId is {} exception {}", pkgId, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get distribute result!");
        return null;
    }

    /**
     * terminateAppInstance.
     *
     * @return boolean
     */
    public static boolean terminateAppInstance(String protocol, String ip, int port, String appInstanceId,
        String userId, String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_TERMINATE_APP_URL
            .replace(APP_INSTANCE_ID, appInstanceId).replace(TENANT_ID, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);
            LOGGER.info("APPlCM terminateAppInstance log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed to terminate application which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to terminate application which appInstanceId is {}", appInstanceId);
        return false;
    }

    /**
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getWorkloadStatus(String protocol, String ip, int port, String appInstanceId, String userId,
        String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_WORKLOAD_STATUS_URL
            .replace(APP_INSTANCE_ID, appInstanceId).replace(TENANT_ID, userId);
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get workload status which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get workload status which appInstanceId is {}", appInstanceId);
        return null;
    }

    /**
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getWorkloadEvents(String protocol, String ip, int port, String appInstanceId, String userId,
        String token) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_WORKLOAD_EVENTS_URL
            .replace(APP_INSTANCE_ID, appInstanceId).replace(TENANT_ID, userId);
        LOGGER.info("work event url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get workload events which appInstanceId is {} exception {}", appInstanceId,
                e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get workload events which appInstanceId is {}", appInstanceId);
        return null;
    }

    /**
     * getHealth.
     */
    public static String getHealth(String protocol, String ip, int port) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_HEALTH;
        LOGGER.info(" health url is {}", url);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, null, String.class);
        } catch (RestClientException e) {
            LOGGER.error("call app lcm health api occur exception {}", e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("call app lcm health api failed");
        return null;
    }

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }


}
