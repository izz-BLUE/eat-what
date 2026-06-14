package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.RequireLogin;
import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.EatRecordRequest;
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
     * 我就吃它
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
