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

import com.github.pagehelper.util.StringUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.EnumOrderOperation;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.system.HostMapper;
import org.edgegallery.appstore.infrastructure.util.HttpClientUtil;
import org.edgegallery.appstore.infrastructure.util.InputParameterUtil;
import org.edgegallery.appstore.infrastructure.util.IpCalculateUtil;
import org.edgegallery.appstore.interfaces.order.facade.dto.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("OrderService")
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final Integer RETAIN_IP_COUNT = 10; // retain 10 ip for online experience

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppService appService;

    @Autowired
    private MecmService mecmService;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private HttpClientUtil httpClientUtil;

    /**
     * update order status.
     *
     * @param token access token
     * @param order order info
     */
    public void updateOrderStatus(String token, Order order) {
        if (StringUtils.isEmpty(order.getMecPackageId())) {
            LOGGER.error("mecm package id is null.");
            return;
        }
        MecmDeploymentInfo mecmDeploymentInfo = mecmService.getDeploymentStatus(token, order.getMecPackageId(),
            order.getUserId());
        if (mecmDeploymentInfo == null || mecmDeploymentInfo.getMecmOperationalStatus() == null) {
            LOGGER.error("mecm deployment info is null.");
            return;
        }

        if (mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Finished")) {
            order.setStatus(EnumOrderStatus.ACTIVATED);
            LOGGER.info("Distributed and instantiated success, modify status to activated");
        } else if (mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Distribute Error")
            || mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Instantiate Error")
            || mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Create Error")) {
            order.setStatus(EnumOrderStatus.ACTIVATE_FAILED);
            LOGGER.error("Distributed or Instantiated failed, modify status to activate failed");
        }
        orderRepository.updateOrder(order);
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
            // Timer will update status every 15 min.
            if (order.getStatus() == EnumOrderStatus.ACTIVATING) {
                updateOrderStatus(token, order);
            }
            OrderDto dto = new OrderDto(order);
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
        return orderRepository.getCountByCondition(params).longValue();
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
     * @return mecm response message
     */
    public String unDeployApp(Order order, String userId, String token) {
        order.setStatus(EnumOrderStatus.DEACTIVATING);
        setOrderDetail(order, EnumOrderOperation.DEACTIVATED.getChinese(),
            EnumOrderOperation.DEACTIVATED.getEnglish());
        orderRepository.updateOrder(order);
        return mecmService.deleteServer(userId, order.getMecPackageId(), token);
    }

    /**
     * set order operation detail.
     *
     * @param order order info
     * @param operationChinese operation Chinese name
     * @param operationEnglish operation English name
     */
    public void setOrderDetail(Order order, String operationChinese, String operationEnglish) {
        String currentTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String orderOperationDetailCn = currentTime + "," + operationChinese;
        String orderOperationDetailEn = currentTime + "," + operationEnglish;
        if (StringUtil.isEmpty(order.getDetailCn())) {
            order.setDetailCn(orderOperationDetailCn);
            order.setDetailEn(orderOperationDetailEn);
        } else {
            order.setDetailCn(order.getDetailCn() + "\n" + orderOperationDetailCn);
            order.setDetailEn(order.getDetailEn() + "\n" + orderOperationDetailEn);
        }
    }

    /**
     * get instantiate parameter.
     *
     * @param release release.
     */
    public String getVmDeployParams(Release release) {
        if ("container".equalsIgnoreCase(release.getDeployMode())) {
            return "";
        }
        String parameter;
        List<MepHost> mepHosts = hostMapper.getHostsByCondition("", "OpenStack");
        if (CollectionUtils.isEmpty(mepHosts)) {
            LOGGER.info("there is not host info.");
            parameter = "app_mp1_ip=192.168.226.0/24;app_n6_ip=192.168.225.0/24;app_internet_ip=192.168.227.0/24";
        } else {
            parameter = mepHosts.get(0).getParameter();
        }

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("status", "ACTIVATED");

        Integer count = orderRepository.getCountByCondition(queryParams);
        count += RETAIN_IP_COUNT;
        Map<String, String> vmParams = InputParameterUtil.getParams(parameter);
        StringBuilder vmInputParams = new StringBuilder();
        Set<Map.Entry<String, String>> entries = vmParams.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            vmInputParams.append(";");
            String ipRange = entry.getValue();
            String tempIp = IpCalculateUtil.getStartIp(ipRange, count);
            vmInputParams.append(entry.getKey()).append("=").append(tempIp);
        }
        LOGGER.info("vm params: {}", vmInputParams.substring(1, vmInputParams.length()));
        return vmInputParams.substring(1, vmInputParams.length());
    }

    /**
     * schedule update query order.
     */
    public boolean scheduledQueryOrder() {
        // periodically refresh order status
        String token = httpClientUtil.getAccessToken();
        if (StringUtils.isEmpty(token)) {
            LOGGER.error("call login or clean env interface occur error,accesstoken is empty");
            return false;
        }
        Map<String, Object> params = new HashMap<>();
        List<Order> orders = orderRepository.queryOrders(params);
        orders.stream().filter(r -> r.getStatus() == EnumOrderStatus.ACTIVATING
            && !StringUtils.isEmpty(r.getMecPackageId())).forEach(p -> updateOrderStatus(token, p));
        return true;
    }

    /**
     * upload package to north, if the returned packageId is valid, set the status of order as activating.
     *
     * @param release app release information
     * @param order order information
     * @param token user token
     * @param userId id of current user
     */
    public void startActivatingOrder(Release release, Order order, String token, String userId) {
        String mecPkgId = mecmService.upLoadPackageToNorth(token, release, order.getMecHostIp(),
            userId, getVmDeployParams(release));
        if (StringUtils.isEmpty(mecPkgId)) {
            LOGGER.error("MEC package id is null, failed to create order");
            throw new AppException("Failed to create order", ResponseConst.RET_UPLOAD_PACKAGE_TO_MECM_NORTH_FAILED);
        }
        order.setMecPackageId(mecPkgId);
        order.setStatus(EnumOrderStatus.ACTIVATING);
        setOrderDetail(order, EnumOrderOperation.ACTIVATED.getChinese(), EnumOrderOperation.ACTIVATED.getEnglish());
        orderRepository.updateOrder(order);
        LOGGER.info("Successfully uploaded package to north, order has been activated, mecPackageId: {}", mecPkgId);
    }

}
