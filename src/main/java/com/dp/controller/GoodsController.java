package com.dp.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dp.dto.GoodsDTO;
import com.dp.dto.Result;
import com.dp.entity.Goods;
import com.dp.service.IGoodsService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

// 商品Controller
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;

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
        GoodsDTO goods = goodsService.queryGoodsById(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(goods);
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
        // 分页查询
        Page<Goods> page = goodsService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .orderBy(StrUtil.isNotBlank(sortBy),
                        "ASC".equalsIgnoreCase(sortOrder),
                        sortBy)
                .page(new Page<>(current, pageSize));

        // map
        Map<String, Object> map = new HashMap<>();
        map.put("total", page.getTotal());
        map.put("list", page.getRecords());

        return Result.ok(map);
    }

    /**
     * 获取推荐商品
     *
     * @param limit
     * @param sortBy
     * 
     * @return 商品列表
     */
    @GetMapping("/recommend")
    public Result queryGoodsByRecommend(@RequestParam Integer count) {

        // 随机获取商品
        List<Goods> goods = goodsService.query()
                .orderByDesc("sold")
                .last("LIMIT " + count)
                .list();
        
        // 转换为DTO
        List<GoodsDTO> goodsDTOList = goods.stream()
                .map(item -> {
                    GoodsDTO goodsDTO = BeanUtil.copyProperties(item, GoodsDTO.class);
                    goodsDTO.setImages(Arrays.asList(item.getImages().split(",")));
                    return goodsDTO;
                })
                .collect(Collectors.toList());

        return Result.ok(goodsDTOList);

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
