package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.entity.Order;

// 订单Service接口
public interface IOrderService extends IService<Order> {
    Long createOrder(OrderCreateDTO orderCreateDTO);
    boolean payOrder(Long orderId, Integer payType);
    OrderDTO queryOrderById(Long orderId);

    Object getOrderList();
}
