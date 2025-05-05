package com.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.GoodsDTO;
import com.dp.dto.Result;
import com.dp.entity.GoodSKU;
import com.dp.entity.Goods;
import com.dp.mapper.GoodsMapper;
import com.dp.service.IGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

// 商品Service实现
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Resource
    private GoodSKUServiceImpl goodSKUService;

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
}