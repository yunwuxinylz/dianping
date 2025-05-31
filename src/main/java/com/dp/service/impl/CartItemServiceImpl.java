package com.dp.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.GoodSKU;
import com.dp.entity.Goods;
import com.dp.entity.Shop;
import com.dp.service.ICartItemService;
import com.dp.service.ICartCacheService;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IShopService;
import com.dp.utils.StockUtils;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 购物车商品服务实现类
 */
@Service
@Slf4j
public class CartItemServiceImpl implements ICartItemService {

    private final IGoodsService goodsService;
    private final IGoodSKUService goodSkuService;
    private final IShopService shopService;
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
            IGoodSKUService goodSkuService,
            IShopService shopService,
            StringRedisTemplate stringRedisTemplate,
            RedissonClient redissonClient,
            StockUtils stockUtils,
            ICartCacheService cartCacheService) {
        this.goodsService = goodsService;
        this.goodSkuService = goodSkuService;
        this.shopService = shopService;
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
    public Result addToCart(Long userId, CartItemDTO cartItem) {
        Long goodsId = cartItem.getGoodsId();
        Long skuId = cartItem.getSkuId();
        Integer count = cartItem.getCount();

        // 参数校验
        if (userId == null || cartItem == null || goodsId == null || count == null) {
            return Result.fail("参数错误");
        }

        // 生成请求标识
        String requestId = generateRequestId(userId, "addToCart", goodsId,
                skuId, count, cartItem.getChecked());

        // 创建分布式锁
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            // 尝试获取锁，不等待
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求处理中");
            }

            // 检查库存
            Goods goods = goodsService.getById(goodsId);
            if (goods == null) {
                return Result.fail("商品不存在");
            }

            // 检查库存
            if (!stockUtils.hasEnoughStock(goodsId, skuId, count)) {
                return Result.fail("库存不足");
            }

            // 从Redis获取购物车并更新
            String cartKey = CART_KEY_PREFIX + userId;

            // 获取购物车数据(使用Hash结构)
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
            List<ShopCartDTO> cartList;

            if (entries.isEmpty()) {
                // 从数据库加载购物车
                cartList = cartCacheService.loadCartFromDB(userId);
            } else {
                cartList = cartCacheService.convertHashToCartList(entries);
            }

            // 处理添加商品逻辑
            boolean success = updateCartWithNewItem(cartList, goods, cartItem);

            if (!success) {
                return Result.fail("库存不足");
            }

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

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
     * 更新购物车
     */
    @Override
    public Boolean updateCartWithNewItem(List<ShopCartDTO> cartList, Goods goods, CartItemDTO cartItem) {
        Long shopId = goods.getShopId();

        // 查找商铺购物车
        ShopCartDTO shopCart = cartList.stream()
                .filter(sc -> sc.getShopId().equals(shopId))
                .findFirst().orElse(null);

        if (shopCart == null) {
            Shop shop = shopService.getById(shopId);
            // 创建新的商铺购物车
            shopCart = new ShopCartDTO();
            shopCart.setShopId(shopId);
            shopCart.setShopName(shop.getName());
            if (shop.getImages() != null) {
                List<String> imageList = Arrays.asList(shop.getImages().split(","));
                shopCart.setShopImage(imageList);
            }
            shopCart.setItems(new ArrayList<>());
            cartList.add(shopCart);
        } else {
            // 查找是否已存在相同商品
            CartItemDTO existItem = shopCart.getItems().stream()
                    .filter(item -> item.getGoodsId().equals(cartItem.getGoodsId()) &&
                            (cartItem.getSkuId() == null ? item.getSkuId() == null
                                    : cartItem.getSkuId().equals(item.getSkuId())))
                    .findFirst().orElse(null);

            if (existItem != null) {
                Integer newCount = existItem.getCount() + cartItem.getCount();
                if (!stockUtils.hasEnoughStock(existItem.getGoodsId(), existItem.getSkuId(), newCount)) {
                    return false;
                }
                // 已存在则增加数量
                existItem.setCount(newCount);
                existItem.setChecked(cartItem.getChecked());
                return true;
            }
        }

        // 不存在则添加新商品
        CartItemDTO newItem = new CartItemDTO();
        newItem.setGoodsId(cartItem.getGoodsId());
        newItem.setSkuId(cartItem.getSkuId());
        newItem.setCount(cartItem.getCount());
        newItem.setChecked(cartItem.getChecked());

        // 设置商品信息
        newItem.setGoodsName(goods.getName());
        newItem.setGoodsImages(goods.getImages());
        newItem.setPrice(goods.getPrice());

        // 设置SKU信息
        if (cartItem.getSkuId() != null) {
            GoodSKU sku = goodSkuService.getById(cartItem.getSkuId());
            if (sku != null) {
                newItem.setSkuName(sku.getName());
                if (sku.getPrice() != null && sku.getPrice() > 0) {
                    newItem.setPrice(sku.getPrice());
                }
            }
        }

        shopCart.getItems().add(newItem);
        return true;
    }

    /**
     * 更新购物车商品数量
     */
    @Override
    public Result updateCartItemCount(Long userId, Long goodsId, Long skuId, Integer count) {
        if (userId == null || goodsId == null || count == null || count < 1) {
            return Result.fail("参数错误");
        }

        String requestId = generateRequestId(userId, "updateCartItemCount", goodsId, skuId, count);
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

            for (ShopCartDTO shopCart : cartList) {
                for (CartItemDTO item : shopCart.getItems()) {
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        item.setCount(count);
                        // 更新Redis缓存
                        cartCacheService.updateCartCache(userId, cartList);

                        // 异步更新数据库
                        cartCacheService.sendCartUpdateMessage(userId);

                        return Result.ok("更新完成");
                    }
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
    public Result removeFromCart(Long userId, Long goodsId, Long skuId) {
        // 生成请求标识
        String requestId = generateRequestId(userId, "removeFromCart", goodsId, skuId);
        RLock lock = redissonClient.getLock(IDEMPOTENT_KEY_PREFIX + requestId);

        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                return Result.fail("请求正在处理中，请稍后再试");
            }

            // 从Redis获取购物车(使用Hash结构)
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

            // 查找并移除购物车项
            boolean found = false;
            for (ShopCartDTO shopCart : cartList) {
                List<CartItemDTO> items = shopCart.getItems();
                for (CartItemDTO item : items) {
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        items.remove(item);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                return Result.fail("商品不存在");
            }

            // 清理空的商铺购物车
            cartList.removeIf(shopCart -> shopCart.getItems().isEmpty());

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok("移除购物车商品成功");
        } catch (Exception e) {
            log.error("移除购物车商品失败", e);
            return Result.fail("移除购物车商品失败");
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

            List<ShopCartDTO> cartList = new ArrayList<>();

            // 更新Redis缓存
            cartCacheService.updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            cartCacheService.sendCartUpdateMessage(userId);

            return Result.ok("购物车已清空");
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.fail("清空购物车失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
