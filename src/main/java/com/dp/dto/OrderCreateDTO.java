package com.dp.dto;

import java.util.List;

import lombok.Data;

// 订单DTO
@Data
public class OrderCreateDTO {
    private Long shopId;
    private String shopName;
    private List<String> shopImage;
    private Long amount;
    private Integer status;
    private List<OrderItemsDTO> items;
    /**
     * 订单备注
     */
    private String remark;

    /**
     * 地址ID
     */
    private Long addressId;

    /**
     * 地址详情
     */
    private String addressDetail;

    /**
     * 收货人
     */
    private String addressName;

    /**
     * 收货人手机号
     */
    private String addressPhone;

    private Integer count;
    private boolean commented;
}