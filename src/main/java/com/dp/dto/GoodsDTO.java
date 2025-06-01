package com.dp.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.dp.entity.GoodSKU;

import lombok.Data;

/**
 * 商品DTO
 */
@Data
public class GoodsDTO {
    private Long id;
    private Long shopId;
    private String shopName;

    private String name;
    private Long price;
    private Long originalPrice;
    private String description;
    private List<String> images;
    private Integer stock;
    private Integer sold;
    private Integer status; // 商品状态：1-上架，0-下架
    private LocalDateTime createTime; // 创建时间
    private List<GoodSKU> skus; // SKU列表
}