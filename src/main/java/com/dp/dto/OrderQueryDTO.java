package com.dp.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private Integer status;
    private List<Integer> statusList;
    private Boolean uncommented;
    private Integer current = 1;
    private Integer pageSize = 10;
}
