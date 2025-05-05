package com.dp.dto;

import lombok.Data;

/**
 * @author dp
 */
@Data
public class ShopDTO {
    private Long id;
    private String name;
    private String typeId;
    private String address;
    private String images;
    private Long avgPrice;
    private Integer sold;
    private Integer score;
    private Integer count;
    
}
