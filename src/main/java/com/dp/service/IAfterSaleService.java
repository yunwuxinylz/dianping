package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.AfterSaleDTO;
import com.dp.dto.AfterSaleStatusDTO;
import com.dp.dto.Result;
import com.dp.entity.AfterSale;

/**
 * 售后服务接口
 */
public interface IAfterSaleService extends IService<AfterSale> {

    /**
     * 申请售后
     * 
     * @param afterSaleDTO 售后申请信息
     * @return 操作结果
     */
    Result applyAfterSale(AfterSaleDTO afterSaleDTO);

    /**
     * 查询售后详情
     * 
     * @param id 售后ID
     * @return 售后详情
     */
    Result getAfterSaleDetail(Long id);

    /**
     * 查询订单的售后列表
     * 
     * @param orderId 订单ID
     * @return 售后列表
     */
    Result getAfterSaleByOrderId(Long orderId);

    /**
     * 查询用户的售后列表
     * 
     * @param current 当前页
     * @param size    每页大小
     * @param status  售后状态（可选）
     * @return 售后列表
     */
    // Result getUserAfterSales(Integer current, Integer size, Integer status);

    /**
     * 商家处理售后
     * 
     * @param afterSaleStatusDTO 售后处理信息
     * @return 处理结果
     */
    Result handleAfterSale(AfterSaleStatusDTO afterSaleStatusDTO);

    /**
     * 查询全部售后申请（管理员）
     * 
     * @param current 当前页
     * @param size    每页大小
     * @param status  售后状态（可选）
     * @return 售后列表
     */
    Result getAllAfterSales(Integer current, Integer size, Integer status);
}