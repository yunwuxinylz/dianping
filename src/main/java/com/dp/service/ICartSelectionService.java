package com.dp.service;

import com.dp.dto.Result;

public interface ICartSelectionService {
    Result checkCartItem(Long userId, Long goodsId, Long skuId, Boolean checked);

    Result checkAllItems(Long userId, Boolean checked);

    Result checkShopItems(Long userId, Long shopId, Boolean checked);

    Result removeCheckedItems(Long userId);
}
