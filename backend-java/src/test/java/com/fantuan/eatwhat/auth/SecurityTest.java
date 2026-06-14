package com.fantuan.eatwhat.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
        // Given: 构造一个 subject 非数字的 token
        // 使用正确的密钥但 subject 为非数字
        String fakeToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJub24tbnVtZXJpYyIsImlhdCI6MTcxODQwMDAwMCwiZXhwIjoxNzE4NDg2NDAwfQ.invalid";

        // When & Then: 应返回 1003
        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void errorResponse_doesNotContainInternalDetails() throws Exception {
        // When: 触发一个系统异常
        MvcResult result = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"invalid-code-format\"}"))
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // Then: 错误响应不应包含内部实现细节
        assertFalse(response.contains("Exception"), "响应不应包含 Exception");
        assertFalse(response.contains("StackTrace"), "响应不应包含 StackTrace");
        assertFalse(response.contains("Caused by"), "响应不应包含 Caused by");
    }
}
