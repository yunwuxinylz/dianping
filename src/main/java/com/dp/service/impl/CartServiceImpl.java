package com.dp.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import com.dp.service.ICartService;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IShopService;
import com.dp.utils.StockUtils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 购物车服务实现类
 */
@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Resource
    private IGoodsService goodsService;

    @Resource
    private IGoodSKUService goodSkuService;

    @Resource
    private IShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 更新购物车商品数量
     */
    @Resource
    private StockUtils stockUtils;

    /**
     * 新增Redisson客户端依赖
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * Redis购物车KEY前缀
     */
    private static final String CART_KEY_PREFIX = "cart:user:";

    /**
     * Redis幂等性KEY前缀
     */
    private static final String IDEMPOTENT_KEY_PREFIX = "cart:idempotent:";

    /**
     * 购物车数据缓存时间(天)
     */
    private static final int CART_EXPIRE_DAYS = 7;

    /**
     * 幂等性锁等待时间(秒)
     */
    private static final long LOCK_WAIT_SECONDS = 0;

    /**
     * 幂等性锁持有时间(秒)
     */
    private static final long LOCK_LEASE_SECONDS = 3;

    // 缓存商品信息的Map，避免频繁查询数据库
    private final Map<Long, Goods> goodsCache = new HashMap<>();
    private final Map<Long, GoodSKU> skuCache = new HashMap<>();
    private final Map<Long, Shop> shopCache = new HashMap<>();

    /**
     * 获取用户购物车
     */
    @Override
    public Result getUserCart(Long userId) {
        // 先从Redis获取购物车
        String cartKey = CART_KEY_PREFIX + userId;
        String cartJson = stringRedisTemplate.opsForValue().get(cartKey);

        if (StrUtil.isBlank(cartJson)) {
            return Result.ok(loadCartFromDB(userId));
        }

        List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
        List<String> stockWarnings = new ArrayList<>();
        boolean hasStockWarning = checkCartItemStock(userId, cartList, stockWarnings);

        if (hasStockWarning) {
            // 更新Redis中的购物车数据
            updateCartCache(userId, cartList);
            return Result.ok(cartList, String.join("\n", stockWarnings));
        }

        return Result.ok(cartList);
    }

    // 提取检查库存的逻辑为单独方法
    private boolean checkCartItemStock(Long userId, List<ShopCartDTO> cartList, List<String> stockWarnings) {
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
     * 从数据库加载购物车并按商铺分组
     */
    private List<ShopCartDTO> loadCartFromDB(Long userId) {
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

        // 存入Redis
        updateCartCache(userId, cartList);

        return cartList;
    }

    // 预加载所有需要的数据，避免循环中多次查询
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
     * 生成请求标识
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

            // 检查库存 - 复用缓存数据
            Goods goods = getGoodsFromCache(goodsId);
            if (goods == null) {
                return Result.fail("商品不存在");
            }

            // 检查库存
            if (!stockUtils.hasEnoughStock(goodsId, skuId, count)) {
                return Result.fail("库存不足");
            }

            // 从Redis获取购物车并更新
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = StrUtil.isBlank(cartJson) ? new ArrayList<>()
                    : JSONUtil.toList(cartJson, ShopCartDTO.class);

            // 处理添加商品逻辑
            boolean success = updateCartWithNewItem(cartList, goods, cartItem);

            if (!success) {
                return Result.fail("库存不足");

            }
            // 更新Redis缓存
            updateCartCache(userId, cartList);

            // 异步更新数据库
            sendCartUpdateMessage(userId);

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

    // 提取更新购物车项的逻辑
    private Boolean updateCartWithNewItem(List<ShopCartDTO> cartList, Goods goods, CartItemDTO cartItem) {
        Long shopId = goods.getShopId();

        // 查找商铺购物车
        ShopCartDTO shopCart = cartList.stream()
                .filter(sc -> sc.getShopId().equals(shopId))
                .findFirst().orElse(null);

        if (shopCart == null) {
            Shop shop = getShopFromCache(shopId);
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
            GoodSKU sku = getSkuFromCache(cartItem.getSkuId());
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
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            if (StrUtil.isBlank(cartJson)) {
                return Result.fail("购物车为空");
            }

            List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);

            for (ShopCartDTO shopCart : cartList) {
                for (CartItemDTO item : shopCart.getItems()) {
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        item.setCount(count);
                        // 更新Redis缓存
                        updateCartCache(userId, cartList);

                        // 异步更新数据库
                        sendCartUpdateMessage(userId);

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

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            if (StrUtil.isBlank(cartJson)) {
                return Result.fail("购物车为空");
            }
            List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);

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
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

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
     * 清空用户购物车
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
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

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

    /**
     * 选中或取消选中购物车商品
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
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            if (StrUtil.isBlank(cartJson)) {
                return Result.fail("购物车为空");
            }
            List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);

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
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

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
     * 全选或取消全选
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
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            if (StrUtil.isBlank(cartJson)) {
                return Result.fail("购物车为空");
            }
            List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);

            // 更新所有商品的选中状态
            for (ShopCartDTO shopCart : cartList) {
                for (CartItemDTO item : shopCart.getItems()) {
                    item.setChecked(checked);
                }
            }

            // 更新Redis缓存
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

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
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            if (StrUtil.isBlank(cartJson)) {
                return Result.fail("购物车为空");
            }
            List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);

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
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

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
     * 移除已选中的商品
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
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            if (StrUtil.isBlank(cartJson)) {
                return Result.fail("购物车为空");
            }
            List<ShopCartDTO> cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);

            // 移除选中的商品
            for (ShopCartDTO shopCart : cartList) {
                shopCart.getItems().removeIf(CartItemDTO::getChecked);
            }

            // 清理空的商铺购物车
            cartList.removeIf(shopCart -> shopCart.getItems().isEmpty());

            // 更新Redis缓存
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

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

    /**
     * 更新Redis缓存中的购物车数据
     */
    private void updateCartCache(Long userId, List<ShopCartDTO> cartList) {
        String cartKey = CART_KEY_PREFIX + userId;
        String cartJson = cartList == null || cartList.isEmpty() ? "[]" : JSONUtil.toJsonStr(cartList);
        stringRedisTemplate.opsForValue().set(cartKey, cartJson, CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 发送购物车更新消息到MQ
     */
    private void sendCartUpdateMessage(Long userId) {
        try {
            rabbitTemplate.convertAndSend("cart.exchange", "cart.save", userId);
        } catch (Exception e) {
            log.error("购物车MQ消息发送失败：{}", userId);
        }
    }
}
