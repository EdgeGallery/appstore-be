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

import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
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

public class GetPackageByIdTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void getAppPackageSuccess() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        ResultActions resultActions = mvc.perform(
            MockMvcRequestBuilders.get(REST_API_ROOT + appId + REST_API_PACKAGES + packageId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());
        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertTrue(obj.getStatus() == 200);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void getAppPackageWithException() throws Exception {
        boolean checkResult = false;
        String appId = "30ec10f4a43041e6a6198ba824311af4";
        String packageId = "44a00b12c13b43318d21840793549339";

            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId + REST_API_PACKAGES + packageId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertTrue(obj.getStatus() == 404);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void getAppPackageFailedByWrongId() throws Exception {
        String appId = "wrongId";
        String packageId = "44a00b12c13b43318d21840793549339";
        ResultActions resultActions =
                mvc.perform(MockMvcRequestBuilders.get(REST_API_ROOT + appId + REST_API_PACKAGES + packageId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());
            MvcResult result = resultActions.andReturn();
            MockHttpServletResponse obj = result.getResponse();
            System.out.println(obj.getCharacterEncoding());
            Assert.assertTrue(obj.getStatus() == 400);
    }

}
