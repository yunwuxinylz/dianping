package com.dp.dto;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * 购物车数据传输对象
 */
@Data
public class CartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<Long, ShopCartDTO> shopCarts;
}