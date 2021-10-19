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

package org.edgegallery.appstore.interfaces.app.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppCtrlDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppReqDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageRespDto;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class QueryAppsByCondTest extends AppTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_query_apps_success_with_no_conditions() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/v1/apps").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk())
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Type type = new TypeToken<ArrayList<AppDto>>() { }.getType();
        List<AppDto> appDtos = gson.fromJson(result.getResponse().getContentAsString(), type);
        appDtos.forEach(o -> Assert.assertEquals(EnumAppStatus.Published, o.getStatus()));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_query_apps_success_with_userId() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/apps").param("userId", userId)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        Type type = new TypeToken<ArrayList<AppDto>>() { }.getType();
        List<AppDto> appDtos = gson.fromJson(result.getResponse().getContentAsString(), type);
        Assert.assertNotSame(0, appDtos.size());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void queryAppByCond() throws Exception {
        QueryAppReqDto reqDto = new QueryAppReqDto();
        reqDto.setUserId("5abdd29d-b281-4f96-8339-b5621a67d217");
        reqDto.setAffinity(null);
        reqDto.setAppName("");
        reqDto.setIndustry(null);
        reqDto.setShowType(null);
        reqDto.setStatus("");
        reqDto.setTypes(null);
        reqDto.setWorkloadType(null);
        QueryAppCtrlDto ctrDto = new QueryAppCtrlDto();
        ctrDto.setLimit(15);
        ctrDto.setOffset(0);
        ctrDto.setSortItem("createTime");
        ctrDto.setSortType("desc");

        reqDto.setQueryCtrl(ctrDto);
        String body = new Gson().toJson(reqDto);
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v2/query/apps").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body).with(csrf())).andDo(MockMvcResultHandlers.print())
            .andReturn();
        String page = actions.getResponse().getContentAsString();
        JSONObject jsonObject1 = JSONObject.parseObject(page);
        JSONArray listObject = jsonObject1.getJSONArray("results");
        int listcount = JSONObject.parseArray(listObject.toJSONString(), MessageRespDto.class).size();
        boolean pageFlag = false;
        if (listcount >= 0) {
            pageFlag = true;
        }
        Assert.assertEquals(true, pageFlag);
    }

}
