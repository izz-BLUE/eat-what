package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.RecommendRequest;
import com.fantuan.eatwhat.dto.response.RecommendResponse;
import com.fantuan.eatwhat.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推荐控制器
 */
@RestController
@RequestMapping("/api/v1/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 一键推荐
     * GET /api/v1/recommend?mealType=晚餐&priceLevel=15-25&taste=重口&userId=1
     */
    @GetMapping
    public ApiResponse<RecommendResponse> recommend(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String priceLevel,
            @RequestParam(required = false) String taste,
            @RequestParam(required = false) Long userId) {

        RecommendRequest request = new RecommendRequest();
        request.setMealType(mealType);
        request.setPriceLevel(priceLevel);
        request.setTaste(taste);
        request.setUserId(userId);

        RecommendResponse response = recommendService.recommend(request);
        if (response == null) {
            return ApiResponse.fail(2002, "没有找到合适的菜品");
        }
        return ApiResponse.success(response);
    }

    /**
     * 换一个
     * GET /api/v1/recommend/swap?mealType=晚餐&priceLevel=15-25&excludeFoodIds=1,2,3&userId=1
     */
    @GetMapping("/swap")
    public ApiResponse<RecommendResponse> swap(
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String priceLevel,
            @RequestParam(required = false) String taste,
            @RequestParam(required = false) String excludeFoodIds,
            @RequestParam(required = false) Long userId) {

        RecommendRequest request = new RecommendRequest();
        request.setMealType(mealType);
        request.setPriceLevel(priceLevel);
        request.setTaste(taste);
        request.setUserId(userId);

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
            return ApiResponse.fail(2002, "没有找到合适的菜品");
        }
        return ApiResponse.success(response);
    }
}
