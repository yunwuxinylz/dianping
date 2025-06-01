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

    /**
     * 分页获取订单列表（支持搜索）
     */
    Result getOrderPage(Integer current, Integer size, String keyword, Integer status);

    /**
     * 更新订单状态
     */
    Result updateOrderStatus(Long id, Integer status);
}
