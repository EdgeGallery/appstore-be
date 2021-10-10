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

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.files.LocalFileServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LocalFileServiceTest {

    @Autowired
    private LocalFileServiceImpl fileService;

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
