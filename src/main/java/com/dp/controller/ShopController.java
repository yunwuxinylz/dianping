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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 
 */
@RestController
@RequestMapping("/shop")
@Tag(name = "商铺管理", description = "商铺相关的API接口")
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
    @Operation(summary = "查询商铺详情", description = "根据商铺ID查询商铺详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result queryShopById(
            @Parameter(description = "商铺ID") @PathVariable Long id) {
        return shopService.queryById(id);
    }

    /**
     * 新增商铺信息
     *
     * @param shop 商铺数据
     * @return 商铺id
     */
    @PostMapping
    @Operation(summary = "新增商铺", description = "创建一个新的商铺")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result saveShop(
            @Parameter(description = "商铺信息") @RequestBody Shop shop) {
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
    @Operation(summary = "更新商铺", description = "更新商铺信息")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result updateShop(
            @Parameter(description = "商铺信息") @RequestBody Shop shop) {
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
    @Operation(summary = "按类型查询商铺", description = "根据商铺类型分页查询商铺信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result queryShopByType(
            @Parameter(description = "商铺类型ID") @RequestParam(value = "typeId") Integer typeId,
            @Parameter(description = "当前页码") @RequestParam(value = "current", required = false) Integer current,
            @Parameter(description = "排序字段") @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "每页大小") @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @Parameter(description = "排序方式(ASC/DESC)") @RequestParam(value = "sortOrder", required = false) String sortOrder) {

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
    @Operation(summary = "搜索商铺", description = "根据商铺名称关键字分页查询商铺信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result queryShopByName(
            @Parameter(description = "商铺名称关键字") @RequestParam(value = "name") String name,
            @Parameter(description = "排序字段") @RequestParam(value = "sortBy", required = false) String sortBy,
            @Parameter(description = "排序方式(ASC/DESC)") @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @Parameter(description = "每页大小") @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @Parameter(description = "当前页码") @RequestParam(value = "current", required = false) Integer current) {
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
    @Operation(summary = "更新商铺销量", description = "更新商铺销量信息")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result putMethodName(
            @Parameter(description = "商铺销量信息") @RequestBody ShopDTO shopDTO) {

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
    @Operation(summary = "商铺推荐", description = "获取推荐商铺列表")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result queryShopByRecommend(
            @Parameter(description = "推荐数量限制") @RequestParam(value = "limit", required = false) Integer limit,
            @Parameter(description = "排序字段") @RequestParam(value = "sortBy", required = false) String sortBy) {
        return shopService.shopRecommendList(limit, sortBy);
    }

    /**
     * 获取商铺类型统计数据
     * 
     * @return 商铺类型统计数据
     */
    @GetMapping("/type-stats")
    @Operation(summary = "商铺类型统计", description = "获取商铺类型统计数据")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result getShopTypeStats() {
        return shopService.getShopTypeStats();
    }

}
