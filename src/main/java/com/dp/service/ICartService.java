package com.dp.service;

import com.dp.dto.CartItemDTO;
import com.dp.dto.Result;

/**
 * 购物车服务接口
 */
public interface ICartService {

    /**
     * 获取用户购物车
     * 
     * @param userId 用户ID
     * @return 购物车DTO
     */
    Result getUserCart(Long userId);

    /**
     * 添加商品到购物车
     * 
     * @param userId   用户ID
     * @param cartItem 购物车项
     * @return 是否添加成功
     */
    Result addToCart(Long userId, CartItemDTO cartItem);

    /**
     * 更新购物车商品数量
     * 
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @param skuId   商品SKU ID
     * @param count   更新后的数量
     * @return 是否更新成功
     */
    Result updateCartItemCount(Long userId, Long goodsId, Long skuId, Integer count);

    /**
     * 移除购物车商品
     * 
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @param skuId   商品SKU ID
     * @return 是否移除成功
     */
    Result removeFromCart(Long userId, Long goodsId, Long skuId);

    /**
     * 清空用户购物车
     * 
     * @param userId 用户ID
     * @return 是否清空成功
     */
    Result clearCart(Long userId);

    /**
     * 选中或取消选中购物车商品
     * 
     * @param userId  用户ID
     * @param goodsId 商品ID
     * @param skuId   商品SKU ID
     * @param checked 是否选中
     * @return 是否操作成功
     */
    Result checkCartItem(Long userId, Long goodsId, Long skuId, Boolean checked);

    /**
     * 全选或取消全选
     * 
     * @param userId  用户ID
     * @param checked 是否全选
     * @return 是否操作成功
     */
    Result checkAllItems(Long userId, Boolean checked);

    /**
     * 选中或取消选中指定商铺的所有商品
     * 
     * @param userId  用户ID
     * @param shopId  商铺ID
     * @param checked 是否选中
     * @return 是否操作成功
     */
    Result checkShopItems(Long userId, Long shopId, Boolean checked);

    /**
     * 移除已选中的商品
     * 
     * @param userId 用户ID
     * @return 是否移除成功
     */
    Result removeCheckedItems(Long userId);
}