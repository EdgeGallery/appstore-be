/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.appstore.interfaces.order.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.OrderServiceFacade;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.OrderDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryOrdersReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "order")
@RequestMapping("/mec/appstore/v1/orders")
@Api(tags = "Order")
@Validated
public class OrderController {

    private static final String USERID = "userId";

    private static final String USERNAME = "userName";

    private static final String REG_ORDER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private OrderServiceFacade orderServiceFacade;

    /**
     * create order.
     *
     * @param createOrderReqDto request body.
     * @param request request.
     */
    @PostMapping(value = "", produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "crate order.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> createOrder(
        @ApiParam(value = "CreateOrderReqDto", required = true) @RequestBody CreateOrderReqDto createOrderReqDto,
        HttpServletRequest request) {
        return orderServiceFacade.createOrder((String) request.getAttribute(USERID), createOrderReqDto,
            request.getHeader(Consts.ACCESS_TOKEN_STR));
    }

    /**
     * deactivate order.
     *
     * @param orderId order id.
     * @param request request.
     */
    @PostMapping(value = "/{orderId}/deactivation", produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "deactivate order.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> deactivateOrder(
        @ApiParam(value = "orderId") @PathVariable("orderId") @Pattern(regexp = REG_ORDER_ID) String orderId,
        HttpServletRequest request) {
        return orderServiceFacade.deactivateOrder((String)request.getAttribute(USERID),
            (String)request.getAttribute(USERNAME), orderId, request.getHeader(Consts.ACCESS_TOKEN_STR));
    }

    /**
     * activate order.
     *
     * @param orderId order id.
     * @param request request.
     */
    @PostMapping(value = "/{orderId}/activation", produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "activate order.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> activateOrder(
        @ApiParam(value = "orderId") @PathVariable("orderId") @Pattern(regexp = REG_ORDER_ID) String orderId,
        HttpServletRequest request) {
        return orderServiceFacade.activateOrder((String)request.getAttribute(USERID),
            (String)request.getAttribute(USERNAME), orderId, request.getHeader(Consts.ACCESS_TOKEN_STR));
    }

    /**
     * query order list.
     *
     * @param queryOrdersReqDto request body.
     * @param request request.
     */
    @PostMapping(value = "list", produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "query order list.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<OrderDto>> queryOrders(
        @ApiParam(value = "QueryOrdersReqDto", required = true) @RequestBody QueryOrdersReqDto queryOrdersReqDto,
        HttpServletRequest request) {
        return orderServiceFacade.queryOrders((String)request.getAttribute(USERID),
            (String)request.getAttribute(USERNAME), queryOrdersReqDto, request.getHeader(Consts.ACCESS_TOKEN_STR));
    }


}
