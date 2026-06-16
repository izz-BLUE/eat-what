package com.fantuan.eatwhat.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 微信认证客户端 - Mock 实现
 *
 * 由 wechat.mock-enabled=true 控制启用，与 Spring Profile 解耦。
 * 适用于开发、测试及生产环境的联调/演练场景。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wechat.mock-enabled", havingValue = "true")
public class WeChatAuthClientMock implements WeChatAuthClient {

    @Override
    public String code2Session(String code) throws Exception {
        log.info("使用 Mock 微信认证客户端");

        // 使用 code 生成稳定的 mock openid
        String openid = "mock_" + Math.abs(code.hashCode() % 1000000000);

        return openid;
    }
}
