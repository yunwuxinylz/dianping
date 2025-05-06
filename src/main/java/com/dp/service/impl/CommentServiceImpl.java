package com.dp.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.CommentDTO;
import com.dp.dto.OrderItemsDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Comment;
import com.dp.entity.Order;
import com.dp.entity.OrderItems;
import com.dp.mapper.CommentMapper;
import com.dp.service.ICommentService;
import com.dp.service.IOrderItemsService;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Resource
    private OrderServiceImpl orderService;

    @Resource
    private IOrderItemsService orderItemsService;  // 添加这个依赖

    @Override
    @Transactional
    public Result submitComment(CommentDTO commentDTO) {
        // 获取当前用户
        UserDTO user = UserHolder.getUser();

        // 用户名
        commentDTO.setNickName(user.getNickName());
        // 用户头像
        commentDTO.setIcon(user.getIcon());

        // 转换DTO为实体
        Comment comment = BeanUtil.copyProperties(commentDTO, Comment.class);

        comment.setUserId(user.getId());

        // 处理图片列表
        if (commentDTO.getImages() != null && !commentDTO.getImages().isEmpty()) {
            String images = String.join(",", commentDTO.getImages());
            comment.setImages(images);
        }

        // 保存评价
        boolean success = save(comment);
        if (!success) {
            return Result.fail("评价保存失败");
        }
        orderService.update()
                .set("commented", 1)
                .eq("id", commentDTO.getOrderId())
                .update();
        return Result.ok();
    }

    /**
     * 获取店铺评价列表
     * @param shopId 店铺ID
     * @param current 当前页码
     * @param pageSize 每页大小
     * @param score 评分筛选
     * @return 评价列表和总数
     */
    @Override
    public Result getShopComments(Long shopId, Integer current, Integer pageSize, Integer score) {
        // 构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getShopId, shopId)
                .eq(score != null, Comment::getScore, score)
                .orderByDesc(Comment::getCreateTime);

        // 使用数据库分页查询
        Page<Comment> page = this.baseMapper.selectPage(
            new Page<>(current, pageSize),
            queryWrapper
        );

        // 获取所有评论的订单ID
        List<Long> orderIds = page.getRecords().stream()
                .map(Comment::getOrderId)
                .collect(Collectors.toList());

        // 批量查询订单商品信息并按订单ID分组
        final Map<Long, List<OrderItems>> orderItemsMap = !orderIds.isEmpty() 
            ? orderItemsService.lambdaQuery()
                    .in(OrderItems::getOrderId, orderIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(OrderItems::getOrderId))
            : new HashMap<>();

        // 转换为DTO
        List<CommentDTO> records = page.getRecords().stream().map(comment -> {
            CommentDTO dto = BeanUtil.copyProperties(comment, CommentDTO.class);
            // 处理图片字符串转列表
            if (comment.getImages() != null) {
                dto.setImages(Arrays.asList(comment.getImages().split(",")));
            }
            // 设置订单商品信息
            List<OrderItems> items = orderItemsMap.getOrDefault(comment.getOrderId(), new ArrayList<>());
            List<OrderItemsDTO> itemDTOs = items.stream()
                    .map(item -> BeanUtil.copyProperties(item, OrderItemsDTO.class))
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
            return dto;
        }).collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("list", records);

        return Result.ok(result);
    }

    /**
     * 检查订单是否已评价
     */
    @Override
    public Result checkOrderComment(Long orderId) {
        // 查询订单的评价
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        // 检查订单是否已评价
        if (order.getCommented()) {
            return Result.ok();
        }
        return Result.fail("订单未评价");
    }
}