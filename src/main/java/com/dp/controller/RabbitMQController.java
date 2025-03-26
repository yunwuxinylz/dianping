package com.dp.controller;

import com.dp.dto.Result;
import com.dp.service.RabbitMQProducerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;  // Changed this line

import javax.annotation.Resource;

/**
 * RabbitMQ控制器
 */
@Slf4j
@RestController
@RequestMapping("/mq")
public class RabbitMQController {

    @Resource
    private RabbitMQProducerService producerService;

    @GetMapping("/direct")
    public Result sendDirectMessage(@RequestParam String message) {
        try {
            log.info("接收到Direct消息请求：{}", message);
            producerService.sendDirectMessage(message);
            return Result.ok("Direct消息发送成功");
        } catch (Exception e) {
            log.error("Direct消息发送失败：", e);
            return Result.fail("消息发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送Topic消息
     *
     * @param message 消息内容
     * @param routingKey 路由键
     * @return 结果
     */
    @GetMapping("/topic")
    public Result sendTopicMessage(@RequestParam String message, @RequestParam String routingKey) {
        producerService.sendTopicMessage(message, routingKey);
        return Result.ok("Topic消息发送成功");
    }

    /**
     * 发送Fanout消息
     *
     * @param message 消息内容
     * @return 结果
     */
    @GetMapping("/fanout")
    public Result sendFanoutMessage(@RequestParam String message) {
        producerService.sendFanoutMessage(message);
        return Result.ok("Fanout消息发送成功");
    }

    /**
     * 发送Headers消息
     *
     * @param message 消息内容
     * @return 结果
     */
    @GetMapping("/headers")
    public Result sendHeadersMessage(@RequestParam String message) {
        producerService.sendHeadersMessage(message);
        return Result.ok("Headers消息发送成功");
    }
}