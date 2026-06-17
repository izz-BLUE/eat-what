package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.auth.UserContext;
import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.dto.request.RecommendationFeedbackRequest;
import com.fantuan.eatwhat.dto.response.RecommendationFeedbackResponse;
import com.fantuan.eatwhat.service.RecommendationFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 推荐反馈控制器
 * 允许匿名访问，不要求登录。
 * 如果请求携带有效 token，自动记录 userId。
 */
@RestController
@RequestMapping("/api/v1/recommendation-feedback")
@RequiredArgsConstructor
public class RecommendationFeedbackController {

    private final RecommendationFeedbackService recommendationFeedbackService;

    /**
     * 提交推荐反馈（不喜欢原因）
     * POST /api/v1/recommendation-feedback
     */
    @PostMapping
    public ApiResponse<RecommendationFeedbackResponse> submitFeedback(@RequestBody RecommendationFeedbackRequest request) {
        Long userId = UserContext.getUserId();
        RecommendationFeedbackResponse response = recommendationFeedbackService.createFeedback(userId, request);
        return ApiResponse.success(response);
    }
}
