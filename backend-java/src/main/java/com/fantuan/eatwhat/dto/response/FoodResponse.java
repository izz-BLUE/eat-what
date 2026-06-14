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
     * 分类
     */
    private String category;

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
