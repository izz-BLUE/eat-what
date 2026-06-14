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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendServiceTest {

    @Mock
    private FoodService foodService;

    @Mock
    private EatRecordService eatRecordService;

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
