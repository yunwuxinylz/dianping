package com.dp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

// 订单实体类
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long shopId;

    private Long goodsId;

    private String goodsName;

    private Long goodsPrice;

    private Integer amount;

    private Long totalPrice;

    private Integer payType;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime updateTime;
}