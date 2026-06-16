package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.UserFeedback;
import com.fantuan.eatwhat.dto.request.FeedbackRequest;
import com.fantuan.eatwhat.dto.response.AdminFeedbackListResponse;
import com.fantuan.eatwhat.dto.response.FeedbackResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.UserFeedbackMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 查询反馈列表（管理后台）
     *
     * @param status  状态筛选（可选）
     * @param page    页码，从 1 开始
     * @param size    每页条数
     * @param keyword 关键词搜索（匹配 content/contact，可选）
     * @return 分页列表
     */
    public AdminFeedbackListResponse listFeedbacks(String status, int page, int size, String keyword) {
        Page<UserFeedback> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.isBlank()) {
            wrapper.eq(UserFeedback::getStatus, status);
        }

        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            wrapper.and(w -> w.like(UserFeedback::getContent, trimmed)
                    .or().like(UserFeedback::getContact, trimmed));
        }

        wrapper.orderByDesc(UserFeedback::getCreatedAt);

        Page<UserFeedback> result = userFeedbackMapper.selectPage(pageObj, wrapper);

        List<FeedbackResponse> items = result.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return AdminFeedbackListResponse.builder()
                .items(items)
                .total(result.getTotal())
                .page(page)
                .size(size)
                .build();
    }

    /**
     * 更新反馈状态（管理后台）
     *
     * @param id     反馈 ID
     * @param status 新状态：NEW / REVIEWED / RESOLVED / IGNORED
     * @return 更新后的反馈
     */
    public FeedbackResponse updateStatus(Long id, String status) {
        UserFeedback feedback = userFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(ResultCode.FEEDBACK_NOT_FOUND);
        }
        feedback.setStatus(status);
        feedback.setUpdatedAt(LocalDateTime.now());
        userFeedbackMapper.updateById(feedback);

        log.info("反馈状态更新: id={}, status={}", id, status);

        return toResponse(feedback);
    }

    /**
     * Entity → Response DTO
     */
    private FeedbackResponse toResponse(UserFeedback entity) {
        return FeedbackResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .rating(entity.getRating())
                .content(entity.getContent())
                .contact(entity.getContact())
                .page(entity.getPage())
                .systemInfo(entity.getSystemInfo())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
