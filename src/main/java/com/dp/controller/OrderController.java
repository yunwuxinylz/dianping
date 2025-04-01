package com.dp.controller;


import com.dp.dto.OrderCreateDTO;
import com.dp.dto.OrderDTO;
import com.dp.dto.Result;
import com.dp.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 订单Controller
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @PostMapping("/create")
    public Result createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        // 创建订单
        Long orderId = orderService.createOrder(orderCreateDTO);
        return Result.ok(orderId);
    }

    @PostMapping("/pay/{orderId}")
    public Result payOrder(@PathVariable("orderId") Long orderId, @RequestParam("payType") Integer payType) {
        // 支付订单
        boolean success = orderService.payOrder(orderId, payType);
        if (!success) {
            return Result.fail("支付失败");
        }
        return Result.ok();
    }

    @GetMapping("/list")
    public Result getOrderList() {
        return Result.ok(orderService.getOrderList());
    }

    @GetMapping("/status/{orderId}")
    public Result queryOrderStatus(@PathVariable("orderId") Long orderId) {
        // 查询订单
        OrderDTO order = orderService.queryOrderById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        return Result.ok(order);
    }
}
