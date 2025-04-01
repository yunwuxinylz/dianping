package com.dp.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMQConfig {
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    // 声明交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    // 声明延迟队列
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", ORDER_EXCHANGE);
        // 设置死信routing key
        args.put("x-dead-letter-routing-key", ORDER_CANCEL_ROUTING_KEY);
        // 设置TTL，30分钟
        args.put("x-message-ttl", 5 * 60 * 1000);
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, args);
    }

    // 声明取消订单队列
    @Bean
    public Queue orderCancelQueue() {
        return new Queue(ORDER_CANCEL_QUEUE);
    }

    // 绑定延迟队列
    @Bean
    public Binding bindingDelay() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    // 绑定取消队列
    @Bean
    public Binding bindingCancel() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }
}
