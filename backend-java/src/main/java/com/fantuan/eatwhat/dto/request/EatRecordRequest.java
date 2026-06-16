package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 吃过记录请求 DTO
 */
@Data
public class EatRecordRequest {

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

    /**
     * 评分（1-5）
     */
    @Min(value = 1, message = "rating 最小为 1")
    @Max(value = 5, message = "rating 最大为 5")
    private Integer rating;

    /**
     * 备注
     */
    @Size(max = 256, message = "note 最长 256 个字符")
    private String note;
}
