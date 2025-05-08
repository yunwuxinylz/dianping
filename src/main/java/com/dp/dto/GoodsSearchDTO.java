package com.dp.dto;

import java.util.List;

import lombok.Data;

/**
 * 商品搜索DTO
 */
@Data
public class GoodsSearchDTO {
    private Long id;
    private String name;
    private Long price;
    private Long originalPrice;
    private String description;
    private List<String> images;
    private Integer stock;
    private Integer sold;

    private Long shopId;

    private String shopName;
    private String address;
    private Double distance;
    private Integer score;

}
