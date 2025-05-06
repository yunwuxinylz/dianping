package com.dp.dto;

import lombok.Data;

/**
 * 订单取消
 */
@Data
public class OrderCancelDTO {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 取消原因
     */
    private String cancelReason;

}
