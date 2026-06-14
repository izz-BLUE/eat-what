package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 吃过记录实体类
 */
@Data
@TableName("eat_records")
public class EatRecord {

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
     * 餐段：早餐、午餐、晚餐、夜宵
     */
    private String mealType;

    /**
     * 评分（1-5）
     */
    private Integer rating;

    /**
     * 备注
     */
    private String note;

    /**
     * 吃的时间
     */
    private LocalDateTime eatenAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
