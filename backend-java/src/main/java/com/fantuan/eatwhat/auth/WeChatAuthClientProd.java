package com.fantuan.eatwhat.auth;

import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.exception.BusinessException;
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

    @Override
    public String code2Session(String code) throws Exception {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        WeChatSessionResponse response;
        try {
            response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(WeChatSessionResponse.class);
        } catch (Exception e) {
            log.error("微信接口请求失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED, "微信接口请求失败");
        }

        if (response == null) {
            log.error("微信接口返回空响应");
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED, "微信接口返回空响应");
        }

        if (response.getErrcode() != null && response.getErrcode() != 0) {
            log.error("微信接口错误: errcode={}", response.getErrcode());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED, "微信接口错误: " + response.getErrmsg());
        }

        if (response.getOpenid() == null || response.getOpenid().isEmpty()) {
            log.error("微信接口返回 openid 为空");
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED, "微信接口返回 openid 为空");
        }

        return response.getOpenid();
    }
}
