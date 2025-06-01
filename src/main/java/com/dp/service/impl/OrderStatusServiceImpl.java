package com.dp.service.impl;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Order;
import com.dp.entity.OrderItems;
import com.dp.mapper.OrderMapper;
import com.dp.service.IOrderItemsService;
import com.dp.service.IOrderStatusService;
import com.dp.utils.RedisConstants;
import com.dp.utils.StockUtils;
import com.dp.utils.UserHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单状态管理服务实现
 */
@Service
@Slf4j
public class OrderStatusServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderStatusService {

    private final IOrderItemsService orderItemsService;
    private final StringRedisTemplate stringRedisTemplate;
    private final StockUtils stockUtils;

    public OrderStatusServiceImpl(IOrderItemsService orderItemsService,
            StringRedisTemplate stringRedisTemplate,
            StockUtils stockUtils) {
        this.orderItemsService = orderItemsService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.stockUtils = stockUtils;
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result cancelOrder(Long orderId, String reason) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        // 查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        return cancel(orderId, reason);
    }

    /**
     * 超时取消订单
     *
     * @param orderId 订单ID
     * @param reason  取消原因
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result rabbitCancelOrder(Long orderId, String reason) {
        String status = stringRedisTemplate.opsForValue().get(RedisConstants.ORDER_STATUS + orderId);
        if (status == null) {
            log.info("订单{}不存在或已支付", orderId);
            return Result.fail("订单不存在或已支付");
        }

        return cancel(orderId, reason);
    }

    /**
     * 内部取消订单方法
     */
    private Result cancel(Long orderId, String reason) {
        // 修改订单状态
        boolean success = this.update()
                .set("status", 3) // 已取消
                .set("cancel_reason", reason) // 取消原因
                .eq("id", orderId)
                .eq("status", 1) // 未支付
                .update();

        if (success) {
            // 获取订单项
            List<OrderItems> orderItems = orderItemsService.lambdaQuery()
                    .eq(OrderItems::getOrderId, orderId)
                    .list();

            // 恢复库存
            for (OrderItems orderItem : orderItems) {
                Long goodsId = orderItem.getGoodsId();
                Long skuId = orderItem.getSkuId();
                Integer count = orderItem.getCount();

                // 使用StockUtils恢复库存
                stockUtils.increaseStock(goodsId, skuId, count);
            }

            // 删除Redis中的订单状态
            stringRedisTemplate.delete(RedisConstants.ORDER_STATUS + orderId);
        }
        return Result.ok();
    }

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result deliveryOrder(Long orderId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 校验订单状态
        if (order.getStatus() != 4) {
            return Result.fail("订单状态异常");
        }

        this.update().set("status", 5) // 确认收货，已完成
                .eq("id", orderId)
                .eq("status", 4) // 待收货
                .update();

        return Result.ok();
    }

    /**
     * 确认订单（商家发货）
     *
     * @param orderId 订单ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result confirmOrder(Long orderId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 校验订单状态
        if (order.getStatus() != 2) {
            return Result.fail("订单状态异常");
        }

        this.update().set("status", 4) // 已发货
                .eq("id", orderId)
                .eq("status", 2) // 已支付
                .update();

        return Result.ok();
    }
}