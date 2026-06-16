package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户自定义菜品实体类
 */
@Data
@TableName("user_custom_foods")
public class UserCustomFood {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 分类（派生字段：第一个 cuisine_tag 或第一个 type_tag）
     */
    private String category;

    /**
     * 食物类型标签，逗号分隔
     */
    private String typeTags;

    /**
     * 菜系/风格标签，逗号分隔
     */
    private String cuisineTags;

    /**
     * 适用餐段，逗号分隔
     */
    private String mealTypes;

    /**
     * 口味标签，逗号分隔
     */
    private String tasteTags;

    /**
     * 价格等级（1-4，可选）
     */
    private Integer priceLevel;

    /**
     * 是否启用
     */
    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
