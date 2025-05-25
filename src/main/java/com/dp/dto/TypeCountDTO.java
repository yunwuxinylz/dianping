package com.dp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 类型统计DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeCountDTO {
    private String name;  // 类型名称
    private Integer value; // 数量
}