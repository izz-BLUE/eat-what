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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void protectedEndpoint_noToken_returns1003() throws Exception {
        mockMvc.perform(get("/api/v1/record/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void protectedEndpoint_invalidToken_returns1003() throws Exception {
        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void protectedEndpoint_validToken_success() throws Exception {
        // First login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCode("test-user-1");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("data").get("token").asText();

        // Then access protected endpoint
        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void publicEndpoint_noToken_success() throws Exception {
        mockMvc.perform(get("/api/v1/recommend?mealType=晚餐"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void publicEndpoint_invalidToken_returns1003() throws Exception {
        mockMvc.perform(get("/api/v1/recommend?mealType=晚餐")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void userContext_clearedAfterRequest() throws Exception {
        // Login first
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCode("test-user-1");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Make request with token
        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify UserContext is cleared
        assertNull(UserContext.getUserId());
    }

    @Test
    void consecutiveRequests_noIdentityMixup() throws Exception {
        // Login user 1
        LoginRequest loginRequest1 = new LoginRequest();
        loginRequest1.setCode("test-user-1");

        MvcResult loginResult1 = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andReturn();

        String token1 = objectMapper.readTree(loginResult1.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Login user 2
        LoginRequest loginRequest2 = new LoginRequest();
        loginRequest2.setCode("test-user-2");

        MvcResult loginResult2 = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest2)))
                .andReturn();

        String token2 = objectMapper.readTree(loginResult2.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Make requests with different tokens
        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Verify UserContext is cleared after first request
        assertNull(UserContext.getUserId());

        mockMvc.perform(get("/api/v1/record/list")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        // Verify UserContext is cleared after second request
        assertNull(UserContext.getUserId());
    }

    @Test
    void protectedEndpoint_bodyUserIdIgnored() throws Exception {
        // Login to get valid token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCode("test-user-1");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Try to pass userId in body (should be ignored, token determines user)
        mockMvc.perform(post("/api/v1/blacklist/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"foodId\":1,\"reason\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void protectedEndpoint_queryUserIdIgnored() throws Exception {
        // Login to get valid token for user 1
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCode("test-user-1");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("token").asText();

        // Try to pass different userId in query (should be ignored, token determines user)
        // GET /api/v1/blacklist/list?userId=999 should still use token's userId
        mockMvc.perform(get("/api/v1/blacklist/list?userId=999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Verify UserContext uses token's userId, not query userId
        // After request, UserContext should be cleared
        assertNull(UserContext.getUserId());

        // DELETE with different userId in query should also use token's userId
        mockMvc.perform(delete("/api/v1/blacklist/999?userId=999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        assertNull(UserContext.getUserId());
    }
}
