package com.dp.dto;

import lombok.Data;

@Data
public class AfterSaleStatusDTO {
    private Long id; // 售后ID
    private String orderId; // 订单ID
    private Integer status; // 售后状态
    private Integer type; // 售后类型
    private String handleMsg; // 处理备注
    private Long amount; // 退款金额
}