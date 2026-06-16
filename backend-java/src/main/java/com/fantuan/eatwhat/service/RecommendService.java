package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.FoodTaxonomy;
import com.fantuan.eatwhat.domain.entity.EatRecord;
import com.fantuan.eatwhat.domain.entity.Food;
import com.fantuan.eatwhat.dto.request.RecommendRequest;
import com.fantuan.eatwhat.dto.response.FoodResponse;
import com.fantuan.eatwhat.dto.response.RecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务
 * 基于规则打分的推荐算法
 *
 * 过滤顺序：
 * enabled → excludeFoodIds → blacklist → dislikes(含 type_tags/cuisine_tags 兼容)
 * → 分类硬过滤(type_tags + cuisine_tags OR) → 餐段硬过滤 → 口味硬过滤 → 参考价位硬过滤
 * → 最近吃过降权 → 历史评分偏好加权 → 随机因素 → Top 5 随机
 */
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final FoodService foodService;
    private final EatRecordService eatRecordService;
    private final UserBlacklistService userBlacklistService;
    private final UserDislikeService userDislikeService;

    /**
     * 推荐一个菜品
     *
     * @param request 推荐请求参数
     * @return 推荐结果，无候选时返回 null
     */
    public RecommendResponse recommend(RecommendRequest request) {
        // 1. 获取候选菜品
        List<Food> candidates = foodService.listAllEnabled();

        // 2. 排除指定菜品（excludeFoodIds）
        if (!CollectionUtils.isEmpty(request.getExcludeFoodIds())) {
            Set<Long> excludeIds = new HashSet<>(request.getExcludeFoodIds());
            candidates = candidates.stream()
                    .filter(f -> !excludeIds.contains(f.getId()))
                    .collect(Collectors.toList());
        }

        // 3. 排除用户黑名单菜品
        if (request.getUserId() != null) {
            Set<Long> blacklistFoodIds = userBlacklistService.getBlacklistFoodIds(request.getUserId());
            if (!blacklistFoodIds.isEmpty()) {
                candidates = candidates.stream()
                        .filter(f -> !blacklistFoodIds.contains(f.getId()))
                        .collect(Collectors.toList());
            }
        }

        // 4. 排除用户不想吃的分类（有效期内，匹配 category + type_tags + cuisine_tags）
        LocalDateTime now = LocalDateTime.now();
        if (request.getUserId() != null) {
            Set<String> dislikeCategories = userDislikeService.getActiveDislikeCategories(request.getUserId(), now);
            if (!dislikeCategories.isEmpty()) {
                candidates = candidates.stream()
                        .filter(f -> !matchesDislike(f, dislikeCategories))
                        .collect(Collectors.toList());
            }
        }

        // 5. 分类筛选（type_tags + cuisine_tags，OR）
        if (!CollectionUtils.isEmpty(request.getTypeTags()) || !CollectionUtils.isEmpty(request.getCuisineTags())) {
            Set<String> selectedTypes = request.getTypeTags() != null
                    ? new HashSet<>(request.getTypeTags()) : Set.of();
            Set<String> selectedCuisines = request.getCuisineTags() != null
                    ? new HashSet<>(request.getCuisineTags()) : Set.of();

            candidates = candidates.stream()
                    .filter(f -> matchesCategory(f, selectedTypes, selectedCuisines))
                    .collect(Collectors.toList());
        }

        // 6. 餐段硬过滤
        if (StringUtils.hasText(request.getMealType())) {
            candidates = candidates.stream()
                    .filter(f -> matchesMealType(f, request.getMealType()))
                    .collect(Collectors.toList());
        }

        // 7. 口味硬过滤
        if (StringUtils.hasText(request.getTaste())) {
            candidates = candidates.stream()
                    .filter(f -> matchesTaste(f.getTasteTags(), request.getTaste()))
                    .collect(Collectors.toList());
        }

        // 8. 参考价位硬过滤（精确整数匹配，空值/不限跳过）
        if (StringUtils.hasText(request.getPriceLevel())) {
            int targetLevel = priceLevelToInt(request.getPriceLevel());
            candidates = candidates.stream()
                    .filter(f -> f.getPriceLevel() != null && f.getPriceLevel() == targetLevel)
                    .collect(Collectors.toList());
        }

        // 无候选 → 返回 null（Controller 返回 2002）
        if (candidates.isEmpty()) {
            return null;
        }

        // 9. 查询用户最近7天吃过的食物
        Map<Long, LocalDateTime> recentEatenMap = eatRecordService.getRecentEatenFoodMap(request.getUserId());

        // 9.5. 查询用户有评分的已吃记录，构建评分偏好数据
        List<RatedFoodInfo> ratedFoods = buildRatedFoods(request.getUserId());

        // 10. 计算每个菜品的得分
        List<ScoredFood> scoredFoods = candidates.stream()
                .map(food -> calculateScore(food, request, recentEatenMap, ratedFoods, now))
                .collect(Collectors.toList());

        // 11. 按得分排序，取 Top 5
        scoredFoods.sort((a, b) -> Integer.compare(b.score, a.score));
        List<ScoredFood> top5 = scoredFoods.stream()
                .limit(5)
                .collect(Collectors.toList());

        // 12. 从 Top 5 中随机选一个
        ScoredFood selected = top5.get(new Random().nextInt(top5.size()));

        // 13. 构建响应
        FoodResponse foodResponse = foodService.toResponse(selected.food);
        return RecommendResponse.builder()
                .food(foodResponse)
                .score(selected.score)
                .reasons(selected.reasons)
                .build();
    }

    /**
     * 计算菜品得分
     */
    private ScoredFood calculateScore(Food food, RecommendRequest request,
                                       Map<Long, LocalDateTime> recentEatenMap,
                                       List<RatedFoodInfo> ratedFoods, LocalDateTime now) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. 餐段匹配理由
        if (StringUtils.hasText(request.getMealType())) {
            reasons.add("适合「" + request.getMealType() + "」时段");
        }

        // 2. 口味匹配理由
        if (StringUtils.hasText(request.getTaste())) {
            reasons.add("口味匹配「" + request.getTaste() + "」");
        }

        // 3. 分类命中理由（找出具体命中的标签）
        if (!CollectionUtils.isEmpty(request.getTypeTags())) {
            Set<String> foodTypes = FoodTaxonomy.parseTags(food.getTypeTags());
            for (String t : request.getTypeTags()) {
                if (foodTypes.contains(t)) {
                    reasons.add("属于「" + t + "」");
                    break; // 每种维度只取第一个命中标签
                }
            }
        }
        if (!CollectionUtils.isEmpty(request.getCuisineTags())) {
            Set<String> foodCuisines = FoodTaxonomy.parseTags(food.getCuisineTags());
            for (String c : request.getCuisineTags()) {
                if (foodCuisines.contains(c)) {
                    reasons.add("菜系「" + c + "」");
                    break;
                }
            }
        }

        // 4. 参考价位命中理由（显示具体价位）
        if (StringUtils.hasText(request.getPriceLevel())) {
            reasons.add("价位匹配「" + request.getPriceLevel() + "」");
        }

        // 5. 最近吃过降权
        int recentEatenDeduction = calculateRecentEatenDeduction(food.getId(), recentEatenMap, now);
        if (request.getUserId() == null) {
            // 匿名用户无历史记录，不输出"最近几天没吃过"
            reasons.add("随机帮你挑一个");
        } else if (recentEatenDeduction == 0) {
            reasons.add("最近几天没吃过，换换口味");
        }
        score += recentEatenDeduction;

        // 6. 历史评分偏好加权（仅登录用户启用）
        int ratingBonus = 0;
        if (request.getUserId() != null && !ratedFoods.isEmpty()) {
            ratingBonus = calculateRatingPreference(food, ratedFoods);
            score += ratingBonus;
            if (ratingBonus > 0) {
                reasons.add("符合你以往喜欢的口味/类型");
            }
        }

        // 7. 随机因素：0-19
        int randomScore = new Random().nextInt(20);
        score += randomScore;

        return new ScoredFood(food, score, reasons);
    }

    // ==================== 硬过滤方法 ====================

    /**
     * 不想吃匹配：dislike 值匹配 food.category、typeTags 或 cuisineTags 任一项
     */
    private boolean matchesDislike(Food food, Set<String> dislikeCategories) {
        // 精确匹配 category
        if (dislikeCategories.contains(food.getCategory())) {
            return true;
        }
        // 匹配 typeTags 中的任一个
        Set<String> foodTypes = FoodTaxonomy.parseTags(food.getTypeTags());
        for (String t : foodTypes) {
            if (dislikeCategories.contains(t)) return true;
        }
        // 匹配 cuisineTags 中的任一个
        Set<String> foodCuisines = FoodTaxonomy.parseTags(food.getCuisineTags());
        for (String c : foodCuisines) {
            if (dislikeCategories.contains(c)) return true;
        }
        return false;
    }

    /**
     * 分类筛选：type_tags 或 cuisine_tags 任一命中（OR）
     */
    private boolean matchesCategory(Food food, Set<String> selectedTypes, Set<String> selectedCuisines) {
        if (!selectedTypes.isEmpty()) {
            Set<String> foodTypes = FoodTaxonomy.parseTags(food.getTypeTags());
            for (String t : foodTypes) {
                if (selectedTypes.contains(t)) return true;
            }
        }
        if (!selectedCuisines.isEmpty()) {
            Set<String> foodCuisines = FoodTaxonomy.parseTags(food.getCuisineTags());
            for (String c : foodCuisines) {
                if (selectedCuisines.contains(c)) return true;
            }
        }
        return false;
    }

    /**
     * 餐段硬过滤：food.mealTypes 必须包含所选餐段
     */
    private boolean matchesMealType(Food food, String mealType) {
        Set<String> mealTypes = FoodTaxonomy.parseTags(food.getMealTypes());
        return mealTypes.contains(mealType);
    }

    /**
     * 口味硬过滤（使用 Set 精确匹配，不用 contains/ LIKE）
     *
     * @param tasteTags 食物口味标签，逗号分隔
     * @param taste     用户选择的口味偏好（清淡/辣/不辣）
     */
    private boolean matchesTaste(String tasteTags, String taste) {
        Set<String> tags = FoodTaxonomy.parseTags(tasteTags);

        switch (taste) {
            case "清淡":
                return tags.contains("清淡");
            case "辣":
                return tags.contains("辣");
            case "不辣":
                return !tags.contains("辣") && !tags.contains("麻");
            default:
                return true;
        }
    }

    // ==================== 参考价位映射 ====================

    /**
     * 将价格档位字符串映射为 priceLevel 整数。
     * 映射：15以内→1, 15-25→2, 25-40→3, 40以上→4
     */
    private int priceLevelToInt(String priceLevel) {
        switch (priceLevel) {
            case "15以内": return 1;
            case "15-25":  return 2;
            case "25-40":  return 3;
            case "40以上": return 4;
            default:       return 0;
        }
    }

    // ==================== 最近吃过降权 ====================

    /**
     * 计算最近吃过扣分
     * < 24h: -100
     * 24h ~ < 48h: -80
     * 48h ~ < 72h: -60
     * 72h ~ <= 168h: -30
     * > 168h: 0
     */
    private int calculateRecentEatenDeduction(Long foodId, Map<Long, LocalDateTime> recentEatenMap,
                                               LocalDateTime now) {
        if (recentEatenMap == null || recentEatenMap.isEmpty()) {
            return 0;
        }

        LocalDateTime lastEatenAt = recentEatenMap.get(foodId);
        if (lastEatenAt == null) {
            return 0;
        }

        Duration duration = Duration.between(lastEatenAt, now);
        if (duration.compareTo(Duration.ofHours(24)) < 0) return -100;
        if (duration.compareTo(Duration.ofHours(48)) < 0) return -80;
        if (duration.compareTo(Duration.ofHours(72)) < 0) return -60;
        if (duration.compareTo(Duration.ofHours(168)) <= 0) return -30;
        return 0;
    }

    // ==================== 评分偏好 ====================

    /**
     * 构建评分偏好数据：查询用户有评分的 EATEN 记录，关联 Food 信息。
     *
     * @param userId 用户ID，null 时返回空列表
     * @return 评分过的食物信息列表
     */
    private List<RatedFoodInfo> buildRatedFoods(Long userId) {
        if (userId == null) {
            return List.of();
        }

        List<EatRecord> ratedRecords = eatRecordService.getRatedEatenRecords(userId);
        if (ratedRecords == null || ratedRecords.isEmpty()) {
            return List.of();
        }

        // 批量查询关联的食物
        List<Long> foodIds = ratedRecords.stream()
                .map(EatRecord::getFoodId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Food> foodMap = foodService.listByIds(foodIds).stream()
                .collect(Collectors.toMap(Food::getId, f -> f));

        return ratedRecords.stream()
                .filter(r -> foodMap.containsKey(r.getFoodId()))
                .map(r -> new RatedFoodInfo(foodMap.get(r.getFoodId()), r.getRating()))
                .collect(Collectors.toList());
    }

    /**
     * 计算历史评分偏好加权。
     *
     * 规则：
     * - rating >= 4（正反馈）：同 type_tags +8、同 cuisine_tags +8、同 taste_tags +5
     * - rating <= 2（负反馈）：同 type_tags -8、同 cuisine_tags -8、同 taste_tags -5
     * - rating = 3：不参与
     * - 每个维度仅检查是否有交集（任一标签命中即触发），不按命中标签数量累加
     * - 上限 +20，下限 -20
     */
    private int calculateRatingPreference(Food candidate, List<RatedFoodInfo> ratedFoods) {
        int bonus = 0;
        Set<String> candidateTypes = FoodTaxonomy.parseTags(candidate.getTypeTags());
        Set<String> candidateCuisines = FoodTaxonomy.parseTags(candidate.getCuisineTags());
        Set<String> candidateTastes = FoodTaxonomy.parseTags(candidate.getTasteTags());

        for (RatedFoodInfo rated : ratedFoods) {
            Set<String> ratedTypes = FoodTaxonomy.parseTags(rated.food().getTypeTags());
            Set<String> ratedCuisines = FoodTaxonomy.parseTags(rated.food().getCuisineTags());
            Set<String> ratedTastes = FoodTaxonomy.parseTags(rated.food().getTasteTags());

            if (rated.rating() >= 4) {
                if (hasIntersection(candidateTypes, ratedTypes)) bonus += 8;
                if (hasIntersection(candidateCuisines, ratedCuisines)) bonus += 8;
                if (hasIntersection(candidateTastes, ratedTastes)) bonus += 5;
            } else if (rated.rating() <= 2) {
                if (hasIntersection(candidateTypes, ratedTypes)) bonus -= 8;
                if (hasIntersection(candidateCuisines, ratedCuisines)) bonus -= 8;
                if (hasIntersection(candidateTastes, ratedTastes)) bonus -= 5;
            }
            // rating == 3: 不参与
        }

        // 上限 +20，下限 -20
        return Math.max(-20, Math.min(20, bonus));
    }

    /**
     * 判断两个 Set 是否有交集
     */
    private boolean hasIntersection(Set<String> set1, Set<String> set2) {
        for (String s : set1) {
            if (set2.contains(s)) return true;
        }
        return false;
    }

    // ==================== 内部类 ====================

    /**
     * 评分过的食物信息（内部记录）
     */
    private record RatedFoodInfo(Food food, int rating) {}

    /**
     * 内部类：带分数的菜品
     */
    private static class ScoredFood {
        final Food food;
        final int score;
        final List<String> reasons;

        ScoredFood(Food food, int score, List<String> reasons) {
            this.food = food;
            this.score = score;
            this.reasons = reasons;
        }
    }
}
