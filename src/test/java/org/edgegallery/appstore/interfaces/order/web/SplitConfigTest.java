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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.TestApplicationWithAdmin;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigOperReqDto;
import org.junit.Assert;
import org.junit.Before;
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
@SpringBootTest(classes = TestApplicationWithAdmin.class)
@AutoConfigureMockMvc
public class SplitConfigTest {

    @Autowired
    private MockMvc mvc;

    private static Gson gson = new Gson();

    private MvcResult addSplitConfig() throws Exception {
        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        List<String> appIds = new ArrayList<>();
        appIds.add(UUID.randomUUID().toString());
        splitConfigOperReqDto.setAppIds(appIds);
        splitConfigOperReqDto.setSplitRatio(0.36);
        return mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v1/apps/splitconfigs").with(csrf())
            .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Before
    public void beforeTest() throws Exception {
        MvcResult result = addSplitConfig();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void addsplitconfig_should_success() throws Exception {
        MvcResult result = addSplitConfig();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void addsplitconfig_invalid_param() throws Exception {
        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        splitConfigOperReqDto.setSplitRatio(0.36);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v1/apps/splitconfigs").with(csrf())
            .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void querysplitconfig_should_success() throws Exception {
        MvcResult result = querySplitConfigList();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    private MvcResult querySplitConfigList() throws Exception {
        return mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/v1/apps/splitconfigs").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void modifysplitconfig_forall_should_success() throws Exception {
        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        splitConfigOperReqDto.setSplitRatio(0.41);
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.put("/mec/appstore/v1/apps/splitconfigs/all").with(csrf())
                .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        splitConfigOperReqDto.setSplitRatio(0.42);
        mvcResult = mvc.perform(MockMvcRequestBuilders.put("/mec/appstore/v1/apps/splitconfigs/all").with(csrf())
            .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void modifysplitconfig_forapp_noexist() throws Exception {
        String appId = "1abdd29f-b281-4f92-8349-b5621b67d217";
        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        splitConfigOperReqDto.setSplitRatio(0.43);
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.put("/mec/appstore/v1/apps/splitconfigs/" + appId).with(csrf())
                .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void modifysplitconfig_forapp_should_success() throws Exception {
        String appId = getFirstAppId();
        Assert.assertNotNull(appId);
        Assert.assertNotEquals("", appId);

        SplitConfigOperReqDto splitConfigOperReqDto = new SplitConfigOperReqDto();
        splitConfigOperReqDto.setSplitRatio(0.43);
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.put("/mec/appstore/v1/apps/splitconfigs/" + appId).with(csrf())
                .content(gson.toJson(splitConfigOperReqDto)).contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void deletesplitconfig_forapp_should_success() throws Exception {
        String appId = getFirstAppId();
        Assert.assertNotNull(appId);
        Assert.assertNotEquals("", appId);

        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.delete("/mec/appstore/v1/apps/splitconfigs/" + appId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    private String getFirstAppId() throws Exception {
        MvcResult mvcResult = querySplitConfigList();
        if (mvcResult.getResponse().getStatus() != HttpStatus.OK.value()) {
            return "";
        }

        System.out.println(mvcResult.getResponse().getContentAsString());
        ResponseObject resObject = gson.fromJson(mvcResult.getResponse().getContentAsString(), ResponseObject.class);

        String dataValue = resObject.getData().toString().replace("appId=all", "");
        int pos = dataValue.indexOf("appId=");
        if (pos > 0) {
            dataValue = dataValue.substring(pos + 6);
            pos = dataValue.indexOf(",");
            if (pos > 0) {
                return dataValue.substring(0, pos);
            }
        }

        return "";
    }
}
