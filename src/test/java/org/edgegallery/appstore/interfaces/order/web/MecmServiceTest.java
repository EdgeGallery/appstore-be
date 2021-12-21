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
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.BasicInfo;
import org.edgegallery.appstore.domain.model.releases.Release;
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
        httpServer.createContext("/north/v1/mechosts", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    List<Map<String, Object>> mechosts = new ArrayList<>();
                    Map<String, Object> mechost = new HashMap<>();
                    mechost.put("mechostIp", hostIp);
                    mechost.put("mechostName", "38node1");
                    mechost.put("mechostCity", "xian");
                    mechost.put("vim", "K8s");
                    mechost.put("affinity", "X86");
                    mechosts.add(mechost);
                    Map<String, Object> rsp = new HashMap<>();
                    rsp.put("data", mechosts);
                    rsp.put("retCode", 0);
                    rsp.put("message", "query mecm host success.");
                    String jsonObject = new Gson().toJson(rsp);
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/north/v1/tenants/testUserId/package", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("POST")) {
                    Map<String, String> mecInfo = new HashMap<>();
                    mecInfo.put("mecmPackageId", "mecmPackageId");
                    mecInfo.put("data", null);
                    mecInfo.put("retCode", "0");
                    mecInfo.put("message", "Create server in progress");
                    mecInfo.put("params", null);
                    String jsonObject = new Gson().toJson(mecInfo);
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/north/v1/tenants/testUserId/packages/testPackageId", new HttpHandler() {
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
                } else if (method.equals("DELETE")) {
                    MecmRespDto testResponse = new MecmRespDto();
                    testResponse.setMecmPackageId("mecmPkgId");
                    testResponse.setMessage("Query server success");
                    testResponse.setRetCode("0");
                    List<Map<String, String>> testData = new ArrayList<>();
                    Map<String, String> testDataRow1 = new HashMap<>();
                    Map<String, String> testDataRow2 = new HashMap<>();
                    testDataRow1.put("hostIp", "123.1.1.0");
                    testDataRow1.put("retCode", "0");
                    testDataRow1.put("message", "Delete server success");
                    testData.add(testDataRow1);
                    testDataRow2.put("hostIp", "123.1.1.1");
                    testDataRow2.put("retCode", "1");
                    testDataRow2.put("message", "failed to delete package");
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
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void queryMecmHosts() throws Exception {
        String url = String.format("/mec/appstore/v1/mechosts");
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void queryMecmHosts_with_params() throws Exception {
        String appId = "appid-test-0001";
        String packageId = "packageid-0005";
        String url = String.format("/mec/appstore/v1/mechosts?appId=%s&packageId=%s", appId, packageId);
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void getMecHostByIpList_success() {
        String userId = "test-userid-0001";
        List<String> mecHostIpList = new ArrayList<>();
        mecHostIpList.add(hostIp);
        Map<String, MecHostBody> result = mecmService.getMecHostByIpList(token, mecHostIpList);
        Assert.assertNotNull(result);
        MecHostBody mecHost = result.get(hostIp);
        Assert.assertEquals("xian", mecHost.getCity());
    }

    @Test
    public void getMecHostByIpList_empty() {
        String userId = "testUserId";
        List<String> mecHostIpList = new ArrayList<>();
        mecHostIpList.add("127.0.0.4");
        Release release = new Release();
        Map<String, MecHostBody> result = mecmService.getMecHostByIpList(token, mecHostIpList);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void upLoadPackageToMecmNorth_success() throws IOException {
        String userId = "testUserId";
        File csarFile = Resources.getResourceAsFile("testfile/test2048_1.0.csar");
        AFile packageAFile = new AFile(csarFile.getName(), csarFile.getAbsolutePath());
        Release release = new Release();
        release.setPackageFile(packageAFile);
        release.setAppBasicInfo(new BasicInfo());
        release.getAppBasicInfo().setVersion("v1.0");
        String hostList = "testHostList";
        String params = "";
        String mecmPkgId = mecmService.upLoadPackageToNorth(token, release, hostList, userId, params);
        Assert.assertEquals(mecmPkgId, "mecmPackageId");
    }

    @Test
    public void upLoadPackageToMecmNorth_fail() throws IOException {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        File csarFile = Resources.getResourceAsFile("testfile/test2048_1.0.csar");
        AFile packageAFile = new AFile(csarFile.getName(), csarFile.getAbsolutePath());
        Release release = new Release();
        release.setPackageFile(packageAFile);
        release.setAppBasicInfo(new BasicInfo());
        release.getAppBasicInfo().setVersion("v1.0");
        String hostList = "testHostList";
        String params = "";
        Assert.assertNull(mecmService.upLoadPackageToNorth(token, release, hostList, userId, params));

        release.getAppBasicInfo().setVersion("");
        Assert.assertNull(mecmService.upLoadPackageToNorth(token, release, hostList, userId, params));
    }

    @Test
    public void getDeploymentStatus_success() {
        String userId = "testUserId";
        String mecmPkgId = "testPackageId";
        MecmDeploymentInfo testInfo = mecmService.getDepolymentStatus(token, mecmPkgId, userId);
        Assert.assertNotNull(testInfo);
        Assert.assertEquals("Finished", testInfo.getMecmOperationalStatus());
    }

    @Test
    public void getDeploymentStatus_fail() {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String mecmPkgId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";
        Assert.assertNull(mecmService.getDepolymentStatus(token, mecmPkgId, userId));
    }

    @Test
    public void deleteServer_success() {
        String userId = "testUserId";
        String mecmPkgId = "testPackageId";
        Assert.assertEquals("Delete server success", mecmService.deleteServer(userId, mecmPkgId, token));
    }

    @Test
    public void deleteServer_fail() {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String mecmPkgId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";
        Assert.assertNull(mecmService.deleteServer(userId, mecmPkgId, token));
    }
}
