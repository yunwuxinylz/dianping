package com.dp.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.GoodsDTO;
import com.dp.dto.Result;
import com.dp.entity.Goods;

// 商品Service接口
public interface IGoodsService extends IService<Goods> {
    List<GoodsDTO> queryGoodsByShopId(Long shopId);

    GoodsDTO queryGoodsById(Long id);

    Result goodsSearchList(String name, String sortBy, String sortOrder, Integer pageSize, Integer current);

    Result goodsRecommendList(Integer count);

    Result getGoodsCount(); // 添加获取商品总数的方法声明
}
