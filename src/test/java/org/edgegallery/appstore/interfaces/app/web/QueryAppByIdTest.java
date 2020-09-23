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
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class QueryAppByIdTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void getAppSuccess() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af2";
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON));

        MvcResult result = resultActions.andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }


    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void getAppFailed() throws Exception {
        boolean checkResult = false;
        String appId = "30ec10f4a43041e6a6198ba824311af9"; //app is not exist.
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
        MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
        int result = mvcResult.getResponse().getStatus();
        Assert.assertEquals(result, HttpStatus.NOT_FOUND.value());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void getAppFailedWithAttackId() throws Exception {
        String appId = "attackId"; //app id is not match the parameter check reg {appId:[0-9a-f]{32}}.
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
    }
}
