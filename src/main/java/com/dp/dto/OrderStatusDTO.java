package com.dp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusDTO {
    private String name;
    private Integer value;
    private Integer status;

}
