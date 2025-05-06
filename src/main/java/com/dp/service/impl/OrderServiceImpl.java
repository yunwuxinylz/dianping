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
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.dp.service.IGoodsService;
import com.dp.service.IOrderItemsService;
import com.dp.service.IOrderService;
import com.dp.utils.RedisConstants;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IOrderItemsService orderItemsService;

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

        // 库存已统一扣减

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

        Long count = this.count(wrapper);

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

        // 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", count);
        return Result.ok(result);
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
                Result result = goodsService.updateStock(orderItem.getGoodsId(), -orderItem.getCount(),
                        orderItem.getSkuId());
                if (!result.getSuccess()) {
                    return Result.fail("订单取消失败");
                }
            }
            // 如果redis中存在订单状态，则删除
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

            for (OrderItems orderItem : orderItems) {
                Result result = goodsService.updateStock(orderItem.getGoodsId(), -orderItem.getCount(),
                        orderItem.getSkuId());
                if (!result.getSuccess()) {
                    log.info("订单{}恢复库存失败，取消失败", orderId);
                    return Result.fail("订单取消失败");
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

        // 根据用户查询所有订单
        List<Order> userOrders = this.lambdaQuery()
               .eq(Order::getUserId, userId)
               .list();

        // 初始化统计对象
        OrderStatisticsDTO statistics = new OrderStatisticsDTO();

        // 初始化订单状态统计
        List<OrderStatusDTO> orderStatusList = new ArrayList<>();
        orderStatusList.add(new OrderStatusDTO("待付款", 0, 1));
        orderStatusList.add(new OrderStatusDTO("已支付", 0, 2));
        orderStatusList.add(new OrderStatusDTO("已取消", 0, 3));
        orderStatusList.add(new OrderStatusDTO("待收货", 0, 4));
        orderStatusList.add(new OrderStatusDTO("已完成", 0, 5));

        // 总订单数
        statistics.setTotalOrders(userOrders.size());

        // 总消费金额（初始为0）
        Long totalAmount = 0L;

        // 计算订单状态分布
        for (Order order : userOrders) {
            // 增加对应状态的订单数量
            for (OrderStatusDTO statusDTO : orderStatusList) {
                if (statusDTO.getStatus().equals(order.getStatus())) {
                    statusDTO.setValue(statusDTO.getValue() + 1);
                    break;
                }
            }

            // 如果订单已支付，增加总消费金额（排除已取消的订单）
            if (order.getStatus() >= 2 && order.getStatus() != 3) {
                totalAmount += order.getAmount();
            }
        }

        statistics.setTotalAmount(totalAmount);
        statistics.setOrderStatus(orderStatusList);

        // 计算最近7天消费趋势
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        // 生成最近7天的日期
        List<String> days = new ArrayList<>();
        Map<String, Double> dailyData = new HashMap<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String dayStr = day.format(formatter);
            days.add(dayStr);
            dailyData.put(dayStr, 0.0);
        }

        // 统计每天消费
        for (Order order : userOrders) {
            // 只统计已支付的订单
            if (order.getStatus() >= 2 && order.getStatus() != 3) {
                LocalDate orderDate = order.getCreateTime().toLocalDate();
                String dayStr = orderDate.format(formatter);

                // 如果是最近7天内的订单，增加该天消费
                if (dailyData.containsKey(dayStr)) {
                    Double currentAmount = dailyData.get(dayStr);
                    dailyData.put(dayStr, currentAmount + (order.getAmount() / 100.0)); // 转换为元
                }
            }
        }

        // 转换为图表所需数据格式
        List<DailySpendingDTO> monthlySpending = new ArrayList<>();
        for (String day : days) {
            monthlySpending.add(new DailySpendingDTO(day, dailyData.get(day)));
        }

        statistics.setMonthlySpending(monthlySpending);

        return Result.ok(statistics);
    }
}