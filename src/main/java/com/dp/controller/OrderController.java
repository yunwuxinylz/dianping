package com.dp.controller;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.dp.dto.OrderDTO;
import com.dp.dto.OrderQueryDTO;
import com.dp.dto.Result;
import com.dp.service.IOrderService;

// 订单Controller
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @PostMapping("/create")
    public Result createOrder(@RequestBody OrderCreateDTO orderDTO) {
        // 创建订单
        Long orderId = orderService.createOrder(orderDTO);
        return Result.ok(orderId);
    }

    @PutMapping("/pay/{orderId}")
    public Result payOrder(@PathVariable Long orderId, @RequestParam Integer payType) {
        // 支付订单
        boolean success = orderService.payOrder(orderId, payType);
        if (!success) {
            return Result.fail("支付失败");
        }
        return Result.ok();
    }

    /**
     * 获取订单列表，支持多种筛选条件
     */
    @GetMapping("/list")
    public Result getOrderList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String statuses,
            @RequestParam(required = false) Boolean uncommented,
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
        
        return orderService.getOrderList(queryDTO);
    }

    @GetMapping("/status/{orderId}")
    public Result queryOrderStatus(@PathVariable Long orderId) {
        // 查询订单
        OrderDTO orderDTO = orderService.queryOrderById(orderId);
        if (orderDTO == null) {
            return Result.fail("订单不存在");
        }
        return Result.ok(orderDTO);
    }

    /**
     * 取消订单
     * @param orderId
     * @param cancelReason
     * @return
     */
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestBody OrderCancelDTO orderCancelDTO) {
        Long orderId = orderCancelDTO.getOrderId();
        String cancelReason = orderCancelDTO.getCancelReason();
        return orderService.cancelOrder(orderId, cancelReason);
    }

    /**
     * 发货
     * @param orderId
     * @return
     */
    @PutMapping("/confirm/{orderId}")
    public Result deliveryOrder(@PathVariable Long orderId) {
        return orderService.confirmOrder(orderId);
    }

    /**
     * 确认收货
     * @param orderId
     * @return
     */
    @PutMapping("/delivery/{orderId}")
    public Result confirmOrder(@PathVariable Long orderId) {
        return orderService.deliveryOrder(orderId);
    }

    /**
     * 统计图形
     * @return
     */
    @GetMapping("/statistics")
    public Result getOrderStatistics() {
        return orderService.getOrderStatistics();
    }
}
