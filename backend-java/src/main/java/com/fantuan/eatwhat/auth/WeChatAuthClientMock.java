package com.fantuan.eatwhat.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 微信认证客户端 - Mock 实现（仅用于开发和测试）
 *
 * 注意：Mock 只在 dev/test Profile 下启用，生产环境即使配置 mock-enabled=true 也不会加载
 */
@Slf4j
@Component
@Profile({"dev", "test"})
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
