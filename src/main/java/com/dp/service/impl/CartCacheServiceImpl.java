package com.dp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.Cart;
import com.dp.entity.GoodSKU;
import com.dp.entity.Goods;
import com.dp.entity.Shop;
import com.dp.mapper.CartMapper;
import com.dp.service.ICartCacheService;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IShopService;
import com.dp.utils.StockUtils;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 购物车缓存服务实现类
 */
@Service
@Slf4j
public class CartCacheServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartCacheService {
    private final IGoodsService goodsService;
    private final IGoodSKUService goodSkuService;
    private final IShopService shopService;
    private final StringRedisTemplate stringRedisTemplate;
    private final StockUtils stockUtils;
    private final RabbitTemplate rabbitTemplate;

    // 缓存相关常量
    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final int CART_EXPIRE_DAYS = 7;

    // 缓存商品信息的Map，避免频繁查询数据库
    private final Map<Long, Goods> goodsCache = new HashMap<>();
    private final Map<Long, GoodSKU> skuCache = new HashMap<>();
    private final Map<Long, Shop> shopCache = new HashMap<>();

    public CartCacheServiceImpl(IGoodsService goodsService,
            IGoodSKUService goodSkuService,
            IShopService shopService,
            StringRedisTemplate stringRedisTemplate,
            StockUtils stockUtils,
            RabbitTemplate rabbitTemplate) {
        this.goodsService = goodsService;
        this.goodSkuService = goodSkuService;
        this.shopService = shopService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.stockUtils = stockUtils;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 获取用户购物车
     */
    @Override
    public Result getUserCart(Long userId) {
        // 从Redis获取购物车(使用Hash结构)
        String cartKey = CART_KEY_PREFIX + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);

        if (entries.isEmpty()) {
            // 如果Redis中没有数据，从数据库加载
            return Result.ok(loadCartFromDB(userId));
        }

        // 将Hash结构转换为购物车列表
        List<ShopCartDTO> cartList = convertHashToCartList(entries);
        List<String> stockWarnings = new ArrayList<>();
        boolean hasStockWarning = checkCartItemStock(userId, cartList, stockWarnings);

        if (hasStockWarning) {
            // 更新Redis中的购物车数据
            updateCartCache(userId, cartList);
            return Result.ok(cartList, String.join("\n", stockWarnings));
        }

        return Result.ok(cartList);
    }

    /**
     * 将Hash结构转换为购物车列表
     */
    @Override
    public List<ShopCartDTO> convertHashToCartList(Map<Object, Object> entries) {
        List<ShopCartDTO> cartList = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            ShopCartDTO shopCart = JSONUtil.toBean((String) entry.getValue(), ShopCartDTO.class);
            cartList.add(shopCart);
        }

