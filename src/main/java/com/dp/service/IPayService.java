package com.dp.service;

import java.util.Map;

import com.dp.dto.Result;

public interface IPayService {
    /**
     * 创建支付宝支付订单
     * 
     * @param orderId 订单ID
     * @return 支付表单HTML或跳转链接
     */
    String createAlipayOrder(Long orderId);

    /**
     * 创建微信支付订单
     * 
     * @param orderId 订单ID
     * @return 微信支付链接或二维码内容
     */
    String createWechatOrder(Long orderId);

    /**
     * 处理支付宝异步通知
     * 
     * @param params 通知参数
     * @return 处理结果
     */
    Result handleAlipayNotify(Map<String, String> params);

    /**
     * 处理微信支付异步通知
     * 
     * @param notifyData 通知数据
     * @return 处理结果
     */
    Result handleWechatNotify(String notifyData);

    /**
     * 处理订单支付
     * 
     * @param orderId 订单ID
     * @param payType 支付类型：1-微信支付，2-支付宝
     * @return 支付结果，包含跳转链接或表单
     */
    Result processPayment(Long orderId, Integer payType);

    /**
     * 确认订单支付状态
     * 
     * @param orderId 订单ID
     * @param payType 支付类型：1-微信支付，2-支付宝
     * @return 支付是否成功
     */
    boolean confirmOrderPayment(Long orderId, Integer payType);

    /**
     * 查询支付订单状态
     * 
     * @param orderId 订单ID
     * @param payType 支付类型：1-微信支付，2-支付宝
     * @return 支付状态结果
     */
    Result queryPaymentStatus(Long orderId, Integer payType);
}