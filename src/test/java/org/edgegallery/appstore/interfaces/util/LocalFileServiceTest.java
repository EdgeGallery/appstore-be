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

package org.edgegallery.appstore.interfaces.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.UUID;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.files.LocalFileServiceImpl;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
public class LocalFileServiceTest {

    private HttpServer httpServer;

    @Autowired
    private LocalFileServiceImpl fileService;

    @Before
    public void before() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8098), 0);
        httpServer.createContext("/mec/appstore/v1/packages", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                if (method.equals("GET")) {
                    byte[] response = new byte[1000];
                    exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
                    exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=testFileName");
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
    public void should_success_when_download_file() {
        String packageDownloadUrl = "http://127.0.0.1:8098/mec/appstore/v1/packages/packageid-0002/action/download-package";
        String parentPath = "usr/apptest/packages" + File.separator + UUID.randomUUID().toString().replace("-", "");
        String targetAppstore = "EdgeGallery AppStore";

        File file = fileService.downloadFile(packageDownloadUrl, parentPath, targetAppstore);
        Assert.assertNotNull(file);
    }

    @Test(expected = AppException.class)
    public void should_exception_when_download_file() {
        String packageDownloadUrl = "http://127.0.0.1:8099/mec/appstore/v1/packages/packageid-0002/action/download-package";
        String parentPath = "usr/apptest/packages" + File.separator + UUID.randomUUID().toString().replace("-", "");
        String targetAppstore = "EdgeGallery AppStore";

        fileService.downloadFile(packageDownloadUrl, parentPath, targetAppstore);
    }

    @Test
    public void should_success_when_create_file() throws IOException {
        String filePath = "usr/apptest/packages" + File.separator + UUID.randomUUID().toString().replace("-", "");
        String canonicalPath = LocalFileServiceImpl.sanitizeFileName("testfile", filePath);

        Assert.assertNotNull(canonicalPath);
    }
}
