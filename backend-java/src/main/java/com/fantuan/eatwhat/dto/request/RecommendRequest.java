package com.fantuan.eatwhat.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 推荐请求 DTO
 */
@Data
public class RecommendRequest {

    /**
     * 餐段：早餐、午餐、晚餐、夜宵
     */
    private String mealType;

    /**
     * 价格偏好：15以内、15-25、25-40、不限
     */
    private String priceLevel;

    /**
     * 口味偏好：清淡、重口、辣、不辣
     */
    private String taste;

    /**
     * 排除的菜品 ID 列表（用于换一个）
     */
    private List<Long> excludeFoodIds;

    /**
     * 用户ID（临时，后续从 token 获取）
     */
    private Long userId;
}
