package com.dp.service;

import com.dp.dto.Result;

/**
 * 订单数据分析服务接口
 */
public interface IOrderAnalysisService {
    /**
     * 获取订单统计信息
     * 
     * @return 订单统计信息
     */
    Result getOrderStatistics();

    /**
     * 获取订单总数
     * 
     * @return 订单总数
     */
    Result getOrderCount();

    /**
     * 获取今日销售额
     * 
     * @return 今日销售额
     */
    Result getTodaySales();

    /**
     * 获取最近7天销售趋势
     * 
     * @return 最近7天销售趋势
     */
    Result getWeekSales();

    /**
     * 获取热销商品排行
     * 
     * @param limit 数量限制
     * @return 热销商品列表
     */
    Result getTopSellingProducts(Integer limit);

    /**
     * 获取订单分类分析
     * 
     * @return 订单分类分析结果
     */
    Result getOrderCategoryAnalysis();
}
