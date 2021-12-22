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
import org.springframework.beans.BeanUtils;

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

    @Column(name = "APPNAME")
    private String appName;

    @Column(name = "ORDERTIME")
    private Date orderTime;

    @Column(name = "OPERATETIME")
    private Date operateTime;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "MECM_HOSTIP")
    private String mecHostIp;

    @Column(name = "MECM_HOSTCITY")
    private String mecHostCity;

    @Column(name = "MECM_APPPACKAGEID")
    private String mecPackageId;

    @Column(name = "DETAILCN")
    private String detailCn;

    @Column(name = "DETAILEN")
    private String detailEn;

    public OrderPo() {
        // empty construct
    }

    static OrderPo of(Order order) {
        OrderPo po = new OrderPo();
        BeanUtils.copyProperties(order, po);
        po.status = order.getStatus().toString();
        return po;
    }

    @Override
    public Order toDomainModel() {
        Order order = new Order();
        BeanUtils.copyProperties(this, order);
        order.setStatus(EnumOrderStatus.valueOf(this.getStatus()));
        return order;
    }

    public Date getOrderTime() {
        return orderTime == null ? null : (Date)orderTime.clone();
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime == null ? null : (Date)orderTime.clone();
    }

    public Date getOperateTime() {
        return operateTime == null ? null : (Date)operateTime.clone();
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime == null ? null : (Date)operateTime.clone();
    }
}
