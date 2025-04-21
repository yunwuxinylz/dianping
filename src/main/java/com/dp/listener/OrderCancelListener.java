package com.dp.listener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Resource;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dp.config.RabbitMQConfig;
import com.dp.service.impl.OrderServiceImpl;
import com.dp.utils.RedisConstants;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderCancelListener {
    @Resource
    private OrderServiceImpl orderService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE, ackMode = "MANUAL")
    public void handleOrderCancel(Message message, Channel channel) {
        String orderId = null;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            orderId = new String(message.getBody(), StandardCharsets.UTF_8);
            Integer retryCount = message.getMessageProperties().getHeader("retry-count");
            retryCount = retryCount == null ? 0 : retryCount;
            final Integer finalRetryCount = retryCount;

            log.info("接收到订单取消消息：{}", orderId);
            String status = stringRedisTemplate.opsForValue().get(RedisConstants.ORDER_STATUS + orderId);

            if (status == null) {
                log.info("订单{}不存在或已支付", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (retryCount < RabbitMQConfig.QUEUE_TTL.length) {
                log.info("订单{}未支付", orderId);

                // 重新发送消息，设置新的延迟时间
                log.info("第{}次重试", retryCount + 1);
                MessageProperties props = message.getMessageProperties();
                props.setHeader("retry-count", retryCount + 1);
                Message newMessage = new Message(message.getBody(), props);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_EXCHANGE,
                        RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY,
                        newMessage,
                        msg -> {
                            msg.getMessageProperties().setDelay(RabbitMQConfig.QUEUE_TTL[finalRetryCount]);
                            return msg;
                        });
                channel.basicAck(deliveryTag, false);
            } else {
                log.info("订单{}已重试{}次，执行取消", orderId, retryCount);
                orderService.cancelOrder(Long.valueOf(orderId));
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception e) {
            log.error("订单{}取消失败", orderId, e);
            try {
                // 消息处理失败，重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("消息确认失败", ex);
            }
        }
    }
}
