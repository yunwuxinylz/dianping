package com.dp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.GoodsDTO;
import com.dp.dto.Result;
import com.dp.service.IGoodsService;

// 商品Controller
@RestController
@RequestMapping("/goods")
public class GoodsController {
    private final IGoodsService goodsService;

    public GoodsController(IGoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * 根据店铺id查询商品列表
     * 
     * @param shopId
     * @return
     */
    @GetMapping("/list")
    public Result queryGoodsByShopId(@RequestParam("shopId") Long shopId) {
        List<GoodsDTO> goodsList = goodsService.queryGoodsByShopId(shopId);
        return Result.ok(goodsList);
    }

    /**
     * 根据商品id查询商品详情
     * 
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Result queryGoodsById(@PathVariable("id") Long id) {
        GoodsDTO goodsDTO = goodsService.queryGoodsById(id);
        if (goodsDTO == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(goodsDTO);
    }

    /**
     * 根据关键字搜索商品
     * 
     * @param keyword
     * @return
     */
    @GetMapping("/search")
    public Result searchGoods(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "pageSize") Integer pageSize,
            @RequestParam(value = "current") Integer current) {

        // 处理排序字段映射
        if ("avg_price".equals(sortBy)) {
            sortBy = "price";
        }
        return goodsService.goodsSearchList(name, sortBy, sortOrder, pageSize, current);
    }

    /**
     * 获取推荐商品
     */
    @GetMapping("/recommend")
    public Result queryGoodsByRecommend(@RequestParam Integer count) {

        return goodsService.goodsRecommendList(count);
    }

    /**
     * 更新库存
     * 
     * @param goodsId
     * @param goodsCount
     * @return
     */
    @PutMapping("/stock")
    public Result updateStock(@RequestBody Map<String, Integer> goodsIdAndCount) {
        Long goodsId = Long.valueOf(goodsIdAndCount.get("goodsId"));
        Integer goodsCount = goodsIdAndCount.get("count");
        Long skuId = Long.valueOf(goodsIdAndCount.get("skuId"));
        if (goodsId == null || goodsCount == null || skuId == null) {
            return Result.fail("参数错误");
        }

        return goodsService.updateStock(goodsId, goodsCount, skuId);
    }

    /**
     * 更新销量
     *
     * @param goodsId
     * @param goodsCount
     * @return
     */
    @PutMapping("/sold")
    public Result updateSold(@RequestBody Map<String, Integer> goodsIdAndCount) {
        Long goodsId = Long.valueOf(goodsIdAndCount.get("goodsId"));
        Integer goodsCount = goodsIdAndCount.get("count");
        Long skuId = Long.valueOf(goodsIdAndCount.get("skuId"));
        if (goodsId == null || goodsCount == null || skuId == null) {
            return Result.fail("参数错误");
        }

        return goodsService.updateSold(goodsId, goodsCount, skuId);
    }
}
