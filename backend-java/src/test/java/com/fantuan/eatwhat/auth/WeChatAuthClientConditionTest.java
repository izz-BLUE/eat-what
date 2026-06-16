package com.fantuan.eatwhat.auth;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 WeChatAuthClient 条件配置：
 * - mock-enabled=true → WeChatAuthClientMock
 * - mock-enabled=false → WeChatAuthClientProd
 * - prod profile + mock-enabled=true → ApplicationContext 可启动且加载 Mock
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class WeChatAuthClientConditionTest {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private WeChatAuthClientMock mockClient;

    @Autowired(required = false)
    private WeChatAuthClientProd prodClient;

    @Test
    void mockEnabledTrue_shouldLoadMockClient() {
        // 默认 test profile 配置 wechat.mock-enabled=true
        assertNotNull(mockClient, "mock-enabled=true 时应存在 WeChatAuthClientMock bean");
        assertNull(prodClient, "mock-enabled=true 时不应存在 WeChatAuthClientProd bean");
    }

    @Test
    void shouldHaveExactlyOneClientBean() {
        int count = context.getBeanNamesForType(WeChatAuthClient.class).length;
        assertEquals(1, count, "应有且仅有一个 WeChatAuthClient 实现");
    }

    @Nested
    @SpringBootTest(
        properties = {"wechat.mock-enabled=false"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
    )
    class MockDisabled {

        @Autowired(required = false)
        private WeChatAuthClientMock mockClient;

        @Autowired(required = false)
        private WeChatAuthClientProd prodClient;

        @Test
        void mockEnabledFalse_shouldLoadProdClient() {
            assertNull(mockClient, "mock-enabled=false 时不应存在 WeChatAuthClientMock bean");
            assertNotNull(prodClient, "mock-enabled=false 时应存在 WeChatAuthClientProd bean");
        }
    }

    @Nested
    @SpringBootTest(
        properties = {
            "spring.profiles.active=prod",
            "spring.flyway.enabled=false",
            "spring.datasource.url=jdbc:h2:mem:testdb-prod;DB_CLOSE_DELAY=-1;MODE=MySQL;INIT=RUNSCRIPT FROM 'classpath:db/h2-init.sql'",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "jwt.secret=test-jwt-secret-key-at-least-32-bytes",
            "jwt.expiration-seconds=3600",
            "wechat.app-id=wx_test",
            "wechat.app-secret=wx_test",
            "wechat.mock-enabled=true",
            "admin.token=test-admin-token"
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
    )
    class ProdProfileWithMockEnabled {

        @Autowired
        private WeChatAuthClient client;

        @Test
        void prodProfile_mockEnabledTrue_shouldLoadMockClient() {
            assertNotNull(client, "prod profile + mock-enabled=true 时应能加载 WeChatAuthClient bean");
            assertTrue(
                client instanceof WeChatAuthClientMock,
                "prod profile + mock-enabled=true 时应加载 Mock 实现，实际: " + client.getClass().getSimpleName()
            );
        }

        @Test
        void contextLoadsSuccessfully() {
            // 到达此处即证明 ApplicationContext 启动成功
            assertTrue(true);
        }
    }
}
