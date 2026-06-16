package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 吃过记录实体类
 */
@Data
@TableName("eat_records")
public class EatRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 餐段：早餐、午餐、晚餐、夜宵
     */
    private String mealType;

    /**
     * 状态：DECIDED-已决定，EATEN-已吃
     */
    private String status;

    /**
     * 决定时间
     */
    private LocalDateTime decidedAt;

    /**
     * 评分（1-5）
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
