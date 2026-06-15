package com.fantuan.eatwhat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
@TableName("foods")
public class Food {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 分类（展示用，保留兼容旧接口）
     */
    private String category;

    /**
     * 食物类型标签，逗号分隔（快餐、小吃、面食、火锅、烧烤、甜品）
     */
    private String typeTags;

    /**
     * 菜系/风格标签，逗号分隔（家常菜、川菜、湘菜、粤菜、日料、西餐）
     */
    private String cuisineTags;

    /**
     * 适用餐段，逗号分隔（早餐、午餐、晚餐、夜宵）
     */
    private String mealTypes;

    /**
     * 口味标签（辣、甜、咸等），逗号分隔
     */
    private String tasteTags;

    /**
     * 价格等级（1-5）
     */
    private Integer priceLevel;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 菜品图片
     */
    private String imageUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
