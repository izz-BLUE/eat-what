package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.FoodTaxonomy;
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
                    .typeTags(food.getTypeTags())
                    .cuisineTags(food.getCuisineTags())
                    .mealTypes(food.getMealTypes())
                    .tasteTags(food.getTasteTags())
                    .priceLevel(food.getPriceLevel())
                    .build();
        });
    }

    // ==================== 基础推荐测试 ====================

    @Test
    void recommend_noUserId_usesBasicLogic() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
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

        Food recentFood = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food normalFood = createFood(2L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);

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

        Food food = createFood(1L, "猪脚饭", "快餐", "快餐", "", "午餐,晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("最近几天没吃过，换换口味"));
    }

    // ==================== 黑名单过滤测试 ====================

    @Test
    void recommend_noUserId_doesNotQueryBlacklist() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        verifyNoInteractions(userBlacklistService);
    }

    @Test
    void recommend_blacklistFiltersOutFood() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));

        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId());
    }

    @Test
    void recommend_allFilteredByBlacklist_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));

        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));

        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    // ==================== 不想吃过滤测试 ====================

    @Test
    void recommend_noUserId_doesNotQueryDislike() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        recommendService.recommend(request);

        verifyNoInteractions(userDislikeService);
    }

    @Test
    void recommend_dislikeFiltersOutByCategory() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));

        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of("火锅"));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId());
    }

    @Test
    void recommend_dislikeFiltersOutByTypeTag() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 日料拉面：category=日料, typeTags=面食
        Food food1 = createFood(2L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        Food food2 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));

        // 不想吃"面食"→ 应过滤日料拉面（type_tags 含面食）
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of("面食"));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_dislikeFiltersOutByCuisineTag() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food1 = createFood(1L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        Food food2 = createFood(2L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));

        // 不想吃"日料"→ 应过滤日料拉面（cuisine_tags 含日料）
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of("日料"));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId());
    }

    // ==================== 分类过滤测试（type_tags + cuisine_tags OR） ====================

    @Test
    void recommend_typeTags_filtersCorrectly() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTypeTags(List.of("火锅"));

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_cuisineTags_filtersCorrectly() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setCuisineTags(List.of("日料"));

        Food food1 = createFood(1L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);
        Food food2 = createFood(2L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_typeTagsAndCuisineTags_orLogic() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTypeTags(List.of("面食"));
        request.setCuisineTags(List.of("日料"));

        // 云吞面：type=面食，日料拉面：type=面食+cuisine=日料，寿司：cuisine=日料
        Food food1 = createFood(1L, "云吞面", "面食", "面食", "", "晚餐", "清淡,鲜", 2);
        Food food2 = createFood(2L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        Food food3 = createFood(3L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);
        Food food4 = createFood(4L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3, food4));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 火锅不应被推荐（既不是面食也不是日料）
        assertNotEquals(4L, response.getFood().getId());
    }

    @Test
    void recommend_typeTagNoodles_matchesRamen() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setTypeTags(List.of("面食"));

        // 日料拉面：type=面食, cuisine=日料
        Food food1 = createFood(1L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        Food food2 = createFood(2L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_cuisineTagJapanese_matchesRamen() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setCuisineTags(List.of("日料"));

        Food food1 = createFood(1L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        Food food2 = createFood(2L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_allFilteredByCategories_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTypeTags(List.of("甜品"));

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    // ==================== 餐段硬过滤测试 ====================

    @Test
    void recommend_breakfast_excludesHotpot() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("早餐");

        // 火锅 mealTypes=午餐,晚餐,夜宵，不含早餐
        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        Food food2 = createFood(2L, "肠粉", "小吃", "小吃", "", "早餐,午餐,夜宵", "清淡,鲜", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId());
    }

    @Test
    void recommend_lunch_canRecommendRamen() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");

        // 日料拉面 mealTypes=午餐,晚餐
        Food food1 = createFood(1L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_milkTea_excludedFromAllMealTypes() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");

        Food food1 = createFood(1L, "奶茶", "甜品", "甜品", "", "", "甜", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // 奶茶 mealTypes 为空，任何餐段硬过滤都会排除
        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    @Test
    void recommend_noMealType_noFilter() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType(null);

        Food food1 = createFood(1L, "奶茶", "甜品", "甜品", "", "", "甜", 1);
        Food food2 = createFood(2L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // mealType 为空时跳过节段过滤，奶茶也可以被推荐
        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
    }

    // ==================== 口味硬过滤测试 ====================

    @Test
    void recommend_tasteLight_onlyMatchesLightTag() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("清淡");

        Food food1 = createFood(1L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        Food food2 = createFood(2L, "烤肉", "烧烤", "烧烤", "", "晚餐", "咸,香", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_tasteLight_doesNotExcludeHotpotCategory() {
        // 清淡不再排除火锅分类，仅按 tasteTags 过滤
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("清淡");
        request.setTypeTags(List.of("火锅"));

        // 火锅 tasteTags=辣,麻，不匹配清淡 → 无候选
        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        // 清淡不排除火锅分类，但火锅 tasteTags 不含"清淡"，所以口味硬过滤排除
        assertNull(response);
    }

    @Test
    void recommend_tasteLight_withLightTaggedHotpot_passes() {
        // 如果有火锅 tag 了"清淡"，则可以通过清淡硬过滤（虽然实际数据中没有）
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("清淡");

        Food food1 = createFood(1L, "清汤火锅", "火锅", "火锅", "", "晚餐", "清淡,鲜", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId());
    }

    @Test
    void recommend_tasteSpicy_onlyReturnsSpicy() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("辣");

        Food food1 = createFood(1L, "麻辣烫", "小吃", "小吃", "", "晚餐", "辣,麻", 2);
        Food food2 = createFood(2L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        Food food3 = createFood(3L, "黄焖鸡米饭", "快餐", "快餐", "", "晚餐", "咸,辣", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getFood().getId() == 1L || response.getFood().getId() == 3L);
    }

    @Test
    void recommend_tasteNoSpicy_excludesSpicyAndNumbing() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("不辣");

        Food food1 = createFood(1L, "麻辣烫", "小吃", "小吃", "", "晚餐", "辣,麻", 2);
        Food food2 = createFood(2L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        Food food3 = createFood(3L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertNotEquals(1L, response.getFood().getId()); // 麻辣烫被排除
    }

    @Test
    void recommend_tasteNoLimit_noFilter() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste(null);

        Food food1 = createFood(1L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        Food food2 = createFood(2L, "麻辣烫", "小吃", "小吃", "", "晚餐", "辣,麻", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
    }

    @Test
    void recommend_tasteStrong_removed_ignored() {
        // "重口" 已删除，传入时应被视为未知口味（Controller 层 1001，Service 层不处理）
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("重口");

        Food food1 = createFood(1L, "麻辣烫", "小吃", "小吃", "", "晚餐", "辣,麻", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // Service 层 matchesTaste 中 default 返回 true，所以重口被当作不限对待
        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
    }

    // ==================== 预算软评分测试 ====================

    @Test
    void recommend_priceScore_15以内_matchesLevel1() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel("15以内");

        Food food = createFood(1L, "沙县小吃", "小吃", "小吃", "", "午餐", "清淡", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("符合预算"));
    }

    @Test
    void recommend_priceScore_40以上_matchesLevel4() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("40以上");

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("符合预算"));
    }

    @Test
    void recommend_priceScore_noLimit_noBonus() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertFalse(response.getReasons().contains("符合预算"));
    }

    // ==================== 推荐理由测试 ====================

    @Test
    void recommend_categoryReason_includedWhenCategorySelected() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTypeTags(List.of("火锅"));

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("符合偏好分类"));
    }

    // ==================== 组合条件测试 ====================

    @Test
    void recommend_categoriesAndTasteAndBlacklist_allApply() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setUserId(1L);
        request.setTypeTags(List.of("小吃"));
        request.setTaste("清淡");

        Food food1 = createFood(1L, "肠粉", "小吃", "小吃", "", "午餐", "清淡,鲜", 1);
        Food food2 = createFood(2L, "麻辣烫", "小吃", "小吃", "", "午餐,晚餐,夜宵", "辣,麻", 2);
        Food food3 = createFood(3L, "臭豆腐", "小吃", "小吃", "", "午餐,晚餐,夜宵", "辣", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));

        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of());
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 肠粉被黑名单排除，麻辣烫和臭豆腐不匹配清淡 → 无候选
        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    @Test
    void recommend_blacklistAndDislikeAndExcludeAllApply() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setUserId(1L);
        request.setExcludeFoodIds(List.of(3L));
        request.setTypeTags(List.of("快餐"));

        Food food1 = createFood(1L, "猪脚饭", "快餐", "快餐", "", "午餐", "咸,香", 2);
        Food food2 = createFood(2L, "黄焖鸡米饭", "快餐", "快餐", "", "午餐", "咸,辣", 2);
        Food food3 = createFood(3L, "盖浇饭", "快餐", "快餐", "", "午餐", "咸", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));

        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));
        when(userDislikeService.getActiveDislikeCategories(eq(1L), any(LocalDateTime.class))).thenReturn(Set.of());
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 猪脚饭被黑名单排除，盖浇饭被 exclude 排除 → 只剩黄焖鸡
        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId());
    }

    @Test
    void recommend_noCandidate_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("早餐");
        request.setTaste("辣");
        request.setTypeTags(List.of("火锅"));

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // 早餐 → 火锅 mealTypes 不含早餐 → 硬过滤排除 → 无候选
        RecommendResponse response = recommendService.recommend(request);

        assertNull(response);
    }

    // ==================== DECIDED 不参与最近吃过降权 ====================

    @Test
    void recommend_decidedRecordDoesNotAffectScore() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food = createFood(1L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));

        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("最近几天没吃过，换换口味"));
    }

    // ==================== 边界测试：calculateRecentEatenDeduction ====================

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

    // ==================== parseTags 测试 ====================

    @Test
    void parseTags_normal() {
        Set<String> result = FoodTaxonomy.parseTags("辣,麻,香");
        assertEquals(Set.of("辣", "麻", "香"), result);
    }

    @Test
    void parseTags_empty() {
        Set<String> result = FoodTaxonomy.parseTags("");
        assertTrue(result.isEmpty());
    }

    @Test
    void parseTags_null() {
        Set<String> result = FoodTaxonomy.parseTags(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseTags_withSpaces() {
        Set<String> result = FoodTaxonomy.parseTags(" 辣 , 麻 , 香 ");
        assertEquals(Set.of("辣", "麻", "香"), result);
    }

    // ==================== 工具方法 ====================

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

    private Food createFood(Long id, String name, String category,
                            String typeTags, String cuisineTags, String mealTypes,
                            String tasteTags, int priceLevel) {
        Food food = new Food();
        food.setId(id);
        food.setName(name);
        food.setCategory(category);
        food.setTypeTags(typeTags);
        food.setCuisineTags(cuisineTags);
        food.setMealTypes(mealTypes);
        food.setTasteTags(tasteTags);
        food.setPriceLevel(priceLevel);
        food.setEnabled(true);
        return food;
    }
}
