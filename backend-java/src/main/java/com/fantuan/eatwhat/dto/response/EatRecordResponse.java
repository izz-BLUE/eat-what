package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 吃过记录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EatRecordResponse {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 食物ID
     */
    private Long foodId;

    /**
     * 食物名称
     */
    private String foodName;

    /**
     * 餐段
     */
    private String mealType;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 备注
     */
    private String note;

    /**
     * 吃的时间
     */
    private LocalDateTime eatenAt;
}
