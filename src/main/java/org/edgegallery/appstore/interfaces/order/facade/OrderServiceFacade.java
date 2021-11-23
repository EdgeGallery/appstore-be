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
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.application.external.mecm.dto.MecmInfo;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.OrderService;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.AFile;
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

    /**
     * create order.
     *
     * @param userId user id
     * @param userName user name
     * @param addOrderReqDto request body
     * @return result
     */
    public ResponseEntity<ResponseObject> createOrder(String userId, String userName,
        CreateOrderReqDto addOrderReqDto, String token) {
        String orderId = UUID.randomUUID().toString();
        String orderNum = orderService.generateOrderNum();
        Order order = new Order(orderId, orderNum, userId, userName, addOrderReqDto);
        orderRepository.addOrder(order);

        // upload package to mec
        // create app instance
        // update stauts to Activating
        LOGGER.error("CREATE ORDER, before create mecm object");

        MecmService mecmService = new MecmService();
        Release release =  appService.getRelease(addOrderReqDto.getAppId(), addOrderReqDto.getAppPackageId());
        MecmInfo mecmInfo = mecmService.upLoadPackageToApm(token, release, order.getMecHostIp(), order.getUserId());
        LOGGER.error("CREATE ORDER, after upload package");
        if(mecmInfo == null)
            throw new AppException("MecmInfo is empty.", ResponseConst.FAIL_TO_GET_MECM_INFO); //ResponseConst.RET_CREATE_ORDER_FAILED
        // update order info // order.setMecInstanceId(); & order.setDetail(); 需要初始化么？ 未知 InstanceId 获取部署状态接口  获取instance id
        LOGGER.error("CREATE ORDER, start to analyze mecminfo");
        order.setMecAppId(mecmInfo.getMecmAppId());
        order.setMecPackageId(mecmInfo.getMecmAppPackageId());
        order.setStatus(EnumOrderStatus.ACTIVATING);
        order.setOperateTime(new Date());
        LOGGER.error("MECM APP ID " + mecmInfo.getMecmAppId());
        LOGGER.error("MECM APP PACKAGE ID " + mecmInfo.getMecmAppPackageId());

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
    public ResponseEntity<ResponseObject> deactivateOrder(String userId, String userName,
        String orderId, String token) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
            () -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
        if (order.getStatus() != EnumOrderStatus.ACTIVATED
            && order.getStatus() != EnumOrderStatus.DEACTIVATE_FAILED) {
            throw new AppException("inactivated orders can't be deactivated.",
                ResponseConst.RET_NOT_ALLOWED_DEACTIVATE_ORDER);
        }
        if (userId.equals(order.getUserId()) || Consts.SUPER_ADMIN_ID.equals(userId)) {
            order.setStatus(EnumOrderStatus.DEACTIVATING);
            orderRepository.updateOrder(order);

            // undeploy app, if success, update status to deactivated, if failed, update status to deactivate_failed
            String result = orderService.unDeployApp(order, userId, token);
            if ("success".equals(result)) {
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
        return ResponseEntity.ok(new ResponseObject("deactivate order success",
            errMsg, "deactivate order success."));
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
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
            () -> new EntityNotFoundException(Order.class, orderId, ResponseConst.RET_ORDER_NOT_FOUND));
        // ? 疑问 可debug看
        if (order.getStatus() != EnumOrderStatus.DEACTIVATED
            && order.getStatus() != EnumOrderStatus.ACTIVATE_FAILED) {
            throw new AppException("unsubscribed orders can't be activated.",
                ResponseConst.RET_NOT_ALLOWED_ACTIVATE_ORDER);
        }
        if (userId.equals(order.getUserId()) || Consts.SUPER_ADMIN_ID.equals(userId)) {
            // upload package to mecm
            // deploy app
            // update status to Activating
            LOGGER.error("[ACTIVATE ORDER], start to activate, then use upload interface");

            MecmService mecmService = new MecmService();
            Release release =  appService.getRelease(order.getAppId(), order.getAppPackageId());
            MecmInfo mecmInfo = mecmService.upLoadPackageToApm(token, release, order.getMecHostIp(), order.getUserId());
            if(mecmInfo == null)
                throw new AppException("MecmInfo is empty.", ResponseConst.FAIL_TO_GET_MECM_INFO);

            LOGGER.error("[ACTIVATE ORDER], after use upload interface");

            order.setMecAppId(mecmInfo.getMecmAppId());
            order.setMecPackageId(mecmInfo.getMecmAppPackageId());
            order.setOperateTime(new Date());
            order.setStatus(EnumOrderStatus.ACTIVATING);

            LOGGER.error("[ACTIVATE ORDER], start to analyze [mecm info]");

            LOGGER.error("MECM APP ID " + mecmInfo.getMecmAppId());
            LOGGER.error("MECM APP PACKAGE ID " + mecmInfo.getMecmAppPackageId());
            orderRepository.updateOrder(order);
        } else {
            throw new PermissionNotAllowedException("can not deactivate order",
                ResponseConst.RET_NO_ACCESS_ACTIVATE_ORDER, userName);
        }
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity
            .ok(new ResponseObject("deactivate order success", errMsg, "deactivate order success."));
    }

    /**
     * query order list.
     *
     * @param userId user id
     * @param queryOrdersReqDto query condition
     * @return order list
     */

    // 权限
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
        List<OrderDto> orderList = orderService.queryOrders(params, token);
        long total = orderService.getCountByCondition(params);

        // Update order instance id.
        // If order is activating, update status based on mecm opertion status.
        List<Order> orders = orderRepository.queryOrders(params);
        for (Order order : orders){
            LOGGER.error("[QUERY ORDER] Each order, check order status, if activating, then update.");
            if(order.getStatus().equals("ACTIVATING")) {
                LOGGER.error("[QUERY ORDER], INTO Status IS Activating, update mecm deploy instance info");
                if (StringUtils.isEmpty(order.getMecAppId()) || StringUtils.isEmpty(order.getMecPackageId()))
                    throw new AppException("Empty value in order info.", ResponseConst.RET_GET_ORDER_INFO_EMPTY);
                LOGGER.error("[QUERY ORDER], before create MECM OBJECT");
                MecmService mecmService = new MecmService();
                MecmDeploymentInfo mecmDeploymentInfo = mecmService.getMecmDepolymentStatus(token, order.getMecAppId(),order.getMecPackageId(), order.getUserId());
                LOGGER.error("[QUERY ORDER], analyze mecm instance id");
                order.setMecInstanceId(mecmDeploymentInfo.getMecmAppInstanceId());
                LOGGER.error("[QUERY ORDER], mecm instance id is " + mecmDeploymentInfo.getMecmAppInstanceId());

                if (mecmDeploymentInfo == null)
                    throw new AppException("Fail to get MecmDeploymentInfo.", ResponseConst.FAIL_TO_GET_MECM_DEPLOYMENT_INFO);
                if (mecmDeploymentInfo.getMecmOperationalStatus().equals("Instantiated")) {
                    order.setStatus(EnumOrderStatus.ACTIVATED);
                    LOGGER.error("!!![Instantiated], modify status to activated");

                }else if (mecmDeploymentInfo.getMecmOperationalStatus().equals("Instantiation failed")) {
                    order.setStatus(EnumOrderStatus.ACTIVATE_FAILED);
                } // instantiating？ not mentioned, do nothing
            }
        }

        return ResponseEntity.ok(new Page<>(orderList, queryOrdersReqDto.getQueryCtrl().getLimit(),
            queryOrdersReqDto.getQueryCtrl().getOffset(), total));
    }

}
