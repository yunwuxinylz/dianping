package com.dp.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 购物车项数据传输对象
 */
@Data
public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImages;

    /**
     * 商品价格（单位：分）
     */
    private Long price;

    /**
     * 商品数量
     */
    private Integer count;

    /**
     * 是否选中
     */
    private Boolean checked;

    /**
     * 商品规格名称
     */
    private String skuName;
}