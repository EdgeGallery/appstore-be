package org.edgegallery.appstore.domain.model.order;

import lombok.Getter;

/**
 * order operation in both Chinese and English
 */
@Getter
public enum EnumOrderOperation {
    CREATED("订单创建", "Order Created"),
    ACTIVATED("订单激活", "Order Activated"),
    DEACTIVATED("订单推定", "Order deactivated");

    private String Chinese;
    private String English;

    EnumOrderOperation(String Chinese, String English) {
        this.Chinese = Chinese;
        this.English = English;
    }

}
