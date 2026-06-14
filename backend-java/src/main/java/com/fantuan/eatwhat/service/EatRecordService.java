package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.EatRecord;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.dto.request.EatRecordRequest;
import com.fantuan.eatwhat.dto.response.EatRecordResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.EatRecordMapper;
import com.fantuan.eatwhat.mapper.FoodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 吃过记录服务
 */
@Service
@RequiredArgsConstructor
public class EatRecordService {

    private final EatRecordMapper eatRecordMapper;
    private final FoodMapper foodMapper;

    /**
     * 创建吃过记录
     */
    public EatRecordResponse createRecord(EatRecordRequest request) {
        // 校验食物是否存在
        Food food = foodMapper.selectById(request.getFoodId());
        if (food == null || !Boolean.TRUE.equals(food.getEnabled())) {
            throw new BusinessException(ResultCode.FOOD_NOT_FOUND);
        }

        // 创建记录
        EatRecord record = new EatRecord();
        record.setUserId(request.getUserId());
        record.setFoodId(request.getFoodId());
        record.setMealType(request.getMealType());
        record.setRating(request.getRating());
        record.setNote(request.getNote());
        record.setEatenAt(LocalDateTime.now());

        eatRecordMapper.insert(record);

        return EatRecordResponse.builder()
                .id(record.getId())
                .foodId(record.getFoodId())
                .foodName(food.getName())
                .mealType(record.getMealType())
                .rating(record.getRating())
                .note(record.getNote())
                .eatenAt(record.getEatenAt())
                .build();
    }

    /**
     * 查询用户最近吃过记录
     *
     * @param userId 用户ID
     * @param limit  返回数量
     * @return 吃过记录列表
     */
    public List<EatRecordResponse> listRecords(Long userId, int limit) {
        LambdaQueryWrapper<EatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EatRecord::getUserId, userId)
                .orderByDesc(EatRecord::getEatenAt)
                .last("LIMIT " + limit);

        List<EatRecord> records = eatRecordMapper.selectList(wrapper);

        // 批量查询食物名称
        List<Long> foodIds = records.stream()
                .map(EatRecord::getFoodId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> foodNameMap = new HashMap<>();
        if (!foodIds.isEmpty()) {
            List<Food> foods = foodMapper.selectBatchIds(foodIds);
            foodNameMap = foods.stream()
                    .collect(Collectors.toMap(Food::getId, Food::getName));
        }

        Map<Long, String> finalFoodNameMap = foodNameMap;
        return records.stream()
                .map(record -> EatRecordResponse.builder()
                        .id(record.getId())
                        .foodId(record.getFoodId())
                        .foodName(finalFoodNameMap.getOrDefault(record.getFoodId(), "未知"))
                        .mealType(record.getMealType())
                        .rating(record.getRating())
                        .note(record.getNote())
                        .eatenAt(record.getEatenAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 查询用户最近7天吃过的食物及其最近吃的时间
     *
     * @param userId 用户ID
     * @return 食物ID -> 最近吃的时间
     */
    public Map<Long, LocalDateTime> getRecentEatenFoodMap(Long userId) {
        if (userId == null) {
            return Map.of();
        }

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        List<Map<String, Object>> results = eatRecordMapper.selectRecentEatenFoods(userId, sevenDaysAgo);

        Map<Long, LocalDateTime> map = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long foodId = ((Number) row.get("foodId")).longValue();
            LocalDateTime lastEatenAt = (LocalDateTime) row.get("lastEatenAt");
            map.put(foodId, lastEatenAt);
        }
        return map;
    }
}
