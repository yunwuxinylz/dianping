package com.dp.service.impl;

import static com.dp.config.RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY;
import static com.dp.config.RabbitMQConfig.ORDER_EXCHANGE;
import static com.dp.config.RabbitMQConfig.QUEUE_TTL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.DailySpendingDTO;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.dto.OrderItemsDTO;
import com.dp.dto.OrderListDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.OrderStatisticsDTO;
import com.dp.dto.OrderStatusDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Order;
import com.dp.entity.OrderItems;
import com.dp.mapper.OrderMapper;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IOrderItemsService;
import com.dp.service.IOrderService;
import com.dp.utils.RedisConstants;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单Service实现
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private final IGoodsService goodsService;
    private final IOrderItemsService orderItemsService;
    private final IGoodSKUService goodSKUService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public OrderServiceImpl(IGoodsService goodsService, IOrderItemsService orderItemsService,
            RabbitTemplate rabbitTemplate, StringRedisTemplate stringRedisTemplate, IGoodSKUService goodSKUService) {
        this.goodsService = goodsService;
        this.orderItemsService = orderItemsService;
        this.rabbitTemplate = rabbitTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.goodSKUService = goodSKUService;
    }

    @Resource
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public Long createOrder(OrderCreateDTO orderDTO) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 创建订单
        Order order = new Order();

        order.setUserId(userId);
        order.setShopId(orderDTO.getShopId());
        order.setShopName(orderDTO.getShopName());
        order.setShopImage(orderDTO.getShopImage());
        order.setRemark(orderDTO.getRemark());
        order.setAmount(orderDTO.getAmount());
        order.setPayType(orderDTO.getPayType());
        order.setCount(orderDTO.getCount());
        order.setCommented(false);
        order.setAddressId(orderDTO.getAddressId());
        order.setAddressDetail(orderDTO.getAddressDetail());
        order.setAddressName(orderDTO.getAddressName());
        order.setAddressPhone(orderDTO.getAddressPhone());
        order.setCommented(false);
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
            // 如果有SKU，则扣减SKU库存
            if (orderItem.getSkuId() != null) {
                boolean success = goodSKUService.update()
                        .setSql("stock = stock - " + orderItem.getCount())
                        .eq("id", orderItem.getSkuId())
                        .ge("stock", orderItem.getCount()) // 确保库存充足
                        .update();
                if (!success) {
                    throw new RuntimeException("库存不足，无法下单");
                }
                // 扣减商品库存
                boolean success1 = goodsService.update()
                        .setSql("stock = stock - " + orderItem.getCount())
                        .eq("id", orderItem.getGoodsId())
                        .ge("stock", orderItem.getCount()) // 确保库存充足
                        .update();
                if (!success1) {
                    throw new RuntimeException("库存不足，无法下单");
                }
            }
        }

        stringRedisTemplate.opsForValue().set(RedisConstants.ORDER_STATUS + order.getId(),
                order.getStatus().toString());

        rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_CANCEL_ROUTING_KEY,
                String.valueOf(order.getId()), message -> {
                    message.getMessageProperties().setDelay(QUEUE_TTL[0]);
                    message.getMessageProperties().setHeader("retry-count", 0);
                    return message;
                });

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

        if (success) {
            // 增加销量
            List<OrderItems> orderItems = orderItemsService.query()
                    .eq("order_id", orderId)
                    .list();

            for (OrderItems orderItem : orderItems) {
                // 如果有SKU，则增加SKU销量
                if (orderItem.getSkuId() != null) {
                    goodSKUService.update()
                            .setSql("sold = sold + " + orderItem.getCount())
                            .eq("id", orderItem.getSkuId())
                            .update();
                }

                // 增加商品销量
                goodsService.update()
                        .setSql("sold = sold + " + orderItem.getCount())
                        .eq("id", orderItem.getGoodsId())
                        .update();
            }

            stringRedisTemplate.delete(RedisConstants.ORDER_STATUS + order.getId());
        }
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
        List<OrderItems> orderItems = orderItemsService.list()
                .stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .collect(Collectors.toList()); // 替换 toList() 为 collect(Collectors.toList())

        // 转换为OrderDTO
        OrderDTO orderDTO = BeanUtil.copyProperties(order, OrderDTO.class);
        List<OrderItemsDTO> orderItemsDTO = orderItems.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemsDTO.class))
                .collect(Collectors.toList()); // 替换 toList() 为 collect(Collectors.toList())
        orderDTO.setItems(orderItemsDTO);

        return orderDTO;
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

    /**
     * 取消订单
     *
     * @param orderId
     * @param reason
     * @return
     */
    @Override
    @Transactional
    public Result cancelOrder(Long orderId, String reason) {
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
            throw new RuntimeException("订单不属于当前用户");
        }
        // 修改订单状态
        boolean success = update()
                .set("status", 3) // 已取消
                .set("cancel_reason", reason) // 取消原因
                .eq("id", orderId)
                .update();

        if (success) {
            List<OrderItems> orderItems = orderItemsService.list()
                    .stream()
                    .filter(item -> item.getOrderId().equals(orderId))
                    .collect(Collectors.toList()); // 替换 toList() 为 collect(Collectors.toList())

            // 恢复库存
            for (OrderItems orderItem : orderItems) {
                // 如果有SKU，则恢复SKU库存
                if (orderItem.getSkuId() != null) {
                    goodSKUService.update()
                            .setSql("stock = stock + " + orderItem.getCount())
                            .eq("id", orderItem.getSkuId())
                            .update();
                    // 恢复商品库存
                    goodsService.update()
                            .setSql("stock = stock + " + orderItem.getCount())
                            .eq("id", orderItem.getGoodsId())
                            .update();
                }
            }

            stringRedisTemplate.delete(RedisConstants.ORDER_STATUS + order.getId());
        }
        return Result.ok();
    }

    /**
     * 超时取消订单
     *
     * @param orderId
     * @param reason
     * @return
     */
    @Override
    @Transactional
    public Result rabbitCancelOrder(Long orderId, String reason) {
        String status = stringRedisTemplate.opsForValue().get(RedisConstants.ORDER_STATUS + orderId);
        if (status == null) {
            log.info("订单{}不存在或已支付", orderId);
            return Result.fail("订单不存在或已支付");
        }

        Order order = getById(orderId);
        // 修改订单状态
        boolean success = this.update()
                .set("status", 3) // 已取消
                .set("cancel_reason", reason) // 取消原因
                .eq("id", orderId)
                .eq("status", 1) // 未支付
                .update();

        if (success) {
            List<OrderItems> orderItems = orderItemsService.list()
                    .stream()
                    .filter(item -> item.getOrderId().equals(orderId))
                    .collect(Collectors.toList()); // 替换 toList() 为 collect(Collectors.toList())

            // 恢复库存
            for (OrderItems orderItem : orderItems) {
                // 如果有SKU，则恢复SKU库存
                if (orderItem.getSkuId() != null) {
                    goodSKUService.update()
                            .setSql("stock = stock + " + orderItem.getCount())
                            .eq("id", orderItem.getSkuId())
                            .update();
                    // 恢复商品库存
                    goodsService.update()
                            .setSql("stock = stock + " + orderItem.getCount())
                            .eq("id", orderItem.getGoodsId())
                            .update();
                }
            }

            stringRedisTemplate.delete(RedisConstants.ORDER_STATUS + order.getId());
            log.info("订单{}超时已取消", orderId);
        }
        return Result.ok(); // 添加返回值
    }

    @Override
    @Transactional
    public Result deliveryOrder(Long orderId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 校验订单状态
        if (order.getStatus() != 4) {
            Result.fail("订单状态异常");
        }

        this.update().set("status", 5) // 确认收货，已完成
                .eq("id", orderId)
                .eq("status", 4) // 待收货
                .update();

        return Result.ok();
    }

    @Override
    @Transactional
    public Result confirmOrder(Long orderId) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 校验订单状态
        if (order.getStatus() != 2) {
            Result.fail("订单状态异常");
        }

        this.update().set("status", 4) // 已发货
                .eq("id", orderId)
                .eq("status", 2) // 已支付
                .update();

        return Result.ok();
    }

    @Override
    public Result getOrderStatistics() {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 初始化统计对象
        OrderStatisticsDTO statistics = new OrderStatisticsDTO();

        // 使用常量定义订单状态
        final int STATUS_UNPAID = 1;
        final int STATUS_PAID = 2;
        final int STATUS_CANCELLED = 3;
        final int STATUS_PENDING_RECEIPT = 4;
        final int STATUS_COMPLETED = 5;

        // 初始化订单状态统计（使用Map提高查找效率）
        Map<Integer, OrderStatusDTO> statusMap = new HashMap<>();
        statusMap.put(STATUS_UNPAID, new OrderStatusDTO("待付款", 0, STATUS_UNPAID));
        statusMap.put(STATUS_PAID, new OrderStatusDTO("已支付", 0, STATUS_PAID));
        statusMap.put(STATUS_CANCELLED, new OrderStatusDTO("已取消", 0, STATUS_CANCELLED));
        statusMap.put(STATUS_PENDING_RECEIPT, new OrderStatusDTO("待收货", 0, STATUS_PENDING_RECEIPT));
        statusMap.put(STATUS_COMPLETED, new OrderStatusDTO("已完成", 0, STATUS_COMPLETED));

        // 获取近15天的日期范围
        LocalDate today = LocalDate.now();
        LocalDate fifteenDaysAgo = today.minusDays(15);

        // 一次性查询所有需要的数据，减少数据库访问次数
        List<Order> userOrders = this.lambdaQuery()
                .eq(Order::getUserId, userId)
                .list();

        // 计算总订单数
        statistics.setTotalOrders(userOrders.size());

        // 计算订单状态分布和总消费金额
        long totalAmount = userOrders.stream()
                .peek(order -> {
                    // 更新对应状态的订单数量
                    Integer status = order.getStatus();
                    if (statusMap.containsKey(status)) {
                        OrderStatusDTO statusDTO = statusMap.get(status);
                        statusDTO.setValue(statusDTO.getValue() + 1);
                    }
                })
                // 只计算已支付且未取消订单的金额
                .filter(order -> order.getStatus() >= STATUS_PAID && order.getStatus() != STATUS_CANCELLED)
                .mapToLong(Order::getAmount)
                .sum();

        statistics.setTotalAmount(totalAmount);
        statistics.setOrderStatus(new ArrayList<>(statusMap.values()));

        // 使用更高效的方式计算每日消费
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Double> dailySpending = new TreeMap<>(); // 使用TreeMap保持日期顺序

        // 初始化日期范围内的每一天
        for (int i = 14; i >= 0; i--) {
            String dayStr = today.minusDays(i).format(formatter);
            dailySpending.put(dayStr, 0.0);
        }

        // 填充每日消费数据
        userOrders.stream()
                .filter(order -> order.getStatus() >= STATUS_PAID && order.getStatus() != STATUS_CANCELLED)
                .filter(order -> {
                    LocalDate orderDate = order.getCreateTime().toLocalDate();
                    return !orderDate.isBefore(fifteenDaysAgo);
                })
                .forEach(order -> {
                    String dayStr = order.getCreateTime().toLocalDate().format(formatter);
                    if (dailySpending.containsKey(dayStr)) {
                        dailySpending.compute(dayStr, (k, v) -> v + (order.getAmount() / 100.0));
                    }
                });

        // 转换为最终数据格式
        List<DailySpendingDTO> monthlySpending = dailySpending.entrySet().stream()
                .map(entry -> new DailySpendingDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        statistics.setMonthlySpending(monthlySpending);

        return Result.ok(statistics);
    }

    @Override
    public Result getOrderCount() {
        try {
            // 从数据库获取订单总数
            long count = count(); // 使用 MyBatis-Plus 的 count() 方法
            return Result.ok(count);
        } catch (Exception e) {
            log.error("获取订单总数失败", e);
            return Result.fail("获取订单总数失败");
        }
    }

    @Override
    public Result getTodaySales() {
        try {
            // 获取今天的开始时间和结束时间
            LocalDateTime today = LocalDate.now().atStartOfDay();
            LocalDateTime tomorrow = today.plusDays(1);

            // 查询今日已支付订单的销售额总和
            // 只统计状态为已支付(2)、待收货(4)和已完成(5)的订单
            Long todaySales = lambdaQuery()
                    .ge(Order::getCreateTime, today)
                    .lt(Order::getCreateTime, tomorrow)
                    .in(Order::getStatus, Arrays.asList(2, 4, 5)) // 已支付、待收货、已完成的订单
                    .list()
                    .stream()
                    .mapToLong(Order::getAmount)
                    .sum();

            return Result.ok(todaySales);
        } catch (Exception e) {
            log.error("获取今日销售额失败", e);
            return Result.fail("获取今日销售额失败");
        }
    }

    @Override
    public Result getWeekSales() {
        try {
            // 获取最近7天的日期范围
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6); // 包括今天在内的7天
            LocalDateTime startTime = sevenDaysAgo.atStartOfDay();
            LocalDateTime endTime = today.plusDays(1).atStartOfDay(); // 今天结束时间

            // 查询最近7天的订单
            List<Order> orders = lambdaQuery()
                    .ge(Order::getCreateTime, startTime)
                    .lt(Order::getCreateTime, endTime)
                    .in(Order::getStatus, Arrays.asList(2, 4, 5)) // 已支付、待收货、已完成的订单
                    .list();

            // 按日期分组计算每天的销售额
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Map<String, Long> dailySales = new TreeMap<>(); // 使用TreeMap保持日期顺序

            // 初始化最近7天的每一天
            for (int i = 6; i >= 0; i--) {
                String dayStr = today.minusDays(i).format(formatter);
                dailySales.put(dayStr, 0L);
            }

            // 填充每日销售数据
            orders.forEach(order -> {
                String dayStr = order.getCreateTime().toLocalDate().format(formatter);
                if (dailySales.containsKey(dayStr)) {
                    dailySales.compute(dayStr, (k, v) -> v + order.getAmount());
                }
            });

            // 转换为前端需要的数据格式
            List<Map<String, Object>> result = dailySales.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("date", entry.getKey());
                        item.put("sales", entry.getValue());
                        return item;
                    })
                    .collect(Collectors.toList());

            return Result.ok(result);
        } catch (Exception e) {
            log.error("获取最近7天销售趋势失败", e);
            return Result.fail("获取最近7天销售趋势失败");
        }
    }
}