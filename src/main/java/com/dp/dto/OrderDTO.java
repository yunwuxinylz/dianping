package com.dp.dto;

import lombok.Data;

import java.time.LocalDateTime;

// 订单DTO
@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private Long shopId;
    private Long goodsId;
    private String goodsName;
    private Long goodsPrice;
    private Integer amount;
    private Long totalPrice;
    private Integer payType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
}