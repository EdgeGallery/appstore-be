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

package org.edgegallery.appstore.interfaces.order.web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class MecmServiceTest {

    @Autowired
    private MecmService mecmService;

    private HttpServer httpServer;

    private String token = "123456789";

    private String hostIp = "192.168.0.1";

    @Autowired
    private MockMvc mvc;

    @Before
    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        httpServer.createContext("/inventory/v1/mechosts", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    Map<String, Object> mechost = new HashMap<>();
                    mechost.put("mechostIp", hostIp);
                    mechost.put("city", "xian");
                    List<Map<String, Object>> mechosts = new ArrayList<>();
                    mechosts.add(mechost);
                    String jsonObject = new Gson().toJson(mechosts);
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/appo/v1/tenants/testUserId/app_instances/testInstanceId", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("DELETE")) {
                    String jsonObject = "delete instance success.";
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/apm/v1/tenants/testUserId/packages/testPackageId/hosts/192.168.0.1", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("DELETE")) {
                    String jsonObject = "delete edge package success.";
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/apm/v1/tenants/testUserId/packages/testPackageId", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("DELETE")) {
                    String jsonObject = "delete apm package success.";
                    byte[] response = jsonObject.getBytes();
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
    public void getMecHostByIpList_success() {
        List<String> mecHostIpList = new ArrayList<>();
        mecHostIpList.add(hostIp);
        Map<String, MecHostBody> result = mecmService.getMecHostByIpList(token, mecHostIpList);
        Assert.assertNotNull(result);
        MecHostBody mecHost = result.get(hostIp);
        Assert.assertEquals("xian", mecHost.getCity());
    }

    @Test
    public void getMecHostByIpList_empty() {
        List<String> mecHostIpList = new ArrayList<>();
        mecHostIpList.add("127.0.0.4");
        Map<String, MecHostBody> result = mecmService.getMecHostByIpList(token, mecHostIpList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void deleteAppInstance_success() {
        String userId = "testUserId";
        String instanceId = "testInstanceId";
        Assert.assertTrue(mecmService.deleteAppInstance(instanceId, userId, token));
    }

    @Test
    public void deleteAppInstance_fail() {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String instanceId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";
        Assert.assertFalse(mecmService.deleteAppInstance(instanceId, userId, token));
    }

    @Test
    public void deleteEdgePackage_success() {
        String userId = "testUserId";
        String packageId = "testPackageId";
        Assert.assertTrue(mecmService.deleteEdgePackage(hostIp, userId, packageId, token));
    }

    @Test
    public void deleteEdgePackage_fail() {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String packageId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";
        Assert.assertFalse(mecmService.deleteEdgePackage(hostIp, userId, packageId, token));
    }

    @Test
    public void deleteApmPackage_success() {
        String userId = "testUserId";
        String packageId = "testPackageId";
        Assert.assertTrue(mecmService.deleteApmPackage(userId, packageId, token));
    }

    @Test
    public void deleteApmPackage_fail() {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String packageId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";
        Assert.assertFalse(mecmService.deleteApmPackage(userId, packageId, token));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void queryMecmHosts() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/mechosts")
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}