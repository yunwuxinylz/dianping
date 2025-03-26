package com.dp.config;

import org.springframework.amqp.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // Direct交换机名称
    public static final String DIRECT_EXCHANGE = "dp.direct";
    // Direct队列名称
    public static final String DIRECT_QUEUE = "dp.direct.queue";
    // Direct路由键
    public static final String DIRECT_ROUTING_KEY = "direct";

    // Topic交换机名称
    public static final String TOPIC_EXCHANGE = "dp.topic";
    // Topic队列名称
    public static final String TOPIC_QUEUE_1 = "dp.topic.queue.1";
    public static final String TOPIC_QUEUE_2 = "dp.topic.queue.2";
    // Topic路由模式
    public static final String TOPIC_PATTERN_1 = "dp.topic.#";
    public static final String TOPIC_PATTERN_2 = "dp.topic.message";

    // Fanout交换机名称
    public static final String FANOUT_EXCHANGE = "dp.fanout";
    // Fanout队列名称
    public static final String FANOUT_QUEUE_1 = "dp.fanout.queue.1";
    public static final String FANOUT_QUEUE_2 = "dp.fanout.queue.2";

    // Headers交换机名称
    public static final String HEADERS_EXCHANGE = "dp.headers";
    // Headers队列名称
    public static final String HEADERS_QUEUE = "dp.headers.queue";

    // =============== Direct Exchange ===============
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    @Bean
    public Queue directQueue() {
        return new Queue(DIRECT_QUEUE);
    }

    @Bean
    public Binding directBinding() {
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(DIRECT_ROUTING_KEY);
    }

    // =============== Topic Exchange ===============
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE_1);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE_2);
    }

    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(TOPIC_PATTERN_1);
    }

    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(TOPIC_PATTERN_2);
    }

    // =============== Fanout Exchange ===============
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Queue fanoutQueue1() {
        return new Queue(FANOUT_QUEUE_1);
    }

    @Bean
    public Queue fanoutQueue2() {
        return new Queue(FANOUT_QUEUE_2);
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }

    // =============== Headers Exchange ===============
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Queue headersQueue() {
        return new Queue(HEADERS_QUEUE);
    }

    @Bean
    public Binding headersBinding() {
        Map<String, Object> headerValues = new HashMap<>();
        headerValues.put("type", "dp");
        headerValues.put("format", "json");
        return BindingBuilder.bind(headersQueue()).to(headersExchange()).whereAll(headerValues).match();
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }
}