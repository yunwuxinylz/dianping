package com.dp.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_goods_sku")
public class GoodSKU implements Serializable{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long goodsId;
    private String name;
    private Long price;
    private Long stock;
    private Long sold;

}
