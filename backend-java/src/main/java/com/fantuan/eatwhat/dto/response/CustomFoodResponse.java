package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 自定义菜品响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomFoodResponse {

    private Long id;

    private String name;

    private String category;

    private String typeTags;

    private String cuisineTags;

    private String mealTypes;

    private String tasteTags;

    private Integer priceLevel;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
