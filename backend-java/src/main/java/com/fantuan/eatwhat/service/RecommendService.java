package com.fantuan.eatwhat.service;

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
     * @return 推荐结果
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

        // 4. 排除用户不想吃的分类（有效期内）
        LocalDateTime now = LocalDateTime.now();
        if (request.getUserId() != null) {
            Set<String> dislikeCategories = userDislikeService.getActiveDislikeCategories(request.getUserId(), now);
            if (!dislikeCategories.isEmpty()) {
                candidates = candidates.stream()
                        .filter(f -> !dislikeCategories.contains(f.getCategory()))
                        .collect(Collectors.toList());
            }
        }

        // 5. 用户选择的分类硬过滤
        if (!CollectionUtils.isEmpty(request.getCategories())) {
            Set<String> selectedCategories = new HashSet<>(request.getCategories());
            candidates = candidates.stream()
                    .filter(f -> selectedCategories.contains(f.getCategory()))
                    .collect(Collectors.toList());
        }

        // 6. 清淡时排除重口味分类
        if ("清淡".equals(request.getTaste())) {
            Set<String> heavyCategories = Set.of("火锅", "烧烤", "川菜", "湘菜");
            candidates = candidates.stream()
                    .filter(f -> !heavyCategories.contains(f.getCategory()))
                    .collect(Collectors.toList());
        }

        // 7. 口味硬过滤
        if (StringUtils.hasText(request.getTaste()) && !"不限".equals(request.getTaste())) {
            candidates = candidates.stream()
                    .filter(f -> matchesTaste(f.getTasteTags(), request.getTaste()))
                    .collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // 5. 查询用户最近7天吃过的食物（如果有 userId）
        Map<Long, LocalDateTime> recentEatenMap = eatRecordService.getRecentEatenFoodMap(request.getUserId());

        // 4. 计算每个菜品的得分
        List<ScoredFood> scoredFoods = candidates.stream()
                .map(food -> calculateScore(food, request, recentEatenMap, now))
                .collect(Collectors.toList());

        // 4. 按得分排序，取 Top 5
        scoredFoods.sort((a, b) -> Integer.compare(b.score, a.score));
        List<ScoredFood> top5 = scoredFoods.stream()
                .limit(5)
                .collect(Collectors.toList());

        // 5. 从 Top 5 中随机选一个
        ScoredFood selected = top5.get(new Random().nextInt(top5.size()));

        // 6. 构建响应
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
                                       Map<Long, LocalDateTime> recentEatenMap, LocalDateTime now) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. 餐段匹配：+30
        int mealTypeScore = calculateMealTypeScore(food, request.getMealType());
        if (mealTypeScore > 0) {
            score += mealTypeScore;
            reasons.add("适合当前餐段");
        }

        // 2. 价格匹配：+20
        int priceScore = calculatePriceScore(food, request.getPriceLevel());
        if (priceScore > 0) {
            score += priceScore;
            reasons.add("符合预算");
        }

        // 3. 口味匹配：+20
        int tasteScore = calculateTasteScore(food, request.getTaste());
        if (tasteScore > 0) {
            score += tasteScore;
            reasons.add("符合口味偏好");
        }

        // 4. 最近吃过降权
        int recentEatenDeduction = calculateRecentEatenDeduction(food.getId(), recentEatenMap, now);
        if (recentEatenDeduction == 0) {
            reasons.add("最近几天没吃过，换换口味");
        }
        score += recentEatenDeduction;

        // 5. 随机因素：0-19
        int randomScore = new Random().nextInt(20);
        score += randomScore;

        return new ScoredFood(food, score, reasons);
    }

    /**
     * 计算最近吃过扣分
     * 小于 24 小时：-100
     * 大于等于 24 小时且小于 48 小时：-80
     * 大于等于 48 小时且小于 72 小时：-60
     * 大于等于 72 小时且小于等于 168 小时：-30
     * 超过 168 小时：0
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

    /**
     * 计算餐段匹配得分
     * 早餐（06:00-10:00）：快餐、小吃、面食
     * 午餐（10:00-14:00）：快餐、小吃、面食、川菜、粤菜、湘菜
     * 下午茶（14:00-17:00）：甜品、小吃
     * 晚餐（17:00-21:00）：火锅、烧烤、川菜、粤菜
     * 夜宵（21:00-06:00）：小吃、烧烤、快餐
     */
    private int calculateMealTypeScore(Food food, String mealType) {
        if (!StringUtils.hasText(mealType)) {
            return 0;
        }

        String category = food.getCategory();
        switch (mealType) {
            case "早餐":
                if (isBreakfastCategory(category)) return 30;
                break;
            case "午餐":
                if (isLunchCategory(category)) return 30;
                break;
            case "晚餐":
                if (isDinnerCategory(category)) return 30;
                break;
            case "夜宵":
                if (isMidnightSnackCategory(category)) return 30;
                break;
            default:
                return 0;
        }
        return 0;
    }

    private boolean isBreakfastCategory(String category) {
        return "快餐".equals(category) || "小吃".equals(category) || "面食".equals(category);
    }

    private boolean isLunchCategory(String category) {
        return "快餐".equals(category) || "小吃".equals(category) || "面食".equals(category)
                || "川菜".equals(category) || "粤菜".equals(category) || "湘菜".equals(category)
                || "家常菜".equals(category);
    }

    private boolean isDinnerCategory(String category) {
        return "火锅".equals(category) || "烧烤".equals(category) || "川菜".equals(category)
                || "粤菜".equals(category) || "西餐".equals(category) || "日料".equals(category)
                || "韩餐".equals(category);
    }

    private boolean isMidnightSnackCategory(String category) {
        return "小吃".equals(category) || "烧烤".equals(category) || "快餐".equals(category);
    }

    /**
     * 口味硬过滤（按逗号拆分精确匹配）
     *
     * @param tasteTags 食物口味标签，逗号分隔
     * @param taste 用户选择的口味偏好
     * @return 是否匹配
     */
    private boolean matchesTaste(String tasteTags, String taste) {
        if (!StringUtils.hasText(tasteTags)) {
            return false;
        }

        // 按逗号拆分并 trim
        Set<String> tags = Arrays.stream(tasteTags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        switch (taste) {
            case "清淡":
                // 必须包含"清淡"，同时不能包含辣、微辣、麻
                return tags.contains("清淡") && !tags.contains("辣") && !tags.contains("微辣") && !tags.contains("麻");
            case "不辣":
                // 排除辣、微辣、麻
                return !tags.contains("辣") && !tags.contains("微辣") && !tags.contains("麻");
            case "辣":
                // 必须包含辣或微辣
                return tags.contains("辣") || tags.contains("微辣");
            case "重口":
                // 包含辣、麻、酸之一，或者同时包含咸和香
                return tags.contains("辣") || tags.contains("麻") || tags.contains("酸")
                        || (tags.contains("咸") && tags.contains("香"));
            default:
                return true;
        }
    }

    /**
     * 计算价格匹配得分
     * 用户价格偏好：15以内(priceLevel=1)、15-25(priceLevel=2)、25-40(priceLevel=3)、不限
     */
    private int calculatePriceScore(Food food, String priceLevel) {
        if (!StringUtils.hasText(priceLevel) || "不限".equals(priceLevel)) {
            return 0;
        }

        int foodPrice = food.getPriceLevel() != null ? food.getPriceLevel() : 0;
        switch (priceLevel) {
            case "15以内":
                if (foodPrice <= 1) return 20;
                break;
            case "15-25":
                if (foodPrice == 2) return 20;
                break;
            case "25-40":
                if (foodPrice == 3) return 20;
                break;
            default:
                return 0;
        }
        return 0;
    }

    /**
     * 计算口味匹配得分
     * 用户口味偏好：清淡、重口、辣、不辣
     */
    private int calculateTasteScore(Food food, String taste) {
        if (!StringUtils.hasText(taste)) {
            return 0;
        }

        String tasteTags = food.getTasteTags();
        if (!StringUtils.hasText(tasteTags)) {
            return 0;
        }

        // 按逗号拆分精确匹配
        Set<String> tags = Arrays.stream(tasteTags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        switch (taste) {
            case "清淡":
                if (tags.contains("清淡") || tags.contains("鲜")) return 20;
                break;
            case "重口":
                // 与 matchesTaste 规则完全一致
                if (tags.contains("辣") || tags.contains("麻") || tags.contains("酸")
                        || (tags.contains("咸") && tags.contains("香"))) return 20;
                break;
            case "辣":
                if (tags.contains("辣") || tags.contains("麻")) return 20;
                break;
            case "不辣":
                if (!tags.contains("辣") && !tags.contains("麻")) return 20;
                break;
            default:
                return 0;
        }
        return 0;
    }

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
