package com.dp.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.Result;
import com.dp.dto.ShopCartDTO;
import com.dp.entity.Cart;

public interface ICartCacheService extends IService<Cart> {
    List<ShopCartDTO> loadCartFromDB(Long userId);

    void updateCartCache(Long userId, List<ShopCartDTO> cartList);

    List<ShopCartDTO> convertHashToCartList(Map<Object, Object> entries);

    Result getUserCart(Long userId);

    boolean checkCartItemStock(Long userId, List<ShopCartDTO> cartList, List<String> stockWarnings);

    void syncCartToDatabase(Long userId);

    void sendCartUpdateMessage(Long userId);
}
