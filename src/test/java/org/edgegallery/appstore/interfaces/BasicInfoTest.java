/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.appstore.interfaces;

import java.io.File;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.releases.BasicInfo;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class BasicInfoTest {

    @Test(expected = AppException.class)
    public void should_exception_rewrite_mf() throws Exception {
        File mfFile = Resources.getResourceAsFile("testfile/csar/test_csar.mf");
        File imageFile = Resources.getResourceAsFile("testfile/csar/Image/cirros.zip");
        File certFile =  Resources.getResourceAsFile("keys/public.p12");
        new BasicInfo().rewriteManifestWithImage(mfFile, imageFile.getCanonicalPath(), certFile.getCanonicalPath(), "Test12345_");
    }
}
