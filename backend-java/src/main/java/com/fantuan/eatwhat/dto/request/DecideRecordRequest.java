package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 决定吃什么请求 DTO
 */
@Data
public class DecideRecordRequest {

    /**
     * 食物ID（与 customFoodId 互斥，DEFAULT 来源时使用）
     */
    private Long foodId;

    /**
     * 自定义食物ID（与 foodId 互斥，CUSTOM 来源时使用）
     */
    private Long customFoodId;

    /**
     * 餐段：早餐、午餐、晚餐、夜宵
     */
    @NotBlank(message = "mealType 不能为空")
    @Pattern(regexp = "^(早餐|午餐|晚餐|夜宵)$", message = "mealType 必须是：早餐、午餐、晚餐、夜宵")
    private String mealType;
}
