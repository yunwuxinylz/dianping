package com.dp.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.entity.ShopFavorite;

@Mapper
public interface FavoriteMapper extends BaseMapper<ShopFavorite> {

}
