package com.dp.service;

import com.dp.config.RabbitMQConfig;
import com.dp.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RabbitMQ生产者服务
 */
@Slf4j
@Service
public class RabbitMQProducerService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到Direct交换机
     *
     * @param content 消息内容
     */
    public void sendDirectMessage(String content) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setType("direct");
        message.setCreateTime(LocalDateTime.now());

        log.info("发送Direct消息：{}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.DIRECT_ROUTING_KEY, message);
    }

    /**
     * 发送消息到Topic交换机
     *
     * @param content 消息内容
     * @param routingKey 路由键
     */
    public void sendTopicMessage(String content, String routingKey) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setType("topic");
        message.setCreateTime(LocalDateTime.now());

        log.info("发送Topic消息：{}, 路由键：{}", message, routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, routingKey, message);
    }

    /**
     * 发送消息到Fanout交换机
     *
     * @param content 消息内容
     */
    public void sendFanoutMessage(String content) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setType("fanout");
        message.setCreateTime(LocalDateTime.now());

        log.info("发送Fanout消息：{}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", message);
    }

    /**
     * 发送消息到Headers交换机
     *
     * @param content 消息内容
     */
    public void sendHeadersMessage(String content) {
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setType("headers");
        message.setCreateTime(LocalDateTime.now());

        org.springframework.amqp.core.Message amqpMessage = rabbitTemplate.getMessageConverter()
                .toMessage(message, new MessageProperties());
        amqpMessage.getMessageProperties().setHeader("type", "dp");
        amqpMessage.getMessageProperties().setHeader("format", "json");

        log.info("发送Headers消息：{}", message);
        rabbitTemplate.send(RabbitMQConfig.HEADERS_EXCHANGE, "", amqpMessage);
    }
}