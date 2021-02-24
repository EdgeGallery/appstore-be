/* Copyright 2020 Huawei Technologies Co., Ltd.
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
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
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
import org.apache.http.util.EntityUtils;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
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

    private static CookieStore cookieStore = new BasicCookieStore();

    private static final String USERNAME = "guest";

    private static final String PASSWORD = "guest";

    private static final String ATP_REPORT_ADDR = "%smec-atp/edgegallery/atp/v1/tasks/%s";

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
    public AtpTestDto sendCreatTask2Atp(String filePath, String token) {
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
                String status = jsonObject.get("status").getAsString();
                LOGGER.info("Create test task {} success, status is {}", id, status);
                return new AtpTestDto(id, status);
            }
            LOGGER.error("Create instance from atp failed,  status is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("Failed to create instance from atp,  exception {}", e.getMessage());
        }

        throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR, "Create instance from atp failed.");
    }

    /**
     * get task status by taskId from atp.
     *
     * @param taskId taskId
     * @param token token
     * @return task status
     */
    public String getTaskStatusFromAtp(String taskId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = String.format(queryTaskUrl, taskId);
        LOGGER.info("get task status frm atp, url: {}", url);
        String status = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("Get task status from atp reponse failed, the taskId is {}, The status code is {}", taskId,
                    response.getStatusCode());
                throw new InvocationException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Get task status from atp reponse failed.");
            }

            JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
            if (jsonObject.has("status")) {
                status = jsonObject.get("status").getAsString();
                LOGGER.info("Get task status: {}", status);
            } else {
                LOGGER.error("Get task status faieed.");
            }

        } catch (RestClientException e) {
            LOGGER.error("Failed to get task status from atp which taskId is {} exception {}", taskId, e.getMessage());
        }
        return status;
    }

    /**
     * query report data from remote atp.
     *
     * @param host atp host
     * @param taskId task id
     * @return data
     */
    public String getReportDataFromRemote(String host, String taskId) {
        LOGGER.info("getReportDataFromRemote, host {}, taskId {}", host, taskId);
        String result = "";
        String authUrl = "";
        if (host.contains("atp")) {
            authUrl = host.replace("atp", "auth") + "login";
        } else {
            authUrl = host.substring(0, host.lastIndexOf(":")) + ":30067/login";
        }
        LOGGER.info("getReportDataFromRemote, authurl {}", authUrl);
        HttpPost httpPost = new HttpPost(authUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("username", USERNAME);
        builder.addTextBody("password", PASSWORD);
        httpPost.setEntity(builder.build());
        try (CloseableHttpClient client = createIgnoreSslHttpClient()) {

            client.execute(httpPost);
            String xsrf = getXsrf();

            httpPost.setHeader("X-XSRF-TOKEN", xsrf);
            client.execute(httpPost);

            String taskUrl = String.format(ATP_REPORT_ADDR, host, taskId);
            LOGGER.info("get report data from atp, url {}", taskUrl);
            HttpGet httpGet = new HttpGet(taskUrl);
            httpGet.setHeader("X-XSRF-TOKEN", xsrf);
            client.execute(httpGet);
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            LOGGER.error("get report data from remote {} error: {}", host, e.getMessage());
        }

        return result;
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
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(cookieStore).setRedirectStrategy(new DefaultRedirectStrategy()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
