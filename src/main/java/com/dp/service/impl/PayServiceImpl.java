package com.dp.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.dp.config.AlipayConfig;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Order;
import com.dp.entity.OrderItems;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IOrderItemsService;
import com.dp.service.IOrderService;
import com.dp.service.IPayService;
import com.dp.utils.RedisConstants;
import com.dp.utils.UserHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayServiceImpl implements IPayService {

    private final AlipayConfig alipayConfig;
    private final IOrderService orderService;
    private final IGoodsService goodsService;
    private final IGoodSKUService goodSKUService;
    private final IOrderItemsService orderItemsService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result processPayment(Long orderId, Integer payType) {
        try {
            // 根据支付方式创建支付订单
            if (payType == 1) {
                // 微信支付
                String payUrl = createWechatOrder(orderId);
                return Result.ok(payUrl);
            } else if (payType == 2) {
                // 支付宝支付
                String payForm = createAlipayOrder(orderId);
                return Result.ok(payForm);
            } else {
                return Result.fail("不支持的支付方式");
            }
        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return Result.fail("创建支付订单失败: " + e.getMessage());
        }
    }

    @Override
    public String createAlipayOrder(Long orderId) {
        try {
            // 获取订单信息
            Order order = validateOrder(orderId);

            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGateway(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    alipayConfig.getFormat(),
                    alipayConfig.getCharset(),
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType());

            // 创建支付请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

            // 设置同步和异步通知地址
            request.setReturnUrl(alipayConfig.getReturnUrl());
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            // 构建业务参数
            BigDecimal amount = new BigDecimal(order.getAmount()).divide(new BigDecimal("100")); // 转换为元
            String bizContent = "{" +
                    "\"out_trade_no\":\"" + order.getId() + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                    "\"total_amount\":\"" + amount.toString() + "\"," +
                    "\"subject\":\"" + order.getShopName() + "订单\"," +
                    "\"body\":\"" + "购买商品" + "\"" +
                    "}";
            request.setBizContent(bizContent);

            // 调用SDK生成支付表单
            String form = alipayClient.pageExecute(request).getBody();

            // 修改表单，确保在当前页面提交
            form = form.replace("target=\"_blank\"", "");

            return form;
        } catch (AlipayApiException e) {
            log.error("生成支付宝订单失败", e);
            throw new RuntimeException("生成支付宝订单失败", e);
        }
    }

    @Override
    public String createWechatOrder(Long orderId) {
        // 获取订单信息
        Order order = validateOrder(orderId);

        // 这里是微信支付的模拟实现，实际项目中需要集成微信支付SDK
        log.info("创建微信支付订单: {}", orderId);

        // 模拟返回微信支付跳转URL
        String returnUrl = "http://localhost:8081/pay/return/" + orderId + "?payType=1";

        // 在真实场景中，这里应该返回微信支付的二维码链接或跳转链接
        return "https://wx.tenpay.com/mock-pay/" + orderId + "?redirect=" + returnUrl;
    }

    @Override
    public Result handleAlipayNotify(Map<String, String> params) {
        try {
            // 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType());

            if (!signVerified) {
                return Result.fail("验签失败");
            }

            // 获取通知中的订单数据
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");

            // 交易成功或交易完成
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                Long orderId = Long.parseLong(outTradeNo);
                // 更新订单状态
                boolean success = confirmOrderPayment(orderId, 2); // 2表示支付宝支付
                if (success) {
                    return Result.ok("支付成功");
                } else {
                    return Result.fail("订单状态更新失败");
                }
            }

            return Result.ok();
        } catch (Exception e) {
            log.error("处理支付宝通知失败", e);
            return Result.fail("处理支付宝通知失败");
        }
    }

    @Override
    public Result handleWechatNotify(String notifyData) {
        try {
            // 实际项目中需要解析XML或JSON格式的通知数据，校验签名等
            log.info("接收到微信支付通知: {}", notifyData);

            // 模拟解析微信支付通知，提取订单号
            // 实际项目中需要使用合适的XML解析工具提取数据
            Long orderId = 123L; // 假设从通知中提取到的订单ID

            // 更新订单状态
            boolean success = confirmOrderPayment(orderId, 1); // 1表示微信支付
            if (success) {
                return Result.ok("支付成功");
            } else {
                return Result.fail("订单状态更新失败");
            }
        } catch (Exception e) {
            log.error("处理微信支付通知失败", e);
            return Result.fail("处理微信支付通知失败");
        }
    }

    @Override
    @Transactional
    public boolean confirmOrderPayment(Long orderId, Integer payType) {
        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            log.warn("支付失败：订单[{}]不存在", orderId);
            throw new RuntimeException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 1) {
            log.warn("支付失败：订单[{}]状态异常，当前状态: {}", orderId, order.getStatus());
            return false;
        }

        // 更新订单状态
        boolean success = orderService.update()
                .set("status", 2) // 已支付
                .set("pay_time", LocalDateTime.now())
                .set("pay_type", payType)
                .eq("id", orderId)
                .eq("status", 1) // 未支付
                .update();

        if (success) {
            log.info("订单[{}]支付成功，支付方式: {}", orderId, payType);

            // 增加销量
            List<OrderItems> orderItems = orderItemsService.query()
                    .eq("order_id", orderId)
                    .list();

            for (OrderItems orderItem : orderItems) {
                Long goodsId = orderItem.getGoodsId();
                Long skuId = orderItem.getSkuId();
                Integer count = orderItem.getCount();

                // 如果有SKU，则增加SKU销量
                if (skuId != null) {
                    goodSKUService.update()
                            .setSql("sold = sold + " + count)
                            .eq("id", skuId)
                            .update();
                }

                // 增加商品销量
                goodsService.update()
                        .setSql("sold = sold + " + count)
                        .eq("id", goodsId)
                        .update();
            }

            // 清除Redis中的订单状态缓存
            stringRedisTemplate.delete(RedisConstants.ORDER_STATUS + orderId);
        } else {
            log.warn("订单[{}]支付状态更新失败", orderId);
        }
        return success;
    }

    @Override
    public Result queryPaymentStatus(Long orderId, Integer payType) {
        try {
            // 查询订单状态
            Order order = orderService.getById(orderId);
            if (order == null) {
                return Result.fail("订单不存在");
            }

            // 如果订单已支付，直接返回成功
            if (order.getStatus() >= 2) {
                return Result.ok(true);
            }

            // 根据支付方式查询支付状态
            boolean isPaid = false;
            if (payType == 2) { // 支付宝支付
                isPaid = queryAlipayStatus(orderId);
            } else if (payType == 1) { // 微信支付
                isPaid = queryWechatStatus(orderId);
            }

            if (isPaid) {
                // 更新订单状态
                confirmOrderPayment(orderId, payType);
                return Result.ok(true);
            }

            // 未支付
            return Result.ok(false);
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return Result.fail("查询支付状态失败: " + e.getMessage());
        }
    }

    // 支付宝状态查询
    private boolean queryAlipayStatus(Long orderId) {
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getGateway(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    alipayConfig.getFormat(),
                    alipayConfig.getCharset(),
                    alipayConfig.getPublicKey(),
                    alipayConfig.getSignType());

            // 创建查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderId + "\"}");

            // 调用API查询
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            return response.isSuccess() &&
                    ("TRADE_SUCCESS".equals(response.getTradeStatus()) ||
                            "TRADE_FINISHED".equals(response.getTradeStatus()));
        } catch (AlipayApiException e) {
            log.error("查询支付宝订单状态失败", e);
            return false;
        }
    }

    // 微信支付状态查询
    private boolean queryWechatStatus(Long orderId) {
        // 实际项目中需要调用微信支付查询API
        log.info("模拟查询微信支付订单状态: {}", orderId);
        // 这里是模拟实现，实际项目中需要调用微信支付API
        return false;
    }

    /**
     * 验证订单有效性
     * 
     * @param orderId 订单ID
     * @return 订单对象
     */
    private Order validateOrder(Long orderId) {
        // 获取用户信息
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不属于当前用户");
        }

        // 校验订单状态
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态异常");
        }

        return order;
    }
}