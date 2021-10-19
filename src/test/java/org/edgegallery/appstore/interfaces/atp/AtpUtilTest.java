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

package org.edgegallery.appstore.interfaces.atp;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.external.atp.AtpUtil;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class AtpUtilTest {

    @Autowired
    private AtpUtil atpUtil;

    private HttpServer httpServer;

    private boolean isDelete = false;

    private String token = "123456789";

    @Before
    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
        httpServer.createContext("/edgegallery/atp/v1/tasks", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("POST")) {
                    AtpTaskDto atpTaskDto = new AtpTaskDto();
                    atpTaskDto.setId("atp-test-id");
                    atpTaskDto.setStatus("testing");
                    String dtp = new Gson().toJson(atpTaskDto);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer.createContext("/edgegallery/atp/v1/tasks/atp-test-id", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    AtpTaskDto atpTaskDto = new AtpTaskDto();
                    atpTaskDto.setId("atp-test-id");
                    atpTaskDto.setStatus("testing");
                    String dtp = new Gson().toJson(atpTaskDto);
                    byte[] response = dtp.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                } else if (method.equals("DELETE")) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                    isDelete = true;
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
    public void should_success_when_create_task() throws IOException {
        File csarFIle = Resources.getResourceAsFile("testfile/new_csar.csar");
        AtpTestDto atpTestDto = atpUtil.sendCreateTask2Atp(csarFIle.getPath(), "123456789");
        Assert.assertNotNull(atpTestDto);
        Assert.assertEquals("atp-test-id", atpTestDto.getAtpTaskId());
    }

    @Test
    public void should_failed_when_create_task_no_token() throws IOException {
        File csarFIle = Resources.getResourceAsFile("testfile/new_csar.csar");
        AtpTestDto atpTestDto = atpUtil.sendCreateTask2Atp(csarFIle.getPath(), null);
        Assert.assertNotNull(atpTestDto);
        Assert.assertNull(atpTestDto.getAtpTaskId());
    }

    @Test(expected = AppException.class)
    public void should_failed_when_create_task_with_error_token() throws IOException {
        File csarFIle = Resources.getResourceAsFile("testfile/new_csar.csar");
        atpUtil.sendCreateTask2Atp(csarFIle.getPath(), "111111111");
    }

    @Test
    public void should_success_when_get_task_from_atp() {
        String status = atpUtil.getTaskStatusFromAtp("atp-test-id", "123456789");
        Assert.assertEquals("testing", status);
    }

    @Test
    public void should_failed_when_get_task_no_token() {
        String status = atpUtil.getTaskStatusFromAtp("atp-test-id", null);
        Assert.assertEquals("", status);
    }

    @Test
    public void should_success_when_del_report_from_atp() {
        isDelete = false;
        atpUtil.deleteTestReportFromAtp("atp-test-id", "123456789");
        Assert.assertTrue(isDelete);
    }

    @Test
    public void should_failed_when_del_report_no_token() {
        isDelete = false;
        atpUtil.deleteTestReportFromAtp("atp-test-id", null);
        Assert.assertFalse(isDelete);
    }

    @Test
    public void should_failed_when_del_report_with_error_token() {
        isDelete = false;
        atpUtil.deleteTestReportFromAtp("atp-test-id", "111111");
        Assert.assertFalse(isDelete);
    }

}
