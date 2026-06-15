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
     * 状态：DECIDED-已决定，EATEN-已吃
     */
    private String status;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 备注
     */
    private String note;

    /**
     * 吃的时间（DECIDED 状态时为空）
     */
    private LocalDateTime eatenAt;

    /**
     * 决定时间
     */
    private LocalDateTime decidedAt;

    /**
     * 食物分类
     */
    private String category;
}
