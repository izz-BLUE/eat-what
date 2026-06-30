package com.fantuan.eatwhat.auth;

import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 微信认证客户端 - 生产实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wechat.mock-enabled", havingValue = "false", matchIfMissing = true)
public class WeChatAuthClientProd implements WeChatAuthClient {

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    private final RestClient restClient = RestClient.create();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String code2Session(String code) throws Exception {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        // 先以 String 接收原始响应，避免微信返回 text/plain 导致 UnknownContentTypeException
        String body;
        try {
            body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("微信接口请求失败: {}", e.getClass().getSimpleName());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        if (body == null || body.isEmpty()) {
            log.error("微信接口返回空响应");
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        WeChatSessionResponse response;
        try {
            response = objectMapper.readValue(body, WeChatSessionResponse.class);
        } catch (Exception e) {
            log.error("微信接口响应解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        if (response.getErrcode() != null && response.getErrcode() != 0) {
            log.error("微信接口错误: errcode={}, errmsg={}", response.getErrcode(), response.getErrmsg());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        if (response.getOpenid() == null || response.getOpenid().isEmpty()) {
            log.error("微信接口返回 openid 为空");
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        return response.getOpenid();
    }
}
