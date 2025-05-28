package com.dp.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ShopCartDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商店ID
     */
    private Long shopId;

    /**
     * 商店名称
     */
    private String shopName;

    /**
     * 购物车商品列表
     */
    private List<CartItemDTO> items;

}
