package com.dp.dto;

import java.util.List;

import lombok.Data;

/**
 * @author dp
 */
@Data
public class ShopDTO {
    private Long id;
    private String name;
    private String address;
    private String area;
    private List<String> images;
    private Long avgPrice;
    private Integer sold;
    private Integer score;
    private Integer count;
    
    private Long typeId;
    private String typeName;
}
