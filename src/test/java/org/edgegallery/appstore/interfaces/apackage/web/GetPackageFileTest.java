/* Copyright 2020-2021 Huawei Technologies Co., Ltd.
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


import org.edgegallery.appstore.interfaces.AppTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class GetPackageFileTest extends AppTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() throws Exception {
        String filePath = "positioning-service.mf";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.post(String.format("/mec/appstore/v1/apps/%s/packages/%s/files", appId, packageId))
                .param("filePath", filePath).with(csrf()).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }


    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_failed_with_empty_file_name() throws Exception {
        String filePath = "";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.post(String.format("/mec/appstore/v1/apps/%s/packages/%s/files", appId, packageId))
                .param("filePath", filePath).with(csrf()).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_failed_with_file_not_exist() throws Exception {
        String filePath = "positioning_eg_1.0:positioning-service.mf111";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.post(String.format("/mec/appstore/v1/apps/%s/packages/%s/files", appId, packageId))
                .param("filePath", filePath).with(csrf()).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
}
