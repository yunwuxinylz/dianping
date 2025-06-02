package com.dp.dto;

import lombok.Data;
import java.util.List;

@Data
public class AfterSaleDTO {
    private String orderId;
    private Integer type;
    private String reason;
    private Long amount;
    private String description;
    private List<String> images;
}