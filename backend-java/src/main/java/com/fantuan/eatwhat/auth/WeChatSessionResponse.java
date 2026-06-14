package com.fantuan.eatwhat.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信 code2Session 响应
 */
@Data
public class WeChatSessionResponse {

    /**
     * 错误码，0 表示成功
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     */
    @JsonProperty("session_key")
    private String sessionKey;

    /**
     * 用户在开放平台的唯一标识（可选）
     */
    private String unionid;
}
