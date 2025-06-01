package com.dp.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;
import com.dp.service.ICartService;
import com.dp.utils.UserHolder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车管理", description = "购物车相关的API接口")
public class CartController {

    @Resource
    private ICartService cartService;

    /**
     * 获取当前用户的购物车
     */
    @GetMapping
    @Operation(summary = "获取购物车", description = "获取当前用户的购物车信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result getUserCart() {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.getUserCart(userId);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车", description = "将商品添加到当前用户的购物车")
    @ApiResponse(responseCode = "200", description = "添加成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result addToCart(
            @Parameter(description = "购物车商品信息") @RequestBody CartItemDTO cartItemDTO) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.addToCart(userId, cartItemDTO);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/count")
    @Operation(summary = "更新商品数量", description = "更新购物车中商品的数量")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result updateCartItemCount(
            @Parameter(description = "商品ID") @RequestParam("goodsId") Long goodsId,
            @Parameter(description = "商品规格ID") @RequestParam(value = "skuId", required = false) Long skuId,
            @Parameter(description = "商品数量") @RequestParam("count") Integer count) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.updateCartItemCount(userId, goodsId, skuId, count);
    }

    /**
     * 移除购物车商品
     */
    @DeleteMapping("/remove")
    @Operation(summary = "移除购物车商品", description = "从购物车中移除指定商品")
    @ApiResponse(responseCode = "200", description = "移除成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result removeFromCart(
            @Parameter(description = "商品ID") @RequestParam("goodsId") Long goodsId,
            @Parameter(description = "商品规格ID") @RequestParam(value = "skuId", required = false) Long skuId) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.removeFromCart(userId, goodsId, skuId);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车", description = "清空当前用户的购物车")
    @ApiResponse(responseCode = "200", description = "清空成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result clearCart() {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.clearCart(userId);
    }

    /**
     * 选中或取消选中购物车商品
     */
    @PutMapping("/check")
    @Operation(summary = "选中/取消选中商品", description = "选中或取消选中购物车中的商品")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result checkCartItem(
            @Parameter(description = "商品ID") @RequestParam("goodsId") Long goodsId,
            @Parameter(description = "商品规格ID") @RequestParam(value = "skuId", required = false) Long skuId,
            @Parameter(description = "是否选中") @RequestParam("checked") Boolean checked) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.checkCartItem(userId, goodsId, skuId, checked);
    }

    /**
     * 全选或取消全选
     */
    @PutMapping("/check/all")
    @Operation(summary = "全选/取消全选", description = "选中或取消选中购物车中的所有商品")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result checkAllItems(
            @Parameter(description = "是否全选") @RequestParam("checked") Boolean checked) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.checkAllItems(userId, checked);
    }

    /**
     * 选中或取消选中商铺商品
     */
    @PutMapping("/check/shop")
    @Operation(summary = "选中/取消选中店铺商品", description = "选中或取消选中购物车中指定店铺的所有商品")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result checkShopItems(
            @Parameter(description = "店铺ID") @RequestParam("shopId") Long shopId,
            @Parameter(description = "是否选中") @RequestParam("checked") Boolean checked) {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.checkShopItems(userId, shopId, checked);
    }

    /**
     * 移除选中商品
     */
    @DeleteMapping("/remove/checked")
    @Operation(summary = "移除选中商品", description = "从购物车中移除所有选中的商品")
    @ApiResponse(responseCode = "200", description = "移除成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result removeCheckedItems() {
        // 获取当前用户
        Long userId = UserHolder.getUser().getId();
        return cartService.removeCheckedItems(userId);
    }
}