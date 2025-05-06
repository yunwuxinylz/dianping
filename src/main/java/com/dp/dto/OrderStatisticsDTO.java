package com.dp.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderStatisticsDTO {
    private List<OrderStatusDTO> orderStatus;
    private List<DailySpendingDTO> monthlySpending;
    private Integer totalOrders;
    private Long totalAmount;
}
