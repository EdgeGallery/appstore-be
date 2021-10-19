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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.infrastructure.persistence.apackage.AppReleasePo;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

public class ProjectServiceCleanEnvTest extends AppTest {

    @Autowired
    private ProjectService projectService;

    private HttpServer httpServer;

    private HttpServer httpServer30091;

    private String token = "123456789";

    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 38067), 0);
        httpServer.createContext("/login", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("POST")) {
                    exchange.getResponseHeaders().add("XSRF-TOKEN", "ddexxx-dfwefdgwer");
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 1);
                }
                exchange.close();
            }
        });
        httpServer.start();

        httpServer30091 = HttpServer.create(new InetSocketAddress("localhost", 30091), 0);
        httpServer30091.createContext("/auth/login-info", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                LoginInfoRespDto loginInfoRespDto = LoginInfoRespDto.builder().accessToken("4687632346763131324564").build();
                String dtp = new Gson().toJson(loginInfoRespDto);
                byte[] response = dtp.getBytes();
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            }
        });
        httpServer30091.start();
    }

    public void after() {
        httpServer.stop(1);
        httpServer30091.stop(1);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_when_clean_release_can_not_get_token() throws IOException {
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

    // @Test
    // @WithMockUser(roles = "APPSTORE_TENANT")
    // public void should_success_when_clean_release2() throws IOException {
    //     before();
    //     Optional.ofNullable(packageMapper.findReleaseNoCondtion()).ifPresent(r -> {
    //         AppReleasePo appReleasePo = r.get(0);
    //         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //         Calendar calendar = Calendar.getInstance();
    //         calendar.setTime(new Date());
    //         calendar.add(Calendar.DAY_OF_YEAR, -2);
    //         String data = sdf.format(calendar.getTime());
    //         appReleasePo.setStartExpTime(data);
    //         packageMapper.updateAppInstanceApp(appReleasePo);
    //     });
    //     boolean isOk = projectService.cleanUnreleasedEnv();
    //     Assert.assertTrue(isOk);
    //     after();
    // }
}
