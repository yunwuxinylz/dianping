package com.dp.service;

import com.dp.dto.Result;
import com.dp.entity.Shop;

public interface IFavoriteService {

    Result addFavorite(Shop shop);

    Result deleteFavorite(Long shopId);

    Result listFavorite();

}
