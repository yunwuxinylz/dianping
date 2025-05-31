package com.dp.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrderDTO {
    private String id;
    private Long userId;
    private Long shopId;
    private String shopName;
    private String shopImage;
    private Long amount;
    private Integer payType;
    private Integer status;
    private LocalDateTime payTime;
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
    private LocalDateTime createTime;
}
