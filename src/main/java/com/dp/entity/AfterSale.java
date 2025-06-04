package com.dp.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("tb_after_sale")
public class AfterSale implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Long shopId;
    private Integer type; // 1-退货退款 2-换货 3-维修 4-仅退款
    private String reason;
    private Long amount;
    private String description;
    private String images;
    private Integer status; // 1-处理中 2-已完成 3-已拒绝
    private String handleMsg;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}