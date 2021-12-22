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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeBody;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeResponse;
import org.edgegallery.appstore.domain.model.system.lcm.ErrorLog;
import org.edgegallery.appstore.domain.model.system.lcm.InstantRequest;
import org.edgegallery.appstore.domain.model.system.lcm.LcmLog;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("HttpClientUtil")
public class HttpClientUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String APP_INSTANCE_ID = "appInstanceId";

    private static final String TENANT_ID = "tenantId";

    private static final String PACKAGE_ID = "packageId";

    private static final String TOKEN = "token";

    private static final String USER_ID = "userId";

    private static final String UPLOAD_PKG_FAILED = "Failed upload pkg exception {}";

    private static final int READ_BUFFER_SIZE = 256;

    private static final CookieStore cookieStore = new BasicCookieStore();

    @Value("${security.oauth2.resource.jwt.key-uri:}")
    private String loginUrl;

    @Value("${client.client-id:}")
    private String clientId;

    @Value("${client.client-secret:}")
    private String clientPW;

    private HttpClientUtil() {

    }

    /**
     * instantiate Application.
     *
     * @return InstantiateAppResult
     */
    public static boolean instantiateApp(MepHost mepHost, Map<String, String> deployParams, LcmLog lcmLog, String pkgId,
        Map<String, String> inputParams) {
        String protocol = mepHost.getProtocol();
        String ip = mepHost.getLcmIp();
        int port = mepHost.getPort();
        //before instantiate, call distribute result interface
        String disRes = getDistributeRes(protocol, ip, port, deployParams, pkgId);
        if (StringUtils.isEmpty(disRes)) {
            LOGGER.error("instantiateApplication get pkg distribute res failed!");
            return false;
        }
        //parse dis res remain
        JsonObject jsonObject = new JsonParser().parse(disRes).getAsJsonObject();
        JsonElement distributeData = jsonObject.get("data");
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<List<DistributeResponse>>() { }.getType();
        List<DistributeResponse> list = gson.fromJson(distributeData, typeEvents);
        String appName = list.get(0).getAppPkgName();
        //set instantiate headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, deployParams.get(TOKEN));
        //set instantiate bodys
        InstantRequest ins = new InstantRequest();
        ins.setAppName(appName);
        ins.setHostIp(mepHost.getMecHost());
        ins.setParameters(inputParams);
        ins.setPackageId(pkgId);
        LOGGER.warn(gson.toJson(ins));
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(ins), headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_INSTANTIATE_APP_URL
            .replace(APP_INSTANCE_ID, deployParams.get(APP_INSTANCE_ID)).replace(TENANT_ID, deployParams.get(USER_ID));
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPlCM instantiate log:{}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            JsonObject jsonError = new JsonParser().parse(errorLog).getAsJsonObject();
            Type typeError = new TypeToken<ErrorLog>() { }.getType();
            ErrorLog errorMessage = gson.fromJson(jsonError, typeError);
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}",
                deployParams.get(APP_INSTANCE_ID), errorLog);
            lcmLog.setLog(errorMessage.getMessage());
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed to instantiate application which appInstanceId is {} exception {}",
                deployParams.get(APP_INSTANCE_ID), e.getMessage());
            lcmLog.setLog("instantiate package failed.");
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to instantiate application which appInstanceId is {}", deployParams.get(APP_INSTANCE_ID));
        return false;
    }

    /**
     * upload pkg.
     */
    public static String uploadPkg(String protocol, String ip, int port, Map<String, String> deployParams,
        LcmLog lcmLog) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("package", new FileSystemResource(deployParams.get("filePath")));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, deployParams.get(TOKEN));
        headers.set("Origin", "mepm");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_UPLOAD_APPPKG_URL
            .replace(TENANT_ID, deployParams.get(USER_ID));
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPLCM upload pkg log:{}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error(UPLOAD_PKG_FAILED, errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error(UPLOAD_PKG_FAILED, e.getMessage());
            lcmLog.setLog("upload to remote file server failed");
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
    public static boolean distributePkg(MepHost mepHost, String userId, String token, String packageId, LcmLog lcmLog) {
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
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("APPLCM distribute pkg log:{}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            JsonObject jsonObject = new JsonParser().parse(errorLog).getAsJsonObject();
            Type typeEvents = new TypeToken<ErrorLog>() { }.getType();
            ErrorLog errorMessage = gson.fromJson(jsonObject, typeEvents);
            LOGGER.error("Failed distribute pkg packageId  {} exception {}", packageId, errorLog);
            lcmLog.setLog(errorMessage.getMessage());
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
    public static boolean deleteHost(String protocol, String ip, int port, Map<String, String> deployParams,
        String pkgId, String hostIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, deployParams.get(TOKEN));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_DELETE_HOST_URL
            .replace(TENANT_ID, deployParams.get(USER_ID)).replace(PACKAGE_ID, pkgId).replace("hostIp", hostIp);
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
    public static boolean deletePkg(String protocol, String ip, int port, Map<String, String> deployParams,
        String pkgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, deployParams.get(TOKEN));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_DELETE_APPPKG_URL
            .replace(TENANT_ID, deployParams.get(USER_ID)).replace(PACKAGE_ID, pkgId);
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
    public static String getDistributeRes(String protocol, String ip, int port, Map<String, String> deployParams,
        String pkgId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, deployParams.get(TOKEN));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_DISTRIBUTE_APPPKG_URL
            .replace(TENANT_ID, deployParams.get(USER_ID)).replace(PACKAGE_ID, pkgId);
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
     * get experience package status.
     * @param protocol protocol.
     * @param ip ip.
     * @param port port.
     * @param userId userId.
     * @param token token.
     * @param lcmLog lcmLog.
     * @return package status
     */
    public static String getPackageStatus(String protocol, String ip, int port, String userId, String token,
        LcmLog lcmLog) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        headers.set("Origin", "mepm");
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_UPLOAD_APPPKG_URL.replace(TENANT_ID, userId);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, requestEntity, String.class);
            LOGGER.info("APPLCM upload pkg log:{}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error(UPLOAD_PKG_FAILED, errorLog);
            lcmLog.setLog(errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error(UPLOAD_PKG_FAILED, e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to upload pkg!");
        return null;
    }

    /**
     * getWorkloadStatus.
     *
     * @return String
     */
    public static String getWorkloadStatus(String protocol, String ip, int port, Map<String, String> deployParams) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_WORKLOAD_STATUS_URL
            .replace(APP_INSTANCE_ID, deployParams.get(APP_INSTANCE_ID)).replace(TENANT_ID, deployParams.get(USER_ID));
        LOGGER.info("url is {}", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, deployParams.get(TOKEN));
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            LOGGER.info("APPlCM terminateAppInstance log:{}", response);
        } catch (RestClientException e) {
            LOGGER.error("Failed to get workload status which appInstanceId is {} exception {}",
                deployParams.get(APP_INSTANCE_ID), e.getMessage());
            return null;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }
        LOGGER.error("Failed to get workload status which appInstanceId is {}", deployParams.get(APP_INSTANCE_ID));
        return null;
    }

    /**
     * getHealth.
     */
    public static boolean getHealth(String protocol, String ip, int port) {
        String url = getUrlPrefix(protocol, ip, port) + Consts.APP_LCM_GET_HEALTH;
        LOGGER.info(" health url is {}", url);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.GET, null, String.class);
        } catch (RestClientException e) {
            LOGGER.error("call app lcm health api occur exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("call app lcm health api failed");
        return false;
    }

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }

    private static String getXsrf() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("XSRF-TOKEN")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private static CloseableHttpClient createIgnoreSslHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                (TrustStrategy) (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(cookieStore).setRedirectStrategy(new DefaultRedirectStrategy()).build();
        } catch (Exception e) {
            LOGGER.error("call sslConnectionSocketFactory to clean env interface occur error {}", e.getMessage());
        }
        return null;
    }

    /**
     * get access token for schedule task.
     *
     * @return token
     */
    public String getAccessToken() {
        int count = 0;
        CloseableHttpClient client = createIgnoreSslHttpClient();
        if (client == null) {
            LOGGER.error("call client interface occur error");
            return null;
        }
        while (count < 10) {
            String authResult = getAuthResult(client);
            if (!StringUtils.isEmpty(authResult) && authResult.contains("\"accessToken\":")) {
                String tokenArr = getTokenString(authResult);
                if (tokenArr != null) {
                    return tokenArr;
                }
            } else {
                count++;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error("sleep failed! {}", e.getMessage());
                }
            }
        }
        return "";
    }

    private String getTokenString(String authResult) {
        String[] authResults = authResult.split(",");
        for (String authRes : authResults) {
            if (authRes.contains("accessToken")) {
                String[] tokenArr = authRes.split(":");
                if (tokenArr.length > 1) {
                    return tokenArr[1].substring(1, tokenArr[1].length() - 1);
                }
            }
        }
        return null;
    }

    private String getAuthResult(CloseableHttpClient client) {
        try {
            URL url = new URL(loginUrl);
            String userLoginUrl = url.getProtocol() + "://" + url.getAuthority() + "/login";
            LOGGER.warn("user login url: {}", userLoginUrl);
            HttpPost httpPost = new HttpPost(userLoginUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("username", clientId + ":" + new Date().getTime());
            builder.addTextBody("password", clientPW);
            httpPost.setEntity(builder.build());
            // first call login interface
            client.execute(httpPost);
            String xsrf = getXsrf();
            httpPost.setHeader("X-XSRF-TOKEN", xsrf);
            // second call login interface
            client.execute(httpPost);
            String xsrfToken = getXsrf();
            //third call auth login-info interface
            String getTokenUrl = url.getProtocol() + "://" + url.getHost() + ":30091/auth/login-info";
            LOGGER.warn("user login-info url: {}", getTokenUrl);
            HttpGet httpGet = new HttpGet(getTokenUrl);
            httpGet.setHeader("X-XSRF-TOKEN", xsrfToken);
            CloseableHttpResponse res = client.execute(httpGet);
            InputStream inputStream = res.getEntity().getContent();
            byte[] bytes = new byte[READ_BUFFER_SIZE];
            StringBuilder buf = new StringBuilder();
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                buf.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
            }
            if (buf.length() > 0) {
                LOGGER.info("response token length: {}", buf.length());
                return buf.toString();
            }
        } catch (IOException e) {
            LOGGER.error("call login or clean env interface occur error {}", e.getMessage());
        }
        return null;
    }

}
