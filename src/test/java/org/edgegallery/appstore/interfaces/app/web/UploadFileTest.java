/* Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.interfaces.app.web;

import com.spencerwi.either.Either;
import java.io.File;
import java.io.FileInputStream;
import org.edgegallery.appstore.domain.model.system.lcm.UploadedFile;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.system.facade.SystemService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

public class UploadFileTest extends AppTest {

    @Autowired
    private SystemService systemService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() throws Exception {
        File file = ResourceUtils.getFile("classpath:testfile/logo.png");
        FileInputStream fileInputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), "originalFilename", "", fileInputStream);
        Either<ResponseObject, UploadedFile> res = systemService
            .uploadFile("e111f3e7-90d8-4a39-9874-ea6ea6752edd", multipartFile);
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_no_originalFilename() throws Exception {
            File file = ResourceUtils.getFile("classpath:testfile/logo.png");
            FileInputStream fileInputStream = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile(file.getName(), fileInputStream);
            // expectedEx.expect(IllegalRequestException.class);
            expectedEx.expectMessage( "File Name is invalid.");
            Either<ResponseObject, UploadedFile> res = systemService
                .uploadFile("e111f3e7-90d8-4a39-9874-ea6ea6752edd", multipartFile);

    }
}
