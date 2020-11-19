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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class AppTest {

    @Autowired
    protected MockMvc mvc;

    static final String POSITIONING_EG_1_CSAR = "testfile/positioning_eg_1.0.csar";

    static final String POSITIONING_EG_UNIQUE_CSAR = "testfile/positioning_eg_unique.csar";

    static final String LOGO_PNG = "testfile/logo.png";

    static final String AR_PNG = "testfile/AR.png";

    public MvcResult registerApp(String iconAddr, String csarAddr, String userId, String userName) throws Exception {
        return registerApp(iconAddr, csarAddr, userId, userName, "Video", "test", "GPU", "Smart City");
    }

    public MvcResult registerApp(String iconAddr, String csarAddr, String userId, String userName, String type,
        String shortDesc, String affinity, String industry) throws Exception {
        File iconFile = Resources.getResourceAsFile(iconAddr);
        File csarFile = Resources.getResourceAsFile(csarAddr);
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
            .file(new MockMultipartFile("file", "positioning_eg_1.csar", MediaType.MULTIPART_FORM_DATA_VALUE,
                FileUtils.openInputStream(csarFile)))
            .file(new MockMultipartFile("icon", "logo.png", MediaType.MULTIPART_FORM_DATA_VALUE,
                FileUtils.openInputStream(iconFile)))
            .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, type.getBytes()))
            .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, shortDesc.getBytes()))
            .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, affinity.getBytes()))
            .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE, industry.getBytes())).with(csrf())
            .param("userId", userId)
            .param("userName", userName));
        return resultActions.andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Test
    public void test() {
        // empty test
    }

}
