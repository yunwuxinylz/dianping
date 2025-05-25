package com.dp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.Result;
import com.dp.dto.ShopDTO;
import com.dp.entity.Shop;
import com.dp.service.IShopService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    private final IShopService shopService;

    public ShopController(IShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * 根据id查询商铺信息
     *
     * @param id 商铺id
     * @return 商铺详情数据
     */
    @GetMapping("/detail/{id}")
    public Result queryShopById(@PathVariable Long id) {
        return shopService.queryById(id);
    }

    /**
     * 新增商铺信息
     *
     * @param shop 商铺数据
     * @return 商铺id
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    /**
     * 更新商铺信息
     *
     * @param shop 商铺数据
     * @return 无
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // 写入数据库

        return shopService.update(shop);
    }

    /**
     * 根据商铺类型分页查询商铺信息
     *
     * @param typeId    商铺类型
     * @param current   页码
     * @param sortBy    排序字段
     * @param sortOrder 排序方式（ASC/DESC）
     * @return 商铺列表
     */
    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam(value = "typeId") Integer typeId,
            @RequestParam(value = "current", required = false) Integer current,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "sortOrder", required = false) String sortOrder) {

        return shopService.shopByType(typeId, current, sortBy, pageSize, sortOrder);
    }

    /**
     * 根据商铺名称关键字分页查询商铺信息
     *
     * @param name    商铺名称关键字
     * @param current 页码
     * @return 商铺列表
     *         type: 'shop',
     *         current: 1,
     *         sortBy: 'price',
     *         sortOrder: 'desc'
     */
    @GetMapping("/search")
    public Result queryShopByName(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "current", required = false) Integer current) {
        // 处理排序字段映射
        if ("price".equals(sortBy)) {
            sortBy = "avg_price";
        }

        return shopService.shopByName(name, sortBy, sortOrder, pageSize, current);
    }

    /**
     * 更新销量
     *
     * @param id 商铺id
     * @return 无
     */
    @PutMapping("/sold")
    public Result putMethodName(@RequestBody ShopDTO shopDTO) {

        return shopService.updateSold(shopDTO);
    }

    /**
     * 商铺推荐
     * 
     * @param limit  数量
     * @param sortBy 排序字段
     * 
     * @return 商铺列表
     */
    @GetMapping("/recommend")
    public Result queryShopByRecommend(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "sortBy", required = false) String sortBy) {
        return shopService.shopRecommendList(limit, sortBy);
    }

    /**
     * 获取商铺类型统计数据
     * @return 商铺类型统计数据
     */
    @GetMapping("/type-stats")
    public Result getShopTypeStats() {
        return shopService.getShopTypeStats();
    }

}
