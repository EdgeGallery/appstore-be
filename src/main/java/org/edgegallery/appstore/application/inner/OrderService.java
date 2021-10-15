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
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
import org.edgegallery.appstore.interfaces.order.facade.dto.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("OrderService")
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private static int MAX_NUM_LEN = 10;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppService appService;

    @Autowired
    private MecmService mecmService;

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
            } catch (Exception e) {
                LOGGER.warn("app not found! appId = {}", order.getAppId());
            }

            // query mec host info
            String mecHostName = "";
            String mecHostCity = "";
            String mecHostIp = order.getMecHostIp();
            if (!StringUtils.isEmpty(mecHostIp)) {
                List<String> mecHostIpLst = new ArrayList<>();
                mecHostIpLst.add(mecHostIp);
                Map<String, MecHostBody> mecHostInfo = mecmService.getMecHostByIpList(token, mecHostIpLst);
                if (!mecHostInfo.isEmpty()) {
                    mecHostName = mecHostInfo.get(mecHostIp).getMechostName();
                    mecHostCity = mecHostInfo.get(mecHostIp).getCity();
                }
            }

            OrderDto dto = new OrderDto(order, release != null ? release.getAppBasicInfo().getAppName() : "",
                release != null ? release.getAppBasicInfo().getVersion() : "", mecHostName, mecHostCity);
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
        return "ES" + strId.substring(strId.length() - MAX_NUM_LEN);
    }
}
