package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.RequireLogin;
import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.CustomFoodCreateRequest;
import com.fantuan.eatwhat.dto.response.CustomFoodResponse;
import com.fantuan.eatwhat.service.UserCustomFoodService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户自定义菜品控制器（必须登录）
 */
@RestController
@RequestMapping("/api/v1/custom-foods")
@RequiredArgsConstructor
@Validated
@RequireLogin
public class CustomFoodController {

    private final UserCustomFoodService userCustomFoodService;

    /**
     * 创建自定义菜品
     */
    @PostMapping
    public ApiResponse<CustomFoodResponse> create(@Valid @RequestBody CustomFoodCreateRequest request) {
        Long userId = UserContext.getUserId();
        CustomFoodResponse response = userCustomFoodService.create(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 查询我的自定义菜品（仅返回 enabled=true）
     */
    @GetMapping
    public ApiResponse<List<CustomFoodResponse>> list() {
        Long userId = UserContext.getUserId();
        List<CustomFoodResponse> items = userCustomFoodService.list(userId);
        return ApiResponse.success(items);
    }

    /**
     * 删除/停用自定义菜品（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable @Min(1) Long id) {
        Long userId = UserContext.getUserId();
        userCustomFoodService.delete(userId, id);
        return ApiResponse.success(null);
    }
}
