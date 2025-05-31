package com.dp.service;

import com.dp.dto.Result;

/**
 * 订单数据分析服务接口
 */
public interface IOrderAnalysisService {
    /**
     * 获取用户订单统计信息
     */
    Result getOrderStatistics();

    /**
     * 获取订单总数
     */
    Result getOrderCount();

    /**
     * 获取今日销售额
     */
    Result getTodaySales();

    /**
     * 获取最近7天销售趋势
     */
    Result getWeekSales();

    /**
     * 获取商品销售排行
     */
    Result getTopSellingProducts(Integer limit);

    /**
     * 获取订单分类统计
     */
    Result getOrderCategoryAnalysis();
}
