package com.dp.service;

import com.dp.config.RabbitMQConfig;
import com.dp.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消费者服务
 */
@Slf4j
@Service
public class RabbitMQConsumerService {

    /**
     * 监听Direct队列
     *
     * @param message 消息
     */
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE)
    public void receiveDirectMessage(Message message) {
        log.info("接收到Direct队列消息：{}", message);
    }

    /**
     * 监听Topic队列1
     *
     * @param message 消息
     */
    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_1)
    public void receiveTopicMessage1(Message message) {
        log.info("接收到Topic队列1消息：{}", message);
    }

    /**
     * 监听Topic队列2
     *
     * @param message 消息
     */
    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_2)
    public void receiveTopicMessage2(Message message) {
        log.info("接收到Topic队列2消息：{}", message);
    }

    /**
     * 监听Fanout队列1
     *
     * @param message 消息
     */
    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE_1)
    public void receiveFanoutMessage1(Message message) {
        log.info("接收到Fanout队列1消息：{}", message);
    }

    /**
     * 监听Fanout队列2
     *
     * @param message 消息
     */
    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE_2)
    public void receiveFanoutMessage2(Message message) {
        log.info("接收到Fanout队列2消息：{}", message);
    }

    /**
     * 监听Headers队列
     *
     * @param message 消息
     */
    @RabbitListener(queues = RabbitMQConfig.HEADERS_QUEUE)
    public void receiveHeadersMessage(Message message) {
        log.info("接收到Headers队列消息：{}", message);
    }
}