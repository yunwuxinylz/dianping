package com.dp.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 订单实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID，使用雪花算法生成
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 商铺ID
     */
    private Long shopId;

    /**
     * 商铺名称
     */
    private String shopName;

    /**
     * 店铺logo
     */
    private String shopImage;

    /**
     * 订单金额，单位：分
     */
    private Long amount;

    /**
     * 支付方式 1：微信支付 2：支付宝支付
     */
    private Integer payType;

    /**
     * 订单状态 1：未支付 2：已支付 3：已取消 4：已完成 5：已退款
     */
    private Integer status;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 地址ID
     */
    private Long addressId;

    /**
     * 地址详情
     */
    private String addressDetail;

    /**
     * 收货人
     */
    private String addressName;

    /**
     * 收货人手机号
     */
    private String addressPhone;

    /**
     * 总数量
     */
    private Integer count;

    /**
     * 是否评价
     */
    private Boolean commented;

    /**
     * 售后状态
     */
    @TableField("after_sale_status")
    private Integer afterSaleStatus;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 订单创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订单支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单更新时间
     */
    private LocalDateTime updateTime;
}