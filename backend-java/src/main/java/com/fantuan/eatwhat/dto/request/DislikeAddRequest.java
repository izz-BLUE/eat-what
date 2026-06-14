package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 添加不想吃请求 DTO
 */
@Data
public class DislikeAddRequest {

    /**
     * 食物分类
     */
    @NotBlank(message = "category 不能为空")
    @Size(max = 32, message = "category 最长 32 个字符")
    private String category;

    /**
     * 有效天数，默认 3 天
     */
    @Min(value = 1, message = "days 最小为 1")
    @Max(value = 30, message = "days 最大为 30")
    private Integer days = 3;
}
