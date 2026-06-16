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
     * 食物ID（CUSTOM 来源时可为 null）
     */
    private Long foodId;

    /**
     * 自定义食物ID（DEFAULT 来源时可为 null）
     */
    private Long customFoodId;

    /**
     * 食物来源：DEFAULT-系统菜品，CUSTOM-自定义菜品
     */
    private String foodSource;

    /**
     * 食物名称快照
     */
    private String foodNameSnapshot;

    /**
     * 分类快照
     */
    private String categorySnapshot;

    /**
     * 类型标签快照
     */
    private String typeTagsSnapshot;

    /**
     * 菜系标签快照
     */
    private String cuisineTagsSnapshot;

    /**
     * 餐段快照
     */
    private String mealTypesSnapshot;

    /**
     * 口味标签快照
     */
    private String tasteTagsSnapshot;

    /**
     * 价格等级快照
     */
    private Integer priceLevelSnapshot;

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
