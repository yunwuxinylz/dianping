package com.dp.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.service.ICartCacheService;
import com.dp.service.ICartSelectionService;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 购物车选中状态服务实现类
 */
@Service
@Slf4j
public class CartSelectionServiceImpl implements ICartSelectionService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ICartCacheService cartCacheService;

    // 相关常量定义
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final String IDEMPOTENT_KEY_PREFIX = "cart:idempotent:";
    private static final long LOCK_WAIT_SECONDS = 0;
    private static final long LOCK_LEASE_SECONDS = 3;

    public CartSelectionServiceImpl(StringRedisTemplate stringRedisTemplate,
            RedissonClient redissonClient,
            ICartCacheService cartCacheService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.cartCacheService = cartCacheService;
    }

    /**
     * 生成请求标识，用于幂等性控制
     */
    private String generateRequestId(Long userId, String action, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(userId).append(":").append(action);
        for (Object param : params) {
            if (param != null) {
                sb.append(":").append(param);
            } else {
                sb.append(":null");
            }
        }
        return DigestUtil.md5Hex(sb.toString());
    }

    /**
     * 检查购物车商品选中状态
     */
    @Override
    public Result checkCartItem(Long userId, Long goodsId, Long skuId, Boolean checked) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "checkCartItem", goodsId, skuId, checked);
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
            List<ShopCartDTO> cartList;

            if (entries.isEmpty()) {
                // 从数据库加载购物车
                cartList = cartCacheService.loadCartFromDB(userId);
                if (cartList.isEmpty()) {
                    return Result.fail("购物车为空");
                }
            } else {
                cartList = cartCacheService.convertHashToCartList(entries);
            }

            // 查找并更新购物车项
            boolean found = false;
            for (ShopCartDTO shopCart : cartList) {
                for (CartItemDTO item : shopCart.getItems()) {
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        item.setChecked(checked);
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }

            if (!found) {
                return Result.fail("商品不存在于购物车");
            }

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("更新购物车商品选中状态失败", e);
            return Result.fail("更新购物车商品选中状态失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 全选/取消全选
     */
    @Override
    public Result checkAllItems(Long userId, Boolean checked) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "checkAllItems", checked);
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
            List<ShopCartDTO> cartList;

            if (entries.isEmpty()) {
                // 从数据库加载购物车
                cartList = cartCacheService.loadCartFromDB(userId);
                if (cartList.isEmpty()) {
                    return Result.fail("购物车为空");
                }
            } else {
                cartList = cartCacheService.convertHashToCartList(entries);
            }

            // 更新所有商品的选中状态
            for (ShopCartDTO shopCart : cartList) {
                for (CartItemDTO item : shopCart.getItems()) {
                    item.setChecked(checked);
                }
            }

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("全选/取消全选失败", e);
            return Result.fail("全选/取消全选失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 选中或取消选中指定商铺的所有商品
     */
    @Override
    public Result checkShopItems(Long userId, Long shopId, Boolean checked) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "checkShopItems", shopId, checked);
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
            List<ShopCartDTO> cartList;

            if (entries.isEmpty()) {
                // 从数据库加载购物车
                cartList = cartCacheService.loadCartFromDB(userId);
                if (cartList.isEmpty()) {
                    return Result.fail("购物车为空");
                }
            } else {
                cartList = cartCacheService.convertHashToCartList(entries);
            }

            // 更新指定商铺的所有商品选中状态
            ShopCartDTO shopCart = cartList.stream()
                    .filter(sc -> sc.getShopId().equals(shopId))
                    .findFirst()
                    .orElse(null);
            if (shopCart != null && !CollectionUtils.isEmpty(shopCart.getItems())) {
                for (CartItemDTO item : shopCart.getItems()) {
                    item.setChecked(checked);
                }
            }

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("更新商铺商品选中状态失败", e);
            return Result.fail("更新商铺商品选中状态失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 移除选中的商品
     */
    @Override
    public Result removeCheckedItems(Long userId) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "removeCheckedItems");
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
            List<ShopCartDTO> cartList;

            if (entries.isEmpty()) {
                // 从数据库加载购物车
                cartList = cartCacheService.loadCartFromDB(userId);
                if (cartList.isEmpty()) {
                    return Result.fail("购物车为空");
                }
            } else {
                cartList = cartCacheService.convertHashToCartList(entries);
            }

            // 移除选中的商品
            for (ShopCartDTO shopCart : cartList) {
                shopCart.getItems().removeIf(CartItemDTO::getChecked);
            }

            // 清理空的商铺购物车
            cartList.removeIf(shopCart -> shopCart.getItems().isEmpty());

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok("移除选中的商品成功");
        } catch (Exception e) {
            log.error("移除选中的商品失败", e);
            return Result.fail("移除选中的商品失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
