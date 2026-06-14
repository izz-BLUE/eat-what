package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.domain.entity.UserDislike;
import com.fantuan.eatwhat.dto.request.DislikeAddRequest;
import com.fantuan.eatwhat.dto.response.DislikeResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.FoodMapper;
import com.fantuan.eatwhat.mapper.UserDislikeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户不想吃服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDislikeService {

    private final UserDislikeMapper userDislikeMapper;
    private final FoodMapper foodMapper;

    /**
     * 添加或更新不想吃（幂等）
     */
    public DislikeResponse addDislike(Long userId, DislikeAddRequest request) {
        // 校验分类是否存在于启用的菜品中
        validateCategoryExists(request.getCategory());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(request.getDays());

        // 先查询是否已存在
        LambdaQueryWrapper<UserDislike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDislike::getUserId, userId)
                .eq(UserDislike::getCategory, request.getCategory());
        UserDislike existing = userDislikeMapper.selectOne(wrapper);

        if (existing != null) {
            // 已存在，更新 expiresAt
            existing.setExpiresAt(expiresAt);
            userDislikeMapper.updateById(existing);
            return toResponse(existing);
        }

        // 不存在，插入新记录
        UserDislike dislike = new UserDislike();
        dislike.setUserId(userId);
        dislike.setCategory(request.getCategory());
        dislike.setExpiresAt(expiresAt);
        dislike.setCreatedAt(now);

        try {
            userDislikeMapper.insert(dislike);
        } catch (DuplicateKeyException e) {
            // 并发插入导致的重复，重新查询并更新
            log.warn("并发插入不想吃，重新查询: userId={}, category={}", userId, request.getCategory());
            UserDislike concurrent = userDislikeMapper.selectOne(wrapper);
            if (concurrent == null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "不想吃记录插入失败");
            }
            concurrent.setExpiresAt(expiresAt);
            userDislikeMapper.updateById(concurrent);
            return toResponse(concurrent);
        }

        return toResponse(dislike);
    }

    /**
     * 查询用户有效的不想吃列表
     */
    public List<DislikeResponse> listActiveDislikes(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<UserDislike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDislike::getUserId, userId)
                .gt(UserDislike::getExpiresAt, now)
                .orderByAsc(UserDislike::getExpiresAt);
        List<UserDislike> dislikes = userDislikeMapper.selectList(wrapper);

        return dislikes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 删除不想吃记录
     */
    public void removeDislike(Long dislikeId, Long userId) {
        UserDislike dislike = userDislikeMapper.selectById(dislikeId);
        if (dislike == null) {
            throw new BusinessException(ResultCode.DISLIKE_NOT_FOUND);
        }
        if (!dislike.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DISLIKE_NOT_FOUND);
        }
        userDislikeMapper.deleteById(dislikeId);
    }

    /**
     * 查询用户有效的不想吃分类集合
     */
    public Set<String> getActiveDislikeCategories(Long userId, LocalDateTime now) {
        if (userId == null) {
            return Set.of();
        }
        List<String> categories = userDislikeMapper.selectActiveCategories(userId, now);
        return new HashSet<>(categories);
    }

    /**
     * 校验分类是否存在于启用的菜品中
     */
    private void validateCategoryExists(String category) {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getCategory, category)
                .eq(Food::getEnabled, true)
                .last("LIMIT 1");
        Food food = foodMapper.selectOne(wrapper);
        if (food == null) {
            throw new BusinessException(ResultCode.FOOD_NOT_FOUND, "分类不存在或无启用菜品: " + category);
        }
    }

    private DislikeResponse toResponse(UserDislike dislike) {
        return DislikeResponse.builder()
                .id(dislike.getId())
                .category(dislike.getCategory())
                .expiresAt(dislike.getExpiresAt())
                .createdAt(dislike.getCreatedAt())
                .build();
    }
}
