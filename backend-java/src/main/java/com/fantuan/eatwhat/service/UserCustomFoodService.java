package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.RecommendDict;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.UserCustomFood;
import com.fantuan.eatwhat.dto.request.CustomFoodCreateRequest;
import com.fantuan.eatwhat.dto.response.CustomFoodResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.UserCustomFoodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户自定义菜品服务
 */
@Service
@RequiredArgsConstructor
public class UserCustomFoodService {

    private final UserCustomFoodMapper userCustomFoodMapper;

    /**
     * 创建自定义菜品
     */
    public CustomFoodResponse create(Long userId, CustomFoodCreateRequest request) {
        // 1. trim name
        String name = request.getName() != null ? request.getName().trim() : "";
        if (name.isEmpty() || name.length() > 64) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "菜品名称 1-64 个字符");
        }

        // 2. 校验 typeTags / cuisineTags 至少一个非空
        boolean hasType = request.getTypeTags() != null && !request.getTypeTags().isEmpty();
        boolean hasCuisine = request.getCuisineTags() != null && !request.getCuisineTags().isEmpty();
        if (!hasType && !hasCuisine) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "typeTags 或 cuisineTags 至少填一个");
        }

        // 3. 校验 mealTypes / tasteTags 非空且合法
        if (request.getMealTypes() == null || request.getMealTypes().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "mealTypes 至少选一个");
        }
        if (request.getTasteTags() == null || request.getTasteTags().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "tasteTags 至少选一个");
        }

        // 4. 校验所有标签在 RecommendDict 中
        validateTags(request.getTypeTags(), RecommendDict.TYPE_TAGS, "typeTag");
        validateTags(request.getCuisineTags(), RecommendDict.CUISINE_TAGS, "cuisineTag");
        validateTags(request.getMealTypes(), RecommendDict.MEAL_TYPES, "mealType");
        validateTags(request.getTasteTags(), RecommendDict.BASE_TASTE_TAGS.stream().toList(), "tasteTag");

        // 5. trim、去重、保留顺序、join 为逗号字符串
        String typeTags = joinTags(request.getTypeTags());
        String cuisineTags = joinTags(request.getCuisineTags());
        String mealTypes = joinTags(request.getMealTypes());
        String tasteTags = joinTags(request.getTasteTags());

        // 6. 派生 category
        String category = deriveCategory(request.getTypeTags(), request.getCuisineTags());

        // 7. 构建实体并插入
        UserCustomFood entity = new UserCustomFood();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setCategory(category);
        entity.setTypeTags(typeTags);
        entity.setCuisineTags(cuisineTags);
        entity.setMealTypes(mealTypes);
        entity.setTasteTags(tasteTags);
        entity.setPriceLevel(request.getPriceLevel());
        entity.setEnabled(true);

        try {
            userCustomFoodMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.CUSTOM_FOOD_DUPLICATE);
        }

        return toResponse(entity);
    }

    /**
     * 查询当前用户启用的自定义菜品（按 updated_at DESC）
     */
    public List<CustomFoodResponse> list(Long userId) {
        LambdaQueryWrapper<UserCustomFood> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCustomFood::getUserId, userId)
               .eq(UserCustomFood::getEnabled, true)
               .orderByDesc(UserCustomFood::getUpdatedAt);
        return userCustomFoodMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 软删除自定义菜品（enabled=false）
     */
    public void delete(Long userId, Long id) {
        UserCustomFood entity = userCustomFoodMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.CUSTOM_FOOD_NOT_FOUND);
        }
        entity.setEnabled(false);
        userCustomFoodMapper.updateById(entity);
    }

    /**
     * 获取用户所有启用的自定义菜品（内部使用，供推荐算法）
     */
    public List<UserCustomFood> getEnabledCustomFoods(Long userId) {
        LambdaQueryWrapper<UserCustomFood> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCustomFood::getUserId, userId)
               .eq(UserCustomFood::getEnabled, true);
        return userCustomFoodMapper.selectList(wrapper);
    }

    // ==================== 私有方法 ====================

    /**
     * 派生 category：优先 cuisineTags 第一个，否则 typeTags 第一个
     * 直接按请求数组顺序取，不依赖 Set 顺序
     */
    static String deriveCategory(List<String> typeTags, List<String> cuisineTags) {
        if (cuisineTags != null && !cuisineTags.isEmpty()) {
            String first = cuisineTags.get(0);
            if (StringUtils.hasText(first)) {
                return first.trim();
            }
        }
        if (typeTags != null && !typeTags.isEmpty()) {
            String first = typeTags.get(0);
            if (StringUtils.hasText(first)) {
                return first.trim();
            }
        }
        return "";
    }

    /**
     * trim、去重、保留提交顺序、join 为逗号字符串
     */
    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag != null) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    seen.add(trimmed);
                }
            }
        }
        return String.join(",", seen);
    }

    /**
     * 校验标签是否都在词典中
     */
    private void validateTags(List<String> tags, List<String> dict, String label) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (String tag : tags) {
            if (tag != null && !tag.trim().isEmpty() && !dict.contains(tag.trim())) {
                throw new BusinessException(ResultCode.PARAM_ERROR,
                        "无效的" + label + ": " + tag);
            }
        }
    }

    /**
     * 实体转响应 DTO
     */
    private CustomFoodResponse toResponse(UserCustomFood entity) {
        return CustomFoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(entity.getCategory())
                .typeTags(entity.getTypeTags())
                .cuisineTags(entity.getCuisineTags())
                .mealTypes(entity.getMealTypes())
                .tasteTags(entity.getTasteTags())
                .priceLevel(entity.getPriceLevel())
                .enabled(entity.getEnabled())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
