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

package org.edgegallery.appstore.interfaces.appstore.web;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.google.gson.Gson;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class AppStoreTest {

    protected String appStoreId = "a09bca74-04cb-4bae-9ee2-9c5072ec9d4b";

    protected Gson gson = new Gson();

    @Autowired
    protected MockMvc mvc;

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void add_appstore_should_success() throws Exception {
        MvcResult result = addAppstore();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    public MvcResult addAppstore() throws Exception {
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/appstores")
            .file(new MockMultipartFile("appStoreName", "", MediaType.TEXT_PLAIN_VALUE, "test appstore".getBytes()))
            .file(new MockMultipartFile("appStoreVersion", "", MediaType.TEXT_PLAIN_VALUE, "1.0".getBytes()))
            .file(new MockMultipartFile("company", "", MediaType.TEXT_PLAIN_VALUE, "huawei".getBytes()))
            .file(new MockMultipartFile("url", "", MediaType.TEXT_PLAIN_VALUE, "http://127.0.0.1:8099".getBytes()))
            .file(new MockMultipartFile("appdTransId", "", MediaType.TEXT_PLAIN_VALUE, "社区_APPD_2.0".getBytes()))
            .file(new MockMultipartFile("description", "", MediaType.TEXT_PLAIN_VALUE, "test".getBytes())).with(csrf()));
        return resultActions.andReturn();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_all_appstore_should_success() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/v1/appstores").param("appStoreName", "test name")
                .param("company", "liantong").param("limitSize", "12").param("offsetPage", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_appstore_by_id_should_success() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/v1/appstores/" + appStoreId)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        gson.fromJson(result.getResponse().getContentAsString(), RegisterRespDto.class);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void delete_appstore_should_success() throws Exception {
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.delete("/mec/appstore/v1/appstores/" + appStoreId).with(csrf()))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }
}
