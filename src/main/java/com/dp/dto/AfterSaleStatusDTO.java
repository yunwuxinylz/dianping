package com.dp.dto;

import lombok.Data;

@Data
public class AfterSaleStatusDTO {
    private Long id; // 售后ID
    private Integer status; // 售后状态
    private String handleMsg; // 处理备注
}