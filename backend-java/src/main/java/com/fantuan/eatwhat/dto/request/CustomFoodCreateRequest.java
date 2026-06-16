package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建自定义菜品请求 DTO
 */
@Data
public class CustomFoodCreateRequest {

    /**
     * 菜品名称
     */
    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 64, message = "菜品名称最长 64 个字符")
    private String name;

    /**
     * 食物类型标签（可选，与 cuisineTags 至少一个非空）
     */
    private List<String> typeTags;

    /**
     * 菜系标签（可选，与 typeTags 至少一个非空）
     */
    private List<String> cuisineTags;

    /**
     * 适用餐段（至少一个）
     */
    @NotEmpty(message = "mealTypes 至少选一个")
    private List<String> mealTypes;

    /**
     * 口味标签（至少一个）
     */
    @NotEmpty(message = "tasteTags 至少选一个")
    private List<String> tasteTags;

    /**
     * 参考价位（1-4，可选）
     */
    @Min(value = 1, message = "priceLevel 最小为 1")
    @Max(value = 4, message = "priceLevel 最大为 4")
    private Integer priceLevel;
}
