package com.dp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.AfterSaleDTO;
import com.dp.dto.AfterSaleStatusDTO;
import com.dp.dto.Result;
import com.dp.service.IAfterSaleService;

import lombok.RequiredArgsConstructor;

/**
 * 售后控制器
 */
@RestController
@RequestMapping("/after-sale")
@RequiredArgsConstructor
public class AfterSaleController {

    private final IAfterSaleService afterSaleService;

    /**
     * 申请售后
     */
    @PostMapping("/apply")
    public Result applyAfterSale(@RequestBody AfterSaleDTO afterSaleDTO) {
        return afterSaleService.applyAfterSale(afterSaleDTO);
    }

    /**
     * 获取售后详情
     */
    @GetMapping("/detail/{id}")
    public Result getAfterSaleDetail(@PathVariable Long id) {
        return afterSaleService.getAfterSaleDetail(id);
    }

    /**
     * 根据订单ID获取售后列表
     */
    @GetMapping("/order/{orderId}")
    public Result getAfterSaleByOrderId(@PathVariable String orderId) {
        Long orderIdLong = Long.parseLong(orderId);
        return afterSaleService.getAfterSaleByOrderId(orderIdLong);
    }

    /**
     * 获取用户的售后记录列表
     */
    @GetMapping("/user/list")
    public Result getUserAfterSales(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return afterSaleService.getUserAfterSales(current, size, status);
    }

    /**
     * 商家处理售后
     */
    @PutMapping("/handle")
    public Result handleAfterSale(@RequestBody AfterSaleStatusDTO afterSaleStatusDTO) {
        return afterSaleService.handleAfterSale(afterSaleStatusDTO);
    }

    /**
     * 管理员查询全部售后记录
     */
    @GetMapping("/admin/list")
    public Result getAllAfterSales(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        return afterSaleService.getAllAfterSales(current, size, status);
    }
}