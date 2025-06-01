package com.dp.service.impl;

import static com.dp.config.RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY;
import static com.dp.config.RabbitMQConfig.ORDER_EXCHANGE;
import static com.dp.config.RabbitMQConfig.QUEUE_TTL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.dto.OrderItemsDTO;
import com.dp.dto.OrderListDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Order;
import com.dp.entity.OrderItems;
import com.dp.mapper.OrderMapper;
import com.dp.service.IOrderItemsService;
import com.dp.service.IOrderService;
import com.dp.utils.RedisConstants;
import com.dp.utils.SnowflakeIdWorker;
import com.dp.utils.StockUtils;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单基础服务实现
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private final IOrderItemsService orderItemsService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final SnowflakeIdWorker snowflakeIdWorker;
    private final StockUtils stockUtils;

    public OrderServiceImpl(IOrderItemsService orderItemsService,
            RabbitTemplate rabbitTemplate, StringRedisTemplate stringRedisTemplate,
            SnowflakeIdWorker snowflakeIdWorker,
            StockUtils stockUtils) {
        this.orderItemsService = orderItemsService;
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.snowflakeIdWorker = snowflakeIdWorker;
        this.stockUtils = stockUtils;
    }

    @Override
    @Transactional
    public Result createOrder(OrderCreateDTO orderDTO) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 创建订单
        Order order = new Order();

        // 使用雪花算法生成订单ID
        long orderId = snowflakeIdWorker.nextId();
        order.setId(orderId);

        order.setUserId(userId);
        order.setShopId(orderDTO.getShopId());
        order.setShopName(orderDTO.getShopName());
        if (orderDTO.getShopImage() != null) {
            List<String> shopImages = orderDTO.getShopImage();
            String shopImagesStr = String.join(",", shopImages);
            order.setShopImage(shopImagesStr);
        }
        order.setRemark(orderDTO.getRemark());
        order.setAmount(orderDTO.getAmount());
        order.setCount(orderDTO.getCount());
        order.setCommented(false);
        order.setAddressId(orderDTO.getAddressId());
        order.setAddressDetail(orderDTO.getAddressDetail());
        order.setAddressName(orderDTO.getAddressName());
        order.setAddressPhone(orderDTO.getAddressPhone());
        order.setStatus(1); // 未支付
        // 保存订单
        this.save(order);

        List<OrderItems> orderItemsList = new ArrayList<>();
        // 保存订单列表项
        for (OrderItemsDTO orderItems : orderDTO.getItems()) {
            OrderItems orderItemsEntity = new OrderItems();
            BeanUtil.copyProperties(orderItems, orderItemsEntity);
            // 添加空值检查
            List<String> goodsImages = orderItems.getGoodsImage();
            String imagesStr = goodsImages != null && !goodsImages.isEmpty()
                    ? String.join(",", goodsImages)
                    : "";
            orderItemsEntity.setGoodsImage(imagesStr);
            orderItemsEntity.setOrderId(order.getId());
            orderItemsList.add(orderItemsEntity);
        }
        // 保存订单列表项
        orderItemsService.saveBatch(orderItemsList);

        // 库存扣减
        for (OrderItems orderItem : orderItemsList) {
            Long goodsId = orderItem.getGoodsId();
            Long skuId = orderItem.getSkuId();
            Integer count = orderItem.getCount();

            // 使用StockUtils扣减库存
            boolean success = stockUtils.decreaseStock(goodsId, skuId, count);
            if (!success) {
                return Result.fail("库存不足，无法下单");
            }
        }

        stringRedisTemplate.opsForValue().set(RedisConstants.ORDER_STATUS + order.getId(),
                order.getStatus().toString());

        // 发送延迟消息，用于处理订单超时
        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_CANCEL_ROUTING_KEY,
                String.valueOf(order.getId()), message -> {
                    message.getMessageProperties().setDelay(QUEUE_TTL[0]);
                    message.getMessageProperties().setHeader("retry-count", 0);
                    return message;
                });

        return Result.ok(order.getId().toString());
    }

    @Override
    public Result queryOrderById(Long orderId) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            return Result.fail("订单不属于当前用户");
        }
        List<OrderItems> orderItems = orderItemsService.list()
                .stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .collect(Collectors.toList());

        // 转换为OrderDTO
        OrderDTO orderDTO = BeanUtil.copyProperties(order, OrderDTO.class);
        orderDTO.setId(order.getId().toString());
        List<OrderItemsDTO> orderItemsDTO = orderItems.stream()
                .map(item -> {
                    OrderItemsDTO itemDTO = BeanUtil.copyProperties(item, OrderItemsDTO.class);
                    if (item.getGoodsImage() != null) {
                        itemDTO.setGoodsImage(Arrays.asList(item.getGoodsImage().split(",")));
                    }
                    return itemDTO;
                })
                .collect(Collectors.toList());
        orderDTO.setItems(orderItemsDTO);

        return Result.ok(orderDTO);
    }

    @Override
    public Result getOrderList(OrderQueryDTO queryDTO) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        // 处理状态条件
        if (queryDTO.getStatusList() != null && !queryDTO.getStatusList().isEmpty()) {
            // 多状态查询
            wrapper.in(Order::getStatus, queryDTO.getStatusList());
        } else if (queryDTO.getStatus() != null) {
            // 单一状态查询
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }

        // 处理未评价条件
        if (Boolean.TRUE.equals(queryDTO.getUncommented())) {
            wrapper.eq(Order::getStatus, 5); // 已完成状态
            wrapper.eq(Order::getCommented, false); // 未评价
        }

        // 按创建时间降序
        wrapper.orderByDesc(Order::getCreateTime);

        // 创建分页对象并执行查询
        Page<Order> orderPage = page(new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize()), wrapper);

        // 获取订单ID列表
        List<Long> orderIds = orderPage.getRecords().stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        // 查询订单商品详情
        final List<OrderItems> orderItemsList = orderIds.isEmpty()
                ? new ArrayList<>()
                : orderItemsService.lambdaQuery()
                        .in(OrderItems::getOrderId, orderIds)
                        .list();

        // 转换为OrderDTO并设置商品详情
        List<OrderListDTO> records = orderPage.getRecords().stream()
                .map(order -> {
                    OrderListDTO orderDTO = BeanUtil.copyProperties(order, OrderListDTO.class);
                    orderDTO.setId(order.getId().toString());
                    // 获取订单的商品列表
                    List<OrderItems> items = orderItemsList.stream()
                            .filter(item -> item.getOrderId().equals(order.getId()))
                            .collect(Collectors.toList());
                    // 转换商品列表为DTO
                    List<OrderItemsDTO> itemDTOs = items.stream()
                            .map(item -> {
                                OrderItemsDTO itemDTO = BeanUtil.copyProperties(item, OrderItemsDTO.class);
                                // 处理图片字符串转换为列表
                                if (item.getGoodsImage() != null) {
                                    itemDTO.setGoodsImage(Arrays.asList(item.getGoodsImage().split(",")));
                                }
                                return itemDTO;
                            })
                            .collect(Collectors.toList());
                    orderDTO.setItems(itemDTOs);
                    return orderDTO;
                }).collect(Collectors.toList());

        return Result.ok(records, orderPage.getTotal());
    }
}