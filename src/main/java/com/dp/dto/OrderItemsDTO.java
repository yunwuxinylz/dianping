package com.dp.dto;

import java.util.List;

import lombok.Data;

/**
 * 订单详情
 */
@Data
public class OrderItemsDTO {
    

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 购买数量
     */
    private Integer count;

    /**
     * 商品单价，单位：分
     */
    private Long price;

    /**
     * 商品图片，多个图片以逗号分隔
     */
    private List<String> goodsImage;  // 将String类型改为List<String>类型

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SKU名称
     */
    private String skuName;

}
