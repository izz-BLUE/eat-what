package com.fantuan.eatwhat.auth;

/**
 * 微信认证客户端接口
 */
public interface WeChatAuthClient {

    /**
     * 使用 code 换取 openid
     *
     * @param code 微信登录 code
     * @return openid
     * @throws Exception 微信接口调用失败
     */
    String code2Session(String code) throws Exception;
}
