package com.dp.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dp.dto.CartAddDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.service.ICartCacheService;
import com.dp.service.ICartItemService;
import com.dp.service.ICartSelectionService;
import com.dp.service.ICartService;

import lombok.extern.slf4j.Slf4j;

/**
 * 购物车服务实现类
 */
@Service
@Slf4j
public class CartServiceImpl implements ICartService {

    private final ICartCacheService cartCacheService;
    private final ICartItemService cartItemService;
    private final ICartSelectionService cartSelectionService;

    public CartServiceImpl(ICartCacheService cartCacheService,
            ICartItemService cartItemService,
            ICartSelectionService cartSelectionService) {
        this.cartCacheService = cartCacheService;
        this.cartItemService = cartItemService;
        this.cartSelectionService = cartSelectionService;
    }

    /**
     * 获取用户购物车
     */
    @Override
    public Result getUserCart(Long userId) {
        return cartCacheService.getUserCart(userId);
    }

    /**
     * 添加商品到购物车
     */
    @Override
    public Result addToCart(Long userId, CartAddDTO cartAddDTO) {
        return cartItemService.addToCart(userId, cartAddDTO);
    }

    /**
     * 更新购物车商品数量
     */
    @Override
    public Result updateCartItemCount(Long userId, Long shopId, Long goodsId, Long skuId, Integer count) {
        return cartItemService.updateCartItemCount(userId, shopId, goodsId, skuId, count);
    }

    /**
     * 移除购物车商品
     */
    @Override
    public Result removeFromCart(Long userId, Long shopId, Long goodsId, Long skuId) {
        return cartItemService.removeFromCart(userId, shopId, goodsId, skuId);
    }

    /**
     * 清空用户购物车
     */
    @Override
    public Result clearCart(Long userId) {
        return cartItemService.clearCart(userId);
    }

    /**
     * 选中或取消选中购物车商品
     */
    @Override
    public Result checkCartItem(Long userId, Long shopId, Long goodsId, Long skuId, Boolean checked) {
        return cartSelectionService.checkCartItem(userId, shopId, goodsId, skuId, checked);
    }

    /**
     * 全选或取消全选
     */
    @Override
    public Result checkAllItems(Long userId, Boolean checked) {
        return cartSelectionService.checkAllItems(userId, checked);
    }

    /**
     * 选中或取消选中指定商铺的所有商品
     */
    @Override
    public Result checkShopItems(Long userId, Long shopId, Boolean checked) {
        return cartSelectionService.checkShopItems(userId, shopId, checked);
    }

    /**
     * 移除已选中的商品
     */
    @Override
    public Result removeCheckedItems(Long userId) {
        return cartSelectionService.removeCheckedItems(userId);
    }

    /**
     * 合并购物车
     */
    @Override
    public Result mergeCart(Long userId, List<ShopCartDTO> guestCart) {
        return cartItemService.mergeCart(userId, guestCart);
    }
}
