package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.Result;
import com.dp.entity.Order;

/**
 * 订单基础服务接口
 */
public interface IOrderService extends IService<Order> {

    /**
     * 创建订单
     * 
     * @param orderDTO 订单创建DTO
     * @return 订单ID
     */
    Result createOrder(OrderCreateDTO orderDTO);

    /**
     * 根据ID查询订单
     * 
     * @param orderId 订单ID
     * @return 订单DTO
     */
    Result queryOrderById(Long orderId);

    /**
     * 获取订单列表
     * 
     * @param queryDTO 查询条件
     * @return 订单列表结果
     */
    Result getOrderList(OrderQueryDTO queryDTO);

}
