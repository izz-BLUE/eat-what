package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐反馈实体类
 */
@Data
@TableName("recommendation_feedbacks")
public class RecommendationFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（匿名提交为NULL） */
    private Long userId;

    /** 食物来源：DEFAULT / CUSTOM */
    private String foodSource;

    /** 菜品ID（DEFAULT来源时必填） */
    private Long foodId;

    /** 自定义菜品ID（CUSTOM来源时必填） */
    private Long customFoodId;

    /** 菜品名称快照 */
    private String foodName;

    /** 不喜欢原因 */
    private String reason;

    /** 推荐时的餐段 */
    private String mealType;

    /** 推荐时的价格级别 */
    private String priceLevel;

    /** 推荐时的口味 */
    private String taste;

    /** 推荐时的类型标签 */
    private String typeTags;

    /** 推荐时的菜系标签 */
    private String cuisineTags;

    private LocalDateTime createdAt;
}
