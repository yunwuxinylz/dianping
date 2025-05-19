package com.dp.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.FavoriteDTO;
import com.dp.dto.Result;
import com.dp.entity.Shop;
import com.dp.entity.ShopFavorite;
import com.dp.entity.ShopType;
import com.dp.mapper.FavoriteMapper;
import com.dp.service.IFavoriteService;
import com.dp.service.IShopTypeService;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, ShopFavorite> implements IFavoriteService {

    private final IShopTypeService shopTypeService;

    public FavoriteService(IShopTypeService shopTypeService) {
        this.shopTypeService = shopTypeService;
    }

    /**
     * 添加收藏
     * 
     * @param shop
     * @return
     */
    @Override
    public Result addFavorite(Shop shop) {
        Long userId = UserHolder.getUser().getId();

        if (userId == null) {
            return Result.fail("请先登录");
        }

        // 判断是否已收藏
        ShopFavorite favorite = this
                .getOne(new LambdaQueryWrapper<ShopFavorite>().eq(ShopFavorite::getShopId, shop.getId()));
        if (favorite != null) {
            return Result.fail("已收藏");
        }

        ShopFavorite shopFavorite = new ShopFavorite();
        shopFavorite.setShopId(shop.getId());
        shopFavorite.setUserId(userId);
        shopFavorite.setAddress(shop.getAddress());
        shopFavorite.setArea(shop.getArea());
        shopFavorite.setAvgPrice(shop.getAvgPrice());
        shopFavorite.setSold(shop.getSold());
        shopFavorite.setScore(shop.getScore());
        shopFavorite.setTypeId(shop.getTypeId());
        shopFavorite.setShopName(shop.getName());
        shopFavorite.setShopImages(shop.getImages());

        ShopType type = shopTypeService.getById(shop.getTypeId());
        shopFavorite.setTypeName(type.getName());
        // 添加收藏
        this.save(shopFavorite);
        return Result.ok();
    }

    /**
     * 删除收藏
     * 
     * @param shopId
     * @return
     */
    @Override
    public Result deleteFavorite(Long shopId) {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        this.remove(new LambdaQueryWrapper<ShopFavorite>().eq(ShopFavorite::getShopId, shopId)
                .eq(ShopFavorite::getUserId, userId));
        return Result.ok();
    }

    /**
     * 获取收藏列表
     * 
     * @return
     */
    @Override
    public Result listFavorite() {
        Long userId = UserHolder.getUser().getId();
        if (userId == null) {
            return Result.fail("请先登录");
        }
        List<ShopFavorite> shopFavorites = this.list(new LambdaQueryWrapper<ShopFavorite>().eq(ShopFavorite::getUserId,
                userId));

        if (shopFavorites.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 转换DTO
        List<FavoriteDTO> favoriteDTOs = shopFavorites.stream().map(shopFavorite -> {
            FavoriteDTO favoriteDTO = BeanUtil.copyProperties(shopFavorite, FavoriteDTO.class);
            if (shopFavorite.getShopImages() != null) {
                List<String> imageList = Arrays.asList(shopFavorite.getShopImages().split(","));
                favoriteDTO.setShopImages(imageList);
            }
            return favoriteDTO;
        }).collect(Collectors.toList());

        return Result.ok(favoriteDTOs);
    }

}
