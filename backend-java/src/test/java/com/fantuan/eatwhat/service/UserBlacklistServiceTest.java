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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UserBlacklistServiceTest {

    @Mock
    private UserBlacklistMapper userBlacklistMapper;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private UserBlacklistService userBlacklistService;

    @Test
    void addToBlacklist_success() {
        // Given
        Long userId = 1L;
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setFoodId(1L);
        request.setReason("不喜欢");

        Food food = createFood(1L, "猪脚饭", "快餐");
        when(foodMapper.selectById(1L)).thenReturn(food);
        when(userBlacklistMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userBlacklistMapper.insert(any(UserBlacklist.class))).thenReturn(1);

        // When
        BlacklistResponse response = userBlacklistService.addToBlacklist(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getFoodId());
        assertEquals("猪脚饭", response.getFoodName());
        assertEquals("不喜欢", response.getReason());
        assertNotNull(response.getCreatedAt()); // createdAt 非空
    }

    @Test
    void addToBlacklist_foodNotFound() {
        // Given
        Long userId = 1L;
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setFoodId(999L);

        when(foodMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userBlacklistService.addToBlacklist(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addToBlacklist_duplicateNoNewRecord() {
        // Given
        Long userId = 1L;
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setFoodId(1L);
        request.setReason("不喜欢");

        Food food = createFood(1L, "猪脚饭", "快餐");
        when(foodMapper.selectById(1L)).thenReturn(food);

        UserBlacklist existing = new UserBlacklist();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setFoodId(1L);
        existing.setReason("不喜欢");
        existing.setCreatedAt(LocalDateTime.now());
        when(userBlacklistMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        // When
        BlacklistResponse response = userBlacklistService.addToBlacklist(userId, request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(userBlacklistMapper, never()).insert(any());
    }

    @Test
    void addToBlacklist_duplicateUpdatesReason() {
        // Given
        Long userId = 1L;
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setFoodId(1L);
        request.setReason("新原因");

        Food food = createFood(1L, "猪脚饭", "快餐");
        when(foodMapper.selectById(1L)).thenReturn(food);

        UserBlacklist existing = new UserBlacklist();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setFoodId(1L);
        existing.setReason("旧原因");
        existing.setCreatedAt(LocalDateTime.now());
        when(userBlacklistMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userBlacklistMapper.updateById(any(UserBlacklist.class))).thenReturn(1);

        // When
        BlacklistResponse response = userBlacklistService.addToBlacklist(userId, request);

        // Then
        assertNotNull(response);
        assertEquals("新原因", response.getReason());
        verify(userBlacklistMapper).updateById(any());
    }

    @Test
    void addToBlacklist_concurrentDuplicateUpdatesReason() {
        // Given
        Long userId = 1L;
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setFoodId(1L);
        request.setReason("并发原因");

        Food food = createFood(1L, "猪脚饭", "快餐");
        when(foodMapper.selectById(1L)).thenReturn(food);

        // 第一次查询不存在
        when(userBlacklistMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)
                .thenReturn(createExistingBlacklist("旧原因"));

        // 插入时抛出 DuplicateKeyException
        when(userBlacklistMapper.insert(any(UserBlacklist.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry"));
        when(userBlacklistMapper.updateById(any(UserBlacklist.class))).thenReturn(1);

        // When
        BlacklistResponse response = userBlacklistService.addToBlacklist(userId, request);

        // Then
        assertNotNull(response);
        assertEquals("并发原因", response.getReason());
        verify(userBlacklistMapper).updateById(any());
    }

    @Test
    void listBlacklist_orderedByCreatedAtDesc() {
        // Given
        Long userId = 1L;

        UserBlacklist blacklist1 = new UserBlacklist();
        blacklist1.setId(1L);
        blacklist1.setFoodId(1L);
        blacklist1.setReason("原因1");
        blacklist1.setCreatedAt(LocalDateTime.now().minusDays(1));

        UserBlacklist blacklist2 = new UserBlacklist();
        blacklist2.setId(2L);
        blacklist2.setFoodId(2L);
        blacklist2.setReason("原因2");
        blacklist2.setCreatedAt(LocalDateTime.now());

        when(userBlacklistMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(blacklist2, blacklist1));

        Food food1 = createFood(1L, "猪脚饭", "快餐");
        Food food2 = createFood(2L, "黄焖鸡", "快餐");
        when(foodMapper.selectBatchIds(List.of(2L, 1L))).thenReturn(List.of(food2, food1));

        // When
        List<BlacklistResponse> result = userBlacklistService.listBlacklist(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getFoodId()); // 最新的在前
        assertEquals(1L, result.get(1).getFoodId());
    }

    @Test
    void removeFromBlacklist_success() {
        // Given
        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setId(1L);
        blacklist.setUserId(1L);
        blacklist.setFoodId(1L);

        when(userBlacklistMapper.selectById(1L)).thenReturn(blacklist);
        when(userBlacklistMapper.deleteById(1L)).thenReturn(1);

        // When
        userBlacklistService.removeFromBlacklist(1L, 1L);

        // Then
        verify(userBlacklistMapper).deleteById(1L);
    }

    @Test
    void removeFromBlacklist_notFound() {
        // Given
        when(userBlacklistMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userBlacklistService.removeFromBlacklist(999L, 1L));
        assertEquals(ResultCode.BLACKLIST_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void removeFromBlacklist_notBelongToUser() {
        // Given
        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setId(1L);
        blacklist.setUserId(1L); // 属于用户 1
        blacklist.setFoodId(1L);

        when(userBlacklistMapper.selectById(1L)).thenReturn(blacklist);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userBlacklistService.removeFromBlacklist(1L, 2L)); // 用户 2 尝试删除
        assertEquals(ResultCode.BLACKLIST_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void getBlacklistFoodIds_nullUserId() {
        // When
        Set<Long> result = userBlacklistService.getBlacklistFoodIds(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getBlacklistFoodIds_withData() {
        // Given
        when(userBlacklistMapper.selectFoodIdsByUserId(1L)).thenReturn(List.of(1L, 2L, 3L));

        // When
        Set<Long> result = userBlacklistService.getBlacklistFoodIds(1L);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Set.of(1L, 2L, 3L)));
    }

    private Food createFood(Long id, String name, String category) {
        Food food = new Food();
        food.setId(id);
        food.setName(name);
        food.setCategory(category);
        food.setEnabled(true);
        return food;
    }

    private UserBlacklist createExistingBlacklist(String reason) {
        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setId(1L);
        blacklist.setUserId(1L);
        blacklist.setFoodId(1L);
        blacklist.setReason(reason);
        blacklist.setCreatedAt(LocalDateTime.now());
        return blacklist;
    }
}
