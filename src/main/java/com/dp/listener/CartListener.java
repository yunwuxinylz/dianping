package com.dp.listener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dp.service.ICartCacheService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * 购物车消息监听器
 */
@Component
@Slf4j
public class CartListener {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ICartCacheService cartCacheService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * Redis购物车KEY前缀
     */
    private static final String CART_SYNC_LOCK_PREFIX = "cart:sync:";

    /**
     * 监听购物车保存消息，将Redis中的购物车数据同步到数据库
     */
    @RabbitListener(queues = "cart.save.queue", ackMode = "MANUAL")
    public void listenCartSave(Long userId, Message message, Channel channel) throws IOException {
        // 创建分布式锁，确保同一用户的购物车同步是串行的
        RLock lock = redissonClient.getLock(CART_SYNC_LOCK_PREFIX + userId);

        try {
            // 尝试获取锁，等待1秒
            if (!lock.tryLock(1, 10, TimeUnit.SECONDS)) {
                // 如果获取不到锁，重新入队处理
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                return;
            }

            log.info("接收到购物车保存消息，用户ID：{}", userId);

            // 同步购物车数据到数据库
            cartCacheService.syncCartToDatabase(userId);

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("购物车同步失败: {}", userId, e);
            // 如果是业务逻辑错误，直接丢弃消息；如果是网络等临时错误，重新入队
            boolean requeue = e instanceof IOException;
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, requeue);
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}