package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.FeedbackRequest;
import com.fantuan.eatwhat.dto.response.FeedbackResponse;
import com.fantuan.eatwhat.service.UserFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 意见反馈控制器
 * 允许匿名访问，不要求登录。
 * 如果请求携带有效 token，自动记录 userId。
 */
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 提交意见反馈
     * POST /api/v1/feedback
     */
    @PostMapping
    public ApiResponse<FeedbackResponse> submitFeedback(@Valid @RequestBody FeedbackRequest request) {
        Long userId = UserContext.getUserId();
        FeedbackResponse response = userFeedbackService.createFeedback(userId, request);
        return ApiResponse.success(response);
    }
}
