package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.CommentDTO;
import com.dp.dto.Result;
import com.dp.entity.Comment;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {
    /**
     * 提交商品评价
     * @param commentDTO 评价信息
     * @return 评价结果
     */
    Result submitComment(CommentDTO commentDTO);

    /**
     * 获取店铺评价列表
     * @param shopId 店铺ID
     * @param current 当前页码
     * @param pageSize 每页大小
     * @param score 评分筛选
     * @return 评价列表
     */
    Result getShopComments(Long shopId, Integer current, Integer pageSize, Integer score);

    /**
     * 检查订单是否已评价
     * @param orderId 订单ID
     * @return 是否已评价
     */
    Result checkOrderComment(Long orderId);
}