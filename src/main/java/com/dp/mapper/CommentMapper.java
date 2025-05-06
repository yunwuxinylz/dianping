package com.dp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论数据访问层接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}