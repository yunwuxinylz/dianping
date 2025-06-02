package com.dp.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.OrderCancelDTO;
import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.Result;
import com.dp.service.IOrderAnalysisService;
import com.dp.service.IOrderService;
import com.dp.service.IOrderStatusService;

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    private final IOrderService orderService;
    private final IOrderStatusService orderStatusService;
    private final IOrderAnalysisService orderAnalysisService;

    public OrderController(IOrderService orderService,
            IOrderStatusService orderStatusService,
            IOrderAnalysisService orderAnalysisService) {
        this.orderService = orderService;
        this.orderStatusService = orderStatusService;
        this.orderAnalysisService = orderAnalysisService;
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result createOrder(@RequestBody OrderCreateDTO orderDTO) {
        // 创建订单
        return orderService.createOrder(orderDTO);
    }

    /**
     * 获取订单列表，支持多种筛选条件
     */
    @GetMapping("/list")
    public Result getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String statuses,
            @RequestParam(required = false) Boolean uncommented,
            @RequestParam(required = false) Boolean afterSaleStatus,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 构建查询条件
        OrderQueryDTO queryDTO = new OrderQueryDTO();
        queryDTO.setCurrent(current);
        queryDTO.setPageSize(pageSize);

        // 处理多状态参数
        if (StringUtils.hasText(statuses)) {
            List<Integer> statusList = Arrays.stream(statuses.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            queryDTO.setStatusList(statusList);
        } else if (status != null) {
            // 处理单一状态参数
            queryDTO.setStatus(status);
        }

        // 处理未评价条件
        queryDTO.setUncommented(uncommented);

        // 处理售后状态条件
        queryDTO.setAfterSaleStatus(afterSaleStatus);

        return orderService.getOrderList(queryDTO);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Result queryOrderDetail(@PathVariable String orderId) {
        // 转换为Long类型
        Long orderIdLong = Long.parseLong(orderId);

        return orderService.queryOrderById(orderIdLong);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrderCancelDTO orderCancelDTO) {
        Long orderId = Long.parseLong(orderCancelDTO.getOrderId());
        String cancelReason = orderCancelDTO.getCancelReason();
        return orderStatusService.cancelOrder(orderId, cancelReason);
    }

    /**
     * 商家发货
     */
    @PutMapping("/confirm/{orderId}")
    public Result confirmOrder(@PathVariable String orderId) {
        Long orderIdLong = Long.parseLong(orderId);
        return orderStatusService.confirmOrder(orderIdLong);
    }

    /**
     * 确认收货
     */
    @PutMapping("/delivery/{orderId}")
    public Result deliveryOrder(@PathVariable String orderId) {
        Long orderIdLong = Long.parseLong(orderId);
        return orderStatusService.deliveryOrder(orderIdLong);
    }

    /**
     * 获取订单统计图表数据
     */
    @GetMapping("/statistics")
    public Result getOrderStatistics() {
        return orderAnalysisService.getOrderStatistics();
    }

    /**
     * 分页获取订单列表（支持搜索）
     */
    @GetMapping("/page")
    public Result getOrderPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return orderService.getOrderPage(page, size, keyword, status);
    }

    /**
     * 获取订单总数
     */
    @GetMapping("/count")
    public Result getOrderCount() {
        return orderAnalysisService.getOrderCount();
    }

    /**
     * 获取今日销售额
     */
    @GetMapping("/today-sales")
    public Result getTodaySales() {
        return orderAnalysisService.getTodaySales();
    }

    /**
     * 获取最近7天销售趋势
     */
    @GetMapping("/week-sales")
    public Result getWeekSales() {
        return orderAnalysisService.getWeekSales();
    }
}
