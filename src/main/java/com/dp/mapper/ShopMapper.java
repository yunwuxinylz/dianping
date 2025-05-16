package com.dp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.dto.TypeCountDTO;
import com.dp.entity.Shop;

@Mapper
public interface ShopMapper extends BaseMapper<Shop> {

    /**
     * 统计各类型商铺数量并关联类型名称
     * @return 商铺类型统计列表
     */
    @Select("SELECT t.name as name, COUNT(s.id) as value FROM tb_shop s JOIN tb_shop_type t ON s.type_id = t.id GROUP BY s.type_id, t.name")
    List<TypeCountDTO> countShopsByType();
}
