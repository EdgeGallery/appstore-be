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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.external.mecm.dto.MecmDeploymentInfo;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;
import org.edgegallery.appstore.infrastructure.persistence.system.HostMapper;
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

    /**
     * update order status.
     *
     * @param token access token
     * @param order order info
     */
    public void updateOrderStatus(String token, Order order) {
        LOGGER.info("[Update Order Status] Each order, appid: {}, order status: {}", order.getAppId(),
            order.getStatus());
        LOGGER.info("[Update Order Status], If status is activating, update mecm deploy instance info");
        if (StringUtils.isEmpty(order.getMecPackageId())) {
            LOGGER.error("[Update Order Status] mecm package id is null, continue");
            return;
        }
        MecmDeploymentInfo mecmDeploymentInfo = mecmService.getMecmDepolymentStatus(token, order.getMecPackageId(),
            order.getUserId());
        LOGGER.info("[Update Order Status], analyze mecm instance id, MECM DEPLOYMENT INFO:{}", mecmDeploymentInfo);
        if (mecmDeploymentInfo == null || mecmDeploymentInfo.getMecmOperationalStatus() == null) {
            LOGGER.error("[Update Order Status] mecm deploy info null ");
            return;
        }
        if (mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Finished")) {
            order.setStatus(EnumOrderStatus.ACTIVATED);
            LOGGER.info("[Update Order Status], Distributed and instantiated success, modify status to activated");
        } else if (mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Failed to distribute")
            || mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Failed to instantiate")) {
            order.setStatus(EnumOrderStatus.ACTIVATE_FAILED);
            LOGGER.error(
                "[Update Order Status], Distributed or Instantiated failed, modify status to activate failed");
        }
        orderRepository.updateOrder(order);
        LOGGER.info("[Update Order Status] Order updated, MecmPackageId:{}", order.getMecPackageId());

        /*
        if (order.getStatus() == EnumOrderStatus.ACTIVATING) {
            LOGGER.info("[Update Order Status], If status is activating, update mecm deploy instance info");
            if (StringUtils.isEmpty(order.getMecPackageId())) {
                LOGGER.error("[Update Order Status] mecm package id is null, continue");
                return;
            }
            MecmDeploymentInfo mecmDeploymentInfo = mecmService.getMecmDepolymentStatus(token, order.getMecPackageId(),
                order.getUserId());
            LOGGER.info("[Update Order Status], analyze mecm instance id, MECM DEPLOYMENT INFO:{}", mecmDeploymentInfo);
            if (mecmDeploymentInfo == null || mecmDeploymentInfo.getMecmOperationalStatus() == null) {
                LOGGER.error("[Update Order Status] mecm deploy info null ");
                return;
            }
            if (mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Finished")) {
                order.setStatus(EnumOrderStatus.ACTIVATED);
                LOGGER.info("[Update Order Status], Distributed and instantiated success, modify status to activated");
            } else if (mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Failed to distribute")
                || mecmDeploymentInfo.getMecmOperationalStatus().equalsIgnoreCase("Failed to instantiate")) {
                order.setStatus(EnumOrderStatus.ACTIVATE_FAILED);
                LOGGER.error(
                    "[Update Order Status], Distributed or Instantiated failed, modify status to activate failed");
            }
        }
        */

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
                Map<String, MecHostBody> mecHostInfo = mecmService.getMecHostByIpList(token, order.getUserId(),
                    mecHostIpLst, order.getAppId(), order.getAppPackageId(), release);
                if (mecHostInfo != null && mecHostInfo.containsKey(mecHostIp)) {
                    mecHostCity = mecHostInfo.get(mecHostIp).getCity();
                }
            }
            // Timer will update status every 15 min.
            if(order.getStatus() == EnumOrderStatus.ACTIVATING){
                updateOrderStatus(token, order);
            }

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

    /* * undeploy app.
     *
     * @param order order info
     * @param userId deactivate user id
     * @param token access token
     * @return mecm response message
     */
    public String unDeployApp(Order order, String userId, String token) {
        String packageId = order.getMecPackageId();
        String res = mecmService.deleteServer(userId, packageId, token);
        return res;
    }

    /*
     * undeploy app.
     *
     * @param order order info
     * @param userId deactivate user id
     * @param token access token
     * @return delete app success or not

    public String unDeployApp(Order order, String userId, String token) {
        String hostIp = order.getMecHostIp();
        String packageId = order.getMecPackageId();

        if (!mecmService.deleteEdgePackage(hostIp, userId, packageId, token)) {
            return "delete edge package from apm failed.";
        }
        if (!mecmService.deleteApmPackage(userId, packageId, token)) {
            return "delete apm package from apm failed.";
        }
        return "success";
    }
    */

    /**
     * set order operation detail.
     *
     * @param order order info
     * @param operationChinese operation Chinese name
     * @param operationEnglish operation English name
     */
    public void logOperationDetail(Order order, String operationChinese, String operationEnglish) {
        String currentTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String orderOperationDetailCn = currentTime + " " + operationChinese;
        String orderOperationDetailEn = currentTime + " " + operationEnglish;
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
    public Map<String, String> getVmDeployParams(Release release) {
        if ("container".equalsIgnoreCase(release.getDeployMode())) {
            return Collections.emptyMap();
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
        Map<String, String> vmInputParams = new HashMap<>();
        Set<Map.Entry<String, String>> entries = vmParams.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String ipRange = entry.getValue();
            String tempIp = IpCalculateUtil.getStartIp(ipRange, count);
            vmInputParams.put(entry.getKey(), tempIp);
        }

        return vmInputParams;
    }
}
