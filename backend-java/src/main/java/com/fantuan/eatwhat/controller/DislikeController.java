package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.RequireLogin;
import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.DislikeAddRequest;
import com.fantuan.eatwhat.dto.response.DislikeResponse;
import com.fantuan.eatwhat.service.UserDislikeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 不想吃控制器
 */
@RestController
@RequestMapping("/api/v1/dislike")
@RequiredArgsConstructor
@Validated
@RequireLogin
public class DislikeController {

    private final UserDislikeService userDislikeService;

    /**
     * 添加或更新不想吃
     * POST /api/v1/dislike/add
     */
    @PostMapping("/add")
    public ApiResponse<DislikeResponse> add(@Valid @RequestBody DislikeAddRequest request) {
        Long userId = UserContext.getUserId();
        DislikeResponse response = userDislikeService.addDislike(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 查询有效的不想吃列表
     * GET /api/v1/dislike/list
     */
    @GetMapping("/list")
    public ApiResponse<List<DislikeResponse>> list() {
        Long userId = UserContext.getUserId();
        List<DislikeResponse> list = userDislikeService.listActiveDislikes(userId);
        return ApiResponse.success(list);
    }

    /**
     * 解除不想吃
     * DELETE /api/v1/dislike/{dislikeId}
     */
    @DeleteMapping("/{dislikeId}")
    public ApiResponse<Void> remove(@PathVariable @Min(1) Long dislikeId) {
        Long userId = UserContext.getUserId();
        userDislikeService.removeDislike(dislikeId, userId);
        return ApiResponse.success();
    }
}
