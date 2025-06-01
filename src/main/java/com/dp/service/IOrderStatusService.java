package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.Result;
import com.dp.entity.Order;

/**
 * 订单状态管理服务接口
 */
public interface IOrderStatusService extends IService<Order> {
    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 操作结果
     */
    Result cancelOrder(Long orderId, String reason);

    /**
     * 超时取消订单（由MQ触发）
     * 
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 操作结果
     */
    Result rabbitCancelOrder(Long orderId, String reason);

    /**
     * 确认收货
     * 
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result deliveryOrder(Long orderId);

    /**
     * 确认订单（商家发货）
     * 
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result confirmOrder(Long orderId);
}