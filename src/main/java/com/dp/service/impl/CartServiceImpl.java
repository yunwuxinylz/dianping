package com.dp.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
     * Redis购物车KEY前缀
     */
    private static final String CART_KEY_PREFIX = "cart:user:";

    /**
     * 购物车数据缓存时间(天)
     */
    private static final int CART_EXPIRE_DAYS = 7;

    /**
     * 获取用户购物车
     */
    @Override
    public Result getUserCart(Long userId) {
        // 先从Redis获取购物车
        String cartKey = CART_KEY_PREFIX + userId;
        String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
        List<ShopCartDTO> cartList = new ArrayList<>();
        List<String> stockWarnings = new ArrayList<>();

        if (StrUtil.isNotBlank(cartJson)) {
            cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
        } else {
            cartList = loadCartFromDB(userId);
        }

        boolean hasStockWarning = false;

        // 检查每个商品的库存
        for (ShopCartDTO shopCart : cartList) {
            for (CartItemDTO item : shopCart.getItems()) {
                // 获取商品库存
                Integer stock;
                if (item.getSkuId() != null) {
                    stock = stockUtils.getSkuStock(item.getSkuId());
                } else {
                    stock = stockUtils.getGoodsStock(item.getGoodsId());
                }

                // 如果库存不足，调整数量并添加提示
                if (stock < item.getCount()) {
                    hasStockWarning = true;
                    String message = String.format("%s的库存不足，已将数量调整为%d",
                            item.getGoodsName(), stock);
                    stockWarnings.add(message);

                    // 更新购物车中的数量
                    item.setCount(stock);

                    // 同步到数据库
                    Cart cart = this.getOne(new LambdaQueryWrapper<Cart>()
                            .eq(Cart::getUserId, userId)
                            .eq(Cart::getGoodsId, item.getGoodsId())
                            .eq(item.getSkuId() != null, Cart::getSkuId, item.getSkuId()));
                    if (cart != null) {
                        cart.setCount(stock);
                        this.updateById(cart);
                    }
                }
            }
        }

        if (hasStockWarning) {
            // 更新Redis中的购物车数据
            stringRedisTemplate.opsForValue().set(cartKey, JSONUtil.toJsonStr(cartList),
                    CART_EXPIRE_DAYS, TimeUnit.DAYS);
            return Result.ok(cartList, (String.join("\n", stockWarnings)));
        }

        return Result.ok(cartList);
    }

    /**
     * 从数据库加载购物车并按商铺分组
     */
    private List<ShopCartDTO> loadCartFromDB(Long userId) {
        // 从数据库查询购物车
        List<Cart> carts = this.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getUpdateTime));

        // 创建购物车列表
        List<ShopCartDTO> cartList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(carts)) {
            // 使用Map临时存储商铺购物车，用于快速查找
            Map<Long, ShopCartDTO> shopCartsMap = new HashMap<>();

            for (Cart cart : carts) {
                // 查询商品信息
                Goods goods = goodsService.getById(cart.getGoodsId());
                if (goods == null)
                    continue;

                Long shopId = goods.getShopId();

                // 获取或创建商铺购物车
                ShopCartDTO shopCart = shopCartsMap.computeIfAbsent(shopId, k -> {
                    ShopCartDTO newShopCart = new ShopCartDTO();
                    newShopCart.setShopId(shopId);
                    newShopCart.setItems(new ArrayList<>());

                    // 设置商铺名称
                    Shop shop = shopService.getById(shopId);
                    if (shop != null) {
                        newShopCart.setShopName(shop.getName());
                    }

                    // 添加到购物车列表
                    cartList.add(newShopCart);
                    return newShopCart;
                });

                // 创建购物车项
                CartItemDTO cartItemDTO = new CartItemDTO();
                cartItemDTO.setGoodsId(cart.getGoodsId());
                cartItemDTO.setSkuId(cart.getSkuId());
                cartItemDTO.setCount(cart.getCount());
                cartItemDTO.setChecked(cart.getChecked());

                // 设置商品信息
                cartItemDTO.setGoodsName(goods.getName());
                cartItemDTO.setGoodsImages(goods.getImages());
                cartItemDTO.setPrice(goods.getPrice());

                // 设置SKU信息
                if (cart.getSkuId() != null) {
                    GoodSKU sku = goodSkuService.getById(cart.getSkuId());
                    if (sku != null) {
                        cartItemDTO.setSkuName(sku.getName());
                        // 如果有特定SKU价格，覆盖默认价格
                        if (sku.getPrice() != null && sku.getPrice() > 0) {
                            cartItemDTO.setPrice(sku.getPrice());
                        }
                    }
                }

                // 添加到商铺购物车
                shopCart.getItems().add(cartItemDTO);
            }
        }

        // 存入Redis
        String cartKey = CART_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(cartKey, JSONUtil.toJsonStr(cartList), CART_EXPIRE_DAYS,
                TimeUnit.DAYS);

        return cartList;
    }

    /**
     * 添加商品到购物车
     */
    @Override
    @Transactional
    public Result addToCart(Long userId, CartItemDTO cartItem) {
        try {
            // 参数校验
            if (userId == null || cartItem == null || cartItem.getGoodsId() == null || cartItem.getCount() == null) {
                return Result.fail("参数错误");
            }

            // 查询商品信息
            Goods goods = goodsService.getById(cartItem.getGoodsId());
            if (goods == null) {
                return Result.fail("商品不存在");
            }

            // 检查商品库存（优先从Redis获取）
            Integer goodsStock = stockUtils.getGoodsStock(cartItem.getGoodsId());
            if (goodsStock < cartItem.getCount()) {
                log.warn("商品库存不足，商品ID：{}，当前库存：{}，请求数量：{}",
                        cartItem.getGoodsId(), goodsStock, cartItem.getCount());
                return Result.fail("商品库存不足");
            }

            // 如果有SKU，检查SKU库存（优先从Redis获取）
            if (cartItem.getSkuId() != null) {
                Integer skuStock = stockUtils.getSkuStock(cartItem.getSkuId());
                if (skuStock < cartItem.getCount()) {
                    log.warn("商品SKU库存不足，商品ID：{}，SKU ID：{}，当前库存：{}，请求数量：{}",
                            cartItem.getGoodsId(), cartItem.getSkuId(), skuStock, cartItem.getCount());
                    return Result.fail("商品SKU库存不足");
                }
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = new ArrayList<>();

            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
            }

            // 获取商铺ID和名称
            Long shopId = goods.getShopId();
            String shopName = null;
            Shop shop = shopService.getById(shopId);
            if (shop != null) {
                shopName = shop.getName();
            }

            // 查找商铺购物车
            ShopCartDTO shopCart = cartList.stream()
                    .filter(sc -> sc.getShopId().equals(shopId))
                    .findFirst()
                    .orElse(null);

            if (shopCart == null) {
                // 创建新的商铺购物车
                shopCart = new ShopCartDTO();
                shopCart.setShopId(shopId);
                shopCart.setShopName(shopName);
                shopCart.setItems(new ArrayList<>());
                cartList.add(shopCart);
            }

            // 查找是否已存在相同商品
            CartItemDTO existItem = shopCart.getItems().stream()
                    .filter(item -> item.getGoodsId().equals(cartItem.getGoodsId()) &&
                            (cartItem.getSkuId() == null ? item.getSkuId() == null
                                    : cartItem.getSkuId().equals(item.getSkuId())))
                    .findFirst()
                    .orElse(null);

            if (existItem != null) {
                // 已存在则增加数量
                int newCount = existItem.getCount() + cartItem.getCount();

                // 再次检查总数量是否超过库存
                if (goodsStock < newCount) {
                    log.warn("商品库存不足，商品ID：{}，当前库存：{}，购物车已有：{}，新增请求：{}",
                            cartItem.getGoodsId(), goodsStock, existItem.getCount(), cartItem.getCount());
                    return Result.fail("商品库存不足");
                }

                // 如果有SKU，再次检查SKU总数量是否超过库存
                if (cartItem.getSkuId() != null) {
                    Integer skuStock = stockUtils.getSkuStock(cartItem.getSkuId());
                    if (skuStock < newCount) {
                        log.warn("商品SKU库存不足，商品ID：{}，SKU ID：{}，当前库存：{}，购物车已有：{}，新增请求：{}",
                                cartItem.getGoodsId(), cartItem.getSkuId(), skuStock, existItem.getCount(),
                                cartItem.getCount());
                        return Result.fail("商品SKU库存不足");
                    }
                }

                existItem.setCount(newCount);
                existItem.setChecked(cartItem.getChecked());
            } else {
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
                        // 如果有特定SKU价格，覆盖默认价格
                        if (sku.getPrice() != null && sku.getPrice() > 0) {
                            newItem.setPrice(sku.getPrice());
                        }
                    }
                }

                shopCart.getItems().add(newItem);
            }

            // 更新Redis缓存
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("添加购物车失败", e);
            return Result.fail("添加购物车失败");
        }
    }

    @Override
    @Transactional
    public Result updateCartItemCount(Long userId, Long goodsId, Long skuId, Integer count) {
        try {
            // 参数校验
            if (userId == null || goodsId == null || count == null || count < 1) {
                return Result.fail("参数错误");
            }

            // 检查库存
            if (!stockUtils.hasEnoughStock(goodsId, skuId, count)) {
                return Result.fail("商品库存不足");
            }

            // 检查商品库存（优先从Redis获取）
            Integer goodsStock = stockUtils.getGoodsStock(goodsId);
            if (goodsStock < count) {
                log.warn("商品库存不足，商品ID：{}，当前库存：{}，请求数量：{}",
                        goodsId, goodsStock, count);
                return Result.fail("商品库存不足");
            }

            // 如果有SKU，检查SKU库存（优先从Redis获取）
            if (skuId != null) {
                Integer skuStock = stockUtils.getSkuStock(skuId);
                if (skuStock < count) {
                    log.warn("商品SKU库存不足，商品ID：{}，SKU ID：{}，当前库存：{}，请求数量：{}",
                            goodsId, skuId, skuStock, count);
                    return Result.fail("商品SKU库存不足");
                }
            }

            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);

            List<ShopCartDTO> cartList = new ArrayList<>();
            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
            }

            // 查找并更新购物车项
            boolean found = false;
            for (ShopCartDTO shopCart : cartList) {
                for (CartItemDTO item : shopCart.getItems()) {
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        item.setCount(count);
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
            log.error("更新购物车数量失败", e);
            return Result.fail("更新购物车数量失败");
        }
    }

    /**
     * 移除购物车商品
     */
    @Override
    @Transactional
    public Result removeFromCart(Long userId, Long goodsId, Long skuId) {
        try {
            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = new ArrayList<>();
            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
            }

            // 查找并移除购物车项
            boolean found = false;
            for (ShopCartDTO shopCart : cartList) {
                List<CartItemDTO> items = shopCart.getItems();
                for (int i = 0; i < items.size(); i++) {
                    CartItemDTO item = items.get(i);
                    if (item.getGoodsId().equals(goodsId) &&
                            ((skuId == null && item.getSkuId() == null) ||
                                    (skuId != null && skuId.equals(item.getSkuId())))) {
                        items.remove(i);
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

            // 清理空的商铺购物车
            cartList.removeIf(shopCart -> shopCart.getItems().isEmpty());

            // 更新Redis缓存
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("移除购物车商品失败", e);
            return Result.fail("移除购物车商品失败");
        }
    }

    /**
     * 清空用户购物车
     */
    @Override
    @Transactional
    public Result clearCart(Long userId) {
        try {
            List<ShopCartDTO> cartList = new ArrayList<>();

            // 更新Redis缓存
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("清空购物车失败", e);
            return Result.fail("清空购物车失败");
        }
    }

    /**
     * 选中或取消选中购物车商品
     */
    @Override
    @Transactional
    public Result checkCartItem(Long userId, Long goodsId, Long skuId, Boolean checked) {
        try {
            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = new ArrayList<>();
            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
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
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("更新购物车商品选中状态失败", e);
            return Result.fail("更新购物车商品选中状态失败");
        }
    }

    /**
     * 全选或取消全选
     */
    @Override
    @Transactional
    public Result checkAllItems(Long userId, Boolean checked) {
        try {
            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = new ArrayList<>();
            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
            }

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
        }
    }

    /**
     * 选中或取消选中指定商铺的所有商品
     */
    @Override
    @Transactional
    public Result checkShopItems(Long userId, Long shopId, Boolean checked) {
        try {
            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = new ArrayList<>();
            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
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
            updateCartCache(userId, cartList);

            // 发送消息到MQ，异步更新数据库
            sendCartUpdateMessage(userId);

            return Result.ok();
        } catch (Exception e) {
            log.error("更新商铺商品选中状态失败", e);
            return Result.fail("更新商铺商品选中状态失败");
        }
    }

    /**
     * 移除已选中的商品
     */
    @Override
    @Transactional
    public Result removeCheckedItems(Long userId) {
        try {
            // 从Redis获取购物车
            String cartKey = CART_KEY_PREFIX + userId;
            String cartJson = stringRedisTemplate.opsForValue().get(cartKey);
            List<ShopCartDTO> cartList = new ArrayList<>();
            if (StrUtil.isNotBlank(cartJson)) {
                cartList = JSONUtil.toList(cartJson, ShopCartDTO.class);
            }

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

            return Result.ok();
        } catch (Exception e) {
            log.error("移除选中的商品失败", e);
            return Result.fail("移除选中的商品失败");
        }
    }

    /**
     * 更新Redis缓存中的购物车数据
     */
    private void updateCartCache(Long userId, List<ShopCartDTO> cartList) {
        String cartKey = CART_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(cartKey, JSONUtil.toJsonStr(cartList), CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 发送购物车更新消息到MQ
     */
    private void sendCartUpdateMessage(Long userId) {
        rabbitTemplate.convertAndSend("cart.exchange", "cart.save", userId);
    }

}
