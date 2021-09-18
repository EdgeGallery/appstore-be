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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.infrastructure.persistence.PersistenceObject;

@Getter
@Setter
@Entity
@Table(name = "app_order")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderPo implements PersistenceObject<Order> {
    @Id
    @Column(name = "ORDERID")
    private String orderId;

    @Column(name = "ORDERNUM")
    private String orderNum;

    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "APPID")
    private String appId;

    @Column(name = "APPPACKAGEID")
    private String appPackageId;

    @Column(name = "ORDERTIME")
    private Date orderTime;

    @Column(name = "OPERATETIME")
    private Date operateTime;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "MECM_HOSTIP")
    private String mecm_hostIp;

    @Column(name = "MECM_APPID")
    private String mecm_appId;

    @Column(name = "MECM_APPPACKAGEID")
    private String mecm_appPackageId;

    @Column(name = "MECM_INSTANCEID")
    private String mecm_instanceId;

    public OrderPo() {
        // empty construct
    }

    static OrderPo of(Order order) {
        OrderPo po = new OrderPo();
        po.orderId = order.getOrderId();
        po.orderNum = order.getOrderNum();
        po.userId = order.getUserId();
        po.userName = order.getUserName();
        po.appId = order.getAppId();
        po.appPackageId = order.getPackageId();
        po.orderTime = order.getOrderTime();
        po.operateTime = order.getOperateTime();
        po.status = order.getStatus().toString();
        po.mecm_hostIp = order.getMecHostIp();
        po.mecm_appId = order.getMecAppId();
        po.mecm_appPackageId = order.getMecPackageId();
        po.mecm_instanceId = order.getMecInstanceId();
        return po;
    }

    @Override
    public Order toDomainModel() {
        return Order.builder().orderId(orderId).orderNum(orderNum).userId(userId).userName(userName)
            .appId(appId).packageId(appPackageId).orderTime(orderTime).operateTime(operateTime)
            .status(EnumOrderStatus.valueOf(status))
            .mecHostIp(mecm_hostIp).mecAppId(mecm_appId).mecPackageId(mecm_appPackageId).mecInstanceId(mecm_instanceId)
            .build();
    }
}
