package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.Result;
import com.dp.entity.Order;

/**
 * 订单服务接口
 */
public interface IOrderService extends IService<Order> {

    /**
     * 创建订单
     * 
     * @param orderDTO 订单创建DTO
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO orderDTO);

    /**
     * 查询订单
     * 
     * @param orderId 订单ID
     * @return 订单DTO
     */
    OrderDTO queryOrderById(Long orderId);

    /**
     * 获取订单列表
     * 
     * @param queryDTO 查询条件
     * @return 查询结果
     */
    Result getOrderList(OrderQueryDTO queryDTO);

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 处理结果
     */
    Result cancelOrder(Long orderId, String reason);

    /**
     * RabbitMQ超时取消订单
     * 
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 处理结果
     */
    Result rabbitCancelOrder(Long orderId, String reason);

    /**
     * 确认收货
     * 
     * @param orderId 订单ID
     * @return 处理结果
     */
    Result deliveryOrder(Long orderId);

    /**
     * 确认发货
     * 
     * @param orderId 订单ID
     * @return 处理结果
     */
    Result confirmOrder(Long orderId);

    /**
     * 获取订单统计信息
     * 
     * @return 统计结果
     */
    Result getOrderStatistics();

    /**
     * 获取订单总数
     * 
     * @return 订单总数
     */
    Result getOrderCount();

    /**
     * 获取今日销售额
     * 
     * @return 今日销售额
     */
    Result getTodaySales();

    /**
     * 获取最近7天销售趋势
     * 
     * @return 销售趋势数据
     */
    Result getWeekSales();
}
