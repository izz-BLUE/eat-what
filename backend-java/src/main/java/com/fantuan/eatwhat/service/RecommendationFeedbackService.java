package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.RecommendationFeedback;
import com.fantuan.eatwhat.dto.request.RecommendationFeedbackRequest;
import com.fantuan.eatwhat.dto.response.RecommendationFeedbackResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.RecommendationFeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 推荐反馈服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationFeedbackService {

    /** 允许的原因值集合 */
    public static final Set<String> VALID_REASONS = Set.of(
            "RECENTLY_EATEN",
            "NOT_IN_MOOD",
            "TOO_EXPENSIVE",
            "TOO_HEAVY",
            "WRONG_TASTE",
            "WRONG_CATEGORY",
            "OTHER"
    );

    /** 允许的来源值集合 */
    private static final Set<String> VALID_SOURCES = Set.of("DEFAULT", "CUSTOM");

    private final RecommendationFeedbackMapper recommendationFeedbackMapper;

    /**
     * 提交推荐反馈
     *
     * @param userId  用户ID（可能为null，表示匿名提交）
     * @param request 反馈请求 DTO
     * @return 反馈响应
     */
    public RecommendationFeedbackResponse createFeedback(Long userId, RecommendationFeedbackRequest request) {
        // 1. 校验 source
        String source = request.getSource();
        if (source == null || !VALID_SOURCES.contains(source)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "来源无效，必须为DEFAULT或CUSTOM");
        }

        // 2. 校验 foodId / customFoodId 互斥
        if ("DEFAULT".equals(source)) {
            if (request.getFoodId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "DEFAULT来源时foodId必填");
            }
            if (request.getCustomFoodId() != null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "DEFAULT来源时customFoodId必须为空");
            }
        } else {
            if (request.getCustomFoodId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "CUSTOM来源时customFoodId必填");
            }
            if (request.getFoodId() != null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "CUSTOM来源时foodId必须为空");
            }
        }

        // 3. 校验 foodName
        String foodName = request.getFoodName();
        if (foodName == null || foodName.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "菜品名称不能为空");
        }
        foodName = foodName.trim();
        if (foodName.length() > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "菜品名称最长100字");
        }

        // 4. 校验 reason
        String reason = request.getReason();
        if (reason == null || !VALID_REASONS.contains(reason)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "反馈原因无效");
        }

        // 5. 保存
        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setUserId(userId);
        feedback.setFoodSource(source);
        feedback.setFoodId(request.getFoodId());
        feedback.setCustomFoodId(request.getCustomFoodId());
        feedback.setFoodName(foodName);
        feedback.setReason(reason);
        feedback.setMealType(trimToNull(request.getMealType()));
        feedback.setPriceLevel(trimToNull(request.getPriceLevel()));
        feedback.setTaste(trimToNull(request.getTaste()));
        feedback.setTypeTags(trimToNull(request.getTypeTags()));
        feedback.setCuisineTags(trimToNull(request.getCuisineTags()));
        feedback.setCreatedAt(LocalDateTime.now());

        recommendationFeedbackMapper.insert(feedback);

        log.info("推荐反馈提交成功: id={}, userId={}, source={}, reason={}", feedback.getId(), userId, source, reason);

        return RecommendationFeedbackResponse.builder()
                .id(feedback.getId())
                .reason(feedback.getReason())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
