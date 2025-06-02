package com.dp.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class CartAddDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private List<String> goodsImages;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品规格名称
     */
    private String skuName;

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
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

}
