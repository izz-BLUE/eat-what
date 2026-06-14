package com.fantuan.eatwhat.auth;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.dto.request.LoginRequest;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.UserMapper;
import com.fantuan.eatwhat.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 安全测试：验证敏感信息不泄露
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginResponse_doesNotContainSensitiveInfo() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCode("dev-user-1");

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // Then: 响应中不应包含敏感信息
        assertFalse(response.contains("appsecret"), "响应不应包含 appsecret");
        assertFalse(response.contains("app_secret"), "响应不应包含 app_secret");
        assertFalse(response.contains("session_key"), "响应不应包含 session_key");
        assertFalse(response.contains("js_code"), "响应不应包含 js_code");
        assertFalse(response.contains("openid"), "响应不应包含 openid");
    }

    @Test
    void jwtTokenSubjectNonNumeric_returns1003() throws Exception {
        // Given: 使用测试密钥生成 subject="non-numeric" 的真实 token
        String testSecret = "test-jwt-secret-key-at-least-32-bytes";
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithNonNumericSubject = Jwts.builder()
                .subject("non-numeric")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        // When & Then: 应返回 1003
        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer " + tokenWithNonNumericSubject))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void wechatException_doesNotLeakSensitiveInfo() throws Exception {
        // Given: 使用单元测试验证 AuthService 不泄露敏感信息
        WeChatAuthClient mockClient = code -> {
            throw new RuntimeException("Connection failed: appsecret=wx_secret_value&js_code=sensitive_code");
        };

        // AuthService 需要所有依赖，这里只测试异常处理逻辑
        JwtTokenService jwtTokenService = new JwtTokenService();
        ReflectionTestUtils.setField(jwtTokenService, "secret", "test-jwt-secret-key-at-least-32-bytes");
        ReflectionTestUtils.setField(jwtTokenService, "expirationSeconds", 3600L);
        jwtTokenService.init();

        // 使用 Mockito 创建 mock UserMapper
        UserMapper mockUserMapper = org.mockito.Mockito.mock(UserMapper.class);

        AuthService authService = new AuthService(mockClient, mockUserMapper, jwtTokenService);

        LoginRequest request = new LoginRequest();
        request.setCode("test-code");

        // When & Then: 异常消息不应泄露到客户端
        try {
            authService.login(request);
            fail("应该抛出异常");
        } catch (BusinessException e) {
            assertEquals(ResultCode.WECHAT_LOGIN_FAILED.getCode(), e.getCode());
            assertFalse(e.getMessage().contains("appsecret"), "响应不应包含 appsecret");
            assertFalse(e.getMessage().contains("js_code"), "响应不应包含 js_code");
            assertFalse(e.getMessage().contains("wx_secret_value"), "响应不应包含 wx_secret_value");
            assertFalse(e.getMessage().contains("sensitive_code"), "响应不应包含 sensitive_code");
        }
    }

    @Test
    void wechatException_logDoesNotContainSensitiveInfo() throws Exception {
        // Given: 使用 Logback ListAppender 捕获日志
        Logger logger = (Logger) LoggerFactory.getLogger(AuthService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            WeChatAuthClient mockClient = code -> {
                throw new RuntimeException("Connection failed: appsecret=wx_secret_value&js_code=sensitive_code");
            };

            JwtTokenService jwtTokenService = new JwtTokenService();
            ReflectionTestUtils.setField(jwtTokenService, "secret", "test-jwt-secret-key-at-least-32-bytes");
            ReflectionTestUtils.setField(jwtTokenService, "expirationSeconds", 3600L);
            jwtTokenService.init();

            UserMapper mockUserMapper = org.mockito.Mockito.mock(UserMapper.class);
            AuthService authService = new AuthService(mockClient, mockUserMapper, jwtTokenService);

            LoginRequest request = new LoginRequest();
            request.setCode("test-code");

            // When
            try {
                authService.login(request);
            } catch (Exception e) {
                // 预期异常
            }

            // Then: 日志不应包含敏感信息
            List<ILoggingEvent> logsList = listAppender.list;
            assertFalse(logsList.isEmpty(), "应该有日志输出");

            String logMessages = logsList.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .reduce("", (a, b) -> a + " " + b);

            assertFalse(logMessages.contains("appsecret"), "日志不应包含 appsecret");
            assertFalse(logMessages.contains("js_code"), "日志不应包含 js_code");
            assertFalse(logMessages.contains("wx_secret_value"), "日志不应包含 wx_secret_value");
            assertFalse(logMessages.contains("sensitive_code"), "日志不应包含 sensitive_code");
        } finally {
            logger.detachAppender(listAppender);
        }
    }
}
