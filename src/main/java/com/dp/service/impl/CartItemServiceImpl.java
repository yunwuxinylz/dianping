package com.dp.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dp.dto.CartAddDTO;
import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.Goods;
import com.dp.service.ICartCacheService;
import com.dp.service.ICartItemService;
import com.dp.service.IGoodsService;
import com.dp.utils.StockUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 购物车商品服务实现类
 */
@Service
@Slf4j
public class CartItemServiceImpl implements ICartItemService {

    private final IGoodsService goodsService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final StockUtils stockUtils;
    private final ICartCacheService cartCacheService;

    // 相关常量定义
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final String IDEMPOTENT_KEY_PREFIX = "cart:idempotent:";
    private static final long LOCK_WAIT_SECONDS = 0;
    private static final long LOCK_LEASE_SECONDS = 3;

    public CartItemServiceImpl(IGoodsService goodsService,
            StringRedisTemplate stringRedisTemplate,
            RedissonClient redissonClient,
            StockUtils stockUtils,
            ICartCacheService cartCacheService) {
        this.goodsService = goodsService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redissonClient = redissonClient;
        this.stockUtils = stockUtils;
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
     * 添加商品到购物车
     */
    @Override
    public Result addToCart(Long userId, CartAddDTO cartAddDTO) {
        Long shopId = cartAddDTO.getShopId();
        Long goodsId = cartAddDTO.getGoodsId();
        Long skuId = cartAddDTO.getSkuId();
        Integer count = cartAddDTO.getCount();
        Boolean checked = cartAddDTO.getChecked();

        // 参数校验
        if (userId == null || cartAddDTO == null || goodsId == null || count == null) {
            return Result.fail("参数错误");
        }

        // 生成请求标识
        String requestId = generateRequestId(userId, "addToCart", shopId, goodsId,
                skuId, count, checked);

        // 创建分布式锁
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            // 尝试获取锁，不等待
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求处理中");
            }

            Goods goods = goodsService.getOne(new LambdaQueryWrapper<Goods>()
                    .eq(Goods::getId, goodsId)
                    .eq(Goods::getStatus, 1));
            if (goods == null) {
                return Result.fail("商品不存在或已下架");
            }

            // 从Redis获取购物车并更新
            String cartKey = CART_KEY_PREFIX + userId;

            // 获取购物车数据(使用shopId作为key)
            Object shopCartJson = stringRedisTemplate.opsForHash().get(cartKey, shopId.toString());

            ShopCartDTO shopCart = new ShopCartDTO();

            if (shopCartJson == null) {
                // 检查库存
                if (!stockUtils.hasEnoughStock(goodsId, skuId, count)) {
                    return Result.fail("库存不足");
                }
                // 创建新的购物车
                shopCart.setShopId(shopId);
                shopCart.setShopName(cartAddDTO.getShopName());
                shopCart.setItems(new ArrayList<>());
                // 将CartAddDTO转换为CartItemDTO
                CartItemDTO cartItem = BeanUtil.toBean(cartAddDTO, CartItemDTO.class);

                shopCart.getItems().add(cartItem);
            } else {
                shopCart = JSONUtil.toBean(shopCartJson.toString(), ShopCartDTO.class);

                boolean isExist = false;
                // 更新购物车项
                for (CartItemDTO item : shopCart.getItems()) {
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        // 更新数量
                        int newCount = item.getCount() + count;
                        if (!stockUtils.hasEnoughStock(goodsId, skuId, newCount)) {
                            return Result.fail("库存不足");
                        }
                        item.setCount(newCount);
                        item.setChecked(checked);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    // 检查库存
                    if (!stockUtils.hasEnoughStock(goodsId, skuId, count)) {
                        return Result.fail("库存不足");
                    }
                    // 创建新的购物车项
                    CartItemDTO cartItem = BeanUtil.toBean(cartAddDTO, CartItemDTO.class);
                    shopCart.getItems().add(cartItem);
                }

            }
            // 更新Redis缓存
            stringRedisTemplate.opsForHash().put(cartKey, shopId.toString(), JSONUtil.toJsonStr(shopCart));
            // 过期时间
            stringRedisTemplate.expire(cartKey, 7, TimeUnit.DAYS);

            // 异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok("添加成功");

        } catch (Exception e) {
            log.error("添加购物车失败", e);
            return Result.fail("添加失败");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 合并购物车
     */
    @Override
    public Result mergeCart(Long userId, List<ShopCartDTO> guestCart) {
        log.info("用户[{}]开始合并购物车，游客购物车项数: {}", userId, guestCart.size());

        // 如果游客购物车为空，直接返回成功
        if (guestCart == null || guestCart.isEmpty()) {
            return Result.ok("购物车已同步");
        }

        // 统计合并结果
        AtomicInteger mergedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try {
            // 逐一将游客购物车项添加到用户购物车
            for (ShopCartDTO shopCart : guestCart) {
                List<CartItemDTO> cartItems = shopCart.getItems();
                if (cartItems == null || cartItems.isEmpty()) {
                    continue;
                }
                for (CartItemDTO item : cartItems) {
                    try {
                        CartAddDTO cartAddDTO = new CartAddDTO();
                        cartAddDTO = BeanUtil.toBean(item, CartAddDTO.class);
                        cartAddDTO.setShopId(shopCart.getShopId());
                        cartAddDTO.setShopName(shopCart.getShopName());
                        Result res = addToCart(userId, cartAddDTO);
                        if (res.getSuccess()) {
                            mergedCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                            log.warn("添加购物车项失败: 用户ID={}, 商品ID={}, SKU={}, 错误: {}",
                                    userId, item.getGoodsId(), item.getSkuId(), res.getErrorMsg());
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        log.error("添加购物车项异常: 用户ID={}, 商品ID={}, SKU={}",
                                userId, item.getGoodsId(), item.getSkuId(), e);
                    }
                }
            }

            // 生成结果消息
            String message;
            if (errorCount.get() > 0) {
                message = String.format("购物车已同步，%d件商品同步成功，%d件同步失败",
                        mergedCount.get(), errorCount.get());
            } else {
                message = String.format("购物车已同步，共%d件商品", mergedCount.get());
            }

            // 返回合并结果
            return Result.ok(message);
        } catch (Exception e) {
            log.error("合并购物车失败: 用户ID={}", userId, e);
            return Result.fail("购物车同步失败，请重试");
        }
    }

    /**
     * 更新购物车商品数量
     */
    @Override
    public Result updateCartItemCount(Long userId, Long shopId, Long goodsId, Long skuId, Integer count) {
        if (userId == null || shopId == null || goodsId == null || skuId == null || count == null || count < 1) {
            return Result.fail("参数错误");
        }

        String requestId = generateRequestId(userId, "updateCartItemCount", shopId, goodsId, skuId, count);
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求处理中");
            }

            // 快速检查库存
            if (!stockUtils.hasEnoughStock(goodsId, skuId, count)) {
                return Result.fail("库存不足");
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            Object shopCartJson = stringRedisTemplate.opsForHash().get(cartKey, shopId.toString());

            ShopCartDTO shopCartDTO = new ShopCartDTO();

            if (shopCartJson == null) {
                return Result.fail("请刷新购物车");
            } else {
                shopCartDTO = JSONUtil.toBean(shopCartJson.toString(), ShopCartDTO.class);
            }

            for (CartItemDTO item : shopCartDTO.getItems()) {
                if (item.getGoodsId().equals(goodsId) &&
                        ((skuId == null && item.getSkuId() == null) ||
                                (skuId != null && skuId.equals(item.getSkuId())))) {
                    item.setCount(count);
                    // 更新Redis缓存
                    stringRedisTemplate.opsForHash().put(cartKey, shopId.toString(), JSONUtil.toJsonStr(shopCartDTO));
                    // 过期时间
                    stringRedisTemplate.expire(cartKey, 7, TimeUnit.DAYS);
                    // 异步更新数据库
                    cartCacheService.sendCartUpdateMessage(userId);

                    return Result.ok("更新完成");
                }
            }

            return Result.fail("商品不存在");

        } catch (Exception e) {
            log.error("更新购物车数量失败", e);
            return Result.fail("更新失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 移除购物车商品
     */
    @Override
    public Result removeFromCart(Long userId, Long shopId, Long goodsId, Long skuId) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "removeFromCart", shopId, goodsId, skuId);
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            String shopIdStr = shopId.toString();
            // 从Redis获取购物车(使用Hash结构)
            String cartKey = CART_KEY_PREFIX + userId;
            Object shopCartJson = stringRedisTemplate.opsForHash().get(cartKey, shopIdStr);
            ShopCartDTO shopCart;

            if (shopCartJson == null) {
                return Result.fail("请刷新购物车");
            } else {
                shopCart = JSONUtil.toBean(shopCartJson.toString(), ShopCartDTO.class);
            }

            List<CartItemDTO> cartItems = shopCart.getItems();
            // 查找并移除购物车项
            for (CartItemDTO item : cartItems) {
                if (item.getGoodsId().equals(goodsId) &&
                        ((skuId == null && item.getSkuId() == null) ||
                                (skuId != null && skuId.equals(item.getSkuId())))) {
                    cartItems.remove(item);
                    if (cartItems.isEmpty()) {
                        stringRedisTemplate.opsForHash().delete(cartKey, shopIdStr);
                    } else {
                        stringRedisTemplate.opsForHash().put(cartKey, shopIdStr, JSONUtil.toJsonStr(shopCart));
                    }

                    // 过期时间
                    stringRedisTemplate.expire(cartKey, 7, TimeUnit.DAYS);

                    // 发送消息到MQ，异步更新数据库
                    cartCacheService.sendCartUpdateMessage(userId);

                    return Result.ok("移除成功");
                }
            }

            return Result.fail("商品不存在");
        } catch (Exception e) {
            log.error("移除购物车商品失败", e);
            return Result.fail("移除失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 清空购物车
     */
    @Override
    public Result clearCart(Long userId) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "clearCart");
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            // 不删除key，只删除value
            stringRedisTemplate.opsForHash().delete(cartKey);
            // 过期时间
            stringRedisTemplate.expire(cartKey, 7, TimeUnit.DAYS);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok("清空成功");
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.fail("清空失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
