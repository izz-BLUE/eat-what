package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.RequireLogin;
import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.BlacklistAddRequest;
import com.fantuan.eatwhat.dto.response.BlacklistResponse;
import com.fantuan.eatwhat.service.UserBlacklistService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 黑名单控制器
 */
@RestController
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
@Validated
@RequireLogin
public class BlacklistController {

    private final UserBlacklistService userBlacklistService;

    /**
     * 加入黑名单
     * POST /api/v1/blacklist/add
     */
    @PostMapping("/add")
    public ApiResponse<BlacklistResponse> add(@Valid @RequestBody BlacklistAddRequest request) {
        Long userId = UserContext.getUserId();
        BlacklistResponse response = userBlacklistService.addToBlacklist(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 查询黑名单列表
     * GET /api/v1/blacklist/list
     */
    @GetMapping("/list")
    public ApiResponse<List<BlacklistResponse>> list() {
        Long userId = UserContext.getUserId();
        List<BlacklistResponse> list = userBlacklistService.listBlacklist(userId);
        return ApiResponse.success(list);
    }

    /**
     * 移出黑名单
     * DELETE /api/v1/blacklist/{blacklistId}
     */
    @DeleteMapping("/{blacklistId}")
    public ApiResponse<Void> remove(@PathVariable @Min(1) Long blacklistId) {
        Long userId = UserContext.getUserId();
        userBlacklistService.removeFromBlacklist(blacklistId, userId);
        return ApiResponse.success();
    }
}
