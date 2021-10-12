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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.appstore.application.inner.BillService;
import org.edgegallery.appstore.application.inner.OrderService;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
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

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BillService billService;

    /**
     * create order.
     *
     * @param userId user id
     * @param userName user name
     * @param addOrderReqDto request body
     * @param token token
     * @return result
     */
    public ResponseEntity<ResponseObject> createOrder(String userId, String userName, CreateOrderReqDto addOrderReqDto,
        String token) {
        try {
            String orderId = UUID.randomUUID().toString();
            String orderNum = orderService.generateOrderNum();
            Order order = new Order(orderId, orderNum, userId, userName, addOrderReqDto);
            orderRepository.addOrder(order);

            // TODO
            // upload package to mec
            // create app instance
            // query app instance status
            // if success, update status to activated, if failed, update status to activate_failed
            Thread.sleep(5000);
            order.setStatus(EnumOrderStatus.ACTIVATED);
            order.setOperateTime(new Date());
            orderRepository.updateOrderStatus(order);

            CreateOrderRspDto dto = CreateOrderRspDto.builder().orderId(orderId).orderNum(orderNum).build();
            ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
            return ResponseEntity.ok(new ResponseObject(dto, errMsg, "create order success."));
        } catch (InterruptedException e) {
            throw new AppException("create order exception.", ResponseConst.RET_CREATE_ORDER_FAILED);
        }
    }

    /**
     * deactivate order.
     *
     * @param userId user id
     * @param userName user name
     * @param orderId order id
     * @return result
     */
    public ResponseEntity<ResponseObject> deactivateOrder(String userId, String userName, String orderId,
        String token) {
        try {
            Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
            if (order.getStatus() != EnumOrderStatus.ACTIVATED
                && order.getStatus() != EnumOrderStatus.DEACTIVATE_FAILED) {
                throw new AppException("inactivated orders can't be deactivated.",
                    ResponseConst.RET_NOT_ALLOWED_DEACTIVATE_ORDER);
            }
            if (userId.equals(order.getUserId()) || Consts.SUPER_ADMIN_ID.equals(userId)) {
                order.setStatus(EnumOrderStatus.DEACTIVATING);
                orderRepository.updateOrderStatus(order);
                // TODO
                // undeploy app
                // query status
                // if success, update status to deactivated, if failed, update status to deactivate_failed
                Thread.sleep(3000);
                order.setStatus(EnumOrderStatus.DEACTIVATED);
                Date activateDateTime = order.getOperateTime();
                order.setOperateTime(new Date());
                orderRepository.updateOrderStatus(order);

                LOGGER.info("geneate bill.");
                billService.generateBillOnDeactivate(order, activateDateTime);
            } else {
                throw new PermissionNotAllowedException("can not deactivate order",
                    ResponseConst.RET_NO_ACCESS_DEACTIVATE_ORDER, userName);
            }
            ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
            return ResponseEntity
                .ok(new ResponseObject("deactivate order success", errMsg, "deactivate order success."));
        } catch (InterruptedException e) {
            throw new AppException("deactivate order exception.", ResponseConst.RET_DEACTIVATE_ORDER_FAILED);
        }
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
        try {
            Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
            if (order.getStatus() != EnumOrderStatus.DEACTIVATED
                && order.getStatus() != EnumOrderStatus.ACTIVATE_FAILED) {
                throw new AppException("unsubscribed orders can't be activated.",
                    ResponseConst.RET_NOT_ALLOWED_ACTIVATE_ORDER);
            }
            if (userId.equals(order.getUserId()) || Consts.SUPER_ADMIN_ID.equals(userId)) {
                order.setStatus(EnumOrderStatus.ACTIVATING);
                orderRepository.updateOrderStatus(order);
                // TODO
                // upload package to mecm
                // deploy app
                // query status
                // if success, update status to activated, if failed, update status to activate_failed
                Thread.sleep(3000);
                order.setStatus(EnumOrderStatus.ACTIVATED);
                order.setOperateTime(new Date());
                orderRepository.updateOrderStatus(order);

            } else {
                throw new PermissionNotAllowedException("can not deactivate order",
                    ResponseConst.RET_NO_ACCESS_ACTIVATE_ORDER, userName);
            }
            ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
            return ResponseEntity
                .ok(new ResponseObject("deactivate order success", errMsg, "deactivate order success."));
        } catch (InterruptedException e) {
            throw new AppException("deactivate order exception.", ResponseConst.RET_ACTIVATE_ORDER_FAILED);
        }
    }

    /**
     * query order list.
     *
     * @param userId user id
     * @param userName user name
     * @param queryOrdersReqDto query condition
     * @return order list
     */
    public ResponseEntity<Page<OrderDto>> queryOrders(String userId, String userName,
        QueryOrdersReqDto queryOrdersReqDto, String token) {
        Map<String, Object> params = new HashMap<>();
        if (!Consts.SUPER_ADMIN_ID.equals(userId)) {
            params.put("userId", userId);
        }
        params.put("appId", queryOrdersReqDto.getAppId());
        params.put("orderNum", queryOrdersReqDto.getOrderNum());
        params.put("status", queryOrdersReqDto.getStatus());
        params.put("orderBeginTime", queryOrdersReqDto.getOrderTimeBegin());
        params.put("orderEndTime", queryOrdersReqDto.getOrderTimeEnd());
        params.put("queryCtrl", queryOrdersReqDto.getQueryCtrl());
        List<OrderDto> orderList = orderService.queryOrders(params, token);
        long total = orderService.getCountByCondition(params);
        return ResponseEntity.ok(new Page<>(orderList, queryOrdersReqDto.getQueryCtrl().getLimit(),
            queryOrdersReqDto.getQueryCtrl().getOffset(), total));
    }

}
