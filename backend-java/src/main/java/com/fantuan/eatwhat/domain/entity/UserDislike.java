package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户不想吃实体类
 */
@Data
@TableName("user_dislikes")
public class UserDislike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 食物分类
     */
    private String category;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
