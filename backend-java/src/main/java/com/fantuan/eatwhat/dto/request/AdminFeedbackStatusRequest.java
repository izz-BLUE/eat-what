package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理后台 - 反馈状态更新请求
 */
@Data
public class AdminFeedbackStatusRequest {

    /** 状态：NEW / REVIEWED / RESOLVED / IGNORED */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(NEW|REVIEWED|RESOLVED|IGNORED)$", message = "状态值不合法，允许：NEW/REVIEWED/RESOLVED/IGNORED")
    private String status;
}
