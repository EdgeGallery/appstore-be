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

package org.edgegallery.appstore.domain.model.order;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.domain.shared.Entity;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderReqDto;

@Getter
@Setter
@NoArgsConstructor
public class Order implements Entity {

    private String orderId;

    private String orderNum;

    private String userId;

    private String userName;

    private String appId;

    private String appPackageId;

    private Date orderTime;

    private Date operateTime;

    private EnumOrderStatus status;

    private String mecHostIp;

    private String mecAppId;

    private String mecPackageId;

    private String mecInstanceId;

    /**
     * construct.
     *
     * @param orderId Order Id
     * @param orderNum Order Num
     * @param userId User ID
     * @param userName User Name
     * @param dto create dto
     */
    public Order(String orderId, String orderNum, String userId, String userName, CreateOrderReqDto dto) {
        this.orderId = orderId;
        this.orderNum = orderNum;
        this.userId = userId;
        this.userName = userName;
        this.appId = dto.getAppId();
        this.appPackageId = dto.getAppPackageId();
        this.orderTime = new Date();
        this.operateTime = this.orderTime;
        this.status = EnumOrderStatus.ACTIVATING;
        this.mecHostIp = dto.getMecHostIp();
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