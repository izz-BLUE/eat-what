package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.RequireLogin;
import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.CompleteRecordRequest;
import com.fantuan.eatwhat.dto.request.DecideRecordRequest;
import com.fantuan.eatwhat.dto.request.EatRecordRequest;
import com.fantuan.eatwhat.dto.request.ReviewRecordRequest;
import com.fantuan.eatwhat.dto.response.EatRecordResponse;
import com.fantuan.eatwhat.service.EatRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 吃过记录控制器
 */
@RestController
@RequestMapping("/api/v1/record")
@RequiredArgsConstructor
@RequireLogin
public class RecordController {

    private final EatRecordService eatRecordService;

    /**
     * 决定吃什么（创建 DECIDED 记录）
     * POST /api/v1/record/decide
     */
    @PostMapping("/decide")
    public ApiResponse<EatRecordResponse> decide(@Valid @RequestBody DecideRecordRequest request) {
        Long userId = UserContext.getUserId();
        EatRecordResponse response = eatRecordService.createDecision(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 完成用餐（DECIDED → EATEN）
     * POST /api/v1/record/{recordId}/complete
     */
    @PostMapping("/{recordId}/complete")
    public ApiResponse<EatRecordResponse> complete(
            @PathVariable Long recordId,
            @Valid @RequestBody CompleteRecordRequest request) {
        Long userId = UserContext.getUserId();
        EatRecordResponse response = eatRecordService.completeRecord(userId, recordId, request);
        return ApiResponse.success(response);
    }

    /**
     * 修改已吃记录的评价
     * PUT /api/v1/record/{recordId}/review
     */
    @PutMapping("/{recordId}/review")
    public ApiResponse<EatRecordResponse> review(
            @PathVariable Long recordId,
            @Valid @RequestBody ReviewRecordRequest request) {
        Long userId = UserContext.getUserId();
        EatRecordResponse response = eatRecordService.reviewRecord(userId, recordId, request);
        return ApiResponse.success(response);
    }

    /**
     * 取消决定（删除 DECIDED 记录）
     * DELETE /api/v1/record/{recordId}/decision
     */
    @DeleteMapping("/{recordId}/decision")
    public ApiResponse<Void> cancelDecision(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        eatRecordService.cancelDecision(userId, recordId);
        return ApiResponse.success();
    }

    /**
     * 获取单条记录详情
     * GET /api/v1/record/{recordId}
     */
    @GetMapping("/{recordId}")
    public ApiResponse<EatRecordResponse> getRecord(@PathVariable Long recordId) {
        Long userId = UserContext.getUserId();
        EatRecordResponse response = eatRecordService.getRecord(userId, recordId);
        return ApiResponse.success(response);
    }

    /**
     * 我就吃它（旧接口，直接创建 EATEN 记录，保留兼容）
     * POST /api/v1/record/eat
     */
    @PostMapping("/eat")
    public ApiResponse<EatRecordResponse> eat(@Valid @RequestBody EatRecordRequest request) {
        Long userId = UserContext.getUserId();
        EatRecordResponse response = eatRecordService.createRecord(userId, request);
        return ApiResponse.success(response);
    }

    /**
     * 获取吃过记录列表
     * GET /api/v1/record/list?limit=20
     */
    @GetMapping("/list")
    public ApiResponse<List<EatRecordResponse>> list(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        Long userId = UserContext.getUserId();
        List<EatRecordResponse> records = eatRecordService.listRecords(userId, limit);
        return ApiResponse.success(records);
    }
}
