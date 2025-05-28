package com.dp.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.entity.Cart;

/**
 * 购物车数据访问接口
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

}