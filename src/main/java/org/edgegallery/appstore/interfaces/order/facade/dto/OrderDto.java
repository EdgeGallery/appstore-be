/* Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.interfaces.order.facade.dto;

import java.text.SimpleDateFormat;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.order.Order;

@Getter
@Setter
public class OrderDto {
    private String orderId;

    private String orderNum;

    private String userId;

    private String userName;

    private String appId;

    private String appName;

    private String appVersion;

    private String orderTime;

    private String operateTime;

    private String status;

    private String mecHostIp;

    private String mecHostCity;

    private String detail;

    /**
     * constructor.
     *
     */
    public OrderDto(Order order, String appName, String appVersion, String mecHostCity) {
        this.orderId = order.getOrderId();
        this.orderNum = order.getOrderNum();
        this.userId = order.getUserId();
        this.userName = order.getUserName();
        this.appId = order.getAppId();
        this.appName = appName;
        this.appVersion = appVersion;
        this.orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getOrderTime());
        this.operateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getOperateTime());
        this.status = order.getStatus().toString();
        this.mecHostIp = order.getMecHostIp();
        this.mecHostCity = mecHostCity;
        this.detail = order.getDetail();
    }
}
