package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connection {
    public static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    /**
     * Shard to upload.
     *
     * @param header header info
     * @param url request url
     * @param upPackage package info
     * @param req request body
     * @param postData upload data
     * @return JSONObject
     */

    public static JSONObject postFiles(JSONObject header, String url, UploadPackageEntity upPackage, JSONObject req,
        byte[] postData) {
        JSONObject ret = new JSONObject();
        String boundary = "----WebKitFormBoundaryZqGhgoAoEb8BCQWC";
        CloseableHttpClient httpClient = createClient();
        CloseableHttpResponse response = null;
        try {
            HttpEntityEnclosingRequestBase requestBase = new HttpPost(url);
            Set<String> keySet = header.keySet();
            for (String key : keySet) {
                if (key.equalsIgnoreCase("Content-Length")) {
                    continue;
                }
                String value = header.getString(key);
                requestBase.setHeader(key, value);
            }
            requestBase.setHeader("Cookie", upPackage.getCookie());
            requestBase.setHeader("roarand", upPackage.getCsrfToken());
            requestBase.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

            MultipartEntityBuilder multiBuilder = MultipartEntityBuilder.create();
            multiBuilder.setBoundary(boundary);
            multiBuilder.addTextBody("formInfo",
                "{\n" + "    \"fileSize\":" + upPackage.getTotalSie() + ",\n" + "    \"fileTotalNum\":" + (int) Math
                    .ceil(upPackage.getTotalSie() / (double) AppConfig.FILE_SIZE) + ",\n" + "    \"fileCurrentIndex"
                    + "\":" + upPackage.getShardCount() + ",\n" + "    \"fileIdentify\":" + upPackage.getFileIdentify()
                    + ",\n" + "    \"fileName\":\"" + upPackage.getFileName() + "\"\n" + "}");

            multiBuilder.setBoundary(boundary);
            multiBuilder.addTextBody("vnfpackageInfo", req.getString("vnfpackageInfo"));

            multiBuilder.setBoundary(boundary);
            multiBuilder.addTextBody("catalogShareInfo", req.getString("catalogShareInfo"));

            multiBuilder.setBoundary(boundary);
            multiBuilder.addBinaryBody("serviceDefFile", postData, ContentType.APPLICATION_OCTET_STREAM, "blob");

            multiBuilder.setBoundary(boundary);

            HttpEntity entity = multiBuilder.build();
            requestBase.setEntity(entity);
            trustEveryone();
            response = httpClient.execute(requestBase);

            int statusCode = response.getStatusLine().getStatusCode();
            ret.put("body", EntityUtils.toString(response.getEntity()));

            if (statusCode == 200) {
                ret.put("retCode", 0);
            } else {
                ret.put("retCode", -1);
                LOGGER.error(ret.toString());
            }
            return ret;
        } catch (ClientProtocolException e) {
            LOGGER.error("PostFiles ClientProtocolException {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("PostFiles IOException {}", e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.error("PostFiles IOException {}", e.getMessage());
                }
            }
            try {
                httpClient.close();
            } catch (IOException e) {
                LOGGER.error("PostFiles IOException {}", e.getMessage());
            }
        }
        return ret;
    }

    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error("TrustEveryone exception: {}", e.getMessage());
        }
    }

    private static CloseableHttpClient createClient() {
        SSLContext sslContext = createIgnoreVerifySsl();
        HostnameVerifier ignoreVerifier = createHostnameVerifier();

        Registry<ConnectionSocketFactory> socket = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.INSTANCE)
            .register("https", new SSLConnectionSocketFactory(sslContext, ignoreVerifier)).build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socket);

        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        return httpClient;
    }

    private static HostnameVerifier createHostnameVerifier() {
        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        return verifier;
    }

    private static SSLContext createIgnoreVerifySsl() {
        SSLContext ssl = null;
        try {
            ssl = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("createIgnoreVerifySsl exception: {}", e.getMessage());
        }

        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] cert = new X509Certificate[1];
                cert[0] = null;
                return cert;
            }
        };

        try {
            if (ssl != null) {
                ssl.init(null, new TrustManager[] {trustManager}, null);
            }
        } catch (KeyManagementException e) {
            LOGGER.error("createIgnoreVerifySsl KeyManagementException: {}", e.getMessage());
        }
        return ssl;
    }
}
