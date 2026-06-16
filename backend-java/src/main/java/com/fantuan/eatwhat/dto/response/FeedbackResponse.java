package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 意见反馈响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private String type;
    private Integer rating;
    private String content;
    private String contact;
    private String page;
    private String systemInfo;
    private String status;
    private LocalDateTime createdAt;
}
