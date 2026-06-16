package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.AdminFeedbackStatusRequest;
import com.fantuan.eatwhat.dto.response.AdminFeedbackListResponse;
import com.fantuan.eatwhat.dto.response.FeedbackResponse;
import com.fantuan.eatwhat.service.UserFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台 - 反馈管理控制器
 * <p>
 * 仅用于开发/管理用途，小程序端不调用。
 * 认证方式：X-Admin-Token 请求头。
 */
@RestController
@RequestMapping("/api/v1/admin/feedback")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 查询反馈列表
     * GET /api/v1/admin/feedback
     */
    @GetMapping
    public ApiResponse<AdminFeedbackListResponse> listFeedbacks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        // 限制每页最大 100 条
        if (size > 100) {
            size = 100;
        }
        if (page < 1) {
            page = 1;
        }

        AdminFeedbackListResponse response = userFeedbackService.listFeedbacks(status, page, size, keyword);
        return ApiResponse.success(response);
    }

    /**
     * 更新反馈状态
     * PUT /api/v1/admin/feedback/{id}/status
     */
    @PutMapping("/{id}/status")
    public ApiResponse<FeedbackResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminFeedbackStatusRequest request) {

        FeedbackResponse response = userFeedbackService.updateStatus(id, request.getStatus());
        return ApiResponse.success(response);
    }
}
