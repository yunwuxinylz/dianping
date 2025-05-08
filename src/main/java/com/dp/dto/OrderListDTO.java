package com.dp.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 订单列表DTO
 */
@Data
public class OrderListDTO {
    private Long id;
    private Long orderId;
    private Long shopId;
    private String shopName;
    private String shopImage;
    private Long amount;
    private List<OrderItemsDTO> items;
    private String addressDetail;
    private String addressName;
    private String addressPhone;
    private Integer count;
    private Integer status;
    private LocalDateTime createTime;
    private boolean commented;

}
