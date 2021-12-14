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
import java.lang.management.GarbageCollectorMXBean;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.application.inner.AppService;
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

    @Autowired
    private AppService appService;

    private HttpServer httpServer;

    private String token = "123456789";

    private String hostIp = "192.168.0.1";

    @Autowired
    private MockMvc mvc;

    @Before
    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        httpServer.createContext("/mecm-north/v1/mechosts", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    Map<String, Object> mechost = new HashMap<>();
                    // change?
                    mechost.put("mechostIp", hostIp);
                    mechost.put("city", "xian");
                    mechost.put("vim", "K8s");
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
        httpServer.createContext("/apm/v1/tenants/testUserId/packages/testPackageId/hosts/192.168.0.1",
            new HttpHandler() {
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
        httpServer.createContext("/mecm-north/v1/tenants/testUserId/package", new HttpHandler() {
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
        // need to delete
        httpServer.createContext("/appo/v1/tenants/testUserId/apps/testAppId/packages/testPackageId/status",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                    if (!token.equals(accessToken)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                        exchange.getResponseBody().write("FORBIDDEN".getBytes());
                    } else if (method.equals("GET")) {
                        Map<String, String> mecInfo = new HashMap<>();
                        mecInfo.put("appInstanceId", "mecmInstanceId");
                        mecInfo.put("operationalStatus", "Instantiated");
                        String jsonObject = new Gson().toJson(mecInfo);
                        byte[] response = jsonObject.getBytes();
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                        exchange.getResponseBody().write(response);
                    }
                    exchange.close();
                }
            });
        httpServer.createContext("/mecm-north/v1/tenants/testUserId/packages/testPackageId",
            new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String method = exchange.getRequestMethod();
                    String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                    if (!token.equals(accessToken)) {
                        exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                        exchange.getResponseBody().write("FORBIDDEN".getBytes());
                    } else if (method.equals("GET")) {
                        MecmStatusRespDto testResponse = new MecmStatusRespDto();
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
        httpServer.start();
    }

    @After
    public void after() {
        httpServer.stop(1);
    }

    @Test
    public void getDeploymentStatus_success(){
        String userId = "testUserId";
        String mecmPkgId = "testPackageId";
        MecmDeploymentInfo testInfo = mecmService.getMecmDepolymentStatus(token, mecmPkgId, userId);
        Assert.assertNotNull(testInfo);
        Assert.assertEquals("Finished", testInfo.getMecmOperationalStatus());
    }

    @Test
    public void getDeploymentStatus_fail(){
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String mecmPkgId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";
        Assert.assertNull(mecmService.getMecmDepolymentStatus(token,mecmPkgId,userId));
    }

    @Test
    public void getMecHostByIpList_success() {
        String userId = "test-userid-0001";
        List<String> mecHostIpList = new ArrayList<>();
        mecHostIpList.add(hostIp);
        Release release = appService.getRelease("appid-test-0001", "packageid-0005");
        Map<String, MecHostBody> result = mecmService.getMecHostByIpList(token, userId, mecHostIpList, "appid-test-0001",
            "packageid-0005", release);
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
        Map<String, MecHostBody> result = mecmService.getMecHostByIpList(token, userId, mecHostIpList, "", "", release);
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
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get("/mecm-north/v1/mechost").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
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
        Map<String, String> params = new HashMap<>();
        String mecmPkgId = mecmService.upLoadPackageToMecmNorth(token, release, hostList, userId, params);
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
        Map<String, String> params = new HashMap<>();
        Assert.assertNull(mecmService.upLoadPackageToMecmNorth(token, release, hostList, userId, params));

        release.getAppBasicInfo().setVersion("");
        Assert.assertNull(mecmService.upLoadPackageToMecmNorth(token, release, hostList, userId, params));
    }

    /*
    @Test
    public void upLoadPackageToApm_success() throws IOException {
        String userId = "testUserId";
        File csarFile = Resources.getResourceAsFile("testfile/test2048_1.0.csar");
        AFile packageAFile = new AFile(csarFile.getName(), csarFile.getAbsolutePath());
        Release release = new Release();
        release.setPackageFile(packageAFile);
        release.setAppBasicInfo(new BasicInfo());
        release.getAppBasicInfo().setVersion("v1.0");
        MecmInfo mecmInfo = mecmService.upLoadPackageToApm(token, release, hostIp, userId);
        Assert.assertEquals(mecmInfo.getMecmAppId(), "mecmAppId");
    }

    @Test
    public void upLoadPackageToApm_fail() throws IOException {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        File csarFile = Resources.getResourceAsFile("testfile/test2048_1.0.csar");
        AFile packageAFile = new AFile(csarFile.getName(), csarFile.getAbsolutePath());
        Release release = new Release();
        release.setPackageFile(packageAFile);
        release.setAppBasicInfo(new BasicInfo());
        release.getAppBasicInfo().setVersion("v1.0");
        Assert.assertNull(mecmService.upLoadPackageToApm(token, release, hostIp, userId));

        release.getAppBasicInfo().setVersion("");
        Assert.assertNull(mecmService.upLoadPackageToApm(token, release, hostIp, userId));
    }

    @Test
    public void getMecmDepolymentStatus_success() throws IOException {
        String userId = "testUserId";
        String mecmAppId = "testAppId";
        String mecmAppPackageId = "testPackageId";
        MecmDeploymentInfo deploymentInfo = mecmService.getMecmDepolymentStatus(token, mecmAppId, mecmAppPackageId,
            userId);
        Assert.assertEquals(deploymentInfo.getMecmOperationalStatus(), "Instantiated");
    }

    @Test
    public void getMecmDepolymentStatus_fail() throws IOException {
        String userId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String mecmAppId = "testAppId";
        String mecmAppPackageId = "testPackageId";
        Assert.assertNull(mecmService.getMecmDepolymentStatus(token, mecmAppId, mecmAppPackageId, userId));
    }

     */
}
