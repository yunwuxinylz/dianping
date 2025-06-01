package com.dp.utils;

import java.util.concurrent.TimeUnit;

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

    private final StringRedisTemplate stringRedisTemplate;

    private final IGoodsService goodsService;

    private final IGoodSKUService goodSkuService;

    public StockUtils(StringRedisTemplate stringRedisTemplate, IGoodsService goodsService,
            IGoodSKUService goodSkuService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.goodsService = goodsService;
        this.goodSkuService = goodSkuService;
    }

    /**
     * Redis商品库存KEY前缀
     */
    public static final String GOODS_STOCK_KEY_PREFIX = "stock:goods:";

    /**
     * Redis SKU库存KEY前缀
     */
    public static final String SKU_STOCK_KEY_PREFIX = "stock:sku:";

    /**
     * 库存数据缓存时间(天)
     */
    public static final int STOCK_EXPIRE_DAYS = 1;

    /**
     * 获取商品库存(优先从Redis获取，不存在则从数据库获取并同步到Redis)
     *
     * @param goodsId 商品ID
     * @return 商品库存数量，商品不存在时返回0
     */
    public Integer getGoodsStock(Long goodsId) {
        if (goodsId == null) {
            return 0;
        }

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
        if (skuId == null) {
            return 0;
        }

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
        if (goodsId == null || count == null || count <= 0) {
            return false;
        }

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

    /**
     * 扣减商品和SKU库存
     * 
     * @param goodsId 商品ID
     * @param skuId   SKU ID，可为null
     * @param count   扣减数量
     * @return true: 扣减成功，false: 扣减失败
     */
    public boolean decreaseStock(Long goodsId, Long skuId, Integer count) {
        if (goodsId == null || count == null || count <= 0) {
            return false;
        }

        // 先检查库存是否充足
        if (!hasEnoughStock(goodsId, skuId, count)) {
            return false;
        }

        try {
            // 扣减商品库存
            boolean success = goodsService.update()
                    .setSql("stock = stock - " + count)
                    .eq("id", goodsId)
                    .ge("stock", count) // 确保库存充足
                    .update();

            if (!success) {
                return false;
            }

            // 如果有SKU，扣减SKU库存
            if (skuId != null) {
                boolean skuSuccess = goodSkuService.update()
                        .setSql("stock = stock - " + count)
                        .eq("id", skuId)
                        .ge("stock", count) // 确保库存充足
                        .update();

                if (!skuSuccess) {
                    // 回滚商品库存
                    goodsService.update()
                            .setSql("stock = stock + " + count)
                            .eq("id", goodsId)
                            .update();
                    return false;
                }
            }

            // 删除缓存
            deleteGoodsStockCache(goodsId);
            if (skuId != null) {
                deleteSkuStockCache(skuId);
            }

            return true;
        } catch (Exception e) {
            log.error("扣减库存失败，商品ID：{}，SKU ID：{}，数量：{}", goodsId, skuId, count, e);
            return false;
        }
    }

    /**
     * 恢复商品和SKU库存
     * 
     * @param goodsId 商品ID
     * @param skuId   SKU ID，可为null
     * @param count   恢复数量
     * @return true: 恢复成功，false: 恢复失败
     */
    public boolean increaseStock(Long goodsId, Long skuId, Integer count) {
        if (goodsId == null || count == null || count <= 0) {
            return false;
        }

        try {
            // 恢复商品库存
            goodsService.update()
                    .setSql("stock = stock + " + count)
                    .eq("id", goodsId)
                    .update();

            // 如果有SKU，恢复SKU库存
            if (skuId != null) {
                goodSkuService.update()
                        .setSql("stock = stock + " + count)
                        .eq("id", skuId)
                        .update();
            }

            // 删除缓存
            deleteGoodsStockCache(goodsId);
            if (skuId != null) {
                deleteSkuStockCache(skuId);
            }

            return true;
        } catch (Exception e) {
            log.error("恢复库存失败，商品ID：{}，SKU ID：{}，数量：{}", goodsId, skuId, count, e);
            return false;
        }
    }

    /**
     * 更新商品库存缓存
     *
     * @param goodsId 商品ID
     * @param stock   新的库存数量
     */
    public void updateGoodsStockCache(Long goodsId, Integer stock) {
        if (goodsId == null || stock == null) {
            return;
        }
        String key = GOODS_STOCK_KEY_PREFIX + goodsId;
        stringRedisTemplate.opsForValue().set(key, stock.toString(), STOCK_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("更新商品库存缓存，商品ID：{}，新库存：{}", goodsId, stock);
    }

    /**
     * 更新SKU库存缓存
     *
     * @param skuId SKU ID
     * @param stock 新的库存数量
     */
    public void updateSkuStockCache(Long skuId, Integer stock) {
        if (skuId == null || stock == null) {
            return;
        }
        String key = SKU_STOCK_KEY_PREFIX + skuId;
        stringRedisTemplate.opsForValue().set(key, stock.toString(), STOCK_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("更新SKU库存缓存，SKU ID：{}，新库存：{}", skuId, stock);
    }

    /**
     * 删除商品库存缓存
     *
     * @param goodsId 商品ID
     */
    public void deleteGoodsStockCache(Long goodsId) {
        if (goodsId == null) {
            return;
        }
        String key = GOODS_STOCK_KEY_PREFIX + goodsId;
        stringRedisTemplate.delete(key);
        log.info("删除商品库存缓存，商品ID：{}", goodsId);
    }

    /**
     * 删除SKU库存缓存
     *
     * @param skuId SKU ID
     */
    public void deleteSkuStockCache(Long skuId) {
        if (skuId == null) {
            return;
        }
        String key = SKU_STOCK_KEY_PREFIX + skuId;
        stringRedisTemplate.delete(key);
        log.info("删除SKU库存缓存，SKU ID：{}", skuId);
    }
}