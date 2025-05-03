package com.dp.service.impl;

import static com.dp.utils.RedisConstants.CACHE_NULL_TTL;
import static com.dp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.dp.utils.RedisConstants.CACHE_SHOP_TTL;
import static com.dp.utils.RedisConstants.LOCK_SHOP_KEY;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.Result;
import com.dp.dto.ShopDTO;
import com.dp.entity.Shop;
import com.dp.mapper.ShopMapper;
import com.dp.service.IShopService;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 判断命中的是否是空值
        if (shopJson != null && shopJson.equals("")) {
            return Result.fail("店铺不存在");
        }

        // 缓存击穿
        // 设置互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // if (!isLock) {
            // Thread.sleep(50);
            // }
            while (!isLock) {
                Thread.sleep(50);
            }
            // 缓存重建
            shop = getById(id);

            if ((shop == null)) {
                // 将空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("店铺不存在");
            }
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 释放锁
            unlock(lockKey);
        }

        // 将店铺信息写入redis

        return Result.ok(shop);
    }

    private void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);

    }

    private boolean tryLock(String lockKey) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    @Override
    public Result update(Shop shop) {
        // 更新数据库

        Long id = shop.getId();
        // 判断是否存在
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        updateById(shop);
        // 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        // 返回

        return Result.ok();
    }

    @Override
    public Result updateSold(ShopDTO shopDTO) {
        Long shopId = shopDTO.getId();
        Integer count = shopDTO.getCount();
        
        // 更新数据库销量
        boolean success = this.update()
            .setSql("sold = sold + {0}", count)  // 使用参数化方式传递count
            .eq("id", shopId)
            .update();
            
        return success ? Result.ok() : Result.fail("更新销量失败");
    }
}
