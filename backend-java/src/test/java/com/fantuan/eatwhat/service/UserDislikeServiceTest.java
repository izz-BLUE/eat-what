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

    // ==================== 精确匹配场景（新分类体系） ====================

    @Test
    void addDislike_typeTagMatch() {
        // "面食" 可以匹配 type_tags=面食 的菜品
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("面食");

        Food food = createFood(1L, "日料", "面食", "日料");
        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(food));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertEquals("面食", response.getCategory());
    }

    @Test
    void addDislike_cuisineTagMatch() {
        // "日料" 可以匹配 cuisine_tags=日料 的菜品
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("日料");

        Food food = createFood(1L, "日料", "面食", "日料");
        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(food));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertEquals("日料", response.getCategory());
    }

    @Test
    void addDislike_categoryExactMatch() {
        // category 精确匹配
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");

        Food food = createFood(1L, "火锅", "火锅", "");
        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(food));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertEquals("火锅", response.getCategory());
    }

    @Test
    void addDislike_substringNotMatch() {
        // 子串不得误匹配："面" 不能匹配 "面食"
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("面"); // 不是合法分类值，词典直接拒绝

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
        // 验证从未查询 DB（词典校验直接拦截）
        verify(foodMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(foodMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void addDislike_emptyCategory() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addDislike_nullCategory() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addDislike_categoryWithSpaces() {
        // 带空格标签：trim 后精确匹配
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory(" 面食 "); // 带空格，词典校验不通过

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addDislike_disabledOnly_notFound() {
        // 只有禁用菜品使用该分类时，应视为不存在
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("面食"); // 词典合法

        // 返回空列表（没有启用的菜品使用该分类）
        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addDislike_onlyDisabledFoodHasCategory_notFound() {
        // 数据库中有该分类的菜品但都是 disabled，selectList 只查 enabled=true
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("面食");

        // selectList 查询 enabled=true，返回空
        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    // ==================== 已有基础测试（适配 selectList） ====================

    @Test
    void addDislike_defaultDays() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");

        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(createFood(1L, "火锅", "火锅", "")));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertEquals("火锅", response.getCategory());
        assertNotNull(response.getExpiresAt());
        assertNotNull(response.getCreatedAt());
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(2)));
    }

    @Test
    void addDislike_customDays() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");
        request.setDays(7);

        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(createFood(1L, "火锅", "火锅", "")));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userDislikeMapper.insert(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void addDislike_categoryNotFound() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("不存在的分类");

        // 词典校验会先拦截非法值，不会到达 DB 查询
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userDislikeService.addDislike(userId, request));
        assertEquals(ResultCode.FOOD_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void addDislike_duplicateNoNewRecord() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");
        request.setDays(3);

        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(createFood(1L, "火锅", "火锅", "")));

        UserDislike existing = new UserDislike();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setCategory("火锅");
        existing.setExpiresAt(LocalDateTime.now().plusDays(1));
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(userDislikeMapper, never()).insert(any());
    }

    @Test
    void addDislike_duplicateRefreshesExpiresAt() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");
        request.setDays(5);

        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(createFood(1L, "火锅", "火锅", "")));

        UserDislike existing = new UserDislike();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setCategory("火锅");
        existing.setExpiresAt(LocalDateTime.now().plusDays(1));
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(4)));
    }

    @Test
    void addDislike_expiredRecordRestored() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");
        request.setDays(3);

        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(createFood(1L, "火锅", "火锅", "")));

        UserDislike expired = new UserDislike();
        expired.setId(1L);
        expired.setUserId(1L);
        expired.setCategory("火锅");
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));
        expired.setCreatedAt(LocalDateTime.now().minusDays(5));
        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(expired);
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

        assertNotNull(response);
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now().plusDays(2)));
    }

    @Test
    void addDislike_concurrentDuplicateUpdatesExpiresAt() {
        Long userId = 1L;
        DislikeAddRequest request = new DislikeAddRequest();
        request.setCategory("火锅");
        request.setDays(3);

        when(foodMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(createFood(1L, "火锅", "火锅", "")));

        when(userDislikeMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)
                .thenReturn(createExistingDislike());

        when(userDislikeMapper.insert(any(UserDislike.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry"));
        when(userDislikeMapper.updateById(any(UserDislike.class))).thenReturn(1);

        DislikeResponse response = userDislikeService.addDislike(userId, request);

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
        assertEquals("火锅", result.get(0).getCategory());
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

    // ==================== 辅助方法 ====================

    private Food createFood(Long id, String category, String typeTags, String cuisineTags) {
        Food food = new Food();
        food.setId(id);
        food.setName("测试菜品");
        food.setCategory(category);
        food.setTypeTags(typeTags);
        food.setCuisineTags(cuisineTags);
        food.setEnabled(true);
        return food;
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
