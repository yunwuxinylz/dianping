package com.dp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.entity.OrderItems;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项数据访问层接口
 */
@Mapper
public interface OrderItemsMapper extends BaseMapper<OrderItems> {
}