package com.dp.dto;

import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class AfterSaleDTO {
    private Long id; // 售后ID
    private String orderId; // 订单ID
    private Integer type; // 售后类型
    private String reason; // 申请原因
    private Long amount; // 退款金额
    private String description; // 问题描述
    private List<String> images; // 问题图片
    private Integer status; // 售后状态 0-未申请 1-处理中 2-已完成 3-已拒绝
    private String handleMsg; // 处理备注
    private LocalDateTime handleTime; // 处理时间
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}