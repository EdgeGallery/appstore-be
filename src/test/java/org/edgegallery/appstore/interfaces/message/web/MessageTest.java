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

package org.edgegallery.appstore.interfaces.message.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edgegallery.appstore.application.inner.MessageService;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageDateEnum;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageRespDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.QueryMessageReqDto;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class MessageTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private MessageService messageService;

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void add_message_should_success() throws Exception {
        MvcResult result = addOneMessage();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    public MvcResult addOneMessage() throws Exception {
        MessageReqDto reqDto = new MessageReqDto();
        reqDto.setSourceAppStore("source appstore");
        reqDto.setAtpTestStatus("success");
        reqDto.setAtpTestTaskId("task id");
        reqDto.setAtpTestReportUrl("report url");
        reqDto.setIconDownloadUrl("icon download url");
        reqDto.setPackageDownloadUrl("package download url");
        BasicMessageInfo basicInfo = new BasicMessageInfo();
        basicInfo.setName("app name");
        basicInfo.setProvider("app provider");
        basicInfo.setVersion("v1");
        basicInfo.setShortDesc("test");
        basicInfo.setAffinity("X86");
        basicInfo.setIndustry("smart city");
        basicInfo.setType("Video");
        reqDto.setBasicInfo(basicInfo);
        String body = new Gson().toJson(reqDto);
        return mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/messages").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body).with(csrf())).andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_message_should_success() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/messages")
            .param("messageType", String.valueOf(EnumMessageType.NOTICE)).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Type type = new TypeToken<ArrayList<MessageRespDto>>() { }.getType();
        Gson gson = new Gson();
        List<MessageRespDto> appDtos = gson.fromJson(result.getResponse().getContentAsString(), type);
        Assert.assertEquals(0, appDtos.size());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_query_message() throws Exception {
        QueryMessageReqDto queryMessageReqDto = new QueryMessageReqDto();
        queryMessageReqDto.setMessageType("NOTICE");
        queryMessageReqDto.setLimit(5);
        queryMessageReqDto.setOffset(0);
        queryMessageReqDto.setTimeFlag(null);
        String body = new Gson().toJson(queryMessageReqDto);
        MvcResult actions = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v2/messages/action/query")
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(body).with(csrf()))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Type type = new TypeToken<ArrayList<MessageRespDto>>() { }.getType();
        Gson gson = new Gson();
        String page = actions.getResponse().getContentAsString();
        JSONObject jsonObject1 = JSONObject.parseObject(page);
        JSONArray listObject = jsonObject1.getJSONArray("results");
        List<MessageRespDto> list = JSONObject.parseArray(listObject.toJSONString(), MessageRespDto.class);
        Assert.assertNotEquals(0, list.size());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_with_diff_MessageType() throws Exception {
        QueryMessageReqDto queryMessageReqDto = new QueryMessageReqDto();
        queryMessageReqDto.setMessageType("NOTICE");
        queryMessageReqDto.setLimit(5);
        queryMessageReqDto.setOffset(0);
        queryMessageReqDto.setTimeFlag(null);
        String body = new Gson().toJson(queryMessageReqDto);
        MvcResult actions = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v2/messages/action/query")
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(body).with(csrf()))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Type type = new TypeToken<ArrayList<MessageRespDto>>() { }.getType();
        Gson gson = new Gson();
        String page = actions.getResponse().getContentAsString();
        JSONObject jsonObject1 = JSONObject.parseObject(page);
        JSONArray listObject = jsonObject1.getJSONArray("results");
        List<MessageRespDto> list = JSONObject.parseArray(listObject.toJSONString(), MessageRespDto.class);
        boolean typeFlag = false;
        for (MessageRespDto messageRespDto : list) {
            if (messageRespDto.getMessageType() != EnumMessageType.NOTICE) {
                typeFlag = true;
            }
        }
        Assert.assertEquals(false, typeFlag);
    }

    @WithMockUser(roles = "APPSTORE_ADMIN")
    public long should_success_query_count() {
        Map<String, Object> params = new HashMap<>();
        params.put("messageType", "NOTICE");
        return messageService.getAllMessageCount(params);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_with_two_page() throws Exception {
        QueryMessageReqDto queryMessageReqDto = new QueryMessageReqDto();
        queryMessageReqDto.setMessageType("NOTICE");
        queryMessageReqDto.setLimit(3);
        queryMessageReqDto.setOffset(2);
        queryMessageReqDto.setTimeFlag(null);
        String body = new Gson().toJson(queryMessageReqDto);
        MvcResult actions = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v2/messages/action/query")
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(body).with(csrf()))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        String page = actions.getResponse().getContentAsString();
        JSONObject jsonObject1 = JSONObject.parseObject(page);
        JSONArray listObject = jsonObject1.getJSONArray("results");
        int listcount = JSONObject.parseArray(listObject.toJSONString(), MessageRespDto.class).size();
        int count = (int) should_success_query_count();
        boolean pageFlag = false;
        if (listcount >= (count - 2)) {
            pageFlag = true;
        }
        Assert.assertEquals(true, pageFlag);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_with_diff_time() throws Exception {
        QueryMessageReqDto queryMessageReqDto = new QueryMessageReqDto();
        queryMessageReqDto.setMessageType("NOTICE");
        queryMessageReqDto.setLimit(5);
        queryMessageReqDto.setOffset(0);
        queryMessageReqDto.setTimeFlag(MessageDateEnum.WEEK);
        String body = new Gson().toJson(queryMessageReqDto);
        MvcResult actions = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v2/messages/action/query")
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(body).with(csrf()))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        String page = actions.getResponse().getContentAsString();
        JSONObject jsonObject1 = JSONObject.parseObject(page);
        JSONArray listObject = jsonObject1.getJSONArray("results");
        List<MessageRespDto> list = JSONObject.parseArray(listObject.toJSONString(), MessageRespDto.class);
        boolean dateFlag = false;
        for (MessageRespDto messageRespDto : list) {
            if (messageRespDto.getTime().startsWith("2021-08-25")) {
                dateFlag = true;
            }
        }
        Assert.assertEquals(false, dateFlag);
    }

}
