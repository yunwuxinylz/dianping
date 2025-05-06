package com.dp.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 评论数据传输对象
 */
@Data
public class CommentDTO {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String icon;

    /**
     * 商品信息
     * 
     */
    private List<OrderItemsDTO> items;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评分
     */
    private Integer score;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 是否匿名评价
     */
    private Boolean isAnonymous;

    /**
     * 商家回复内容
     */
    private String replyContent;

    /**
     * 商家回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}