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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmInfo;
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

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

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
        if (userId.equals(release.getUser().getUserId())) {
            LOGGER.error("User can not subscribe own app.");
            throw new AppException("User can not subscribe own app.", ResponseConst.RET_SUBSCRIBE_OWN_APP);
        }
        String orderId = UUID.randomUUID().toString();
        String orderNum = orderService.generateOrderNum();
        Order order = new Order(orderId, orderNum, userId, userName, addOrderReqDto);

        String currentTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String orderCreationDetailCn = currentTime + " " + EnumOrderOperation.CREATED.getChinese();
        String orderCreationDetailEn = currentTime + " " + EnumOrderOperation.CREATED.getEnglish();
        order.setDetailCn(orderCreationDetailCn);
        order.setDetailEn(orderCreationDetailEn);
        orderRepository.addOrder(order);

        // upload package to mec
        // create app instance
        // update stauts to Activating
        MecmInfo mecmInfo = mecmService.upLoadPackageToApm(token, release, order.getMecHostIp(), order.getUserId());
        if (mecmInfo == null) {
            LOGGER.error("[CREATE ORDER], Mecm Info is null. Failed to create order.");
            throw new AppException("[CREATE ORDER], Failed To Utilize MECM Upload Interface.",
                ResponseConst.RET_UPLOAD_PACKAGE_TO_APM_FAILED);
        }
        LOGGER.info("[CREATE ORDER], Start to analyze MecmInfo.");
        order.setMecAppId(mecmInfo.getMecmAppId());
        order.setMecPackageId(mecmInfo.getMecmAppPackageId());
        order.setStatus(EnumOrderStatus.ACTIVATING);
        LOGGER.info("[CREATE ORDER] MECM APP ID :{}", mecmInfo.getMecmAppId());
        LOGGER.info("[CREATE ORDER] MECM APP PACKAGE ID:{} ", mecmInfo.getMecmAppPackageId());

        order.setOperateTime(new Date());
        currentTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String orderActivationDetailCn = currentTime + " " + EnumOrderOperation.ACTIVATED.getChinese();
        String orderActivationDetailEn = currentTime + " " + EnumOrderOperation.ACTIVATED.getEnglish();
        order.setDetailCn(order.getDetailCn() + "\n" + orderActivationDetailCn);
        order.setDetailEn(order.getDetailEn() + "\n" + orderActivationDetailEn);

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
            if ("success".equals(result)) {

                String currentTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
                String orderDeactivationDetailCn = currentTime + " " + EnumOrderOperation.DEACTIVATED.getChinese();
                String orderDeactivationDetailEn = currentTime + " " + EnumOrderOperation.DEACTIVATED.getEnglish();
                order.setDetailCn(order.getDetailCn() + "\n" + orderDeactivationDetailCn);
                order.setDetailEn(order.getDetailEn() + "\n" + orderDeactivationDetailEn);

                order.setStatus(EnumOrderStatus.DEACTIVATED);
                // set mecm info to empty
                order.setMecInstanceId("");
                order.setMecAppId("");
                order.setMecPackageId("");
            } else {
                LOGGER.error("deactivate Order failed.");
                order.setStatus(EnumOrderStatus.DEACTIVATE_FAILED);
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
            MecmInfo mecmInfo = mecmService.upLoadPackageToApm(token, release, order.getMecHostIp(), order.getUserId());
            LOGGER.info("[ACTIVATE ORDER], after use upload interface");
            if (mecmInfo == null) {
                LOGGER.error("[ACTIVATE ORDER], Mecm Info is null.");
                throw new AppException("[ACTIVATE ORDER], Failed To Utilize MECM Upload Interface.",
                    ResponseConst.RET_UPLOAD_PACKAGE_TO_APM_FAILED);
            }

            order.setOperateTime(new Date());
            String currentTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String orderActivationDetailCn = currentTime + " " + EnumOrderOperation.ACTIVATED.getChinese();
            String orderActivationDetailEn = currentTime + " " + EnumOrderOperation.ACTIVATED.getEnglish();
            order.setDetailCn(order.getDetailCn() + "\n" + orderActivationDetailCn);
            order.setDetailEn(order.getDetailEn() + "\n" + orderActivationDetailEn);
            orderRepository.updateOrder(order);
        } else {
            throw new PermissionNotAllowedException("can not deactivate order",
                ResponseConst.RET_NO_ACCESS_ACTIVATE_ORDER, userName);
        }
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject("deactivate order success", errMsg, "deactivate order success."));
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
        LOGGER.error("[Query Order] starting query order function");
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
        LOGGER.error("[Query Order] params: {}", params);
        List<OrderDto> orderList = orderService.queryOrders(params, token);
        LOGGER.error("[Query Order] order list: {}", orderList);
        long total = orderService.getCountByCondition(params);

        // Update order instance id.
        // If order is activating, update status based on mecm opertion status.

        LOGGER.info("[QUERY ORDER] After update each order status");

        return ResponseEntity.ok(new Page<>(orderList, queryOrdersReqDto.getQueryCtrl().getLimit(),
            queryOrdersReqDto.getQueryCtrl().getOffset(), total));
    }

}
