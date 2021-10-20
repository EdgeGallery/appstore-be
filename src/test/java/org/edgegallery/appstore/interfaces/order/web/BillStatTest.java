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
import org.edgegallery.appstore.domain.shared.QueryCtrlDto;
import org.edgegallery.appstore.interfaces.TestApplicationWithAdmin;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryBillsReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.StatOverallReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopOrderAppReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopSaleAppReqDto;
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
@SpringBootTest(classes = TestApplicationWithAdmin.class)
@AutoConfigureMockMvc
public class BillStatTest {
    private static Gson gson = new Gson();

    @Autowired
    protected MockMvc mvc;

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void querybilllist_should_success() throws Exception {
        QueryBillsReqDto queryBillsReqDto = new QueryBillsReqDto();
        queryBillsReqDto.setQueryCtrl(new QueryCtrlDto(0, 0, "", ""));
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v1/bills/list").with(csrf())
            .content(gson.toJson(queryBillsReqDto)).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void statoverall_should_success() throws Exception {
        StatOverallReqDto statOverallReqDto = new StatOverallReqDto();
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/bills/statistics/overall").with(csrf())
                .content(gson.toJson(statOverallReqDto)).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void stattopsaleapp_should_success() throws Exception {
        TopSaleAppReqDto topSaleAppReqDto = new TopSaleAppReqDto();
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/bills/statistics/sales/topapps").with(csrf())
                .content(gson.toJson(topSaleAppReqDto)).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void stattoporderapp_should_success() throws Exception {
        TopOrderAppReqDto topOrderAppReqDto = new TopOrderAppReqDto();
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/bills/statistics/orders/topapps").with(csrf())
                .content(gson.toJson(topOrderAppReqDto)).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }
}
