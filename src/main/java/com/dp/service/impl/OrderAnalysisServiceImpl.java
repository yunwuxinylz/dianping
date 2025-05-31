package com.dp.service.impl;

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

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dp.dto.DailySpendingDTO;
import com.dp.dto.OrderStatusDTO;
import com.dp.dto.OrderStatisticsDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Order;
import com.dp.entity.OrderItems;
import com.dp.service.IOrderAnalysisService;
import com.dp.service.IOrderItemsService;
import com.dp.utils.UserHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单数据分析服务实现
 */
@Service
@Slf4j
public class OrderAnalysisServiceImpl implements IOrderAnalysisService {

    private final OrderServiceImpl orderService;
    private final IOrderItemsService orderItemsService;

    public OrderAnalysisServiceImpl(OrderServiceImpl orderService, IOrderItemsService orderItemsService) {
        this.orderService = orderService;
        this.orderItemsService = orderItemsService;
    }

    @Override
    public Result getOrderStatistics() {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 初始化统计对象
        OrderStatisticsDTO statistics = new OrderStatisticsDTO();

        // 订单状态常量定义
        final int STATUS_UNPAID = 1;
        final int STATUS_PAID = 2;
        final int STATUS_CANCELLED = 3;
        final int STATUS_PENDING_RECEIPT = 4;
        final int STATUS_COMPLETED = 5;

        // 初始化订单状态统计
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
        List<Order> userOrders = orderService.lambdaQuery()
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

        // 计算每日消费
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
            long count = orderService.count();
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
            Long todaySales = orderService.lambdaQuery()
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
            List<Order> orders = orderService.lambdaQuery()
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

    @Override
    public Result getTopSellingProducts(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认获取前10个
            }

            // 聚合查询各商品的销售数量
            List<OrderItems> orderItems = orderItemsService.list(
                    new LambdaQueryWrapper<OrderItems>()
                            .select(OrderItems::getGoodsId, OrderItems::getGoodsName)
                            .last("GROUP BY goods_id ORDER BY SUM(count) DESC LIMIT " + limit));

            // 处理结果
            List<Map<String, Object>> result = orderItems.stream()
                    .map(item -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("goodsId", item.getGoodsId());
                        map.put("goodsName", item.getGoodsName());
                        // 获取商品总销量
                        Long salesCount = orderItemsService.lambdaQuery()
                                .eq(OrderItems::getGoodsId, item.getGoodsId())
                                .list()
                                .stream()
                                .mapToLong(OrderItems::getCount)
                                .sum();
                        map.put("salesCount", salesCount);
                        return map;
                    })
                    .collect(Collectors.toList());

            return Result.ok(result);
        } catch (Exception e) {
            log.error("获取热销商品排行失败", e);
            return Result.fail("获取热销商品排行失败");
        }
    }

    @Override
    public Result getOrderCategoryAnalysis() {
        try {
            // 获取当前月份的开始和结束时间
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            // 查询本月的订单
            List<Order> orders = orderService.lambdaQuery()
                    .ge(Order::getCreateTime, startOfMonth)
                    .lt(Order::getCreateTime, endOfMonth)
                    .in(Order::getStatus, Arrays.asList(2, 4, 5)) // 已支付、待收货、已完成的订单
                    .list();

            // 1. 按时段统计订单数
            Map<String, Long> ordersByHour = new TreeMap<>();
            for (int i = 0; i < 24; i++) {
                ordersByHour.put(String.format("%02d:00", i), 0L);
            }

            // 2. 按支付方式统计
            Map<Integer, Long> ordersByPayType = new HashMap<>();

            // 3. 按地区统计
            Map<String, Long> ordersByRegion = new HashMap<>();

            // 处理订单数据
            for (Order order : orders) {
                // 按小时统计
                int hour = order.getCreateTime().getHour();
                String hourKey = String.format("%02d:00", hour);
                ordersByHour.put(hourKey, ordersByHour.getOrDefault(hourKey, 0L) + 1);

                // 按支付方式统计
                Integer payType = order.getPayType();
                if (payType != null) {
                    ordersByPayType.put(payType, ordersByPayType.getOrDefault(payType, 0L) + 1);
                }

                // 按地区统计 - 示例，假设从地址中提取
                String region = extractRegion(order.getAddressDetail());
                if (region != null) {
                    ordersByRegion.put(region, ordersByRegion.getOrDefault(region, 0L) + 1);
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("ordersByHour", convertMapToList(ordersByHour, "hour", "count"));
            result.put("ordersByPayType", convertPayTypeMap(ordersByPayType));
            result.put("ordersByRegion", convertMapToList(ordersByRegion, "region", "count"));

            return Result.ok(result);
        } catch (Exception e) {
            log.error("获取订单分类统计失败", e);
            return Result.fail("获取订单分类统计失败");
        }
    }

    // 从地址中提取地区信息
    private String extractRegion(String address) {
        if (address == null || address.isEmpty()) {
            return "未知";
        }

        // 简单示例：提取省份或城市
        // 实际项目中可能需要更复杂的地址解析逻辑
        String[] parts = address.split(" ");
        if (parts.length > 0) {
            return parts[0];
        }
        return "未知";
    }

    // 将Map转换为前端需要的列表格式
    private List<Map<String, Object>> convertMapToList(Map<String, Long> map, String keyName, String valueName) {
        return map.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put(keyName, entry.getKey());
                    item.put(valueName, entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    // 转换支付方式统计结果
    private List<Map<String, Object>> convertPayTypeMap(Map<Integer, Long> payTypeMap) {
        Map<Integer, String> payTypeNames = new HashMap<>();
        payTypeNames.put(1, "微信支付");
        payTypeNames.put(2, "支付宝支付");
        payTypeNames.put(3, "银行卡支付");

        return payTypeMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("payType", entry.getKey());
                    item.put("payTypeName", payTypeNames.getOrDefault(entry.getKey(), "其他支付方式"));
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
