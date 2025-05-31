package com.dp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 支付宝配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    // 应用ID
    private String appId;

    // 商户私钥
    private String privateKey;

    // 支付宝公钥
    private String publicKey;

    // 服务网关
    private String gateway;

    // 异步通知地址
    private String notifyUrl;

    // 同步回调地址
    private String returnUrl;

    // 签名方式
    private String signType = "RSA2";

    // 字符编码格式
    private String charset = "UTF-8";

    // 格式
    private String format = "json";
}