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
     * 分类（快餐、火锅、川菜等）
     */
    private String category;

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
