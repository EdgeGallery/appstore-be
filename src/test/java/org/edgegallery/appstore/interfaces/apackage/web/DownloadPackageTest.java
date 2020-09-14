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

package org.edgegallery.appstore.interfaces.apackage.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.interfaces.AppInterfacesTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class DownloadPackageTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_download_package_success() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        try {
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            File storeFile = new File(
                "" + File.separator + "home" + File.separator + APPSTORE_ROOT + File.separator + REAL_APP_ID
                    + File.separator + REAL_APP_ID + CSAR_EXTENSION);
            createFile(storeFile.getCanonicalPath());
            FileUtils.copyFile(csarFile, storeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResultActions resultActions = mvc.perform(
            MockMvcRequestBuilders.get(REST_API_ROOT + appId + REST_API_PACKAGES + packageId + ACTION_DOWNLOAD)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertNotNull(obj.getContentAsString());
    }
}
