package com.dp.controller;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.Result;
import com.dp.entity.ShopType;
import com.dp.service.IShopTypeService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("/types")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
