package com.dp.listener;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dp.dto.CartItemDTO;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.Cart;
import com.dp.service.ICartService;
import com.rabbitmq.client.Channel;

import cn.hutool.json.JSONUtil;
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
    private ICartService cartService;

    @Resource
    private RedissonClient redissonClient;

    /**
     * Redis购物车KEY前缀
     */
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final String CART_SYNC_LOCK_PREFIX = "cart:sync:";
    private static final String SHOP_FIELD_PREFIX = "shop:";

    /**
     * 监听购物车保存消息，将Redis中的购物车数据同步到数据库
     */
    @RabbitListener(queues = "cart.save.queue", concurrency = "3")
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
            syncCartToDatabase(userId);

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

    /**
     * 同步Redis购物车数据到数据库 (适配Hash结构)
     */
    private void syncCartToDatabase(Long userId) {
        // 获取Redis中的购物车数据
        String cartKey = CART_KEY_PREFIX + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);

        if (entries.isEmpty()) {
            log.info("用户[{}]的Redis购物车数据不存在", userId);
            // 清空MySQL中的购物车数据
            cartService.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            return;
        }

        // 转换Hash结构为购物车列表
        List<ShopCartDTO> cartList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(SHOP_FIELD_PREFIX)) {
                ShopCartDTO shopCart = JSONUtil.toBean((String) entry.getValue(), ShopCartDTO.class);
                cartList.add(shopCart);
            }
        }

        // 如果购物车为空，清空数据库
        if (cartList.isEmpty()) {
            log.info("用户[{}]的Redis购物车为空，清空数据库购物车", userId);
            cartService.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            return;
        }

        // 使用批处理提高性能
        processBatchCartSync(userId, cartList);
    }

    private void processBatchCartSync(Long userId, List<ShopCartDTO> cartList) {
        // 查询现有购物车记录，只查询必要字段
        List<Cart> existingCarts = cartService.list(
                new LambdaQueryWrapper<Cart>()
                        .select(Cart::getId, Cart::getGoodsId, Cart::getSkuId, Cart::getCount, Cart::getChecked)
                        .eq(Cart::getUserId, userId));

        // 创建复合键到购物车项的映射
        Map<String, Cart> existingCartMap = new HashMap<>(existingCarts.size());
        for (Cart cart : existingCarts) {
            String key = cart.getGoodsId() + ":" + (cart.getSkuId() == null ? "null" : cart.getSkuId());
            existingCartMap.put(key, cart);
        }

        // 批量处理：更新、插入、删除
        List<Cart> cartsToUpdate = new ArrayList<>();
        List<Cart> cartsToInsert = new ArrayList<>();
        Set<String> redisCartKeys = new HashSet<>();

        // 处理Redis中的购物车数据
        for (ShopCartDTO shopCart : cartList) {
            if (CollectionUtils.isEmpty(shopCart.getItems())) {
                continue;
            }

            for (CartItemDTO item : shopCart.getItems()) {
                String key = item.getGoodsId() + ":" + (item.getSkuId() == null ? "null" : item.getSkuId());
                redisCartKeys.add(key);

                Cart existingCart = existingCartMap.get(key);
                if (existingCart != null) {
                    // 只在数据真正变更时才更新
                    if (existingCart.getCount() != item.getCount() || existingCart.getChecked() != item.getChecked()) {
                        existingCart.setCount(item.getCount());
                        existingCart.setChecked(item.getChecked());
                        existingCart.setUpdateTime(LocalDateTime.now());
                        cartsToUpdate.add(existingCart);
                    }
                } else {
                    // 添加新项
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setGoodsId(item.getGoodsId());
                    newCart.setSkuId(item.getSkuId());
                    newCart.setCount(item.getCount());
                    newCart.setChecked(item.getChecked());
                    newCart.setCreateTime(LocalDateTime.now());
                    newCart.setUpdateTime(LocalDateTime.now());
                    cartsToInsert.add(newCart);
                }
            }
        }

        // 找出要删除的项
        List<Long> idsToRemove = new ArrayList<>();
        for (Map.Entry<String, Cart> entry : existingCartMap.entrySet()) {
            if (!redisCartKeys.contains(entry.getKey())) {
                idsToRemove.add(entry.getValue().getId());
            }
        }

        // 批量执行数据库操作
        if (!cartsToUpdate.isEmpty()) {
            cartService.updateBatchById(cartsToUpdate);
        }

        if (!cartsToInsert.isEmpty()) {
            cartService.saveBatch(cartsToInsert);
        }

        if (!idsToRemove.isEmpty()) {
            cartService.removeByIds(idsToRemove);
        }

        log.info("用户[{}]购物车数据已同步到数据库，更新{}件，新增{}件，删除{}件",
                userId, cartsToUpdate.size(), cartsToInsert.size(), idsToRemove.size());
    }
}