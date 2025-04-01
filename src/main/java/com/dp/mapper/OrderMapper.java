package com.dp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.entity.Order;
import org.apache.ibatis.annotations.Mapper;

// 订单Mapper
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}