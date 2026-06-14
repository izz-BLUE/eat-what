package com.fantuan.eatwhat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {

    /**
     * 微信登录 code
     */
    @NotBlank(message = "code 不能为空")
    private String code;

    /**
     * 昵称（可选）
     */
    private String nickname;

    /**
     * 头像 URL（可选）
     */
    private String avatarUrl;
}
