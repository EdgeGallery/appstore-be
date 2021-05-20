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

import org.edgegallery.appstore.interfaces.AppTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class GetPackageByIdTest extends AppTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_with_wrong_appId() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_with_wrong_packageId() throws Exception {
        String packageId = "30ec10f4a43041e6a6198ba824311af3";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

}
