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

package org.edgegallery.appstore.infrastructure.persistence.order;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRepositoryImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void store(Order order) {
        OrderPo orderPo = OrderPo.of(order);
        Optional<OrderPo> existed = orderMapper.findByOrderId(order.getOrderId());
        if (existed.isPresent()) {
            orderMapper.update(orderPo);
        } else {
            orderMapper.insert(orderPo);
        }
    }

    @Override
    public List<Order> queryOrders(Map<String, Object> params) {
        return orderMapper.queryOrders(params).stream().map(OrderPo::toDomainModel).collect(Collectors.toList());
    }

    @Override
    public long getCountByCondition(Map<String, Object> params) {
        return orderMapper.getCountByCondition(params).longValue();
    }

    @Override
    public String maxOrderNum() {
        return orderMapper.maxOrderNum();
    }

    @Override
    public Optional<Order> findByOrderId(String orderId) {
        return orderMapper.findByOrderId(orderId).map(OrderPo::toDomainModel);
    }

}
