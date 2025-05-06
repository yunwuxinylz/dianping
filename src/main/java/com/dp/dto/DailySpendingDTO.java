package com.dp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailySpendingDTO {
    private String day;
    private Double amount;
}
