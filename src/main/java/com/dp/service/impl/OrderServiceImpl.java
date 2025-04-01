package com.dp.service.impl;

import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.config.RabbitMQConfig;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.dto.UserDTO;
import com.dp.entity.Goods;
import com.dp.entity.Order;
import com.dp.mapper.OrderMapper;
import com.dp.service.IGoodsService;
import com.dp.service.IOrderService;
import com.dp.utils.SystemConstants;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

// 订单Service实现
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private IGoodsService goodsService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO orderCreateDTO) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询商品
        Goods goods = goodsService.getById(orderCreateDTO.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 检查库存
        if (goods.getStock() < orderCreateDTO.getAmount()) {
            throw new RuntimeException("库存不足");
        }

        // 扣减库存
        boolean success = goodsService.update()
                .setSql("stock = stock - " + orderCreateDTO.getAmount())
                .setSql("sold = sold + " + orderCreateDTO.getAmount())
                .eq("id", goods.getId())
                .gt("stock", orderCreateDTO.getAmount() - 1)
                .update();
        if (!success) {
            throw new RuntimeException("库存不足");
        }

        // 创建订单
        Order order = new Order();

        order.setUserId(userId);
        order.setShopId(goods.getShopId());
        order.setGoodsId(goods.getId());
        order.setGoodsName(goods.getName());
        order.setGoodsPrice(goods.getPrice());
        order.setAmount(orderCreateDTO.getAmount());
        order.setTotalPrice(goods.getPrice() * orderCreateDTO.getAmount());
        order.setStatus(1); // 未支付

        // 保存订单
        this.save(order);

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_DELAY_ROUTING_KEY,
                order.getId().toString());

        return order.getId();
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, Integer payType) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        //
        // // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        //
        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 校验订单状态
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态异常");
        }

        // 模拟支付，实际项目中应该调用支付接口
        // 更新订单状态
        boolean success = update()
                .set("status", 2) // 已支付
                .set("pay_time", LocalDateTime.now())
                .set("pay_type", payType)
                .eq("id", orderId)
                .eq("status", 1) // 未支付
                .update();

        return success;
    }

    @Override
    public OrderDTO queryOrderById(Long orderId) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return null;
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 转换为DTO
        return BeanUtil.copyProperties(order, OrderDTO.class);
    }

    @Override
    public Object getOrderList() {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        // 根据类型分页查询
        return this.query()
                .eq("user_id", userId)
                .page(new Page<>(1, SystemConstants.DEFAULT_PAGE_SIZE));
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getById(orderId);

        if (order == null) {
            log.info("订单未找到");
            return;
        }

        if (order.getStatus() != 1) {
            log.info("订单已支付，订单号：{}", orderId);
            return;
        }

        // 修改订单状态
        boolean success = update()
                .set("status", 4) // 已取消
                .eq("id", orderId)
                .eq("status", 1) // 未支付
                .update();

        if (success) {
            // 恢复库存
            goodsService.update()
                    .setSql("stock = stock + " + order.getAmount())
                    .setSql("sold = sold - " + order.getAmount())
                    .eq("id", order.getGoodsId())
                    .update();
        }
        log.info("订单{}超时已取消", orderId);
    }
}