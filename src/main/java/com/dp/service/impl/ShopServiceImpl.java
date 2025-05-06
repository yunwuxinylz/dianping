package com.dp.service.impl;

import static com.dp.utils.RedisConstants.CACHE_NULL_TTL;
import static com.dp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.dp.utils.RedisConstants.CACHE_SHOP_TTL;
import static com.dp.utils.RedisConstants.LOCK_SHOP_KEY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.Result;
import com.dp.dto.ShopDTO;
import com.dp.entity.Shop;
import com.dp.entity.ShopType;
import com.dp.mapper.ShopMapper;
import com.dp.service.IShopService;
import com.dp.service.IShopTypeService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IShopTypeService shopTypeService;

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

    @Transactional
    @Override
    public Result updateSold(ShopDTO shopDTO) {
        Long shopId = shopDTO.getId();
        Integer count = shopDTO.getCount();
        Shop shop = this.getById(shopId);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        if (shop.getSold() < count) {
            return Result.fail("库存不足");
        }

        // 更新数据库销量
        boolean success = this.update()
                .setSql("sold = sold +" + count) // 使用参数化方式传递count
                .eq("id", shopId)
                .ge("sold", count)
                .update();

        return success ? Result.ok(shop.getSold() + count) : Result.fail("更新销量失败");
    }

    @Override
    public Result shopByType(Integer typeId, Integer current, String sortBy, Integer pageSize, String sortOrder) {
        ShopType shopType = shopTypeService.getById(typeId);
        if (shopType == null) {
            return Result.fail("商铺类型不存在");
        }
        // 根据类型分页查询
        Page<Shop> page = this.query()
                .eq("type_id", typeId)
                .orderBy(StrUtil.isNotBlank(sortBy),
                        "ASC".equalsIgnoreCase(sortOrder),
                        sortBy)
                .page(new Page<>(current, pageSize));

        // 转DTO
        List<ShopDTO> shopDTOList = page.getRecords()
                .stream()
                .map(shop -> {
                    ShopDTO shopDTO = BeanUtil.copyProperties(shop, ShopDTO.class);
                    if (shop.getImages() != null) {
                        List<String> imageList = Arrays.asList(shop.getImages().split(","));
                        shopDTO.setImages(imageList);
                    }
                    shopDTO.setTypeName(shopType.getName());
                    return shopDTO;
                })
                .collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotal());
        map.put("list", shopDTOList);
        // 返回数据
        return Result.ok(map);
    }

    @Override
    public Result shopByName(String name, String sortBy, String sortOrder, Integer pageSize, Integer current) {
        // 根据类型分页查询
        Page<Shop> page = this.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .orderBy(StrUtil.isNotBlank(sortBy),
                        "ASC".equalsIgnoreCase(sortOrder),
                        sortBy)
                .page(new Page<>(current, pageSize));

        List<Long> typeIds = page.getRecords()
                .stream()
                .map(Shop::getTypeId)
                .collect(Collectors.toList());

        List<ShopType> shopTypes = shopTypeService.listByIds(typeIds);
        Map<Long, String> typeMap = shopTypes.stream()
                .collect(Collectors.toMap(ShopType::getId, ShopType::getName));

        // 转DTO
        List<ShopDTO> shopDTOList = page.getRecords()
                .stream()
                .map(shop -> {
                    ShopDTO shopDTO = BeanUtil.copyProperties(shop, ShopDTO.class);
                    if (shop.getImages() != null) {
                        List<String> imageList = Arrays.asList(shop.getImages().split(","));
                        shopDTO.setImages(imageList);
                    }
                    shopDTO.setTypeName(typeMap.get(shop.getTypeId()));
                    return shopDTO;
                })
                .collect(Collectors.toList());

        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotal());
        map.put("list", shopDTOList);
        // 返回数据
        return Result.ok(map);
    }

    @Override
    public Result shopRecommendList(Integer limit, String sortBy) {
        // 根据类型分页查询
        List<Shop> shops = this.query()
                .orderByDesc(sortBy)
                .last("limit " + limit)
                .list();

        List<Long> typeIds = shops.stream()
                .map(Shop::getTypeId)
                .collect(Collectors.toList());

        List<ShopType> shopTypes = shopTypeService.listByIds(typeIds);
        Map<Long, String> typeMap = shopTypes.stream()
                .collect(Collectors.toMap(ShopType::getId, ShopType::getName));

        // 转DTO
        List<ShopDTO> shopDTOList = shops.stream()
                .map(shop -> {
                    ShopDTO shopDTO = BeanUtil.copyProperties(shop, ShopDTO.class);
                    if (shop.getImages() != null) {
                        List<String> imageList = Arrays.asList(shop.getImages().split(","));
                        shopDTO.setImages(imageList);
                    }
                    shopDTO.setTypeName(typeMap.get(shop.getTypeId()));
                    return shopDTO;
                }).collect(Collectors.toList());
        return Result.ok(shopDTOList);
    }

}
