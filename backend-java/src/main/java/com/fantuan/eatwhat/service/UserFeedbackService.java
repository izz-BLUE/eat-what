package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.domain.entity.UserFeedback;
import com.fantuan.eatwhat.dto.request.FeedbackRequest;
import com.fantuan.eatwhat.dto.response.FeedbackResponse;
import com.fantuan.eatwhat.mapper.UserFeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 意见反馈服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackService {

    private final UserFeedbackMapper userFeedbackMapper;

    /**
     * 提交意见反馈
     *
     * @param userId  用户ID（可能为null，表示匿名提交）
     * @param request 反馈请求 DTO
     * @return 反馈响应
     */
    public FeedbackResponse createFeedback(Long userId, FeedbackRequest request) {
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setType(request.getType());
        feedback.setRating(request.getRating());
        feedback.setContent(request.getContent());
        feedback.setContact(trimToNull(request.getContact()));
        feedback.setPage(trimToNull(request.getPage()));
        feedback.setSystemInfo(trimToNull(request.getSystemInfo()));
        feedback.setStatus("NEW");
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        userFeedbackMapper.insert(feedback);

        log.info("反馈提交成功: id={}, userId={}, type={}", feedback.getId(), userId, request.getType());

        return FeedbackResponse.builder()
                .id(feedback.getId())
                .type(feedback.getType())
                .rating(feedback.getRating())
                .content(feedback.getContent())
                .contact(feedback.getContact())
                .page(feedback.getPage())
                .systemInfo(feedback.getSystemInfo())
                .status(feedback.getStatus())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
