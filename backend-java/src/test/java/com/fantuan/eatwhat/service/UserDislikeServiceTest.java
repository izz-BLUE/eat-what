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
class UserDislikeServiceTest {

    @Mock
    private UserDislikeMapper userDislikeMapper;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private UserDislikeService userDislikeService;

    @Test
    void addDislike_defaultDays() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        // days 默认 3

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Food());
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(request);

        assertNotNull(response);
        assertEquals("火锅", response.getCategory());
        assertNotNull(response.getExpiresAt());
        assertNotNull(response.getCreatedAt());
        // 验证 expiresAt 大约是 3 天后
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(2)));
    }

    @Test
    void addDislike_customDays() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(7);

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Food());
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(request);

        assertNotNull(response);
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void addDislike_categoryNotFound() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("不存在的分类");

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addDislike_duplicateNoNewRecord() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(3);

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Food());

        UserDislike existing = new UserDislike();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setCategory("火锅");
        existing.setExpiresAt(LocalDateTime.now().plusDays(1));
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(userDislikeMapper, never()).insert(any());
    }

    @Test
    void addDislike_duplicateRefreshesExpiresAt() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(5);

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Food());

        UserDislike existing = new UserDislike();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setCategory("火锅");
        existing.setExpiresAt(LocalDateTime.now().plusDays(1)); // 即将过期
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(request);

        assertNotNull(response);
        // expiresAt 应该被刷新为大约 5 天后
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(4)));
    }

    @Test
    void addDislike_expiredRecordRestored() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(3);

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Food());

        // 已过期记录
        UserDislike expired = new UserDislike();
        expired.setId(1L);
        expired.setUserId(1L);
        expired.setCategory("火锅");
        expired.setExpiresAt(LocalDateTime.now().minusDays(1)); // 已过期
        expired.setCreatedAt(LocalDateTime.now().minusDays(5));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expired);
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(request);

        assertNotNull(response);
        // 过期记录被恢复，expiresAt 更新为 3 天后
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(2)));
    }

    @Test
    void addDislike_concurrentDuplicateUpdatesExpiresAt() {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(3);

        when(foodMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Food());

        // 第一次查询不存在
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)
                .thenReturn(createExistingDislike());

        // 插入时抛出 DuplicateKeyException
        when(userDislikeMapper.insert(any(UserDislike.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry"));
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(request);

        assertNotNull(response);
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(2)));
    }

    @Test
    void listActiveDislikes_onlyReturnsUnexpired() {
        Long userId = 1L;

        UserDislike active = new UserDislike();
        active.setId(1L);
        active.setCategory("火锅");
        active.setExpiresAt(LocalDateTime.now().plusDays(2));
        active.setCreatedAt(LocalDateTime.now());

        when(userDislikeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(active));

        List<DislikeResponse> result = userDislikeService.listActiveDislikes(userId);

        assertEquals(1, result.size());
        assertEquals("火锅", result.get(0).getCategory());
    }

    @Test
    void listActiveDislikes_orderedByExpiresAtAsc() {
        Long userId = 1L;

        UserDislike soon = new UserDislike();
        soon.setId(1L);
        soon.setCategory("火锅");
        soon.setExpiresAt(LocalDateTime.now().plusDays(1));
        soon.setCreatedAt(LocalDateTime.now());

        UserDislike later = new UserDislike();
        later.setId(2L);
        later.setCategory("川菜");
        later.setExpiresAt(LocalDateTime.now().plusDays(5));
        later.setCreatedAt(LocalDateTime.now());

        when(userDislikeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(soon, later));

        List<DislikeResponse> result = userDislikeService.listActiveDislikes(userId);

        assertEquals(2, result.size());
        assertEquals("火锅", result.get(0).getCategory()); // 快到期的在前
        assertEquals("川菜", result.get(1).getCategory());
    }

    @Test
    void removeDislike_success() {
        UserDislike dislike = new UserDislike();
        dislike.setId(1L);
        dislike.setUserId(1L);
        dislike.setCategory("火锅");

        when(userDislikeMapper.selectById(1L)).thenReturn(dislike);
        when(userDislikeMapper.deleteById(1L)).thenReturn(1);

        userDislikeService.removeDislike(1L, 1L);

        verify(userDislikeMapper).deleteById(1L);
    }

    @Test
    void removeDislike_notFound() {
        when(userDislikeMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.removeDislike(999L, 1L));
        assertEquals(ResultCode.DISLIKE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void removeDislike_notBelongToUser() {
        UserDislike dislike = new UserDislike();
        dislike.setId(1L);
        dislike.setUserId(1L);

        when(userDislikeMapper.selectById(1L)).thenReturn(dislike);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.removeDislike(1L, 2L));
        assertEquals(ResultCode.DISLIKE_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void getActiveDislikeCategories_nullUserId() {
        Set<String> result = userDislikeService.getActiveDislikeCategories(null, LocalDateTime.now());
        assertTrue(result.isEmpty());
    }

    @Test
    void getActiveDislikeCategories_withData() {
        LocalDateTime now = LocalDateTime.now();
        when(userDislikeMapper.selectActiveCategories(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of("火锅", "川菜"));

        Set<String> result = userDislikeService.getActiveDislikeCategories(1L, now);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(Set.of("火锅", "川菜")));
    }

    private UserDislike createExistingDislike() {
        UserDislike dislike = new UserDislike();
        dislike.setId(1L);
        dislike.setUserId(1L);
        dislike.setCategory("火锅");
        dislike.setExpiresAt(LocalDateTime.now().plusDays(1));
        dislike.setCreatedAt(LocalDateTime.now());
        return dislike;
    }
}
