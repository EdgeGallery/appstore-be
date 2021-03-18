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

package org.edgegallery.appstore.interfaces.message.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
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
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class MessageTest {

    @Autowired
    protected MockMvc mvc;

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
}
