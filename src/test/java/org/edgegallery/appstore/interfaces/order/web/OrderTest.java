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
import java.util.Map;
import java.util.Optional;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.QueryCtrlDto;
import org.edgegallery.appstore.interfaces.TestApplicationWithAdmin;
import org.edgegallery.appstore.interfaces.controlleradvice.RestReturn;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryOrdersReqDto;
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
import org.springframework.util.CollectionUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationWithAdmin.class)
@AutoConfigureMockMvc
public class OrderTest {

    private static final String APPID = "appid-test-0001";

    private static final String APPPACKAGEID = "packageid-0003";

    private static final String MECHOSTIP = "127.0.0.1";

    private static Gson gson = new Gson();

    @Autowired
    private MockMvc mvc;

    private MvcResult createOrder() throws Exception {
        CreateOrderReqDto createOrderReqDto = new CreateOrderReqDto();
        createOrderReqDto.setAppId(APPID);
        createOrderReqDto.setAppPackageId(APPPACKAGEID);
        createOrderReqDto.setMecHostIp(MECHOSTIP);
        return mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/orders").with(csrf()).content(gson.toJson(createOrderReqDto))
                .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Before
    public void beforeTest() throws Exception {
        MvcResult result = createOrder();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void createorder_should_success() throws Exception {
        MvcResult result = createOrder();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_orders_should_success() throws Exception {
        MvcResult mvcResult = queryOrderList();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void deactivate_order_should_success() throws Exception {
        MvcResult mvcResult = deactivateOrder();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    private MvcResult deactivateOrder() throws Exception {
        String orderId = getOrderIdByStatus(EnumOrderStatus.ACTIVATED.toString());
        Assert.assertNotNull(orderId);
        Assert.assertNotEquals("", orderId);
        return mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/orders/" + orderId + "/deactivation").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void deactivate_deactivatedorder_should_failed() throws Exception {
        MvcResult mvcResult = deactivateOrder();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        String orderId = getOrderIdByStatus(EnumOrderStatus.DEACTIVATED.toString());
        Assert.assertNotNull(orderId);
        Assert.assertNotEquals("", orderId);
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/orders/" + orderId + "/deactivation").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        RestReturn restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_NOT_ALLOWED_DEACTIVATE_ORDER, restReturn.getRetCode());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void activate_order_should_success() throws Exception {
        MvcResult mvcResult = deactivateOrder();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());

        String orderId = getOrderIdByStatus(EnumOrderStatus.DEACTIVATED.toString());
        Assert.assertNotNull(orderId);
        Assert.assertNotEquals("", orderId);
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/orders/" + orderId + "/activation").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void activate_activatedorder_should_failed() throws Exception {
        String orderId = getOrderIdByStatus(EnumOrderStatus.ACTIVATED.toString());
        Assert.assertNotNull(orderId);
        Assert.assertNotEquals("", orderId);
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/orders/" + orderId + "/activation").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        RestReturn restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_NOT_ALLOWED_ACTIVATE_ORDER, restReturn.getRetCode());
    }

    private MvcResult queryOrderList() throws Exception {
        QueryOrdersReqDto queryOrdersReqDto = new QueryOrdersReqDto();
        QueryCtrlDto queryCtrl = new QueryCtrlDto(0, 20, "", "");
        queryOrdersReqDto.setQueryCtrl(queryCtrl);
        return mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v1/orders/list").with(csrf())
            .content(gson.toJson(queryOrdersReqDto)).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
    }

    private String getOrderIdByStatus(String status) throws Exception {
        MvcResult mvcResult = queryOrderList();
        if (mvcResult.getResponse().getStatus() != HttpStatus.OK.value()) {
            return "";
        }

        Page orderResult = gson.fromJson(mvcResult.getResponse().getContentAsString(), Page.class);
        if (orderResult == null || CollectionUtils.isEmpty(orderResult.getResults())) {
            return "";
        }

        Optional<Map> firstMatchOrder = orderResult.getResults().stream()
            .filter(item -> status.equals((String) ((Map) item).get("status"))).findFirst();
        return firstMatchOrder.isPresent() ? (String) firstMatchOrder.get().get("orderId") : "";
    }
}