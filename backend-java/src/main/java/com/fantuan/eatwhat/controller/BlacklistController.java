package com.fantuan.eatwhat.controller;

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
public class BlacklistController {

    private final UserBlacklistService userBlacklistService;

    /**
     * 加入黑名单
     * POST /api/v1/blacklist/add
     */
    @PostMapping("/add")
    public ApiResponse<BlacklistResponse> add(@Valid @RequestBody BlacklistAddRequest request) {
        BlacklistResponse response = userBlacklistService.addToBlacklist(request);
        return ApiResponse.success(response);
    }

    /**
     * 查询黑名单列表
     * GET /api/v1/blacklist/list?userId=1
     */
    @GetMapping("/list")
    public ApiResponse<List<BlacklistResponse>> list(@RequestParam @Min(1) Long userId) {
        List<BlacklistResponse> list = userBlacklistService.listBlacklist(userId);
        return ApiResponse.success(list);
    }

    /**
     * 移出黑名单
     * DELETE /api/v1/blacklist/{blacklistId}?userId=1
     */
    @DeleteMapping("/{blacklistId}")
    public ApiResponse<Void> remove(@PathVariable @Min(1) Long blacklistId,
                                     @RequestParam @Min(1) Long userId) {
        userBlacklistService.removeFromBlacklist(blacklistId, userId);
        return ApiResponse.success();
    }
}
