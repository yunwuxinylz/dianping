package com.dp.dto;

import lombok.Data;

import java.util.List;

import com.dp.entity.GoodSKU;

/**
 * 商品DTO
 */
@Data
public class GoodsDTO {
    private Long id;
    private Long shopId;
    private String name;
    private Long price;
    private Long originalPrice;
    private String description;
    private List<String> images;
    private Integer stock;
    private Integer sold;
    private List<GoodSKU> skus; // SKU列表

}