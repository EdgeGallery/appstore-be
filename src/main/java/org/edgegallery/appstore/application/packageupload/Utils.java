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

package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    public static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final String BSP_SESSION_PREFIX = "bspsession=";

    private Utils() {
    }

    /**
     * Get cookie info.
     *
     * @param hostIp host port
     * @param userName user name
     * @param password password
     * @return JSONObject
     */
    public static JSONObject getSessionCookie(String hostIp, String userName, String password) {
        JSONObject json = new JSONObject();
        String session = loginSession(hostIp, userName, password, BSP_SESSION_PREFIX);
        if (session.length() > 0) {
            json.put("code", 200);
            json.put("body", session);
        }
        return json;
    }

    private static String loginSession(String hostIp, String userName, String password, String sessionPrefix) {
        GetSessionCookieRedirectStrategy redirectStrategy = new GetSessionCookieRedirectStrategy(sessionPrefix);
        HttpClientBuilder clientBuilder = HttpClients.custom().setRedirectStrategy(redirectStrategy)
            .setSSLSocketFactory(createSslConnSocketFactory());

        try (CloseableHttpClient client = clientBuilder.build()) {
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
            requestConfigBuilder.setCircularRedirectsAllowed(true);

            HttpGet get = new HttpGet();
            get.setConfig(requestConfigBuilder.build());
            HttpPost post = new HttpPost();
            post.setConfig(requestConfigBuilder.build());

            get.setURI(URI.create(String.format("%s", hostIp)));
            try (CloseableHttpResponse response = client.execute(get)) {
                String location = redirectStrategy.location;
                if (location.equals("") || !location.contains("?")) {
                    LOGGER.error("get redirect location failed" + EntityUtils.toString(response.getEntity()));
                }
            } finally {
                get.releaseConnection();
            }

            String queryParams = redirectStrategy.location.split("\\?")[1];
            List<NameValuePair> nvps = new ArrayList<>();
            String hostUrl = String.format("%s/unisso/validateUser.action?", hostIp);
            post.setURI(URI.create(hostUrl + queryParams));
            nvps.add(new BasicNameValuePair("userpasswordcredentials.username", userName));
            nvps.add(new BasicNameValuePair("userpasswordcredentials.password", password));
            nvps.add(new BasicNameValuePair("__checkbox_warnCheck", "true"));
            nvps.add(new BasicNameValuePair("Submit", "login"));

            post.setEntity(new UrlEncodedFormEntity(nvps));
            post.addHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpResponse response = client.execute(post)) {
                if (redirectStrategy.session.equals("")) {
                    LOGGER.error("sessionCookie: session failed" + EntityUtils.toString(response.getEntity()));
                    return "";
                }
            } finally {
                post.releaseConnection();
            }

            String sessionCookie = redirectStrategy.session;
            LOGGER.info("sessionCookie: " + sessionCookie);

            if (Boolean.TRUE.equals(redirectStrategy.isLicenselogin)) {
                String redirectUrlParam = URLEncoder.encode("service=/unisess/v1/auth?service=/", "UTF-8");
                String licenseDirectLoginUrl = String
                    .format("%s/plat/licapp/v1/licensedirectlogin?%s&_=%s", hostIp, redirectUrlParam,
                        System.currentTimeMillis());
                get.setURI(URI.create(licenseDirectLoginUrl));
                get.addHeader("X_Requested_With", "XMLHttpRequest");
                get.addHeader("Cookie", sessionCookie);
                try (CloseableHttpResponse response = client.execute(get)) {
                    LOGGER.info("isLicenseLogin");
                    if (response.getStatusLine().getStatusCode() != 200) {
                        return "";
                    }
                } finally {
                    get.releaseConnection();
                }
            }

            String sessionUrl = String.format("%s/unisess/v1/auth/session", hostIp);
            get.setURI(URI.create(sessionUrl));
            get.addHeader("Accept", "application/json");
            get.addHeader("Cookie", sessionCookie);
            get.addHeader("X_Requested_With", "XMLHttpRequest");
            String csrfToken = "";
            try (CloseableHttpResponse response = client.execute(get)) {
                JSONObject sessionInfo = JSON.parseObject(EntityUtils.toString(response.getEntity()));
                csrfToken = sessionInfo.getString("csrfToken");
                LOGGER.info("csrfToken: " + csrfToken);
            } finally {
                get.releaseConnection();
            }

            String sessionParam = String
                .format("{\"session\":\"%s\",\"csrfToken\":\"%s\",\"onekey\":\"%s\"}", sessionCookie, csrfToken, "");
            LOGGER.info("sessionParam: " + sessionParam);
            return sessionParam;
        } catch (IOException e) {
            LOGGER.error("loginSession IOException: {}", e.getMessage());
        }
        return "";
    }

    private static LayeredConnectionSocketFactory createSslConnSocketFactory() {
        SSLConnectionSocketFactory sslSf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            }).build();

            sslSf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
                @Override
                public void verify(String s, SSLSocket sslSocket) throws IOException {
                    // not need implement
                }

                @Override
                public void verify(String s, X509Certificate x509Certificate) throws SSLException {
                    // not need implement
                }

                @Override
                public void verify(String s, String[] strings, String[] strings1) throws SSLException {
                    // not need implement
                }

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("createSslConnSocketFactory NoSuchAlgorithmException: {}", e.getMessage());
        } catch (KeyStoreException e) {
            LOGGER.error("createSslConnSocketFactory KeyStoreException: {}", e.getMessage());
        } catch (KeyManagementException e) {
            LOGGER.error("createSslConnSocketFactory KeyManagementException: {}", e.getMessage());
        }
        return sslSf;
    }

    private static class GetSessionCookieRedirectStrategy extends DefaultRedirectStrategy {
        public String session = "";

        public Boolean isLicenselogin = false;

        public String location = "";

        private String sessionPrefix;

        public GetSessionCookieRedirectStrategy(String sessionPrefix) {
            this.sessionPrefix = sessionPrefix;
        }

        @Override
        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
            boolean isRedirect = false;
            try {
                isRedirect = super.isRedirected(request, response, context);
                Header[] requestHeaders = response.getAllHeaders();
                for (Header header : requestHeaders) {
                    if (header.getName().equalsIgnoreCase("Set-Cookie") && header.getValue().contains(sessionPrefix)) {
                        session = header.getValue().split(";")[0];
                    }
                    if (header.getName().equalsIgnoreCase("Location")) {
                        location = header.getValue();
                        if (location.contains("/plat/licapp/v1/themes/default/licenseChooseMenu.html")) {
                            isLicenselogin = true;
                        }
                    }
                }
            } catch (ProtocolException e) {
                LOGGER.error("ProtocolException");
            }

            if (!isRedirect) {
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 301 || responseCode == 302) {
                    return true;
                }
            }
            return isRedirect;
        }
    }
}
