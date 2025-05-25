package com.dp.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.GoodsDTO;
import com.dp.dto.GoodsSearchDTO;
import com.dp.dto.Result;
import com.dp.entity.GoodSKU;
import com.dp.entity.Goods;
import com.dp.entity.Shop;
import com.dp.mapper.GoodsMapper;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IShopService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

// 商品Service实现
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    private final IGoodSKUService goodSKUService;

    private final IShopService shopService;

    public GoodsServiceImpl(IGoodSKUService goodSKUService, IShopService shopService) {
        this.goodSKUService = goodSKUService;
        this.shopService = shopService;
    }

    /**
     * 根据店铺ID查询商品列表
     * 
     * @param shopId
     * @return
     */
    @Override
    public List<GoodsDTO> queryGoodsByShopId(Long shopId) {
        // 查询商品列表
        List<Goods> goodsList = query().eq("shop_id", shopId).eq("status", 1).list();
        // 转换为DTO
        return goodsList.stream().map(goods -> {
            GoodsDTO goodsDTO = BeanUtil.copyProperties(goods, GoodsDTO.class);
            goodsDTO.setImages(Arrays.asList(goods.getImages().split(",")));
            return goodsDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据商品ID查询商品详情
     * 
     * @param id
     * @return
     */
    @Override
    public GoodsDTO queryGoodsById(Long id) {
        // 查询商品
        Goods goods = getById(id);
        if (goods == null) {
            return null;
        }
        // 转换为DTO
        GoodsDTO goodsDTO = BeanUtil.copyProperties(goods, GoodsDTO.class);
        goodsDTO.setImages(Arrays.asList(goods.getImages().split(",")));

        // 查询商品的SKU列表
        List<GoodSKU> skuList = goodSKUService.query()
                .eq("goods_id", id)
                .list();
        goodsDTO.setSkus(skuList);

        return goodsDTO;
    }

    /**
     * 更新库存
     * 
     * @param goodsId
     * @param count
     * @param skuId
     * @return
     */
    @Transactional
    @Override
    public Result updateStock(Long goodsId, Integer count, Long skuId) {
        // 查询商品当前状态
        Goods goods = getById(goodsId);
        GoodSKU goodSKU = goodSKUService.getById(skuId);
        if (goods == null) {
            return Result.fail("商品不存在");
        }

        // 检查库存是否充足
        if (goodSKU.getStock() < count) {
            return Result.fail("库存不足");
        }

        // 更新sku的销量
        boolean success = goodSKUService.update()
                .setSql("stock = stock - " + count)
                .eq("id", skuId)
                .ge("stock", count)
                .update();

        if (success) {
            // 使用乐观锁更新库存
            boolean success2 = update()
                    .setSql("stock = stock - " + count)
                    .eq("id", goodsId)
                    .ge("stock", count) // 乐观锁条件
                    .update();

            if (success2) {
                Map<String, Object> map = new HashMap<>();
                map.put("goodStock", goods.getStock() - count);
                map.put("skuStock", goodSKU.getStock() - count);
                return Result.ok(map);
            }
        }
        return Result.fail("库存不足");
    }

    @Override
    public Result updateSold(Long goodsId, Integer count, Long skuId) {

        Goods goods = getById(goodsId);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        // 更新sku的销量
        boolean success = goodSKUService.update()
                .setSql("sold = sold + " + count)
                .eq("id", skuId)
                .update();

        if (success) {
            // 使用乐观锁更新销量
            boolean success2 = update()
                    .setSql("sold = sold + " + count)
                    .eq("id", goodsId)
                    .update();

            if (success2) {
                return Result.ok(goods.getSold() + count);
            }
        }
        return Result.fail("销量不足");
    }

    /**
     * 商品搜索列表
     * 
     * @param name
     * @param sortBy
     * @param sortOrder
     * @param pageSize
     * @param current
     * @return
     */
    @Override
    public Result goodsSearchList(String name, String sortBy, String sortOrder, Integer pageSize, Integer current) {
        Page<Goods> page = this.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .orderBy(StrUtil.isNotBlank(sortBy),
                        "ASC".equalsIgnoreCase(sortOrder),
                        sortBy)
                .page(new Page<>(current, pageSize));

        // 获取所有店铺ID
        List<Long> shopIds = page.getRecords().stream()
                .map(Goods::getShopId)
                .collect(Collectors.toList());

        // 一次性查询所有店铺信息
        Map<Long, Shop> shopMap = shopIds.isEmpty()
                ? new HashMap<>()
                : shopService.listByIds(shopIds).stream()
                        .collect(Collectors.toMap(Shop::getId, shop -> shop));

        // 转换为DTO
        List<GoodsSearchDTO> goodsDTOList = page.getRecords().stream()
                .map(item -> {
                    GoodsSearchDTO goodsDTO = BeanUtil.copyProperties(item, GoodsSearchDTO.class);
                    if (item.getImages() != null) {
                        goodsDTO.setImages(Arrays.asList(item.getImages().split(",")));
                    }
                    // 从Map中获取店铺信息
                    Shop shop = shopMap.get(item.getShopId());
                    if (shop != null) {
                        goodsDTO.setShopName(shop.getName());
                        goodsDTO.setAddress(shop.getAddress());
                        goodsDTO.setScore(shop.getScore());
                        goodsDTO.setDistance(shop.getDistance());

                    }
                    return goodsDTO;
                })
                .collect(Collectors.toList());

        return Result.ok(goodsDTOList, page.getTotal());
    }

    /**
     * 商品推荐列表
     * 
     * @param count
     * @return
     */
    @Override
    public Result goodsRecommendList(Integer count) {
        // 随机获取商品
        List<Goods> goods = this.query()
                .orderByDesc("sold")
                .last("LIMIT " + count)
                .list();

        // 转换为DTO
        List<GoodsSearchDTO> goodsDTOList = goods.stream()
                .map(item -> {
                    GoodsSearchDTO goodsDTO = BeanUtil.copyProperties(item, GoodsSearchDTO.class);
                    if (item.getImages() != null) {
                        goodsDTO.setImages(Arrays.asList(item.getImages().split(",")));
                    }
                    // 根据店铺id查询店铺详情
                    Shop shop = shopService.getById(item.getShopId());
                    if (shop != null) {
                        goodsDTO.setShopName(shop.getName());
                        goodsDTO.setAddress(shop.getAddress());
                    }
                    return goodsDTO;
                })
                .collect(Collectors.toList());

        return Result.ok(goodsDTOList);
    }

    @Override
    public Result getGoodsCount() {
        try {
            // 从数据库获取商品总数
            // 使用 MyBatis-Plus 的 selectCount 方法
            long count = count(); // 或者 goodsMapper.selectCount(null);
            return Result.ok(count); // 使用 Result.ok 而不是 Result.success
        } catch (Exception e) {
            // 记录日志会更好
            // log.error("获取商品总数失败", e);
            return Result.fail("获取商品总数失败"); // 使用 Result.fail 而不是 Result.error
        }
    }
}