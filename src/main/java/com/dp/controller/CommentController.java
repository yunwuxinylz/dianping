package com.dp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.CommentDTO;
import com.dp.dto.Result;
import com.dp.service.ICommentService;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final ICommentService commentService;

    public CommentController(ICommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 提交商品评价
     * 
     * @param commentDTO 评价数据
     * @return 评价结果
     */
    @PostMapping("/submit")
    public Result submitComment(@RequestBody CommentDTO commentDTO) {
        return commentService.submitComment(commentDTO);
    }

    /**
     * 获取店铺评价列表
     * 
     * @param shopId   店铺ID
     * @param current  当前页码
     * @param pageSize 每页大小
     * @param score    评分筛选
     * @return 评价列表
     */
    @GetMapping("/shop")
    public Result getShopComments(
            @RequestParam("shopId") Long shopId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "score", required = false) Integer score) {
        return commentService.getShopComments(shopId, current, pageSize, score);
    }

    /**
     * 检查订单是否已评价
     * 
     * @param orderId 订单ID
     * @return 是否已评价
     */
    @GetMapping("/check/{orderId}")
    public Result checkOrderComment(@PathVariable Long orderId) {
        return commentService.checkOrderComment(orderId);
    }
}