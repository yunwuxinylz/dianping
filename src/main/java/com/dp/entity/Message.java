package com.dp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}