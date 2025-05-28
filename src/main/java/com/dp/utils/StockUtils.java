package com.dp.utils;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dp.entity.GoodSKU;
import com.dp.entity.Goods;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;

import lombok.extern.slf4j.Slf4j;

/**
 * 商品库存工具类
 */
@Component
@Slf4j
public class StockUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IGoodsService goodsService;

    @Resource
    private IGoodSKUService goodSkuService;

    /**
     * Redis商品库存KEY前缀
     */
    private static final String GOODS_STOCK_KEY_PREFIX = "goods:stock:";

    /**
     * Redis SKU库存KEY前缀
     */
    private static final String SKU_STOCK_KEY_PREFIX = "sku:stock:";

    /**
     * 库存数据缓存时间(天)
     */
    private static final int STOCK_EXPIRE_DAYS = 1;

    /**
     * 获取商品库存(优先从Redis获取，不存在则从数据库获取并同步到Redis)
     *
     * @param goodsId 商品ID
     * @return 商品库存数量，商品不存在时返回0
     */
    public Integer getGoodsStock(Long goodsId) {
        String key = GOODS_STOCK_KEY_PREFIX + goodsId;
        String goodsStockStr = stringRedisTemplate.opsForValue().get(key);

        if (goodsStockStr != null) {
            return Integer.parseInt(goodsStockStr);
        }

        // Redis中不存在，从数据库获取
        Goods goods = goodsService.getById(goodsId);
        if (goods != null && goods.getStock() != null) {
            // 同步到Redis
            stringRedisTemplate.opsForValue().set(key, goods.getStock().toString(), STOCK_EXPIRE_DAYS, TimeUnit.DAYS);
            return goods.getStock();
        }

        return 0;
    }

    /**
     * 获取SKU库存(优先从Redis获取，不存在则从数据库获取并同步到Redis)
     *
     * @param skuId SKU ID
     * @return SKU库存数量，SKU不存在时返回0
     */
    public Integer getSkuStock(Long skuId) {
        String key = SKU_STOCK_KEY_PREFIX + skuId;
        String skuStockStr = stringRedisTemplate.opsForValue().get(key);

        if (skuStockStr != null) {
            return Integer.parseInt(skuStockStr);
        }

        // Redis中不存在，从数据库获取
        GoodSKU sku = goodSkuService.getById(skuId);
        if (sku != null && sku.getStock() != null) {
            // 同步到Redis
            stringRedisTemplate.opsForValue().set(key, sku.getStock().toString(), STOCK_EXPIRE_DAYS, TimeUnit.DAYS);
            return sku.getStock().intValue();
        }

        return 0;
    }

    /**
     * 检查商品是否有足够库存
     *
     * @param goodsId 商品ID
     * @param skuId   SKU ID，可为null
     * @param count   需要的数量
     * @return true: 库存充足，false: 库存不足
     */
    public boolean hasEnoughStock(Long goodsId, Long skuId, Integer count) {
        // 检查商品库存
        Integer goodsStock = getGoodsStock(goodsId);
        if (goodsStock < count) {
            log.warn("商品库存不足，商品ID：{}，当前库存：{}，请求数量：{}",
                    goodsId, goodsStock, count);
            return false;
        }

        // 如果有SKU，检查SKU库存
        if (skuId != null) {
            Integer skuStock = getSkuStock(skuId);
            if (skuStock < count) {
                log.warn("商品SKU库存不足，商品ID：{}，SKU ID：{}，当前库存：{}，请求数量：{}",
                        goodsId, skuId, skuStock, count);
                return false;
            }
        }

        return true;
    }
}