package com.dp.listener;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dp.config.RabbitMQConfig;
import com.dp.entity.VoucherOrder;
import com.dp.service.IVoucherOrderService;
import com.rabbitmq.client.Channel;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SeckillOrderListener {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE, ackMode = "MANUAL")
    public void listenSeckillOrder(Message message, Channel channel) {
        VoucherOrder voucherOrder = JSONUtil.toBean(new String(message.getBody()), VoucherOrder.class);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("接收到秒杀订单消息：{}", voucherOrder);
            voucherOrderService.createVoucherOrder(voucherOrder);
            // 消息处理成功，手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理秒杀订单异常", e);
            try {
                // 消息处理失败，拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}
