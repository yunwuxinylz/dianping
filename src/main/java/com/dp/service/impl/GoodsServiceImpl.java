package com.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.GoodsDTO;
import com.dp.entity.Goods;
import com.dp.mapper.GoodsMapper;
import com.dp.service.IGoodsService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// 商品Service实现
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Override
    public List<GoodsDTO> queryGoodsByShopId(Long shopId) {
        // 查询商品列表
        List<Goods> goodsList = query().eq("shop_id", shopId).eq("status", 1).list();
        // 转换为DTO
        return goodsList.stream().map(goods -> {
            GoodsDTO goodsDTO = BeanUtil.copyProperties(goods, GoodsDTO.class);
            goodsDTO.setImageList(Arrays.asList(goods.getImages().split(",")));
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
        goodsDTO.setImageList(Arrays.asList(goods.getImages().split(",")));
        return goodsDTO;
    }
}