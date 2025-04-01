package com.dp.controller;

import com.dp.dto.GoodsDTO;
import com.dp.dto.Result;
import com.dp.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 商品Controller
@RestController
@RequestMapping("/shop/goods")
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;

    @GetMapping("/{shopId}")
    public Result queryGoodsByShopId(@PathVariable("shopId") Long shopId) {
        List<GoodsDTO> goodsList = goodsService.queryGoodsByShopId(shopId);
        return Result.ok(goodsList);
    }

    @GetMapping("/detail/{id}")
    public Result queryGoodsById(@PathVariable("id") Long id) {
        GoodsDTO goods = goodsService.queryGoodsById(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(goods);
    }
}

