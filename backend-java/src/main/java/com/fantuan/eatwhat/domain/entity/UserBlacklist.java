package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户黑名单实体类
 */
@Data
@TableName("user_blacklist")
public class UserBlacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 食物ID
     */
    private Long foodId;

    /**
     * 拉黑原因
     */
    private String reason;

    private LocalDateTime createdAt;
}
