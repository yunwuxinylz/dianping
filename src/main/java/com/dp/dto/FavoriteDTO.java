package com.dp.dto;

import java.util.List;

import lombok.Data;

@Data
public class FavoriteDTO {
    private Long shopId;
    private String shopName;
    private List<String> shopImages;
    private String address;
    private String area;
    private Long avgPrice;
    private Integer sold;
    private Integer score;
    private Long typeId;
    private String typeName;
}
