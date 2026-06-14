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

        if (candidates.isEmpty()) {
            return null;
        }

        // 4. 查询用户最近7天吃过的食物（如果有 userId）
        Map<Long, LocalDateTime> recentEatenMap = eatRecordService.getRecentEatenFoodMap(request.getUserId());
        LocalDateTime now = LocalDateTime.now();

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

        boolean hasLight = tasteTags.contains("清淡") || tasteTags.contains("鲜");
        boolean hasSpicy = tasteTags.contains("辣") || tasteTags.contains("麻");
        boolean hasStrong = tasteTags.contains("咸") || tasteTags.contains("香") || tasteTags.contains("重");

        switch (taste) {
            case "清淡":
                if (hasLight) return 20;
                break;
            case "重口":
                if (hasStrong) return 20;
                break;
            case "辣":
                if (hasSpicy) return 20;
                break;
            case "不辣":
                if (!hasSpicy) return 20;
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
