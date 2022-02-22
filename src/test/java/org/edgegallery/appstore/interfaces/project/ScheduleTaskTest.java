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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.inner.OrderService;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.order.web.MecmRespDto;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

public class ScheduleTaskTest extends AppTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    private HttpServer httpServer;

    private HttpServer httpServer8001;

    private String token = "4687632346763131324564";

    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 38067), 0);
        httpServer.createContext("/v1/accesstoken", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {
                    exchange.getResponseHeaders().add("XSRF-TOKEN", "ddexxx-dfwefdgwer");
                    Map<String, String> result = new HashMap<>();
                    result.put("accessToken", "4687632346763131324564");
                    String dtp = new Gson().toJson(result);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.start();

        httpServer8001 = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        httpServer8001.createContext("/mecm-north/v1/tenants/testUserId/packages/testPackageId", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    MecmRespDto testResponse = new MecmRespDto();
                    testResponse.setMecmPackageId("mecmPkgId");
                    testResponse.setMessage("Query server success");
                    testResponse.setRetCode("0");
                    List<Map<String, String>> testData = new ArrayList<>();
                    Map<String, String> testDataRow1 = new HashMap<>();
                    Map<String, String> testDataRow2 = new HashMap<>();
                    testDataRow1.put("hostIp", "123.1.1.0");
                    testDataRow1.put("retCode", "0");
                    testDataRow1.put("status", "Finished");
                    testData.add(testDataRow1);
                    testDataRow2.put("hostIp", "123.1.1.1");
                    testDataRow2.put("retCode", "1");
                    testDataRow2.put("status", "Distributed");
                    testData.add(testDataRow2);
                    testResponse.setData(testData);
                    testResponse.setParams("");
                    String jsonObject = new Gson().toJson(testResponse);
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer8001.start();
    }

    public void after() {
        httpServer.stop(1);
        httpServer8001.stop(1);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_when_clean_release_can_not_get_token() {
        boolean isOk = projectService.cleanUnreleasedEnv();
        Assert.assertFalse(isOk);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_when_clean_release() throws IOException {
        before();
        boolean isOk = projectService.cleanUnreleasedEnv();
        Assert.assertTrue(isOk);
        after();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_getExpirTime() throws Exception {
        packageServiceFacade.scheduledDeletePackage();
        File tempFile = Resources.getResourceAsFile("testfile/logo.png");
        long expTime = packageServiceFacade.getExpireTime(tempFile);
        Assert.assertTrue(expTime > 0);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_when_schedule_query_order() throws IOException {
        before();
        boolean isOk = orderService.scheduledQueryOrder();
        Assert.assertTrue(isOk);
        after();
    }
}
