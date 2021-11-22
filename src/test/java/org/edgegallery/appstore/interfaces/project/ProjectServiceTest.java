/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.appstore.interfaces.project;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.system.lcm.DistributeResponse;
import org.edgegallery.appstore.domain.model.system.lcm.UploadResponse;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class ProjectServiceTest extends AppTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    protected PackageMapper packageMapper;

    private HttpServer httpServer;

    private String token = "123456789";

    @Before
    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 30201), 0);
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/packages", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("POST")) {
                    UploadPackageDto uploadPackageDto = new UploadPackageDto();
                    UploadResponse uploadResponse = new UploadResponse();
                    uploadResponse.setAppId("test-app-id");
                    uploadResponse.setPackageId("test-pkg-id");
                    uploadPackageDto.setData(uploadResponse);
                    String dtp = new Gson().toJson(uploadPackageDto);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/packages/test-pkg-id", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    DistributeResponseDto dto = new DistributeResponseDto();
                    List<DistributeResponse> data = new ArrayList<>();
                    DistributeResponse dis = new DistributeResponse();
                    dis.setAppId("test-app-id");
                    dis.setPackageId("test-pkg-id");
                    data.add(dis);
                    dto.setData(data);
                    String dtp = new Gson().toJson(dto);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                } else if (method.equals("POST")) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                exchange.close();
            }
        });
        httpServer.createContext(
            "/lcmcontroller/v2/tenants/userId/app_instances/3f50936d-f10f-41ff-9c05-bdf5da951b53/instantiate",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                    if (!token.equals(accessToken)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                        exchange.getResponseBody().write("FORBIDDEN".getBytes());
                    } else if (method.equals("GET")) {
                        DistributeResponseDto dto = new DistributeResponseDto();
                        List<DistributeResponse> data = new ArrayList<>();
                        DistributeResponse dis = new DistributeResponse();
                        dis.setAppId("test-app-id");
                        dis.setPackageId("test-pkg-id");
                        data.add(dis);
                        dto.setData(data);
                        String dtp = new Gson().toJson(dto);
                        byte[] response = dtp.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    } else if (method.equals("POST")) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    }
                    exchange.close();
                }
            });
        httpServer.createContext("/lcmcontroller/v2/tenants/userId/app_instances/3f50936d-f10f-41ff-9c05-bdf5da951b53",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                    if (!token.equals(accessToken)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                        exchange.getResponseBody().write("FORBIDDEN".getBytes());
                    } else if (method.equals("GET")) {
                        WorkloadStatusDto workloadStatusDto = new WorkloadStatusDto();
                        WorkLoadStatus workLoadStatus = new WorkLoadStatus();
                        List<PortFromLcm> ports = new ArrayList<>();
                        ports.add(PortFromLcm.builder().nodePort("5588").build());
                        ports.add(PortFromLcm.builder().nodePort("9901").build());
                        workLoadStatus.getServices().add(ServiceFromLcm.builder().serviceName("serviceName1").ports(ports).build());
                        workLoadStatus.getServices().add(ServiceFromLcm.builder().serviceName("serviceName2").ports(ports).build());
                        workloadStatusDto.setData(new Gson().toJson(workLoadStatus));
                        String dtp = new Gson().toJson(workloadStatusDto);
                        byte[] response = dtp.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    exchange.close();
                }
            });
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_when_deploy_apk() throws IOException {
        projectService.setInstantiateAppSleepTime(1000);
        projectService.setUploadPkgSleepTime(1000);
        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Published.toString());
            r.setAppId("appid-test-0001");
            r.setAppInstanceId("3f50936d-f10f-41ff-9c05-bdf5da951b53");
            try {
                File csarFIle = Resources.getResourceAsFile("testfile/new_csar.csar");
                r.setPackageAddress(csarFIle.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            packageMapper.updateRelease(r);
            packageMapper.updateAppInstanceApp(r);
        });
        ResponseEntity<ResponseObject> response = projectService
            .deployAppById("appid-test-0001", unPublishedPackageId, "userId", "123456789");
        Assert.assertEquals(200, response.getStatusCode().value());
        Assert.assertEquals("get app url success.", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_when_get_node_status_not_instantiate() {
        ResponseEntity<ResponseObject> ret = projectService.getNodeStatus(unPublishedPackageId, "userId", "123456789");
        Assert.assertEquals(200, ret.getStatusCode().value());
        System.out.println(Objects.requireNonNull(ret.getBody()).getMessage());
        Assert.assertEquals("this pacakge not instantiate", Objects.requireNonNull(ret.getBody()).getMessage());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_when_get_node_status() {
        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Published.toString());
            r.setAppId("appid-test-0001");
            r.setAppInstanceId("3f50936d-f10f-41ff-9c05-bdf5da951b53");
            try {
                File csarFIle = Resources.getResourceAsFile("testfile/new_csar.csar");
                r.setPackageAddress(csarFIle.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            packageMapper.updateRelease(r);
            packageMapper.updateAppInstanceApp(r);
        });
        ResponseEntity<ResponseObject> ret = projectService.getNodeStatus(unPublishedPackageId, "userId", "123456789");
        Assert.assertEquals(200, ret.getStatusCode().value());
        System.out.println(Objects.requireNonNull(ret.getBody()).getMessage());
        Assert.assertEquals("get app url success.", Objects.requireNonNull(ret.getBody()).getMessage());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_query_experience_status() throws Exception {
        String packageId = "packageid-0003";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/experience/packages/%s/status", packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String s = result.getResponse().getContentAsString();
        Type type = new TypeToken<ResponseObject>() { }.getType();
        ResponseObject packageDtos = gson.fromJson(result.getResponse().getContentAsString(), type);
        Assert.assertEquals(25.0, packageDtos.getData());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_experience_status_error_packageId() throws Exception {
        String packageId = "packageid-0002";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/experience/packages/%s/status", packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        String s = result.getResponse().getContentAsString();
        Type type = new TypeToken<ResponseObject>() { }.getType();
        ResponseObject packageDtos = gson.fromJson(result.getResponse().getContentAsString(), type);
        Assert.assertNotEquals(25.0, packageDtos.getData());
    }


    private static CloseableHttpClient createIgnoreSslHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(new BasicCookieStore()).setRedirectStrategy(new DefaultRedirectStrategy())
                .build();
        } catch (Exception e) {
        }
        return null;
    }
}
