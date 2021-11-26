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

package org.edgegallery.appstore.application.inner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;
import org.edgegallery.appstore.interfaces.order.facade.dto.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("OrderService")
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppService appService;

    @Autowired
    private MecmService mecmService;

    public void updateOrderStatus(String token, Order order) {
        LOGGER.info("[Update Order Status] Each order, appid: {}, mecm app id: {}, order status: {}", order.getAppId(),
            order.getMecAppId(), order.getMecAppId());

        if (order.getStatus() == EnumOrderStatus.ACTIVATING) {
            LOGGER.info("[QUERY ORDER], If Status IS Activating, update mecm deploy instance info");

            if (StringUtils.isEmpty(order.getMecAppId()) || StringUtils.isEmpty(order.getMecPackageId())) {
                LOGGER.error("[QUERY ORDER] order mecm appid or mecm package id is null, continue");
                return;
            }

            MecmDeploymentInfo mecmDeploymentInfo = mecmService.getMecmDepolymentStatus(token, order.getMecAppId(),
                order.getMecPackageId(), order.getUserId());
            LOGGER.info("[QUERY ORDER], analyze mecm instance id, MECM DEPLOYMENT INFO:{}", mecmDeploymentInfo);

            if (mecmDeploymentInfo == null || mecmDeploymentInfo.getMecmAppInstanceId() == null
                || mecmDeploymentInfo.getMecmOperationalStatus() == null) {
                LOGGER.error("[QUERY ORDER] mecm deploy info null ");
                return;
            }

            order.setMecInstanceId(mecmDeploymentInfo.getMecmAppInstanceId());
            LOGGER.info("[QUERY ORDER], mecm instance id is:{}" + mecmDeploymentInfo.getMecmAppInstanceId());

            if (mecmDeploymentInfo.getMecmOperationalStatus().equals("Instantiated")) {
                order.setStatus(EnumOrderStatus.ACTIVATED);
                LOGGER.info("[QUERY ORDER], mecm operational status Instantiated, modify status to activated");

            } else if (mecmDeploymentInfo.getMecmOperationalStatus().equals("Instantiation failed")) {
                order.setStatus(EnumOrderStatus.ACTIVATE_FAILED);
                LOGGER.error(
                    "[QUERY ORDER], mecm operational status Instantiated failed, modify status to activate failed");

            }
        }
        LOGGER.error("[Update Order Status] Order updated, MecmAppId: {}, MecmPackageId:{}", order.getMecAppId(),
            order.getMecPackageId());
    }

    /**
     * query order list.
     *
     * @param params query condition.
     */
    public List<OrderDto> queryOrders(Map<String, Object> params, String token) {
        List<Order> orders = orderRepository.queryOrders(params);
        List<OrderDto> dtoList = new ArrayList<>();
        for (Order order : orders) {
            Release release = null;
            try {
                release = appService.getRelease(order.getAppId(), order.getAppPackageId());
            } catch (DomainException e) {
                LOGGER.warn("app not found! appId = {}", order.getAppId());
            }

            // query mec host info
            String mecHostCity = "";
            String mecHostIp = order.getMecHostIp();
            if (!StringUtils.isEmpty(mecHostIp)) {
                List<String> mecHostIpLst = new ArrayList<>();
                mecHostIpLst.add(mecHostIp);
                Map<String, MecHostBody> mecHostInfo = mecmService.getMecHostByIpList(token, mecHostIpLst);
                if (mecHostInfo != null && mecHostInfo.containsKey(mecHostIp)) {
                    mecHostCity = mecHostInfo.get(mecHostIp).getCity();
                }
            }
            // update mecm order status, whether activated or not
            updateOrderStatus(token, order);

            OrderDto dto = new OrderDto(order, release != null ? release.getAppBasicInfo().getAppName() : "",
                release != null ? release.getAppBasicInfo().getVersion() : "", mecHostCity);
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * query orders count.
     *
     * @param params query condition.
     */
    public long getCountByCondition(Map<String, Object> params) {
        return orderRepository.getCountByCondition(params);
    }

    /**
     * generate order num.
     *
     * @return order num
     */
    public String generateOrderNum() {
        String maxOrderNum = orderRepository.maxOrderNum();
        if (StringUtils.isEmpty(maxOrderNum)) {
            return "ES0000000001";
        }
        int maxId = Integer.parseInt(maxOrderNum.substring(2));
        String strId = "0000000000" + (++maxId);
        int maxNumLen = 10;
        return "ES" + strId.substring(strId.length() - maxNumLen);
    }

    /**
     * undeploy app.
     *
     * @param order order info
     * @param userId deactivate user id
     * @param token access token
     * @return delete app success or not
     */
    public String unDeployApp(Order order, String userId, String token) {
        String appInstanceId = order.getMecInstanceId();
        String hostIp = order.getMecHostIp();
        String packageId = order.getMecPackageId();

        if (!mecmService.deleteAppInstance(appInstanceId, userId, token)) {
            return "delete instantiate app from appo failed";
        }
        if (!mecmService.deleteEdgePackage(hostIp, userId, packageId, token)) {
            return "delete edge package from apm failed.";
        }
        if (!mecmService.deleteApmPackage(userId, packageId, token)) {
            return "delete apm package from apm failed.";
        }
        return "success";
    }
}
