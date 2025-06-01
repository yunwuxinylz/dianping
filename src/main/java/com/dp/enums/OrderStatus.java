package com.dp.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    UNPAID(1, "待付款"),
    PAID(2, "已支付"),
    CANCELLED(3, "已取消"),
    SHIPPED(4, "已发货"),
    COMPLETED(5, "已完成"),
    AFTER_SALE(6, "售后中");

    private final int value;
    private final String desc;

    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static String getDescByValue(int value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getValue() == value) {
                return status.getDesc();
            }
        }
        return "";
    }
}