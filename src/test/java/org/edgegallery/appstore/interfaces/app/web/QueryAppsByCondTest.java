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

import java.io.File;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.interfaces.AppInterfacesTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class QueryAppsByCondTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_query_apps_success_with_no_conditions() throws Exception {
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/apps")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = resultActions.andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_query_apps_success_with_appName() throws Exception {
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/apps?name=AR")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = resultActions.andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_query_apps_success_with_other_name() throws Exception {
        String resultCompare = "[]";
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/apps?name=Nothing")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertEquals(obj.getContentAsString(), resultCompare);
    }
}
