package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改用餐评价请求 DTO
 */
@Data
public class ReviewRecordRequest {

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
