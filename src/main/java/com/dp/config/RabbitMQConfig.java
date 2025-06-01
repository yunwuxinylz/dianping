package com.dp.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";
    public static final Integer[] QUEUE_TTL = { 10 * 1000, 40 * 1000, 70 * 1000, 3 * 60 * 1000, 5 * 60 * 1000,
            10 * 60 * 1000 };

    public static final String SECKILL_QUEUE = "seckill.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_ROUTING_KEY = "sckill.create.key";

    // 只需要一个取消订单队列
    @Bean
    public Queue orderCancelQueue() {
        return new Queue(ORDER_CANCEL_QUEUE);
    }

    // 声明延迟交换机
    @Bean
    public CustomExchange orderExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(ORDER_EXCHANGE, "x-delayed-message", true, false, args);
    }

    // 绑定队列到延迟交换机
    @Bean
    public Binding bindingOrder() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderExchange())
                .with(ORDER_CANCEL_ROUTING_KEY)
                .noargs();
    }

    // 秒杀队列

    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE);
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);

    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with(SECKILL_ROUTING_KEY);

    }

    /**
     * 购物车交换机
     */
    @Bean
    public DirectExchange cartExchange() {
        return new DirectExchange("cart.exchange");
    }

    /**
     * 购物车更新队列
     */
    @Bean
    public Queue cartUpdateQueue() {
        return QueueBuilder
                .durable("cart.update.queue")
                .build();
    }

    /**
     * 购物车保存队列
     */
    @Bean
    public Queue cartSaveQueue() {
        return QueueBuilder
                .durable("cart.save.queue")
                .build();
    }

    /**
     * 绑定购物车更新队列到交换机
     */
    @Bean
    public Binding cartUpdateBinding(Queue cartUpdateQueue, DirectExchange cartExchange) {
        return BindingBuilder
                .bind(cartUpdateQueue)
                .to(cartExchange)
                .with("cart.update");
    }

    /**
     * 绑定购物车保存队列到交换机
     */
    @Bean
    public Binding cartSaveBinding(Queue cartSaveQueue, DirectExchange cartExchange) {
        return BindingBuilder
                .bind(cartSaveQueue)
                .to(cartExchange)
                .with("cart.save");
    }
}
