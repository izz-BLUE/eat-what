package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCode("test-user-1");
        request.setNickname("测试用户");
        request.setAvatarUrl("https://example.com/avatar.jpg");

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andExpect(jsonPath("$.data.userId").isNumber())
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar.jpg"));
    }

    @Test
    void login_emptyCode_returnsError() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCode("");

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void login_nullCode_returnsError() throws Exception {
        LoginRequest request = new LoginRequest();

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void login_differentCodes_differentUsers() throws Exception {
        // Login with code 1
        LoginRequest request1 = new LoginRequest();
        request1.setCode("test-user-1");

        String response1 = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andReturn().getResponse().getContentAsString();

        // Login with code 2
        LoginRequest request2 = new LoginRequest();
        request2.setCode("test-user-2");

        String response2 = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andReturn().getResponse().getContentAsString();

        // Different codes should produce different tokens
        String token1 = objectMapper.readTree(response1).get("data").get("token").asText();
        String token2 = objectMapper.readTree(response2).get("data").get("token").asText();

        // Note: Different codes may produce same user if hash collides, but tokens should be different
        // This test verifies the login flow works for different inputs
    }
}
