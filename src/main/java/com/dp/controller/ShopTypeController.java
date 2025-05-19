package com.dp.controller;

import java.util.List;

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
 * 
 */
@RestController
@RequestMapping("/shop")
public class ShopTypeController {
    private final IShopTypeService typeService;

    public ShopTypeController(IShopTypeService typeService) {
        this.typeService = typeService;
    }

    /**
     * 查询店铺类型列表
     * 
     * @return
     */
    @GetMapping("/types")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
