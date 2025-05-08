package com.dp.service;

import com.dp.dto.Result;
import com.dp.dto.ShopDTO;
import com.dp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * 
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);

    Result updateSold(ShopDTO shopDTO);

    Result shopByType(Integer typeId, Integer current, String sortBy, Integer pageSize, String sortOrder);

    Result shopByName(String name, String sortBy, String sortOrder, Integer pageSize, Integer current);

    Result shopRecommendList(Integer limit, String sortBy);

}
