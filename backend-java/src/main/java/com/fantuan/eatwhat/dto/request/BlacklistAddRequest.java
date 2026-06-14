package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 加入黑名单请求 DTO
 */
@Data
public class BlacklistAddRequest {

    /**
     * 食物ID
     */
    @NotNull(message = "foodId 不能为空")
    @Min(value = 1, message = "foodId 必须大于 0")
    private Long foodId;

    /**
     * 拉黑原因
     */
    @Size(max = 128, message = "reason 最长 128 个字符")
    private String reason;
}
