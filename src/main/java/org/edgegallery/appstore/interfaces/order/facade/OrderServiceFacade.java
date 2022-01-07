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

package org.edgegallery.appstore.interfaces.order.facade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.OrderService;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.EnumOrderOperation;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.PermissionNotAllowedException;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderRspDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.OrderDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryOrdersReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("OrderServiceFacade")
public class OrderServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceFacade.class);
    private static final String DELETE_SERVER_SUCCESS = "Delete server success";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppService appService;

    @Autowired
    private MecmService mecmService;

    /**
     * create order.
     *
     * @param userId user id
     * @param userName user name
     * @param addOrderReqDto request body
     * @return result
     */
    public ResponseEntity<ResponseObject> createOrder(String userId, String userName, CreateOrderReqDto addOrderReqDto,
        String token) {
        Release release = appService.getRelease(addOrderReqDto.getAppId(), addOrderReqDto.getAppPackageId());
        if (!Consts.SUPER_ADMIN_NAME.equals(userName) && userId.equals(release.getUser().getUserId())) {
            LOGGER.error("User can not subscribe to its own app, order creation failed");
            throw new AppException("User can not subscribe to its own app", ResponseConst.RET_SUBSCRIBE_OWN_APP);
        }
        String orderId = UUID.randomUUID().toString();
        String orderNum = orderService.generateOrderNum();
        Order order = new Order(orderId, orderNum, userId, userName, addOrderReqDto);
        orderService.setOrderDetail(order, EnumOrderOperation.CREATED.getChinese(),
            EnumOrderOperation.CREATED.getEnglish());
        orderRepository.addOrder(order);
        LOGGER.info("Created order successfully");

        orderService.startActivatingOrder(release, order, token, userId);
        return ResponseEntity.ok(new ResponseObject(CreateOrderRspDto.builder().orderId(orderId).orderNum(orderNum)
            .build(), new ErrorMessage(ResponseConst.RET_SUCCESS,null), "Created order Successfully"));

    }

    /**
     * deactivate order.
     *
     * @param userId user id
     * @param userName name of current user
     * @param orderId order id
     * @return result
     */
    public ResponseEntity<ResponseObject> deactivateOrder(String userId, String userName, String orderId,
        String token) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
        if (order.getStatus() != EnumOrderStatus.ACTIVATED && order.getStatus() != EnumOrderStatus.DEACTIVATE_FAILED) {
            throw new AppException("inactivated orders can't be deactivated.",
                ResponseConst.RET_NOT_ALLOWED_DEACTIVATE_ORDER);
        }
        if (!userId.equals(order.getUserId())) {
            throw new PermissionNotAllowedException("can not deactivate order",
                ResponseConst.RET_NO_ACCESS_DEACTIVATE_ORDER, userName);
        }

        // undeploy app, if success, update status to deactivated, if failed, update status to deactivate_failed
        String unDeployAppResult = orderService.unDeployApp(order, userId, token);
        if (StringUtils.isEmpty(unDeployAppResult)) {
            LOGGER.error("Some parameters of unsubscribe are empty.");
            throw new AppException("Some parameters of unsubscribe are empty.",
                ResponseConst.RET_DEACTIVATE_PARAM_INVALID);
        }

        if (unDeployAppResult.equalsIgnoreCase(DELETE_SERVER_SUCCESS)) {
            LOGGER.info("Undeploy package successfully.");
            order.setStatus(EnumOrderStatus.DEACTIVATED);
            orderRepository.updateOrder(order);
            return ResponseEntity.ok(new ResponseObject("deactivate order success",
                new ErrorMessage(ResponseConst.RET_SUCCESS, null), "deactivate order success"));
        }

        LOGGER.error("Failed to undeploy package.");
        order.setStatus(EnumOrderStatus.DEACTIVATE_FAILED);
        orderRepository.updateOrder(order);
        throw new AppException("Failed to deactivate order.",
            ResponseConst.RET_DEACTIVATE_ORDER_FAILED);
    }

    /**
     * activate order.
     *
     * @param userId user id
     * @param userName user name
     * @param orderId order id
     * @return result
     */
    public ResponseEntity<ResponseObject> activateOrder(String userId, String userName, String orderId, String token) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
        if (order.getStatus() != EnumOrderStatus.DEACTIVATED && order.getStatus() != EnumOrderStatus.ACTIVATE_FAILED) {
            throw new AppException("unsubscribed orders can't be activated.",
                ResponseConst.RET_NOT_ALLOWED_ACTIVATE_ORDER);
        }
        if (!userId.equals(order.getUserId())) {
            throw new PermissionNotAllowedException("can not activate order",
                ResponseConst.RET_NO_ACCESS_ACTIVATE_ORDER, userName);
        }
        // upload package to north, if return mecm packageId is not empty, update status to Activating
        Release release = appService.getRelease(order.getAppId(), order.getAppPackageId());
        orderService.startActivatingOrder(release, order, token, userId);
        return ResponseEntity.ok(new ResponseObject("activate order success",
            new ErrorMessage(ResponseConst.RET_SUCCESS, null), "activate order success."));
    }

    /**
     * query order list.
     *
     * @param userId user id
     * @param queryOrdersReqDto query condition
     * @return order list
     */

    public ResponseEntity<Page<OrderDto>> queryOrders(String userId, String role,
        QueryOrdersReqDto queryOrdersReqDto, String token) {
        Map<String, Object> queryOrderParams = new HashMap<>();
        if (!StringUtils.isEmpty(role) && !role.contains("ROLE_APPSTORE_ADMIN")) {
            queryOrderParams.put("userId", userId);
        }
        queryOrderParams.put("appId", queryOrdersReqDto.getAppId());
        queryOrderParams.put("orderNum", queryOrdersReqDto.getOrderNum());
        queryOrderParams.put("status", queryOrdersReqDto.getStatus());
        queryOrderParams.put("orderBeginTime", queryOrdersReqDto.getOrderTimeBegin());
        queryOrderParams.put("orderEndTime", queryOrdersReqDto.getOrderTimeEnd());
        queryOrderParams.put("queryCtrl", queryOrdersReqDto.getQueryCtrl());
        LOGGER.info("query order params: {}", queryOrderParams);

        return ResponseEntity.ok(new Page<>(orderService.queryOrders(queryOrderParams, token),
            queryOrdersReqDto.getQueryCtrl().getLimit(), queryOrdersReqDto.getQueryCtrl().getOffset(),
            orderService.getCountByCondition(queryOrderParams)));
    }

}
