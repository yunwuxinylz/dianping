package com.dp.service;

import java.util.List;

import com.dp.dto.CartAddDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;

public interface ICartItemService {
    Result addToCart(Long userId, CartAddDTO cartAddDTO);

    Result mergeCart(Long userId, List<ShopCartDTO> guestCart);

    Result updateCartItemCount(Long userId, Long shopId, Long goodsId, Long skuId, Integer count);

    Result removeFromCart(Long userId, Long shopId, Long goodsId, Long skuId);

    Result clearCart(Long userId);

}
