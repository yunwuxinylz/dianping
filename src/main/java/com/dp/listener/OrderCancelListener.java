package com.dp.listener;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.dp.config.RabbitMQConfig;
import com.dp.service.impl.OrderServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderCancelListener {
    @Resource
    private OrderServiceImpl orderService;
    
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    public void handleOrderCancel(String orderId) {
        orderService.cancelOrder(Long.valueOf(orderId));
    }
}
