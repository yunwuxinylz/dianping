package com.dp.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop_favorite")
public class ShopFavorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private Long userId;
    private String shopName;
    private String shopImages;
    private String address;
    private String area;
    private Long avgPrice;
    private Integer sold;
    private Integer score;
    private Long typeId;
    private String typeName;
    private LocalDateTime createTime;
}
