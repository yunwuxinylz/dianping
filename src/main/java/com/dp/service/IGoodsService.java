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
    Result updateStock(Long goodsId, Integer count, Long skuId);
    Result updateSold(Long goodsId, Integer count, Long skuId);
}
