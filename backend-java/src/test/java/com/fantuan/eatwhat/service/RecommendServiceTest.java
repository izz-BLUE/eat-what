package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.FoodTaxonomy;
import com.fantuan.eatwhat.domain.entity.EatRecord;
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
import java.util.ArrayList;
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

    @Mock
    private UserCustomFoodService userCustomFoodService;

    @InjectMocks
    private RecommendService recommendService;

    @BeforeEach
    void setUp() {
        // 默认无自定义菜，现有测试走 Phase 2 默认菜逻辑
        when(userCustomFoodService.getEnabledCustomFoods(any())).thenReturn(List.of());
        when(eatRecordService.getRecentEatenCustomFoodMap(any())).thenReturn(Map.of());
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
        // 默认无评分记录（各测试按需覆盖）
        when(eatRecordService.getRatedEatenRecords(any())).thenReturn(List.of());
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
        assertTrue(response.getScore() >= 0, "score 应 >=0（随机因素可能为0）");
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

    // ==================== 参考价位硬过滤测试 ====================

    @Test
    void recommend_priceLevel_15以内_onlyReturnsLevel1() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel("15以内");

        Food food1 = createFood(1L, "沙县小吃", "小吃", "小吃", "", "午餐", "清淡", 1);
        Food food2 = createFood(2L, "黄焖鸡米饭", "快餐", "快餐", "", "午餐", "咸,辣", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1, response.getFood().getPriceLevel(),
                "15以内 只能返回 priceLevel=1，实际: " + response.getFood().getPriceLevel());
    }

    @Test
    void recommend_priceLevel_15以内_excludesLevel3() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel("15以内");

        Food food1 = createFood(1L, "寿司", "日料", "", "日料", "午餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // 寿司 priceLevel=3，15以内硬过滤排除 → 无候选
        RecommendResponse response = recommendService.recommend(request);
        assertNull(response);
    }

    @Test
    void recommend_priceLevel_15_25_onlyReturnsLevel2() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("15-25");

        Food food1 = createFood(1L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        Food food2 = createFood(2L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2, response.getFood().getPriceLevel(),
                "15-25 只能返回 priceLevel=2，实际: " + response.getFood().getPriceLevel());
    }

    @Test
    void recommend_priceLevel_25_40_onlyReturnsLevel3() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("25-40");

        Food food1 = createFood(1L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        Food food2 = createFood(2L, "兰州拉面", "面食", "面食", "", "晚餐", "清淡", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(3, response.getFood().getPriceLevel(),
                "25-40 只能返回 priceLevel=3，实际: " + response.getFood().getPriceLevel());
    }

    @Test
    void recommend_priceLevel_40以上_onlyReturnsLevel4() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("40以上");

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food food2 = createFood(2L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(4, response.getFood().getPriceLevel(),
                "40以上 只能返回 priceLevel=4，实际: " + response.getFood().getPriceLevel());
    }

    @Test
    void recommend_priceLevel_noLimit_noFilter() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel(null);

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        Food food2 = createFood(2L, "沙县小吃", "小吃", "小吃", "", "午餐", "清淡", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 空值不限，火锅(4)和沙县(1)都可能返回
    }

    @Test
    void recommend_priceLevel_emptyString_noFilter() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel("");

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
    }

    @Test
    void recommend_priceLevel_noCandidate_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("15以内");

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // 4 不属于 15以内 → 无候选
        RecommendResponse response = recommendService.recommend(request);
        assertNull(response, "15以内选无可选时应返回 null（Controller code=2002）");
    }

    @Test
    void recommend_priceLevel_withCategoriesAndMealType_andLogic() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel("15以内");
        request.setTypeTags(List.of("小吃"));

        Food food1 = createFood(1L, "沙县小吃", "小吃", "小吃", "", "午餐", "清淡", 1);
        Food food2 = createFood(2L, "猪脚饭", "快餐", "快餐", "", "午餐", "咸,香", 2);
        Food food3 = createFood(3L, "凉皮", "小吃", "小吃", "", "午餐,晚餐,夜宵", "辣,酸", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1, response.getFood().getPriceLevel(),
                "分类+餐段+价格组合 AND：必须 priceLevel=1");
        assertTrue(response.getFood().getTypeTags() != null
                && response.getFood().getTypeTags().contains("小吃"),
                "分类+餐段+价格 AND：必须 typeTags 含小吃");
    }

    @Test
    void recommend_priceLevel_swapAlsoEnforcesPrice() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("25-40");
        request.setExcludeFoodIds(List.of(1L));

        Food food1 = createFood(1L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        Food food2 = createFood(2L, "拉面", "日料", "面食", "日料", "晚餐", "咸,鲜", 3);
        Food food3 = createFood(3L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(3, response.getFood().getPriceLevel(),
                "swap 也遵守价位，排除清蒸鱼后应返回拉面(3)非火锅(4)");
        assertNotEquals(1L, response.getFood().getId(), "应排除清蒸鱼");
    }

    @Test
    void recommend_priceLevel_excludeAllCandidates_returnsNull() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("15以内");
        request.setExcludeFoodIds(List.of(1L, 2L));

        Food food1 = createFood(1L, "沙县小吃", "小吃", "小吃", "", "晚餐", "清淡", 1);
        Food food2 = createFood(2L, "兰州拉面", "面食", "面食", "", "晚餐", "清淡", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // 排除所有 15以内 候选
        RecommendResponse response = recommendService.recommend(request);
        assertNull(response, "excludeFoodIds 排除该档位全部候选后应返回 null");
    }

    @Test
    void recommend_priceLevel_continuousRecommendNeverReturnsOtherLevel() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setPriceLevel("40以上");

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food food2 = createFood(2L, "麻辣火锅", "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
        Food food3 = createFood(3L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2, food3));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        // 连续推荐 10 次，不应出现非 4 的菜品
        for (int i = 0; i < 10; i++) {
            RecommendResponse response = recommendService.recommend(request);
            assertNotNull(response, "40以上 hard filter 应有候选");
            assertEquals(4, response.getFood().getPriceLevel(),
                    "连续推荐 #" + (i + 1) + " 出现 priceLevel=" + response.getFood().getPriceLevel()
                    + " (" + response.getFood().getName() + ")，应为4");
        }
    }

    @Test
    void recommend_priceLevel_addsReason() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel("15以内");

        Food food = createFood(1L, "沙县小吃", "小吃", "小吃", "", "午餐", "清淡", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("价位匹配「15以内」"),
                "应包含具体价位理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_priceLevel_noLimit_noPriceReason() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setPriceLevel(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("价位")),
                "不限价位时不应有价位相关理由");
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
        assertTrue(response.getReasons().contains("属于「火锅」"),
                "应包含具体分类理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_mealType_addsReason() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");

        Food food = createFood(1L, "猪脚饭", "快餐", "快餐", "", "午餐,晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("适合「午餐」时段"),
                "选择餐段时应包含餐段理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_tasteSpicy_addsReason() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("辣");

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("口味匹配「辣」"),
                "选择口味时应包含口味理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_tasteNotSpicy_addsReason() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setTaste("不辣");

        Food food = createFood(1L, "清蒸鱼", "粤菜", "", "粤菜", "晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("口味匹配「不辣」"),
                "选择不辣时应包含口味理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_cuisineTag_addsSpecificReason() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setCuisineTags(List.of("日料"));

        Food food = createFood(1L, "寿司", "日料", "", "日料", "晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("菜系「日料」"),
                "选择菜系时应包含具体菜系理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_noFilters_onlyFreshnessReason() {
        // 已登录用户，未设筛选 → 仅有"最近几天没吃过"
        RecommendRequest request = new RecommendRequest();
        request.setUserId(1L);

        Food food = createFood(1L, "猪脚饭", "快餐", "快餐", "", "午餐,晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 不传任何筛选条件时，不应出现餐段、口味、分类、价位相关理由
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("时段")),
                "未选餐段不应出现时段理由: " + response.getReasons());
        assertFalse(response.getReasons().stream().anyMatch(r -> r.startsWith("口味匹配")),
                "未选口味不应出现口味理由: " + response.getReasons());
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("属于")),
                "未选分类不应出现分类理由: " + response.getReasons());
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("菜系")),
                "未选菜系不应出现菜系理由: " + response.getReasons());
        assertFalse(response.getReasons().stream().anyMatch(r -> r.startsWith("价位匹配")),
                "未选价位不应出现价位理由: " + response.getReasons());
        // 已登录无记录 → 应包含新鲜度理由
        assertTrue(response.getReasons().stream().anyMatch(r -> r.contains("最近几天没吃过")),
                "已登录无记录应包含新鲜度理由: " + response.getReasons());
    }

    @Test
    void recommend_anonymous_noFilters_randomReason() {
        // 匿名用户，未设筛选 → "随机帮你挑一个"
        RecommendRequest request = new RecommendRequest();
        request.setUserId(null);

        Food food = createFood(1L, "猪脚饭", "快餐", "快餐", "", "午餐,晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("随机帮你挑一个"),
                "匿名推荐应有随机理由: " + response.getReasons());
    }

    @Test
    void recommend_anonymous_noRecentEatenText() {
        // 匿名用户 → 不应输出"最近几天没吃过"
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("最近几天没吃过")),
                "匿名用户不应出现'最近几天没吃过': " + response.getReasons());
    }

    @Test
    void recommend_loggedIn_noRecentRecord_showsFreshnessReason() {
        // 已登录 + 无最近记录 → 输出"最近几天没吃过，换换口味"
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("最近几天没吃过，换换口味"),
                "已登录无记录应包含新鲜度理由: " + response.getReasons());
    }

    @Test
    void recommend_loggedIn_recentlyEaten_noFreshnessReason() {
        // 已登录 + 最近吃过(12h) → 不输出"最近几天没吃过"
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));

        Map<Long, LocalDateTime> recentEatenMap = Map.of(1L, LocalDateTime.now().minusHours(12));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(recentEatenMap);

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("最近几天没吃过")),
                "最近吃过不应出现'最近几天没吃过': " + response.getReasons());
    }

    @Test
    void recommend_allFilters_produceMultipleReasons() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setTaste("清淡");
        request.setPriceLevel("15以内");
        request.setTypeTags(List.of("小吃"));

        Food food = createFood(1L, "肠粉", "小吃", "小吃", "", "午餐", "清淡,鲜", 1);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("适合「午餐」时段"));
        assertTrue(response.getReasons().contains("口味匹配「清淡」"));
        assertTrue(response.getReasons().contains("属于「小吃」"));
        assertTrue(response.getReasons().contains("价位匹配「15以内」"),
                "全部筛选条件命中时应有 4 条匹配理由，实际: " + response.getReasons());
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

    // ==================== 评分偏好：匿名用户不启用 ====================

    @Test
    void recommend_anonymousUser_noRatingPreference() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(food));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 匿名用户不应有评分偏好理由
        assertFalse(response.getReasons().stream().anyMatch(r -> r.contains("符合你以往喜欢的口味/类型")),
                "匿名用户不应出现评分偏好理由: " + response.getReasons());
    }

    // ==================== 评分偏好：rating >= 4 加分 ====================

    @Test
    void recommend_highRating_sameTypeTag_addsBonus() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 候选：火锅
        Food candidate = createFood(1L, "清汤火锅", "火锅", "火锅", "川菜", "晚餐", "清淡,鲜", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 历史高分记录：麻辣火锅（同 typeTags=火锅, rating=5）
        Food ratedFood = createFood(101L, "麻辣火锅", "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 5);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "正反馈加分 > 0 应包含个性化理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_highRating_sameCuisineTag_addsBonus() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 候选：日料寿司
        Food candidate = createFood(1L, "寿司", "日料", "", "日料", "晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 历史高分记录：日料拉面（同 cuisineTags=日料, rating=4）
        Food ratedFood = createFood(101L, "拉面", "日料", "面食", "日料", "午餐,晚餐", "咸,鲜", 3);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 4);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "同菜系高分应触发正反馈理由，实际: " + response.getReasons());
    }

    @Test
    void recommend_highRating_sameTasteTag_addsBonus() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 候选：麻辣烫（tasteTags=辣,麻）
        Food candidate = createFood(1L, "麻辣烫", "小吃", "小吃", "", "晚餐", "辣,麻", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 历史高分记录：火锅（tasteTags=辣,麻，同口味, rating=5）
        Food ratedFood = createFood(101L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 5);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "同口味高分应触发正反馈理由，实际: " + response.getReasons());
    }

    // ==================== 评分偏好：rating <= 2 降权 ====================

    @Test
    void recommend_lowRating_sameTypeTag_penalizes() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 候选 1：火锅（typeTags=火锅）— 应受降权
        Food hotpot = createFood(1L, "清汤火锅", "火锅", "火锅", "川菜", "晚餐", "清淡,鲜", 4);
        // 候选 2：猪脚饭 — 不受影响
        Food porkRice = createFood(2L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(hotpot, porkRice));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 历史低分记录：麻辣火锅（typeTags=火锅, rating=1）
        Food ratedFood = createFood(101L, "麻辣火锅", "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 1);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        // 多次推荐：火锅被降权但非硬过滤，仍可能被推荐
        // 我们验证降权不会导致崩溃，且不出现正反馈理由
        boolean hotpotAppeared = false;
        for (int i = 0; i < 20; i++) {
            RecommendResponse response = recommendService.recommend(request);
            assertNotNull(response);
            if (response.getFood().getId() == 1L) hotpotAppeared = true;
            // 负反馈不应出现正反馈理由
            assertFalse(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                    "负反馈不应触发个性化理由: " + response.getReasons());
        }
        // 火锅虽被降权但非硬过滤，可能偶尔出现
        assertTrue(hotpotAppeared, "降权不是硬过滤，火锅应在随机因素下偶尔出现");
    }

    // ==================== 评分偏好：rating = 3 不影响 ====================

    @Test
    void recommend_neutralRating3_noEffect() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food candidate = createFood(1L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 历史记录：同类型 rating=3 → 不做任何加减分
        Food ratedFood = createFood(101L, "黄焖鸡米饭", "快餐", "快餐", "", "晚餐", "咸,辣", 2);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 3);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // rating=3 不参与计算，不应有评分偏好理由
        assertFalse(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "rating=3 不应触发个性化理由: " + response.getReasons());
    }

    // ==================== 评分偏好：正负平衡 ====================

    @Test
    void recommend_mixedRatings_cancelOut() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 候选：火锅（typeTags=火锅, cuisineTags=川菜）
        Food candidate = createFood(1L, "清汤火锅", "火锅", "火锅", "川菜", "晚餐", "清淡,鲜", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 正反馈：火锅(5分) → typeTags 命中 +8, cuisineTags 命中 +8 → +16
        Food highRated = createFood(101L, "麻辣火锅", "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
        EatRecord highRecord = createEatRecord(1L, 1L, 101L, 5);
        // 负反馈A：火锅(1分) → typeTags 命中 -8
        Food lowRatedA = createFood(102L, "鸳鸯火锅", "火锅", "火锅", "", "晚餐", "辣,鲜", 4);
        EatRecord lowRecordA = createEatRecord(2L, 1L, 102L, 1);
        // 负反馈B：回锅肉(1分) → cuisineTags "川菜" 命中 -8 → +16-8-8=0
        Food lowRatedB = createFood(103L, "回锅肉", "川菜", "", "川菜", "晚餐", "辣,咸", 3);
        EatRecord lowRecordB = createEatRecord(3L, 1L, 103L, 1);

        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(highRecord, lowRecordA, lowRecordB));
        when(foodService.listByIds(List.of(101L, 102L, 103L))).thenReturn(List.of(highRated, lowRatedA, lowRatedB));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // +16 - 8 - 8 = 0，bonus 为 0，不应出现理由
        assertFalse(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "正负平衡(bonus=0)不应出现个性化理由: " + response.getReasons());
    }

    // ==================== 评分偏好：上限 +20 ====================

    @Test
    void recommend_ratingBonus_cappedAtPlus20() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food candidate = createFood(1L, "清汤火锅", "火锅", "火锅", "川菜", "晚餐", "清淡,鲜", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 4 条高分火锅记录：4 × 8 = +32 → 应被 cap 在 +20
        List<EatRecord> records = new ArrayList<>();
        List<Food> ratedFoods = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Food f = createFood(100L + i, "火锅" + i, "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
            ratedFoods.add(f);
            records.add(createEatRecord((long) i, 1L, 100L + i, 5));
        }
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(records);
        when(foodService.listByIds(any())).thenReturn(ratedFoods);

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // cap 后 bonus = +20 > 0，应有理由
        assertTrue(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "cap 后 bonus > 0 应有理由，实际: " + response.getReasons());
        // score 最大 = 0(recent) + 20(capped) + 19(random) = 39
        assertTrue(response.getScore() >= 0 && response.getScore() <= 39,
                "score 应在 0-39 范围（0+20+19），实际: " + response.getScore());
    }

    // ==================== 评分偏好：下限 -20 ====================

    @Test
    void recommend_ratingPenalty_cappedAtMinus20() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        Food candidate = createFood(1L, "清汤火锅", "火锅", "火锅", "川菜", "晚餐", "清淡,鲜", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(candidate));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 4 条低分火锅记录：4 × -8 = -32 → 应被 cap 在 -20
        List<EatRecord> records = new ArrayList<>();
        List<Food> ratedFoods = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Food f = createFood(100L + i, "火锅" + i, "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
            ratedFoods.add(f);
            records.add(createEatRecord((long) i, 1L, 100L + i, 1));
        }
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(records);
        when(foodService.listByIds(any())).thenReturn(ratedFoods);

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // bonus = -20 <= 0，不应有理由
        assertFalse(response.getReasons().contains("符合你以往喜欢的口味/类型"),
                "cap 后 bonus <= 0 不应有理由: " + response.getReasons());
        // score 最小 = 0(recent) + (-20)(penalty cap) + 0(random) = -20
        assertTrue(response.getScore() >= -20 && response.getScore() <= 19,
                "score 应在 -20-19 范围，实际: " + response.getScore());
    }

    // ==================== 评分偏好：黑名单硬排除优先 ====================

    @Test
    void recommend_blacklistExcludesDespitePositiveRating() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(1L);

        // 火锅在黑名单但仍被高分评分过
        Food hotpot = createFood(1L, "麻辣火锅", "火锅", "火锅", "川菜", "晚餐", "辣,麻", 4);
        Food porkRice = createFood(2L, "猪脚饭", "快餐", "快餐", "", "晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(hotpot, porkRice));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 火锅在黑名单
        when(userBlacklistService.getBlacklistFoodIds(1L)).thenReturn(Set.of(1L));

        // 历史高分记录：火锅（rating=5）— 虽有高分评分，但黑名单硬含义优先
        Food ratedFood = createFood(101L, "清汤火锅", "火锅", "火锅", "川菜", "晚餐", "清淡,鲜", 4);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 5);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 黑名单硬过滤排除火锅，应返回猪脚饭
        assertEquals(2L, response.getFood().getId(),
                "黑名单应硬排除，即使有高分评分历史");
    }

    // ==================== 评分偏好：不影响硬过滤 ====================

    @Test
    void recommend_ratingPreference_doesNotOverrideHardFilter() {
        RecommendRequest request = new RecommendRequest();
        request.setMealType("早餐");
        request.setUserId(1L);

        // 肠粉：适合早餐
        Food changfen = createFood(1L, "肠粉", "小吃", "小吃", "", "早餐,午餐", "清淡,鲜", 1);
        // 火锅：不适合早餐
        Food hotpot = createFood(2L, "火锅", "火锅", "火锅", "", "午餐,晚餐,夜宵", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(changfen, hotpot));
        when(eatRecordService.getRecentEatenFoodMap(1L)).thenReturn(Map.of());

        // 历史高分记录对火锅有正反馈 — 但火锅不合适早餐（硬过滤排除）
        Food ratedFood = createFood(101L, "麻辣火锅", "火锅", "火锅", "", "午餐,晚餐", "辣,麻", 4);
        EatRecord ratedRecord = createEatRecord(1L, 1L, 101L, 5);
        when(eatRecordService.getRatedEatenRecords(1L)).thenReturn(List.of(ratedRecord));
        when(foodService.listByIds(List.of(101L))).thenReturn(List.of(ratedFood));

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        // 火锅被餐段硬过滤排除，评分偏好不能覆盖硬过滤
        assertEquals(1L, response.getFood().getId(),
                "评分偏好不能覆盖餐段硬过滤，早餐不应返回火锅");
    }

    // ==================== 甜品/茶饮主餐推荐排除 ====================

    @Test
    void recommend_dessertExcludedWhenMealTypeSpecified() {
        // 指定餐段"午餐"时，甜品不应被推荐
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setUserId(null);

        Food dessert = createFood(1L, "蛋糕", "甜品", "甜品", "", "午餐,晚餐,夜宵", "甜", 2);
        Food porkRice = createFood(2L, "猪脚饭", "快餐", "快餐", "", "午餐,晚餐", "咸,香", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(dessert, porkRice));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId(),
                "指定餐段时不应推荐甜品");
    }

    @Test
    void recommend_teaDrinkExcludedWhenMealTypeSpecified() {
        // 指定餐段"晚餐"时，茶饮不应被推荐
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food tea = createFood(1L, "百香果茶", "茶饮", "茶饮", "", "午餐,晚餐,夜宵", "甜,酸", 2);
        Food hotpot = createFood(2L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        when(foodService.listAllEnabled()).thenReturn(List.of(tea, hotpot));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(2L, response.getFood().getId(),
                "指定餐段时不应推荐茶饮");
    }

    @Test
    void recommend_dessertAllowedWhenNoMealType() {
        // 不指定餐段时，甜品可以被推荐
        RecommendRequest request = new RecommendRequest();
        request.setMealType("");
        request.setUserId(null);

        Food dessert = createFood(1L, "蛋糕", "甜品", "甜品", "", "午餐,晚餐,夜宵", "甜", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(dessert));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertEquals(1L, response.getFood().getId(),
                "不指定餐段时甜品应正常推荐");
    }

    @Test
    void recommend_normalFoodNotAffectedBySideItemFilter() {
        // 正常主餐不应被 side-item 过滤影响
        RecommendRequest request = new RecommendRequest();
        request.setMealType("晚餐");
        request.setUserId(null);

        Food food1 = createFood(1L, "火锅", "火锅", "火锅", "", "晚餐", "辣,麻", 4);
        Food food2 = createFood(2L, "寿司", "日料", "", "日料", "午餐,晚餐", "清淡,鲜", 3);
        when(foodService.listAllEnabled()).thenReturn(List.of(food1, food2));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNotNull(response);
        assertTrue(response.getFood().getId() == 1L || response.getFood().getId() == 2L,
                "正常主餐不应被 side-item 过滤影响");
    }

    @Test
    void recommend_onlyDessertsAndTeaAvailableWithMealType_returnsNull() {
        // 指定餐段但候选池只有甜品和茶饮时，应返回 null（触发 2002）
        RecommendRequest request = new RecommendRequest();
        request.setMealType("午餐");
        request.setUserId(null);

        Food dessert = createFood(1L, "蛋糕", "甜品", "甜品", "", "午餐,晚餐,夜宵", "甜", 2);
        Food tea = createFood(2L, "百香果茶", "茶饮", "茶饮", "", "午餐,晚餐,夜宵", "甜,酸", 2);
        when(foodService.listAllEnabled()).thenReturn(List.of(dessert, tea));
        when(eatRecordService.getRecentEatenFoodMap(null)).thenReturn(Map.of());

        RecommendResponse response = recommendService.recommend(request);

        assertNull(response, "候选池全是甜品/茶饮且指定餐段时应返回 null");
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

    private EatRecord createEatRecord(Long id, Long userId, Long foodId, int rating) {
        EatRecord record = new EatRecord();
        record.setId(id);
        record.setUserId(userId);
        record.setFoodId(foodId);
        record.setStatus("EATEN");
        record.setRating(rating);
        return record;
    }
}
