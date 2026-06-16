package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.common.EatRecordStatus;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.EatRecord;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.dto.request.CompleteRecordRequest;
import com.fantuan.eatwhat.dto.request.DecideRecordRequest;
import com.fantuan.eatwhat.dto.request.EatRecordRequest;
import com.fantuan.eatwhat.dto.request.ReviewRecordRequest;
import com.fantuan.eatwhat.dto.response.EatRecordResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.EatRecordMapper;
import com.fantuan.eatwhat.mapper.FoodMapper;
import com.fantuan.eatwhat.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EatRecordServiceTest {

    @Mock
    private EatRecordMapper eatRecordMapper;

    @Mock
    private FoodMapper foodMapper;

    @Mock
    private com.fantuan.eatwhat.mapper.UserCustomFoodMapper userCustomFoodMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private EatRecordService eatRecordService;

    // ==================== 旧接口兼容测试 ====================

    @Test
    void createRecord_success() {
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");
        request.setRating(5);
        request.setNote("很好吃");

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");
        food.setEnabled(true);

        when(foodMapper.selectById(1L)).thenReturn(food);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());
        when(eatRecordMapper.insert(any(EatRecord.class))).thenReturn(1);

        EatRecordResponse response = eatRecordService.createRecord(userId, request);

        assertNotNull(response);
        assertEquals(1L, response.getFoodId());
        assertEquals("猪脚饭", response.getFoodName());
        assertEquals("快餐", response.getCategory());
        assertEquals("晚餐", response.getMealType());
        assertEquals(EatRecordStatus.EATEN, response.getStatus());
        assertEquals(5, response.getRating());
        assertEquals("很好吃", response.getNote());
        assertNotNull(response.getEatenAt());
        assertNull(response.getDecidedAt());
    }

    @Test
    void createRecord_foodNotFound() {
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(999L);
        request.setMealType("晚餐");

        when(foodMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.createRecord(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void createRecord_foodDisabled() {
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");
        food.setEnabled(false);

        when(foodMapper.selectById(1L)).thenReturn(food);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.createRecord(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 决定接口测试 ====================

    @Test
    void createDecision_success() {
        Long userId = 1L;
        DecideRecordRequest request = new DecideRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");
        food.setEnabled(true);

        when(foodMapper.selectById(1L)).thenReturn(food);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(eatRecordMapper.insert(any(EatRecord.class))).thenReturn(1);

        EatRecordResponse response = eatRecordService.createDecision(userId, request);

        assertNotNull(response);
        assertEquals(1L, response.getFoodId());
        assertEquals("猪脚饭", response.getFoodName());
        assertEquals(EatRecordStatus.DECIDED, response.getStatus());
        assertNotNull(response.getDecidedAt());
        assertNull(response.getEatenAt());
    }

    @Test
    void createDecision_idempotent_sameFoodSameMealType() {
        Long userId = 1L;
        DecideRecordRequest request = new DecideRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");
        food.setEnabled(true);

        EatRecord existingDecided = new EatRecord();
        existingDecided.setId(100L);
        existingDecided.setUserId(userId);
        existingDecided.setFoodId(1L);
        existingDecided.setMealType("晚餐");
        existingDecided.setStatus(EatRecordStatus.DECIDED);
        existingDecided.setDecidedAt(LocalDateTime.now().minusMinutes(5));

        when(foodMapper.selectById(1L)).thenReturn(food);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingDecided);

        // 同一食物 + 同一餐段 → 幂等返回，不删除不插入不更新
        EatRecordResponse response = eatRecordService.createDecision(userId, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getFoodId());
        assertEquals("晚餐", response.getMealType());
        verify(eatRecordMapper, never()).deleteById(anyLong());
        verify(eatRecordMapper, never()).insert(any(EatRecord.class));
        verify(eatRecordMapper, never()).updateById(any(EatRecord.class));
    }

    @Test
    void createDecision_idempotent_sameFoodDifferentMealType() {
        Long userId = 1L;
        DecideRecordRequest request = new DecideRecordRequest();
        request.setFoodId(1L);
        request.setMealType("午餐"); // 新餐段

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");
        food.setEnabled(true);

        EatRecord existingDecided = new EatRecord();
        existingDecided.setId(100L);
        existingDecided.setUserId(userId);
        existingDecided.setFoodId(1L);
        existingDecided.setMealType("晚餐"); // 旧餐段
        existingDecided.setStatus(EatRecordStatus.DECIDED);
        existingDecided.setDecidedAt(LocalDateTime.now().minusMinutes(5));

        when(foodMapper.selectById(1L)).thenReturn(food);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingDecided);
        when(eatRecordMapper.updateById(any(EatRecord.class))).thenReturn(1);

        // 同一食物 + 不同餐段 → 更新 mealType，返回原记录
        EatRecordResponse response = eatRecordService.createDecision(userId, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("午餐", response.getMealType());
        verify(eatRecordMapper).updateById(any(EatRecord.class));
        verify(eatRecordMapper, never()).deleteById(anyLong());
        verify(eatRecordMapper, never()).insert(any(EatRecord.class));
    }

    @Test
    void createDecision_replacesDifferentFood() {
        Long userId = 1L;
        DecideRecordRequest request = new DecideRecordRequest();
        request.setFoodId(2L);
        request.setMealType("午餐");

        Food food2 = new Food();
        food2.setId(2L);
        food2.setName("黄焖鸡");
        food2.setCategory("快餐");
        food2.setEnabled(true);

        EatRecord existingDecided = new EatRecord();
        existingDecided.setId(100L);
        existingDecided.setUserId(userId);
        existingDecided.setFoodId(1L);
        existingDecided.setStatus(EatRecordStatus.DECIDED);

        when(foodMapper.selectById(2L)).thenReturn(food2);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingDecided);
        when(eatRecordMapper.insert(any(EatRecord.class))).thenReturn(1);

        // 不同食物 → 删除旧 DECIDED，插入新 DECIDED
        EatRecordResponse response = eatRecordService.createDecision(userId, request);

        assertNotNull(response);
        assertEquals(2L, response.getFoodId());
        verify(eatRecordMapper).deleteById(100L);
        verify(eatRecordMapper).insert(any(EatRecord.class));
    }

    @Test
    void createDecision_foodNotFound() {
        Long userId = 1L;
        DecideRecordRequest request = new DecideRecordRequest();
        request.setFoodId(999L);
        request.setMealType("晚餐");

        when(foodMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.createDecision(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 完成接口测试 ====================

    @Test
    void completeRecord_success() {
        Long userId = 1L;
        Long recordId = 100L;
        CompleteRecordRequest request = new CompleteRecordRequest();
        request.setRating(5);
        request.setNote("很好吃");

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setMealType("晚餐");
        record.setStatus(EatRecordStatus.DECIDED);
        record.setDecidedAt(LocalDateTime.now().minusHours(1));

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(eatRecordMapper.updateById(record)).thenReturn(1);
        when(foodMapper.selectById(1L)).thenReturn(food);

        EatRecordResponse response = eatRecordService.completeRecord(userId, recordId, request);

        assertNotNull(response);
        assertEquals(EatRecordStatus.EATEN, response.getStatus());
        assertNotNull(response.getEatenAt());
        assertEquals(5, response.getRating());
        assertEquals("很好吃", response.getNote());
    }

    @Test
    void completeRecord_notDecided_fails() {
        Long userId = 1L;
        Long recordId = 100L;
        CompleteRecordRequest request = new CompleteRecordRequest();
        request.setRating(5);

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setStatus(EatRecordStatus.EATEN);

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.completeRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_STATUS_INVALID.getCode(), exception.getCode());
    }

    @Test
    void completeRecord_notFound() {
        Long userId = 1L;
        Long recordId = 999L;
        CompleteRecordRequest request = new CompleteRecordRequest();

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.completeRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void completeRecord_notOwner_returnsNotFound() {
        Long userId = 1L;
        Long recordId = 100L;
        CompleteRecordRequest request = new CompleteRecordRequest();

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.completeRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void completeRecord_updateReturnsZero_throwsNotFound() {
        Long userId = 1L;
        Long recordId = 100L;
        CompleteRecordRequest request = new CompleteRecordRequest();
        request.setRating(5);

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setMealType("晚餐");
        record.setStatus(EatRecordStatus.DECIDED);

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        // updateById 返回 0 → 记录已被其他操作删除
        when(eatRecordMapper.updateById(record)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.completeRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 取消接口测试 ====================

    @Test
    void cancelDecision_success() {
        Long userId = 1L;
        Long recordId = 100L;

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setStatus(EatRecordStatus.DECIDED);

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(eatRecordMapper.deleteById(recordId)).thenReturn(1);

        assertDoesNotThrow(() -> eatRecordService.cancelDecision(userId, recordId));
        verify(eatRecordMapper).deleteById(recordId);
    }

    @Test
    void cancelDecision_notDecided_fails() {
        Long userId = 1L;
        Long recordId = 100L;

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setStatus(EatRecordStatus.EATEN);

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.cancelDecision(userId, recordId));
        assertEquals(ResultCode.RECORD_STATUS_INVALID.getCode(), exception.getCode());
        verify(eatRecordMapper, never()).deleteById(anyLong());
    }

    @Test
    void cancelDecision_deleteReturnsZero_throwsNotFound() {
        Long userId = 1L;
        Long recordId = 100L;

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setStatus(EatRecordStatus.DECIDED);

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        // deleteById 返回 0 → 记录已被其他操作删除
        when(eatRecordMapper.deleteById(recordId)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.cancelDecision(userId, recordId));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 评价接口测试 ====================

    @Test
    void reviewRecord_success() {
        Long userId = 1L;
        Long recordId = 100L;
        ReviewRecordRequest request = new ReviewRecordRequest();
        request.setRating(4);
        request.setNote("还不错");

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setMealType("晚餐");
        record.setStatus(EatRecordStatus.EATEN);
        record.setRating(3);
        record.setNote("一般");
        record.setEatenAt(LocalDateTime.now().minusDays(1));

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");

        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(eatRecordMapper.updateById(record)).thenReturn(1);
        when(foodMapper.selectById(1L)).thenReturn(food);

        EatRecordResponse response = eatRecordService.reviewRecord(userId, recordId, request);

        assertNotNull(response);
        assertEquals(4, response.getRating());
        assertEquals("还不错", response.getNote());
    }

    @Test
    void reviewRecord_notEaten_fails() {
        Long userId = 1L;
        Long recordId = 100L;
        ReviewRecordRequest request = new ReviewRecordRequest();
        request.setRating(4);

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setStatus(EatRecordStatus.DECIDED);

        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.reviewRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_STATUS_INVALID.getCode(), exception.getCode());
    }

    @Test
    void reviewRecord_notOwner_returnsNotFound() {
        Long userId = 1L;
        Long recordId = 100L;
        ReviewRecordRequest request = new ReviewRecordRequest();

        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.reviewRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void reviewRecord_updateReturnsZero_throwsNotFound() {
        Long userId = 1L;
        Long recordId = 100L;
        ReviewRecordRequest request = new ReviewRecordRequest();
        request.setRating(4);

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setStatus(EatRecordStatus.EATEN);
        record.setRating(3);

        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(eatRecordMapper.updateById(record)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.reviewRecord(userId, recordId, request));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 详情接口测试 ====================

    @Test
    void getRecord_success() {
        Long userId = 1L;
        Long recordId = 100L;

        EatRecord record = new EatRecord();
        record.setId(recordId);
        record.setUserId(userId);
        record.setFoodId(1L);
        record.setMealType("晚餐");
        record.setStatus(EatRecordStatus.DECIDED);
        record.setDecidedAt(LocalDateTime.now());

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");

        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);
        when(foodMapper.selectById(1L)).thenReturn(food);

        EatRecordResponse response = eatRecordService.getRecord(userId, recordId);

        assertNotNull(response);
        assertEquals(recordId, response.getId());
        assertEquals("猪脚饭", response.getFoodName());
        assertEquals("快餐", response.getCategory());
        assertEquals(EatRecordStatus.DECIDED, response.getStatus());
    }

    @Test
    void getRecord_notFound() {
        Long userId = 1L;
        Long recordId = 999L;

        when(eatRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.getRecord(userId, recordId));
        assertEquals(ResultCode.RECORD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 列表查询测试 ====================

    @SuppressWarnings("unchecked")
    @Test
    void listRecords_includesStatusAndCategory() {
        Long userId = 1L;
        int limit = 10;

        EatRecord record1 = new EatRecord();
        record1.setId(1L);
        record1.setUserId(userId);
        record1.setFoodId(1L);
        record1.setMealType("晚餐");
        record1.setStatus(EatRecordStatus.EATEN);
        record1.setEatenAt(LocalDateTime.now().minusDays(2));

        EatRecord record2 = new EatRecord();
        record2.setId(2L);
        record2.setUserId(userId);
        record2.setFoodId(2L);
        record2.setMealType("午餐");
        record2.setStatus(EatRecordStatus.DECIDED);
        record2.setDecidedAt(LocalDateTime.now());

        List<EatRecord> records = List.of(record2, record1);

        when(eatRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        Food food1 = new Food();
        food1.setId(1L);
        food1.setName("猪脚饭");
        food1.setCategory("快餐");

        Food food2 = new Food();
        food2.setId(2L);
        food2.setName("黄焖鸡");
        food2.setCategory("快餐");

        when(foodMapper.selectBatchIds(anyList())).thenReturn(List.of(food1, food2));

        List<EatRecordResponse> response = eatRecordService.listRecords(userId, limit);

        assertEquals(2, response.size());
        assertEquals(EatRecordStatus.DECIDED, response.get(0).getStatus());
        assertEquals(EatRecordStatus.EATEN, response.get(1).getStatus());
        assertEquals("快餐", response.get(0).getCategory());
    }

    // ==================== 旧接口清理 DECIDED 测试 ====================

    @Test
    void createRecord_clearsExistingDecided() {
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");
        request.setRating(5);

        Food food = new Food();
        food.setId(1L);
        food.setName("猪脚饭");
        food.setCategory("快餐");
        food.setEnabled(true);

        EatRecord existingDecided = new EatRecord();
        existingDecided.setId(100L);
        existingDecided.setUserId(userId);
        existingDecided.setFoodId(1L);
        existingDecided.setStatus(EatRecordStatus.DECIDED);

        when(foodMapper.selectById(1L)).thenReturn(food);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existingDecided));
        when(eatRecordMapper.deleteById(100L)).thenReturn(1);
        when(eatRecordMapper.insert(any(EatRecord.class))).thenReturn(1);

        EatRecordResponse response = eatRecordService.createRecord(userId, request);

        assertNotNull(response);
        assertEquals(EatRecordStatus.EATEN, response.getStatus());
        verify(eatRecordMapper).deleteById(100L);
        verify(eatRecordMapper).insert(any(EatRecord.class));
    }

    @Test
    void createRecord_clearsExistingDecidedDifferentFood() {
        Long userId = 1L;
        EatRecordRequest request = new EatRecordRequest();
        request.setFoodId(2L);
        request.setMealType("午餐");

        Food food2 = new Food();
        food2.setId(2L);
        food2.setName("黄焖鸡");
        food2.setCategory("快餐");
        food2.setEnabled(true);

        EatRecord existingDecided = new EatRecord();
        existingDecided.setId(100L);
        existingDecided.setUserId(userId);
        existingDecided.setFoodId(1L);
        existingDecided.setStatus(EatRecordStatus.DECIDED);

        when(foodMapper.selectById(2L)).thenReturn(food2);
        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(userId);
        when(eatRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existingDecided));
        when(eatRecordMapper.deleteById(100L)).thenReturn(1);
        when(eatRecordMapper.insert(any(EatRecord.class))).thenReturn(1);

        EatRecordResponse response = eatRecordService.createRecord(userId, request);

        assertNotNull(response);
        assertEquals(EatRecordStatus.EATEN, response.getStatus());
        verify(eatRecordMapper).deleteById(100L);
    }

    @Test
    void createDecision_userNotFound() {
        Long userId = 999L;
        DecideRecordRequest request = new DecideRecordRequest();
        request.setFoodId(1L);
        request.setMealType("晚餐");

        when(userMapper.selectUserIdForUpdate(userId)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eatRecordService.createDecision(userId, request));
        assertEquals(ResultCode.USER_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 最近吃过查询 ====================

    @Test
    void getRecentEatenFoodMap_nullUserId() {
        Map<Long, LocalDateTime> result = eatRecordService.getRecentEatenFoodMap(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRecentEatenFoodMap_onlyEatenRecords() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> row = Map.of(
                "foodId", 1L,
                "lastEatenAt", now.minusHours(12)
        );

        when(eatRecordMapper.selectRecentEatenFoods(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(row));

        Map<Long, LocalDateTime> result = eatRecordService.getRecentEatenFoodMap(userId);

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
    }
}
