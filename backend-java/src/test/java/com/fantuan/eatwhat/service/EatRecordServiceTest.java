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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EatRecordServiceTest {

    @Mock
    private EatRecordMapper eatRecordMapper;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private EatRecordService eatRecordService;

    @Test
    void createRecord_success() {
        // Given
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");
        request.setRating(5);
        request.setNote("很好吃");

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setEnabled(true);

        when(foodMapper.selectById(1L)).thenReturn(food);
        when(eatRecordMapper.insert(any(EatRecord.class))).thenReturn(1);

        // When
        EatRecordResponse response = eatRecordService.createRecord(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getFoodId());
        assertEquals("猪脚饭", response.getFoodName());
        assertEquals("晚餐", response.getMealType());
        assertEquals(5, response.getRating());
        assertEquals("很好吃", response.getNote());
        assertNotNull(response.getEatenAt());
    }

    @Test
    void createRecord_foodNotFound() {
        // Given
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(999L);
        request.setMealType("晚餐");

        when(foodMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.createRecord(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void createRecord_foodDisabled() {
        // Given
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setEnabled(false);

        when(foodMapper.selectById(1L)).thenReturn(food);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.createRecord(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    void listRecords_orderedByEatenAtDesc() {
        // Given
        Long userId = 1L;
        int limit = 10;

        EatRecord record1 = new EatRecord();
        record1.setId(1L);
        record1.setFoodId(1L);
        record1.setMealType("晚餐");
        record1.setEatenAt(LocalDateTime.now().minusDays(2));

        EatRecord record2 = new EatRecord();
        record2.setId(2L);
        record2.setFoodId(2L);
        record2.setMealType("午餐");
        record2.setEatenAt(LocalDateTime.now());

        List<EatRecord> records = List.of(record2, record1); // 已按 eatenAt 倒序

        when(eatRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        Food food1 = new Food();
        food1.setId(1L);
        food1.setName("猪脚饭");

        Food food2 = new Food();
        food2.setId(2L);
        food2.setName("黄焖鸡");

        when(foodMapper.selectBatchIds(List.of(2L, 1L))).thenReturn(List.of(food2, food1));

        // When
        List<EatRecordResponse> response = eatRecordService.listRecords(userId, limit);

        // Then
        assertEquals(2, response.size());
        assertEquals(2L, response.get(0).getFoodId()); // 最新的在前
        assertEquals(1L, response.get(1).getFoodId());
    }

    @Test
    void getRecentEatenFoodMap_multipleRecordsUseLatest() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        // 同一食物有两条记录，应该使用最近的一条
        Map<String, Object> row = Map.of(
                "foodId", 1L,
                "lastEatenAt", now.minusHours(12) // 最近一次是 12 小时前
        );

        when(eatRecordMapper.selectRecentEatenFoods(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(row));

        // When
        Map<Long, LocalDateTime> result = eatRecordService.getRecentEatenFoodMap(userId);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
        assertEquals(now.minusHours(12).toLocalDate(), result.get(1L).toLocalDate());
    }

    @Test
    void getRecentEatenFoodMap_nullUserId() {
        // When
        Map<Long, LocalDateTime> result = eatRecordService.getRecentEatenFoodMap(null);

        // Then
        assertTrue(result.isEmpty());
    }
}
