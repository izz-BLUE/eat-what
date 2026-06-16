package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.dto.response.FoodResponse;
import com.fantuan.eatwhat.mapper.FoodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品服务
 */
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodMapper foodMapper;

    /**
     * 查询菜品列表
     *
     * @param category   分类筛选（可选）
     * @param priceLevel 价格等级筛选（可选）
     * @return 菜品列表
     */
    public List<FoodResponse> listFoods(String category, Integer priceLevel) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getEnabled, true);

        if (StringUtils.hasText(category)) {
            wrapper.eq(Food::getCategory, category);
        }
        if (priceLevel != null) {
            wrapper.eq(Food::getPriceLevel, priceLevel);
        }

        List<Food> foods = foodMapper.selectList(wrapper);
        return foods.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查询所有启用的菜品
     */
    public List<Food> listAllEnabled() {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getEnabled, true);
        return foodMapper.selectList(wrapper);
    }

    /**
     * 根据 ID 列表查询菜品
     */
    public List<Food> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Food::getId, ids);
        wrapper.eq(Food::getEnabled, true);
        return foodMapper.selectList(wrapper);
    }

    /**
     * 实体转响应 DTO
     */
    public FoodResponse toResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .typeTags(food.getTypeTags())
                .cuisineTags(food.getCuisineTags())
                .mealTypes(food.getMealTypes())
                .tasteTags(food.getTasteTags())
                .priceLevel(food.getPriceLevel())
                .imageUrl(food.getImageUrl())
                .source("DEFAULT")
                .customFoodId(null)
                .build();
    }

    /**
     * 从 UserCustomFood 构建 FoodResponse（source=CUSTOM）
     */
    public FoodResponse fromCustomFood(com.fantuan.eatwhat.domain.entity.UserCustomFood customFood) {
        return FoodResponse.builder()
                .id(customFood.getId())
                .name(customFood.getName())
                .category(customFood.getCategory())
                .typeTags(customFood.getTypeTags())
                .cuisineTags(customFood.getCuisineTags())
                .mealTypes(customFood.getMealTypes())
                .tasteTags(customFood.getTasteTags())
                .priceLevel(customFood.getPriceLevel())
                .imageUrl(null)
                .source("CUSTOM")
                .customFoodId(customFood.getId())
                .build();
    }
}
