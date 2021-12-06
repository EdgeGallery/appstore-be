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

import lombok.Getter;

/**
 * order operation in both Chinese and English.
 */
@Getter
public enum EnumOrderOperation {
    CREATED("订单创建", "Order Created"),
    ACTIVATED("订单激活", "Order Activated"),
    DEACTIVATED("订单退订", "Order deactivated");

    private String chinese;
    private String english;

    EnumOrderOperation(String chinese, String english) {
        this.chinese = chinese;
        this.english = english;
    }

}