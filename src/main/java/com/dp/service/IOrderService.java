package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.Result;
import com.dp.entity.Order;

// 订单Service接口
public interface IOrderService extends IService<Order> {
    Long createOrder(OrderCreateDTO orderDTO);

    boolean payOrder(Long orderId, Integer payType);

    OrderDTO queryOrderById(Long orderId);

    Result getOrderList(OrderQueryDTO queryDTO);

    Result cancelOrder(Long orderId, String cancelReason);

    Result rabbitCancelOrder(Long orderId, String cancelReason);

    Result deliveryOrder(Long orderId);

    Result confirmOrder(Long orderId);

    Result getOrderStatistics();

    Result getOrderCount();

    Result getTodaySales();

    Result getWeekSales();
}
