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

import org.edgegallery.appstore.interfaces.AppInterfacesTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class DownloadIconTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_download_icon_success() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";

        ResultActions resultActions = mvc.perform(
            MockMvcRequestBuilders.get(REST_API_ROOT + appId + ICON)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM));
        MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print()).andReturn();
        int status = mvcResult.getResponse().getStatus();
        Assert.assertEquals(HttpStatus.OK.value(), status);
    }
}
