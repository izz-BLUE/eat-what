package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.domain.entity.UserBlacklist;
import com.fantuan.eatwhat.dto.request.BlacklistAddRequest;
import com.fantuan.eatwhat.dto.response.BlacklistResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.FoodMapper;
import com.fantuan.eatwhat.mapper.UserBlacklistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户黑名单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBlacklistService {

    private final UserBlacklistMapper userBlacklistMapper;
    private final FoodMapper foodMapper;

    /**
     * 加入黑名单（幂等）
     */
    public BlacklistResponse addToBlacklist(Long userId, BlacklistAddRequest request) {
        // 校验食物是否存在
        Food food = foodMapper.selectById(request.getFoodId());
        if (food == null || !Boolean.TRUE.equals(food.getEnabled())) {
            throw new BusinessException(ResultCode.FOOD_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();

        // 先查询是否已存在
        LambdaQueryWrapper<UserBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlacklist::getUserId, userId)
                .eq(UserBlacklist::getFoodId, request.getFoodId());
        UserBlacklist existing = userBlacklistMapper.selectOne(wrapper);

        if (existing != null) {
            // 已存在，检查是否需要更新 reason
            if (request.getReason() != null && !request.getReason().equals(existing.getReason())) {
                existing.setReason(request.getReason());
                userBlacklistMapper.updateById(existing);
            }
            return toResponse(existing, food);
        }

        // 不存在，插入新记录
        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setUserId(userId);
        blacklist.setFoodId(request.getFoodId());
        blacklist.setReason(request.getReason());
        blacklist.setCreatedAt(now);

        try {
            userBlacklistMapper.insert(blacklist);
        } catch (DuplicateKeyException e) {
            // 并发插入导致的重复，重新查询并更新 reason
            log.warn("并发插入黑名单，重新查询: userId={}, foodId={}", userId, request.getFoodId());
            UserBlacklist concurrent = userBlacklistMapper.selectOne(wrapper);
            if (concurrent == null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "黑名单插入失败");
            }
            // 更新 reason（如果本次请求有新的 reason）
            if (request.getReason() != null && !request.getReason().equals(concurrent.getReason())) {
                concurrent.setReason(request.getReason());
                userBlacklistMapper.updateById(concurrent);
            }
            return toResponse(concurrent, food);
        }

        return toResponse(blacklist, food);
    }

    /**
     * 查询用户黑名单列表
     */
    public List<BlacklistResponse> listBlacklist(Long userId) {
        LambdaQueryWrapper<UserBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlacklist::getUserId, userId)
                .orderByDesc(UserBlacklist::getCreatedAt);
        List<UserBlacklist> blacklists = userBlacklistMapper.selectList(wrapper);

        if (blacklists.isEmpty()) {
            return List.of();
        }

        // 批量查询食物信息
        List<Long> foodIds = blacklists.stream()
                .map(UserBlacklist::getFoodId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Food> foodMap = new HashMap<>();
        if (!foodIds.isEmpty()) {
            List<Food> foods = foodMapper.selectBatchIds(foodIds);
            foodMap = foods.stream()
                    .collect(Collectors.toMap(Food::getId, f -> f));
        }

        Map<Long, Food> finalFoodMap = foodMap;
        return blacklists.stream()
                .map(b -> {
                    Food food = finalFoodMap.get(b.getFoodId());
                    return BlacklistResponse.builder()
                            .id(b.getId())
                            .foodId(b.getFoodId())
                            .foodName(food != null ? food.getName() : "未知")
                            .category(food != null ? food.getCategory() : "未知")
                            .reason(b.getReason())
                            .createdAt(b.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 移出黑名单
     */
    public void removeFromBlacklist(Long blacklistId, Long userId) {
        UserBlacklist blacklist = userBlacklistMapper.selectById(blacklistId);
        if (blacklist == null) {
            throw new BusinessException(ResultCode.BLACKLIST_NOT_FOUND);
        }
        if (!blacklist.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.BLACKLIST_NOT_FOUND);
        }
        userBlacklistMapper.deleteById(blacklistId);
    }

    /**
     * 查询用户所有黑名单食物ID
     */
    public Set<Long> getBlacklistFoodIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        List<Long> foodIds = userBlacklistMapper.selectFoodIdsByUserId(userId);
        return new HashSet<>(foodIds);
    }

    private BlacklistResponse toResponse(UserBlacklist blacklist, Food food) {
        return BlacklistResponse.builder()
                .id(blacklist.getId())
                .foodId(blacklist.getFoodId())
                .foodName(food != null ? food.getName() : "未知")
                .category(food != null ? food.getCategory() : "未知")
                .reason(blacklist.getReason())
                .createdAt(blacklist.getCreatedAt())
                .build();
    }
}
