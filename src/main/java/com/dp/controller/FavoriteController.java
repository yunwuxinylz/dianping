package com.dp.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.Result;
import com.dp.entity.Shop;
import com.dp.service.IFavoriteService;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    private final IFavoriteService favoriteService;

    public FavoriteController(IFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/add")
    public Result addFavorite(@RequestBody Shop shop) {
        return favoriteService.addFavorite(shop);
    }

    @DeleteMapping("/delete")
    public Result deleteFavorite(@RequestParam Long shopId) {
        return favoriteService.deleteFavorite(shopId);
    }

    @GetMapping("/list")
    public Result listFavorite() {
        return favoriteService.listFavorite();
    }

}
