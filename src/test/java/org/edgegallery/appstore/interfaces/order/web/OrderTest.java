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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.OrderService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.QueryCtrlDto;
import org.edgegallery.appstore.interfaces.TestApplicationWithAdmin;
import org.edgegallery.appstore.interfaces.controlleradvice.RestReturn;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryOrdersReqDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    protected MecmService mecmService;

    @Autowired
    private AppService appService;

    private HttpServer httpServer8001;

    private String token = "4687632346763131324564";

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
    public void before() throws IOException {
        System.out.println("start to test");
        httpServer8001 = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        httpServer8001.createContext("/mecm-north/v1/tenants/testUserId/packages/testPackageId", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String method = exchange.getRequestMethod();
                String accessToken = exchange.getRequestHeaders().get("access_token").get(0);
                if (!token.equals(accessToken)) {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, "FORBIDDEN".length());
                    exchange.getResponseBody().write("FORBIDDEN".getBytes());
                } else if (method.equals("GET")) {
                    MecmRespDto testResponse = new MecmRespDto();
                    testResponse.setMecmPackageId("mecmPkgId");
                    testResponse.setMessage("Query server success");
                    testResponse.setRetCode("0");
                    List<Map<String, String>> testData = new ArrayList<>();
                    Map<String, String> testDataRow1 = new HashMap<>();
                    Map<String, String> testDataRow2 = new HashMap<>();
                    testDataRow1.put("hostIp", "123.1.1.0");
                    testDataRow1.put("retCode", "0");
                    testDataRow1.put("status", "Finished");
                    testData.add(testDataRow1);
                    testDataRow2.put("hostIp", "123.1.1.1");
                    testDataRow2.put("retCode", "1");
                    testDataRow2.put("status", "Distributed");
                    testData.add(testDataRow2);
                    testResponse.setData(testData);
                    testResponse.setParams("");
                    String jsonObject = new Gson().toJson(testResponse);
                    byte[] response = jsonObject.getBytes();
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
                    exchange.getResponseBody().write(response);
                }
                exchange.close();
            }
        });
        httpServer8001.start();
    }

    @After
    public void after() {
        httpServer8001.stop(1);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void get_VM_Deploy_param_success() {
        Release release = appService.getRelease("appid-test-0001", "packageid-0002");
        String res = orderService.getVmDeployParams(release);
        Assert.assertNotNull(orderService.getVmDeployParams(release));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void create_order_should_success() throws Exception {
        String mecmPkgId = "mecm-test-pkgId";
        Mockito.when(mecmService.upLoadPackageToNorth(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any())).thenReturn(mecmPkgId);
        MvcResult result = createOrder();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void create_order_should_failed() throws Exception {
        String mecmPkgId = null;
        Mockito.when(mecmService.upLoadPackageToNorth(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any())).thenReturn(mecmPkgId);
        MvcResult result = createOrder();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        RestReturn restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_UPLOAD_PACKAGE_TO_MECM_NORTH_FAILED, restReturn.getRetCode());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_orders_should_success() throws Exception {
        MvcResult mvcResult = queryOrderList();
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void activate_order_should_success() throws Exception {
        String orderId = getOrderIdByStatus(EnumOrderStatus.DEACTIVATED.toString());
        Assert.assertNotNull(orderId);
        Assert.assertNotEquals("", orderId);
        String testPkgId = "mecm-test-packageid";
        Mockito.when(mecmService.upLoadPackageToNorth(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any())).thenReturn(testPkgId);
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.post("/mec/appstore/v1/orders/" + orderId + "/activation").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void activate_order_should_failed() throws Exception {
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

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void activate_order_should_exception() throws Exception {
        String orderId = getOrderIdByStatus(EnumOrderStatus.DEACTIVATED.toString());
        Assert.assertNotNull(orderId);
        Assert.assertNotEquals("", orderId);
        String testPkgId = "";
        Mockito.when(mecmService.upLoadPackageToNorth(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any())).thenReturn(testPkgId);
        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.post("/mec/appstore/v1/orders/" + orderId + "/activation").with(csrf())
                    .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        RestReturn restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_UPLOAD_PACKAGE_TO_MECM_NORTH_FAILED, restReturn.getRetCode());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_order_status_null() {
        Order order = getOrder(EnumOrderStatus.ACTIVATING);
        assert (order != null);
        String token = "testToken";
        MecmDeploymentInfo mecmDeploymentInfo = null;
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(order.getStatus(), EnumOrderStatus.ACTIVATING);

        order.setMecPackageId("");
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(order.getStatus(), EnumOrderStatus.ACTIVATING);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void update_order_status_activated() {
        Order order = getOrder(EnumOrderStatus.ACTIVATING);
        assert (order != null);
        String token = "testToken";
        MecmDeploymentInfo mecmDeploymentInfo = new MecmDeploymentInfo();
        mecmDeploymentInfo.setMecmOperationalStatus("Finished");
        mecmDeploymentInfo.setMecmAppPackageId("mecmAppPackageId");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATED, order.getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void update_order_status_activate_failed() {
        Order order = getOrder(EnumOrderStatus.ACTIVATING);
        assert (order != null);
        String token = "testToken";
        MecmDeploymentInfo mecmDeploymentInfo = new MecmDeploymentInfo();
        mecmDeploymentInfo.setMecmOperationalStatus("Instantiate Error");
        mecmDeploymentInfo.setMecmAppPackageId("mecmAppPackageId");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATE_FAILED, order.getStatus());

        mecmDeploymentInfo.setMecmOperationalStatus("Distribute Error");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATE_FAILED, order.getStatus());

        mecmDeploymentInfo.setMecmOperationalStatus("Create Error");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATE_FAILED, order.getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void query_order_update_activating() {
        Order order = getOrder(EnumOrderStatus.ACTIVATING);
        assert (order != null);
        String token = "testToken";
        MecmDeploymentInfo mecmDeploymentInfo = new MecmDeploymentInfo();
        mecmDeploymentInfo.setMecmOperationalStatus("Distributing");
        mecmDeploymentInfo.setMecmAppPackageId("mecmAppPackageId");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATING, order.getStatus());

        mecmDeploymentInfo.setMecmOperationalStatus("Distributed");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATING, order.getStatus());

        mecmDeploymentInfo.setMecmOperationalStatus("Instantiating");
        Mockito.when(mecmService.getDeploymentStatus(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(mecmDeploymentInfo);
        orderService.updateOrderStatus(token, order);
        Assert.assertEquals(EnumOrderStatus.ACTIVATING, order.getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void unDeployApp_failed() {
        Order order = getOrder(EnumOrderStatus.ACTIVATED);
        assert (order != null);
        String token = "testToken";
        String userId = "testUserId";
        String msg = "failed to delete package";
        Mockito.when(mecmService.deleteServer(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(msg);
        Assert.assertEquals(msg, orderService.unDeployApp(order, userId, token));

        msg = "failed to delete instantiation";
        Mockito.when(mecmService.deleteServer(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(msg);
        Assert.assertEquals(msg, orderService.unDeployApp(order, userId, token));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void unDeployApp_success() {
        Order order = getOrder(EnumOrderStatus.ACTIVATED);
        assert (order != null);
        String token = "testToken";
        String userId = "testUserId";
        String msg = "Delete server success";
        Mockito.when(mecmService.deleteServer(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(msg);
        Assert.assertEquals("Delete server success", orderService.unDeployApp(order, userId, token));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void deactivate_order_should_success() throws Exception {
        String msg = "Delete server success";
        Mockito.when(mecmService.deleteServer(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(msg);
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
    public void deactivate_order_should_failed() throws Exception {
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

    private Order getOrder(EnumOrderStatus status) {
        Map<String, Object> params = new HashMap<>();
        QueryCtrlDto queryCtrl = new QueryCtrlDto();
        queryCtrl.setLimit(10);
        queryCtrl.setOffset(0);
        params.put("queryCtrl", queryCtrl);

        List<Order> orders = orderRepository.queryOrders(params);
        Optional<Order> order = orders.stream().filter(item -> status.equals(item.getStatus()))
            .findFirst();
        return order.orElse(null);
    }

}
