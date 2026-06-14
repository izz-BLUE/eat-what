package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 不想吃响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DislikeResponse {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 食物分类
     */
    private String category;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
