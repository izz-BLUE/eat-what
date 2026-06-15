package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜品响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponse {

    private Long id;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 分类（展示用）
     */
    private String category;

    /**
     * 食物类型标签，逗号分隔
     */
    private String typeTags;

    /**
     * 菜系/风格标签，逗号分隔
     */
    private String cuisineTags;

    /**
     * 适用餐段，逗号分隔
     */
    private String mealTypes;

    /**
     * 口味标签，逗号分隔
     */
    private String tasteTags;

    /**
     * 价格等级 1-5
     */
    private Integer priceLevel;

    /**
     * 菜品图片
     */
    private String imageUrl;
}