        return cartList;
    }

    /**
     * 检查购物车商品库存
     */
    @Override
    public boolean checkCartItemStock(Long userId, List<ShopCartDTO> cartList, List<String> stockWarnings) {
        boolean hasStockWarning = false;

        for (ShopCartDTO shopCart : cartList) {
            for (CartItemDTO item : shopCart.getItems()) {
                // 获取商品库存
                Integer stock = (item.getSkuId() != null) ? stockUtils.getSkuStock(item.getSkuId())
                        : stockUtils.getGoodsStock(item.getGoodsId());

                // 如果库存不足，调整数量并添加提示
                if (stock < item.getCount()) {
                    hasStockWarning = true;
                    stockWarnings.add(String.format("%s的库存不足，已将数量调整为%d", item.getGoodsName(), stock));
                    item.setCount(stock);
                }
            }
        }

        // 如果有库存警告，异步更新数据库
        if (hasStockWarning) {
            sendCartUpdateMessage(userId);
        }

        return hasStockWarning;
    }

    /**
     * 从数据库加载购物车
     */
    @Override
    public List<ShopCartDTO> loadCartFromDB(Long userId) {
        // 从数据库查询购物车 - 使用索引优化
        List<Cart> carts = this.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getUpdateTime));

        // 创建购物车列表
        List<ShopCartDTO> cartList = new ArrayList<>();
        if (CollectionUtils.isEmpty(carts)) {
            updateCartCache(userId, cartList);
            return cartList;
        }

        // 批量预加载所需数据
        preloadCartData(carts);

        // 使用Map临时存储商铺购物车，用于快速查找
        Map<Long, ShopCartDTO> shopCartsMap = new HashMap<>();

        for (Cart cart : carts) {
            // 获取商品信息 - 使用缓存
            Goods goods = getGoodsFromCache(cart.getGoodsId());
            if (goods == null)
                continue;

            Long shopId = goods.getShopId();

            // 获取或创建商铺购物车
            ShopCartDTO shopCart = shopCartsMap.computeIfAbsent(shopId, k -> {
                ShopCartDTO newShopCart = new ShopCartDTO();
                newShopCart.setShopId(shopId);
                newShopCart.setItems(new ArrayList<>());

                // 设置商铺名称和图片 - 使用缓存
                Shop shop = getShopFromCache(shopId);
                if (shop != null) {
                    newShopCart.setShopName(shop.getName());
                    if (shop.getImages() != null) {
                        List<String> imageList = Arrays.asList(shop.getImages().split(","));
                        newShopCart.setShopImage(imageList);
                    }
                }

                cartList.add(newShopCart);
                return newShopCart;
            });

            // 创建购物车项
            CartItemDTO cartItemDTO = createCartItemDTO(cart, goods);
            shopCart.getItems().add(cartItemDTO);
        }

        // 存入Redis (使用Hash结构)
        updateCartCache(userId, cartList);

        return cartList;
    }

    /**
     * 预加载所有需要的数据，避免循环中多次查询
     */
    private void preloadCartData(List<Cart> carts) {
        Set<Long> goodsIds = new HashSet<>();
        Set<Long> skuIds = new HashSet<>();

        // 收集所有需要的IDs
        for (Cart cart : carts) {
            goodsIds.add(cart.getGoodsId());
            if (cart.getSkuId() != null) {
                skuIds.add(cart.getSkuId());
            }
        }

        // 批量加载商品
        if (!goodsIds.isEmpty()) {
            goodsService.listByIds(goodsIds).forEach(goods -> {
                goodsCache.put(goods.getId(), goods);
                shopCache.computeIfAbsent(goods.getShopId(), id -> shopService.getById(id));
            });
        }

        // 批量加载SKU
        if (!skuIds.isEmpty()) {
            goodSkuService.listByIds(skuIds).forEach(sku -> skuCache.put(sku.getId(), sku));
        }
    }

    // 从缓存获取Goods
    private Goods getGoodsFromCache(Long goodsId) {
        return goodsCache.computeIfAbsent(goodsId, id -> goodsService.getById(id));
    }

    // 从缓存获取Shop
    private Shop getShopFromCache(Long shopId) {
        return shopCache.computeIfAbsent(shopId, id -> shopService.getById(id));
    }

    // 从缓存获取SKU
    private GoodSKU getSkuFromCache(Long skuId) {
        return skuCache.computeIfAbsent(skuId, id -> goodSkuService.getById(id));
    }

    // 创建购物车项DTO
    private CartItemDTO createCartItemDTO(Cart cart, Goods goods) {
        CartItemDTO item = new CartItemDTO();
        item.setGoodsId(cart.getGoodsId());
        item.setSkuId(cart.getSkuId());
        item.setCount(cart.getCount());
        item.setChecked(cart.getChecked());

        // 设置商品信息
        item.setGoodsName(goods.getName());
        item.setGoodsImages(goods.getImages());
        item.setPrice(goods.getPrice());

        // 设置SKU信息
        if (cart.getSkuId() != null) {
            GoodSKU sku = getSkuFromCache(cart.getSkuId());
            if (sku != null) {
                item.setSkuName(sku.getName());
                // 如果有特定SKU价格，覆盖默认价格
                if (sku.getPrice() != null && sku.getPrice() > 0) {
                    item.setPrice(sku.getPrice());
                }
            }
        }

        return item;
    }

    /**
     * 更新购物车缓存
     */
    @Override
    public void updateCartCache(Long userId, List<ShopCartDTO> cartList) {
        String cartKey = CART_KEY_PREFIX + userId;

        // 首先删除现有数据(包括旧格式的String类型数据)
        stringRedisTemplate.delete(cartKey);

        if (cartList != null && !cartList.isEmpty()) {
            // 使用Hash结构保存每个商铺的购物车
            Map<String, String> shopEntries = new HashMap<>();

            for (ShopCartDTO shopCart : cartList) {
                String shopField = shopCart.getShopId().toString();
                shopEntries.put(shopField, JSONUtil.toJsonStr(shopCart));
            }

            // 批量写入Hash
            if (!shopEntries.isEmpty()) {
                stringRedisTemplate.opsForHash().putAll(cartKey, shopEntries);
            }
        }

        // 设置过期时间
        stringRedisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 同步Redis购物车数据到数据库 (适配Hash结构)
     */
    @Override
    public void syncCartToDatabase(Long userId) {
        // 获取Redis中的购物车数据
        String cartKey = CART_KEY_PREFIX + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);

        if (entries.isEmpty()) {
            log.info("用户[{}]的Redis购物车数据不存在", userId);
            // 清空MySQL中的购物车数据
            this.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            return;
        }

        // 转换Hash结构为购物车列表
        List<ShopCartDTO> cartList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            ShopCartDTO shopCart = JSONUtil.toBean((String) entry.getValue(), ShopCartDTO.class);
            cartList.add(shopCart);
        }

        // 如果购物车为空，清空数据库
        if (cartList.isEmpty()) {
            log.info("用户[{}]的Redis购物车为空，清空数据库购物车", userId);
            this.remove(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
            return;
        }

        // 使用批处理提高性能
        processBatchCartSync(userId, cartList);
    }

    /**
     * 批量处理购物车同步
     */
    private void processBatchCartSync(Long userId, List<ShopCartDTO> cartList) {
        // 查询现有购物车记录，只查询必要字段
        List<Cart> existingCarts = this.list(
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
            this.updateBatchById(cartsToUpdate);
        }

        if (!cartsToInsert.isEmpty()) {
            this.saveBatch(cartsToInsert);
        }

        if (!idsToRemove.isEmpty()) {
            this.removeByIds(idsToRemove);
        }

        log.info("用户[{}]购物车数据已同步到数据库，更新{}件，新增{}件，删除{}件",
                userId, cartsToUpdate.size(), cartsToInsert.size(), idsToRemove.size());
    }

    /**
     * 发送购物车更新消息
     */
    @Override
    public void sendCartUpdateMessage(Long userId) {
        try {
            rabbitTemplate.convertAndSend("cart.exchange", "cart.save", userId);
        } catch (Exception e) {
            log.error("购物车MQ消息发送失败：{}", userId);
        }
    }
}
