package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.response.FoodResponse;
import com.fantuan.eatwhat.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜品控制器
 */
@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    /**
     * 查询菜品列表
     * GET /api/v1/foods?category=快餐&priceLevel=2
     */
    @GetMapping
    public ApiResponse<List<FoodResponse>> listFoods(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer priceLevel) {
        List<FoodResponse> foods = foodService.listFoods(category, priceLevel);
        return ApiResponse.success(foods);
    }
}
