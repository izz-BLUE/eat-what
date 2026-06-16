package com.fantuan.eatwhat.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 WeChatAuthClientMock 的条件配置：
 * - 不再依赖 @Profile，完全由 @ConditionalOnProperty 控制
 */
class WeChatMockProfileTest {

    @Test
    void mockClient_shouldNotHaveProfileAnnotation() {
        // WeChatAuthClientMock 应该不再有 @Profile 注解
        // （修复前：@Profile({"dev","test"}) 导致 prod+mock 无法启动）
        Profile profileAnnotation = WeChatAuthClientMock.class.getAnnotation(Profile.class);
        assertNull(profileAnnotation,
            "WeChatAuthClientMock 不应再有 @Profile 注解，应由 @ConditionalOnProperty 全权控制");
    }

    @Test
    void mockClient_shouldHaveConditionalOnProperty() {
        ConditionalOnProperty conditional =
            WeChatAuthClientMock.class.getAnnotation(ConditionalOnProperty.class);
        assertNotNull(conditional,
            "WeChatAuthClientMock 应有 @ConditionalOnProperty 注解");

        assertEquals("wechat.mock-enabled", conditional.name()[0]);
        assertEquals("true", conditional.havingValue());
    }

    @Test
    void prodClient_shouldHaveConditionalOnProperty() {
        ConditionalOnProperty conditional =
            WeChatAuthClientProd.class.getAnnotation(ConditionalOnProperty.class);
        assertNotNull(conditional,
            "WeChatAuthClientProd 应有 @ConditionalOnProperty 注解");

        assertEquals("wechat.mock-enabled", conditional.name()[0]);
        assertEquals("false", conditional.havingValue());
    }
}
