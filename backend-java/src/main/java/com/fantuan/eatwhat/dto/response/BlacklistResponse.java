package com.fantuan.eatwhat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 黑名单响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistResponse {

    /**
     * 黑名单记录ID
     */
    private Long id;

    /**
     * 食物ID
     */
    private Long foodId;

    /**
     * 食物名称
     */
    private String foodName;

    /**
     * 食物分类
     */
    private String category;

    /**
     * 拉黑原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
