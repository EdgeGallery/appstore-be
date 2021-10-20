/* Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.order.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import org.edgegallery.appstore.interfaces.TestApplicationWithNoAdmin;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigOperReqDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationWithNoAdmin.class)
@AutoConfigureMockMvc
public class SplitConfigWithNoAdminTest {

    private static final String APPID = "1abdd29f-b281-4f92-8349-b5621b67d217";

    @Autowired
    private MockMvc mvc;

    private static Gson gson = new Gson();

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void querysplitconfig_nopermission() throws Exception {
        querySplitConfig();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void querysplitconfig_nopermission_tenant() throws Exception {
        querySplitConfig();
    }

    private void querySplitConfig() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/v1/apps/splitconfigs").contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void addsplitconfig_nopermission() throws Exception {
        addSplitConfig();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void addsplitconfig_nopermission_tenant() throws Exception {
    }

    private void addSplitConfig() throws Exception {
        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v1/apps/splitconfigs").with(csrf())
            .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void modifysplitconfig_nopermission() throws Exception {
        modifySplitConfig();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void modifysplitconfig_nopermission_tenant() throws Exception {
        modifySplitConfig();
    }

    private void modifySplitConfig() throws Exception {
        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.put("/mec/appstore/v1/apps/splitconfigs/" + APPID).with(csrf())
                .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void deletesplitconfig_nopermission() throws Exception {
        deleteSplitConfig();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void deletesplitconfig_nopermission_tenant() throws Exception {
        deleteSplitConfig();
    }

    private void deleteSplitConfig() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.delete("/mec/appstore/v1/apps/splitconfigs/" + APPID).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }
}
