package com.dp.listener;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dp.dto.CartDTO;
import com.dp.dto.CartItemDTO;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.Cart;
import com.dp.service.ICartService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * 购物车消息监听器
 */
@Component
@Slf4j
public class CartListener {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ICartService cartService;

    /**
     * Redis购物车KEY前缀
     */
    private static final String CART_KEY_PREFIX = "cart:user:";

    /**
     * 监听购物车保存消息，将Redis中的购物车数据同步到数据库
     */
    @RabbitListener(queues = "cart.save.queue")
    @Transactional
    public void listenCartSave(Long userId, Message message, Channel channel) throws IOException {
        try {
            log.info("接收到购物车保存消息，用户ID：{}", userId);

            // 同步购物车数据到数据库
            syncCartToDatabase(userId);

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("处理购物车保存消息异常", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 同步Redis购物车数据到数据库
     * 
     * @param userId 用户ID
     */
    private void syncCartToDatabase(Long userId) {
        // 获取Redis中的购物车数据
        String cartKey = CART_KEY_PREFIX + userId;
        CartDTO cartDTO = (CartDTO) redisTemplate.opsForValue().get(cartKey);

        // 如果Redis中没有数据，直接返回
        if (cartDTO == null || cartDTO.getShopCarts() == null || cartDTO.getShopCarts().isEmpty()) {
            return;
        }

        // 获取数据库中现有的购物车数据
        List<Cart> existingCarts = cartService.list(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        Map<String, Cart> existingCartMap = new HashMap<>();

        // 创建复合键（商品ID+SKU ID）到购物车项的映射
        for (Cart cart : existingCarts) {
            String key = cart.getGoodsId() + "-" + (cart.getSkuId() == null ? "null" : cart.getSkuId());
            existingCartMap.put(key, cart);
        }

        // 将Redis中的购物车数据与数据库比对并更新
        List<Cart> cartsToUpdate = new ArrayList<>();
        List<Cart> cartsToInsert = new ArrayList<>();
        List<Long> cartsToDelete = new ArrayList<>();

        // 记录Redis中存在的项目
        Set<String> redisCartKeys = new HashSet<>();

        // 处理Redis中的购物车数据
        for (Map.Entry<Long, ShopCartDTO> entry : cartDTO.getShopCarts().entrySet()) {
            ShopCartDTO shopCart = entry.getValue();
            if (CollectionUtils.isEmpty(shopCart.getItems())) {
                continue;
            }

            for (CartItemDTO item : shopCart.getItems()) {
                String key = item.getGoodsId() + "-" + (item.getSkuId() == null ? "null" : item.getSkuId());
                redisCartKeys.add(key);

                Cart existingCart = existingCartMap.get(key);
                if (existingCart != null) {
                    // 更新现有项
                    existingCart.setCount(item.getCount());
                    existingCart.setChecked(item.getChecked());
                    existingCart.setUpdateTime(LocalDateTime.now());
                    cartsToUpdate.add(existingCart);
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

        // 找出数据库中有但Redis中没有的项（需要删除）
        for (Map.Entry<String, Cart> entry : existingCartMap.entrySet()) {
            if (!redisCartKeys.contains(entry.getKey())) {
                cartsToDelete.add(entry.getValue().getId());
            }
        }

        // 批量更新数据库
        if (!cartsToUpdate.isEmpty()) {
            cartService.updateBatchById(cartsToUpdate);
        }

        if (!cartsToInsert.isEmpty()) {
            cartService.saveBatch(cartsToInsert);
        }

        if (!cartsToDelete.isEmpty()) {
            cartService.removeByIds(cartsToDelete);
        }

        log.info("用户[{}]购物车数据已同步到数据库，更新{}件，新增{}件，删除{}件",
                userId, cartsToUpdate.size(), cartsToInsert.size(), cartsToDelete.size());
    }
}