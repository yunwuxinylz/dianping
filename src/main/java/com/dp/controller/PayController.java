package com.dp.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.Result;
import com.dp.service.IPayService;
import com.dp.service.IOrderService;
import com.dp.entity.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pay")
@Slf4j
public class PayController {

    private final IPayService payService;
    private final IOrderService orderService;

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Result createPayment(@RequestBody Map<String, Object> paymentData) {
        Long orderId = Long.parseLong(paymentData.get("orderId").toString());
        Integer payType = Integer.parseInt(paymentData.get("payType").toString());
        log.info("创建支付订单: orderId={}, payType={}", orderId, payType);
        return payService.processPayment(orderId, payType);
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderId}")
    public Result queryPaymentStatus(@PathVariable String orderId, @RequestParam Integer payType) {
        Long orderIdLong = Long.parseLong(orderId);
        log.info("查询支付状态: orderId={}, payType={}", orderIdLong, payType);
        return payService.queryPaymentStatus(orderIdLong, payType);
    }

    /**
     * 支付回调接口
     */
    @GetMapping("/return")
    public String paymentReturn(HttpServletRequest request, HttpServletResponse response) {
        // 从请求参数中获取订单ID
        String orderIdStr = request.getParameter("out_trade_no");
        String tradeStatus = request.getParameter("trade_status");

        log.info("支付宝回调: out_trade_no={}, trade_status={}", orderIdStr, tradeStatus);

        if (orderIdStr == null) {
            // 尝试从查询参数中获取
            orderIdStr = request.getParameter("orderId");
        }

        // 如果还是为空，使用默认值
        if (orderIdStr == null) {
            log.warn("支付回调未获取到订单ID，使用请求参数中的orderId");
            orderIdStr = request.getParameter("orderId");
            if (orderIdStr == null) {
                log.error("无法获取订单ID");
                orderIdStr = "0";
            }
        }

        Long orderId = Long.parseLong(orderIdStr);
        Integer payType = 2; // 默认支付宝支付

        try {
            // 获取payType参数
            String payTypeStr = request.getParameter("payType");
            if (payTypeStr != null) {
                payType = Integer.parseInt(payTypeStr);
            }
        } catch (NumberFormatException e) {
            log.warn("解析payType参数失败", e);
        }

        log.info("处理支付回调: orderId={}, payType={}", orderId, payType);

        // 判断支付状态
        boolean isPaid = "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);

        // 如果没有明确的支付状态，则查询订单状态
        if (!isPaid) {
            // 查询订单状态
            Order order = orderService.getById(orderId);
            isPaid = order != null && order.getStatus() >= 2;

            if (!isPaid) {
                // 尝试查询支付状态
                Result result = payService.queryPaymentStatus(orderId, payType);
                isPaid = result.getSuccess() && (Boolean) result.getData();
            }
        }

        // 前端地址，确保使用正确的前端地址
        String frontendUrl = "http://localhost:3000/#/order/result?orderId=" + orderId +
                "&payResult=" + (isPaid ? "success" : "pending") +
                "&payType=" + payType;

        // 重定向到前端结果页面
        try {
            log.info("重定向到前端页面: {}", frontendUrl);
            response.sendRedirect(frontendUrl);
        } catch (IOException e) {
            log.error("重定向失败", e);
        }

        return "redirect";
    }

    /**
     * 支付宝异步通知
     */
    @PostMapping("/notify/alipay")
    public String alipayNotify(HttpServletRequest request) {
        log.info("收到支付宝异步通知");
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = String.join(",", values);
            params.put(name, valueStr);
            log.info("支付宝异步通知参数: {}={}", name, valueStr);
        }
        Result result = payService.handleAlipayNotify(params);
        return result.getSuccess() ? "success" : "fail";
    }

    /**
     * 微信支付异步通知
     */
    @PostMapping("/notify/wechat")
    public String wechatNotify(@RequestBody String requestBody) {
        log.info("收到微信支付异步通知");
        Result result = payService.handleWechatNotify(requestBody);
        return result.getSuccess() ? "<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>"
                : "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";
    }

    /**
     * 模拟普通支付
     */
}