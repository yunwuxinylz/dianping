package com.dp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoodsDTO {
    private Long id;
    private Long shopId;
    private String name;
    private Long price;
    private String description;
    private List<String> imageList;
    private Integer stock;
    private Integer sold;
    private Integer status;
}