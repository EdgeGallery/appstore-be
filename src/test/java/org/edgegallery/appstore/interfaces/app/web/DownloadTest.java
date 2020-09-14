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


import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.interfaces.AppInterfacesTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

public class DownloadTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void downloadAppSuccess() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";
        try {
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            File storeFile = new File(
                File.separator + "home" + File.separator + APPSTORE_ROOT + File.separator + REAL_APP_ID + File.separator
                    + REAL_APP_ID + CSAR_EXTENSION);
            createFile(storeFile.getCanonicalPath());
            Files.copy(csarFile, storeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId + ACTION_DOWNLOAD).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void downloadAppWithWrongId() throws Exception {
        String appId = "testId"; //testId is not match the [0-9a-f]{32}

        try {
            mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId + ACTION_DOWNLOAD).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_OCTET_STREAM));
        } catch (NestedServletException e) {
            Assert.assertEquals("download.appId: must match \"[0-9a-f]{32}\"", e.getRootCause().getMessage());
        }
    }
}
