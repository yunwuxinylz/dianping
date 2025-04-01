package com.dp.dto;

import lombok.Data;

// 订单创建DTO
@Data
public class OrderCreateDTO {
    private Long goodsId;
    private Long shopId;
    private Integer amount;
}