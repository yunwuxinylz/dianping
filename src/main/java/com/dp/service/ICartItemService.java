package com.dp.service;

import java.util.List;

import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.Goods;

public interface ICartItemService {
    Result addToCart(Long userId, CartItemDTO cartItem);

    Result updateCartItemCount(Long userId, Long goodsId, Long skuId, Integer count);

    Result removeFromCart(Long userId, Long goodsId, Long skuId);

    Result clearCart(Long userId);

    Boolean updateCartWithNewItem(List<ShopCartDTO> cartList, Goods goods, CartItemDTO cartItem);
}
