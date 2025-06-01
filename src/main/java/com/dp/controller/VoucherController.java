package com.dp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.Result;
import com.dp.entity.Voucher;
import com.dp.service.IVoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * 
 */
@RestController
@RequestMapping("/voucher")
@Tag(name = "优惠券管理", description = "优惠券相关的API接口")
public class VoucherController {

    private final IVoucherService voucherService;

    public VoucherController(IVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    /**
     * 新增普通券
     *
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    @Operation(summary = "新增普通优惠券", description = "创建一个新的普通优惠券")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result addVoucher(
            @Parameter(description = "优惠券信息") @RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 新增秒杀券
     *
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    @Operation(summary = "新增秒杀优惠券", description = "创建一个新的秒杀优惠券")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result addSeckillVoucher(
            @Parameter(description = "优惠券信息，包含秒杀信息") @RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 查询店铺的优惠券列表
     *
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    @Operation(summary = "查询店铺优惠券", description = "查询指定店铺的优惠券列表")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class)))
    public Result queryVoucherOfShop(
            @Parameter(description = "店铺ID") @PathVariable Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }
}
