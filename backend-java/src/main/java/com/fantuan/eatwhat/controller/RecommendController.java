package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.common.RecommendDict;
import com.fantuan.eatwhat.dto.request.RecommendRequest;
import com.fantuan.eatwhat.dto.response.RecommendResponse;
import com.fantuan.eatwhat.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐控制器（可选登录）
 */
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 一键推荐
     * GET /api/v1/recommend?mealType=晚餐&priceLevel=15-25&taste=辣&typeTags=快餐,面食&cuisineTags=川菜
     * 兼容旧参数：GET /api/v1/recommend?categories=快餐,面食
     */
    @GetMapping
    public ApiResponse<RecommendResponse> recommend(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String priceLevel,
            @RequestParam(required = false) String taste,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String typeTags,
            @RequestParam(required = false) String cuisineTags) {

        Long userId = UserContext.getUserId();

        // 参数校验
        if (mealType != null && !mealType.isEmpty() && !RecommendDict.isValidMealType(mealType)) {
            return ApiResponse.fail(1001, "无效的餐段: " + mealType);
        }
        if (priceLevel != null && !priceLevel.isEmpty() && !RecommendDict.isValidPriceLevel(priceLevel)) {
            return ApiResponse.fail(1001, "无效的参考价位: " + priceLevel);
        }
        if (taste != null && !taste.isEmpty() && !RecommendDict.isValidTaste(taste)) {
            return ApiResponse.fail(1001, "无效的口味: " + taste);
        }

        // 合并新旧分类参数
        List<String> mergedTypeTags = new ArrayList<>();
        List<String> mergedCuisineTags = new ArrayList<>();

        // 解析新参数
        if (typeTags != null && !typeTags.isEmpty()) {
            mergedTypeTags.addAll(parseCommaSeparated(typeTags));
        }
        if (cuisineTags != null && !cuisineTags.isEmpty()) {
            mergedCuisineTags.addAll(parseCommaSeparated(cuisineTags));
        }

        // 解析旧参数并分发到 typeTags 或 cuisineTags
        if (categories != null && !categories.isEmpty()) {
            for (String cat : parseCommaSeparated(categories)) {
                String kind = RecommendDict.classifyCategory(cat);
                if (kind == null) {
                    return ApiResponse.fail(1001, "无效的分类: " + cat);
                }
                if ("type".equals(kind)) {
                    mergedTypeTags.add(cat);
                } else {
                    mergedCuisineTags.add(cat);
                }
            }
        }

        // 去重
        mergedTypeTags = new ArrayList<>(new LinkedHashSet<>(mergedTypeTags));
        mergedCuisineTags = new ArrayList<>(new LinkedHashSet<>(mergedCuisineTags));

        // 校验新参数中的值合法
        for (String t : mergedTypeTags) {
            if (!RecommendDict.isValidTypeTag(t)) {
                return ApiResponse.fail(1001, "无效的食物类型: " + t);
            }
        }
        for (String c : mergedCuisineTags) {
            if (!RecommendDict.isValidCuisineTag(c)) {
                return ApiResponse.fail(1001, "无效的菜系: " + c);
            }
        }

        // 合计最多 3 个
        int total = mergedTypeTags.size() + mergedCuisineTags.size();
        if (total > 3) {
            return ApiResponse.fail(1001, "分类选择合计最多3个，当前" + total + "个");
        }

        RecommendRequest request = new RecommendRequest();
        request.setMealType(emptyToNull(mealType));
        request.setPriceLevel(emptyToNull(priceLevel));
        request.setTaste(emptyToNull(taste));
        request.setUserId(userId);
        request.setTypeTags(mergedTypeTags.isEmpty() ? null : mergedTypeTags);
        request.setCuisineTags(mergedCuisineTags.isEmpty() ? null : mergedCuisineTags);

        RecommendResponse response = recommendService.recommend(request);
        if (response == null) {
            return ApiResponse.fail(2002, "当前条件没有合适菜品，请调整分类或口味");
        }
        return ApiResponse.success(response);
    }

    /**
     * 换一个
     * GET /api/v1/recommend/swap?mealType=晚餐&excludeFoodIds=1,2,3
     */
    @GetMapping("/swap")
    public ApiResponse<RecommendResponse> swap(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String priceLevel,
            @RequestParam(required = false) String taste,
            @RequestParam(required = false) String excludeFoodIds,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String typeTags,
            @RequestParam(required = false) String cuisineTags) {

        Long userId = UserContext.getUserId();

        // 参数校验
        if (mealType != null && !mealType.isEmpty() && !RecommendDict.isValidMealType(mealType)) {
            return ApiResponse.fail(1001, "无效的餐段: " + mealType);
        }
        if (priceLevel != null && !priceLevel.isEmpty() && !RecommendDict.isValidPriceLevel(priceLevel)) {
            return ApiResponse.fail(1001, "无效的参考价位: " + priceLevel);
        }
        if (taste != null && !taste.isEmpty() && !RecommendDict.isValidTaste(taste)) {
            return ApiResponse.fail(1001, "无效的口味: " + taste);
        }

        // 合并新旧分类参数（与 recommend 相同逻辑）
        List<String> mergedTypeTags = new ArrayList<>();
        List<String> mergedCuisineTags = new ArrayList<>();

        if (typeTags != null && !typeTags.isEmpty()) {
            mergedTypeTags.addAll(parseCommaSeparated(typeTags));
        }
        if (cuisineTags != null && !cuisineTags.isEmpty()) {
            mergedCuisineTags.addAll(parseCommaSeparated(cuisineTags));
        }
        if (categories != null && !categories.isEmpty()) {
            for (String cat : parseCommaSeparated(categories)) {
                String kind = RecommendDict.classifyCategory(cat);
                if (kind == null) {
                    return ApiResponse.fail(1001, "无效的分类: " + cat);
                }
                if ("type".equals(kind)) {
                    mergedTypeTags.add(cat);
                } else {
                    mergedCuisineTags.add(cat);
                }
            }
        }

        mergedTypeTags = new ArrayList<>(new LinkedHashSet<>(mergedTypeTags));
        mergedCuisineTags = new ArrayList<>(new LinkedHashSet<>(mergedCuisineTags));

        for (String t : mergedTypeTags) {
            if (!RecommendDict.isValidTypeTag(t)) {
                return ApiResponse.fail(1001, "无效的食物类型: " + t);
            }
        }
        for (String c : mergedCuisineTags) {
            if (!RecommendDict.isValidCuisineTag(c)) {
                return ApiResponse.fail(1001, "无效的菜系: " + c);
            }
        }

        int total = mergedTypeTags.size() + mergedCuisineTags.size();
        if (total > 3) {
            return ApiResponse.fail(1001, "分类选择合计最多3个，当前" + total + "个");
        }

        RecommendRequest request = new RecommendRequest();
        request.setMealType(emptyToNull(mealType));
        request.setPriceLevel(emptyToNull(priceLevel));
        request.setTaste(emptyToNull(taste));
        request.setUserId(userId);
        request.setTypeTags(mergedTypeTags.isEmpty() ? null : mergedTypeTags);
        request.setCuisineTags(mergedCuisineTags.isEmpty() ? null : mergedCuisineTags);

        // 解析 excludeFoodIds
        if (excludeFoodIds != null && !excludeFoodIds.isEmpty()) {
            try {
                List<Long> ids = Arrays.stream(excludeFoodIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                request.setExcludeFoodIds(ids);
            } catch (NumberFormatException e) {
                return ApiResponse.fail(1001, "excludeFoodIds 格式错误");
            }
        } else {
            request.setExcludeFoodIds(Collections.emptyList());
        }

        RecommendResponse response = recommendService.recommend(request);
        if (response == null) {
            return ApiResponse.fail(2002, "当前条件没有合适菜品，请调整分类或口味");
        }
        return ApiResponse.success(response);
    }

    /**
     * 解析逗号分隔参数
     */
    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 空字符串转为 null（空=不限）
     */
    private String emptyToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }
}
