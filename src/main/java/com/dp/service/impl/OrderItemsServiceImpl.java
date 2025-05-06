package com.dp.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.entity.OrderItems;
import com.dp.mapper.OrderItemsMapper;
import com.dp.service.IOrderItemsService;

/**
 * 订单项服务实现类
 */
@Service
public class OrderItemsServiceImpl extends ServiceImpl<OrderItemsMapper, OrderItems> implements IOrderItemsService {
}