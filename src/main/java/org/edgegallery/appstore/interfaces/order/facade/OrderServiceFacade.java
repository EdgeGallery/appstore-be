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
        LOGGER.error("[Create Order] userid is {}", userId);
        if (!userName.equals("admin") && userId.equals(release.getUser().getUserId())) {
            LOGGER.error("User can not subscribe own app.");
            throw new AppException("User can not subscribe own app.", ResponseConst.RET_SUBSCRIBE_OWN_APP);
        }
        String orderId = UUID.randomUUID().toString();
        String orderNum = orderService.generateOrderNum();
        Order order = new Order(orderId, orderNum, userId, userName, addOrderReqDto);
        orderService.logOperationDetail(order, EnumOrderOperation.CREATED.getChinese(),
            EnumOrderOperation.CREATED.getEnglish());
        orderRepository.addOrder(order);

        // upload package to mec
        // create app instance
        // update status to Activating
        String params = orderService.getVmDeployParams(release);
        String mecmPkgId = mecmService.upLoadPackageToNorth(token, release, order.getMecHostIp(), userId, params);
        if (mecmPkgId == null) {
            LOGGER.error("mecm package id is null. Failed to create order.");
            throw new AppException("Failed to create order.", ResponseConst.RET_UPLOAD_PACKAGE_TO_MECM_NORTH_FAILED);
        }
        order.setMecPackageId(mecmPkgId);
        order.setStatus(EnumOrderStatus.ACTIVATING);
        order.setOperateTime(new Date());
        orderService.logOperationDetail(order, EnumOrderOperation.ACTIVATED.getChinese(),
            EnumOrderOperation.ACTIVATED.getEnglish());
        orderRepository.updateOrder(order);
        CreateOrderRspDto dto = CreateOrderRspDto.builder().orderId(orderId).orderNum(orderNum).build();
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(dto, errMsg, "create order success."));
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
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
        if (order.getStatus() != EnumOrderStatus.ACTIVATED && order.getStatus() != EnumOrderStatus.DEACTIVATE_FAILED) {
            throw new AppException("inactivated orders can't be deactivated.",
                ResponseConst.RET_NOT_ALLOWED_DEACTIVATE_ORDER);
        }
        if (userId.equals(order.getUserId()) || Consts.SUPER_ADMIN_ID.equals(userId)) {
            order.setStatus(EnumOrderStatus.DEACTIVATING);
            orderRepository.updateOrder(order);

            // undeploy app, if success, update status to deactivated, if failed, update status to deactivate_failed
            String result = orderService.unDeployApp(order, userId, token);
            if (StringUtils.isEmpty(result)) {
                LOGGER.error("Failed to utilize delete server interface.");
                throw new AppException("Failed to utilize delete server interface.",
                    ResponseConst.RET_DELETE_SERVER_FAILED);
            } else if (result.equalsIgnoreCase("failed to delete package") || result.equalsIgnoreCase(
                "failed to delete instantiation")) {
                LOGGER.error("Failed to undeploy package.");
                order.setStatus(EnumOrderStatus.DEACTIVATE_FAILED);
            } else if (result.equalsIgnoreCase("Delete server success")) {
                LOGGER.info("Success to undeploy package.");
                order.setStatus(EnumOrderStatus.DEACTIVATED);
                orderService.logOperationDetail(order, EnumOrderOperation.DEACTIVATED.getChinese(),
                    EnumOrderOperation.DEACTIVATED.getEnglish());
            }
            orderRepository.updateOrder(order);
        } else {
            throw new PermissionNotAllowedException("can not deactivate order",
                ResponseConst.RET_NO_ACCESS_DEACTIVATE_ORDER, userName);
        }
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject("deactivate order success", errMsg, "deactivate order success."));
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
        if (userId.equals(order.getUserId()) || Consts.SUPER_ADMIN_ID.equals(userId)) {
            order.setStatus(EnumOrderStatus.ACTIVATING);
            orderRepository.updateOrder(order);

            /// upload package to mecm
            // deploy app
            // update status to Activating
            Release release = appService.getRelease(order.getAppId(), order.getAppPackageId());
            String params = orderService.getVmDeployParams(release);
            String mecmPkgId = mecmService.upLoadPackageToNorth(token, release, order.getMecHostIp(),
                order.getUserId(), params);
            if (mecmPkgId == null || StringUtils.isEmpty(mecmPkgId)) {
                LOGGER.error("mecm package id is null. Failed to activate order.");
                throw new AppException("Failed to activate order.",
                    ResponseConst.RET_UPLOAD_PACKAGE_TO_MECM_NORTH_FAILED);
            }

            order.setOperateTime(new Date());
            orderService.logOperationDetail(order, EnumOrderOperation.ACTIVATED.getChinese(),
                EnumOrderOperation.ACTIVATED.getEnglish());
            orderRepository.updateOrder(order);
        } else {
            throw new PermissionNotAllowedException("can not activate order",
                ResponseConst.RET_NO_ACCESS_ACTIVATE_ORDER, userName);
        }
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject("activate order success", errMsg, "activate order success."));
    }

    /**
     * query order list.
     *
     * @param userId user id
     * @param queryOrdersReqDto query condition
     * @return order list
     */

    public ResponseEntity<Page<OrderDto>> queryOrders(String userId, QueryOrdersReqDto queryOrdersReqDto,
        String token) {
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
        LOGGER.info("query order params: {}", params);
        List<OrderDto> orderList = orderService.queryOrders(params, token);
        long total = orderService.getCountByCondition(params);

        return ResponseEntity.ok(new Page<>(orderList, queryOrdersReqDto.getQueryCtrl().getLimit(),
            queryOrdersReqDto.getQueryCtrl().getOffset(), total));
    }

}
