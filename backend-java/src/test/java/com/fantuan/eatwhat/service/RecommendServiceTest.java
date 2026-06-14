package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.dto.request.RecommendRequest;
import com.fantuan.eatwhat.dto.response.FoodResponse;
import com.fantuan.eatwhat.dto.response.RecommendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendServiceTest {

    @Mock
    private FoodService foodService;

    @Mock
    private EatRecordService eatRecordService;

    @Mock
    private UserBlacklistService userBlacklistService;

    @Mock
    private UserDislikeService userDislikeService;

    @InjectMocks
    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        when(foodService.toResponse(any(Food.class))).thenAnswer(invocation -> {
            Food food = invocation.getArgument(0);
            return FoodResponse.builder()
                    .id(food.getId())
                    .name(food.getName())
                    .category(food.getCategory())
                    .tasteTags(food.getTasteTags())
                    .priceLevel(food.getPriceLevel())
                    .build();
        });
    }

    @Test
    void recommend_noUserId_usesBasicLogic() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertNotNull(response.getFood());
        assertTrue(response.getScore() > 0);
    }

    @Test
    void recommend_withUserId_appliesDeduction() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food recentFood = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        Food normalFood = createFood(2L, "寿司", "日料", "清淡,鲜", 3);

        when(foodService.listAllEnabled()).thenReturn(List.of(recentFood, normalFood));

        Map<Long, LocalDateTime> recentEatenMap = Map.of(1L, LocalDateTime.now().minusHours(12));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(recentEatenMap);

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertNotNull(response.getFood());
    }

    @Test
    void recommend_noRecentEaten_showsReason() {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(1L);

        Food food = createFood(1L, "猪脚饭", "快餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("最近几天没吃过，换换口味"));
    }

    // ========== 黑名单过滤测试 ==========

    @Test
    void recommend_noUserId_doesNotQueryBlacklist() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // userId 为空时不应查询黑名单
        verifyNoInteractions(userBlacklistService);
    }

    @Test
    void recommend_blacklistFiltersOutFood() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));

        // 黑名单包含 food1
        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // food1 被黑名单过滤，只能推荐 food2
        assertEquals(2L, response.getFood().getId());
    }

    @Test
    void recommend_blacklistAndExcludeFoodIdsBothApply() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);
        request.setExcludeFoodIds(List.of(2L));

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "清淡,鲜", 3);
        Food food3 = createFood(3L, "烤肉", "烧烤", "咸,香", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));

        // 黑名单包含 food1
        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // food1 被黑名单过滤，food2 被 excludeFoodIds 排除，只能推荐 food3
        assertEquals(3L, response.getFood().getId());
    }

    @Test
    void recommend_allFilteredByBlacklist_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));

        // 所有菜品都在黑名单中
        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));

        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    // ========== 不想吃过滤测试 ==========

    @Test
    void recommend_noUserId_doesNotQueryDislike() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        recommendService.recommend(request);

        // userId 为空时不应查询不想吃
        verifyNoInteractions(userDislikeService);
    }

    @Test
    void recommend_dislikeFiltersOutCategory() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));

        // 不想吃火锅
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of("火锅"));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 火锅被过滤，只能推荐寿司
        assertEquals(2L, response.getFood().getId());
        assertEquals("日料", response.getFood().getCategory());
    }

    @Test
    void recommend_expiredDislikeDoesNotAffect() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));

        // 没有有效的不想吃记录
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of());
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 火锅没有被过滤（过期的不想吃不影响）
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_blacklistAndDislikeAndExcludeAllApply() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setUserId(1L);
        request.setExcludeFoodIds(List.of(3L)); // excludeFoodIds 排除寿司

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        Food food2 = createFood(2L, "川菜", "川菜", "辣", 2);
        Food food3 = createFood(3L, "寿司", "日料", "清淡,鲜", 3);
        Food food4 = createFood(4L, "猪脚饭", "快餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3, food4));

        // 黑名单排除火锅
        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));
        // 不想吃川菜
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of("川菜"));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 火锅被黑名单过滤，川菜被不想吃过滤，寿司被 excludeFoodIds 排除，只能推荐猪脚饭
        assertEquals(4L, response.getFood().getId());
    }

    @Test
    void recommend_allFilteredByDislike_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));

        // 不想吃火锅（唯一候选）
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of("火锅"));
        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    // ========== 边界测试：calculateRecentEatenDeduction ==========

    @Test
    void deduction_23h59m_returns100() throws Exception {
        int result = invokeDeduction(23, 59);
        assertEquals(-100, result);
    }

    @Test
    void deduction_24h01m_returns80() throws Exception {
        int result = invokeDeduction(24, 1);
        assertEquals(-80, result);
    }

    @Test
    void deduction_47h59m_returns80() throws Exception {
        int result = invokeDeduction(47, 59);
        assertEquals(-80, result);
    }

    @Test
    void deduction_48h01m_returns60() throws Exception {
        int result = invokeDeduction(48, 1);
        assertEquals(-60, result);
    }

    @Test
    void deduction_71h59m_returns60() throws Exception {
        int result = invokeDeduction(71, 59);
        assertEquals(-60, result);
    }

    @Test
    void deduction_72h01m_returns30() throws Exception {
        int result = invokeDeduction(72, 1);
        assertEquals(-30, result);
    }

    @Test
    void deduction_168h01m_returns0() throws Exception {
        int result = invokeDeduction(168, 1);
        assertEquals(0, result);
    }

    @Test
    void deduction_exactly168h_returns30() throws Exception {
        int result = invokeDeductionSeconds(168, 0, 0);
        assertEquals(-30, result);
    }

    @Test
    void deduction_168h1s_returns0() throws Exception {
        int result = invokeDeductionSeconds(168, 0, 1);
        assertEquals(0, result);
    }

    /**
     * 通过反射调用私有方法 calculateRecentEatenDeduction（小时+分钟）
     */
    private int invokeDeduction(long hours, long minutes) throws Exception {
        return invokeDeductionSeconds(hours, minutes, 0);
    }

    private int invokeDeductionSeconds(long hours, long minutes, long seconds) throws Exception {
        Method method = RecommendService.class.getDeclaredMethod(
                "calculateRecentEatenDeduction", Long.class, Map.class, LocalDateTime.class);
        method.setAccessible(true);

        Long foodId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastEatenAt = now.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds);
        Map<Long, LocalDateTime> recentEatenMap = new HashMap<>();
        recentEatenMap.put(foodId, lastEatenAt);

        return (int) method.invoke(recommendService, foodId, recentEatenMap, now);
    }

    private Food createFood(Long id, String name, String category, String tasteTags, int priceLevel) {
        Food food = new Food();
        food.setId(id);
        food.setName(name);
        food.setCategory(category);
        food.setTasteTags(tasteTags);
        food.setPriceLevel(priceLevel);
        food.setEnabled(true);
        return food;
    }
}
