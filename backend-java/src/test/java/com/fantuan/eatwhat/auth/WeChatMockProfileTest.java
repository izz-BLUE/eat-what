package com.fantuan.eatwhat.auth;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 Mock Profile 隔离
 */
class WeChatMockProfileTest {

    @Test
    void prodProfile_mockEnabled_doesNotLoadMock() {
        // Given: 使用 prod Profile 且 mock-enabled=true
        // 预期: Mock 不应该被加载，因为 @Profile({"dev", "test"}) 限制

        // 验证 WeChatAuthClientMock 类有 @Profile 注解
        Profile profileAnnotation = WeChatAuthClientMock.class.getAnnotation(Profile.class);
        assertNotNull(profileAnnotation, "WeChatAuthClientMock 应该有 @Profile 注解");

        // 验证 @Profile 只包含 dev 和 test
        String[] profiles = profileAnnotation.value();
        assertEquals(2, profiles.length);
        assertEquals("dev", profiles[0]);
        assertEquals("test", profiles[1]);

        // 验证 prod 不在允许的 Profile 中
        boolean prodAllowed = false;
        for (String profile : profiles) {
            if ("prod".equals(profile)) {
                prodAllowed = true;
                break;
            }
        }
        assertFalse(prodAllowed, "prod Profile 不应该允许加载 Mock");
    }

    @Test
    void devProfile_mockEnabled_loadsMock() {
        // 验证 dev Profile 在允许列表中
        Profile profileAnnotation = WeChatAuthClientMock.class.getAnnotation(Profile.class);
        assertNotNull(profileAnnotation);

        boolean devAllowed = false;
        for (String profile : profileAnnotation.value()) {
            if ("dev".equals(profile)) {
                devAllowed = true;
                break;
            }
        }
        assertTrue(devAllowed, "dev Profile 应该允许加载 Mock");
    }

    @Test
    void testProfile_mockEnabled_loadsMock() {
        // 验证 test Profile 在允许列表中
        Profile profileAnnotation = WeChatAuthClientMock.class.getAnnotation(Profile.class);
        assertNotNull(profileAnnotation);

        boolean testAllowed = false;
        for (String profile : profileAnnotation.value()) {
            if ("test".equals(profile)) {
                testAllowed = true;
                break;
            }
        }
        assertTrue(testAllowed, "test Profile 应该允许加载 Mock");
    }
}
