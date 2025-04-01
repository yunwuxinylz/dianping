package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.GoodsDTO;
import com.dp.entity.Goods;

import java.util.List;

// 商品Service接口
public interface IGoodsService extends IService<Goods> {
    List<GoodsDTO> queryGoodsByShopId(Long shopId);
    GoodsDTO queryGoodsById(Long id);
}
