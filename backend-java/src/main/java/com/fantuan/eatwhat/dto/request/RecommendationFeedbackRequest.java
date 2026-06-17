package com.fantuan.eatwhat.dto.request;

import lombok.Data;

/**
 * 推荐反馈请求 DTO
 */
@Data
public class RecommendationFeedbackRequest {

    /** 食物来源：DEFAULT / CUSTOM */
    private String source;

    /** 菜品ID（DEFAULT来源时必填） */
    private Long foodId;

    /** 自定义菜品ID（CUSTOM来源时必填） */
    private Long customFoodId;

    /** 菜品名称 */
    private String foodName;

    /** 不喜欢原因 */
    private String reason;

    /** 推荐时的餐段（可选） */
    private String mealType;

    /** 推荐时的价格级别（可选） */
    private String priceLevel;

    /** 推荐时的口味（可选） */
    private String taste;

    /** 推荐时的类型标签（可选） */
    private String typeTags;

    /** 推荐时的菜系标签（可选） */
    private String cuisineTags;
}
