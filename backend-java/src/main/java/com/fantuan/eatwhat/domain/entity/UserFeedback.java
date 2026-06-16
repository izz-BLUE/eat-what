package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 意见反馈实体类
 */
@Data
@TableName("user_feedbacks")
public class UserFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（匿名提交为NULL） */
    private Long userId;

    /** 反馈类型：FEATURE/BUG/RECOMMENDATION/UI/OTHER */
    private String type;

    /** 满意度评分（1-5，可选） */
    private Integer rating;

    /** 反馈内容 */
    private String content;

    /** 联系方式（可选） */
    private String contact;

    /** 来源页面路径 */
    private String page;

    /** 微信环境信息（JSON） */
    private String systemInfo;

    /** 处理状态：NEW */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
