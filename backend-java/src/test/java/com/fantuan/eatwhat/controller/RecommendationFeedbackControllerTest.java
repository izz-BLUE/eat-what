package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.LoginRequest;
import com.fantuan.eatwhat.dto.request.RecommendationFeedbackRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCode("test-user-1");

        MvcResult result = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);
        token = jsonNode.get("data").get("token").asText();
    }

    // ============ 匿名 / 登录成功 ============

    @Test
    void submitFeedback_noToken_success() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "匿名反馈测试菜", "RECENTLY_EATEN");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reason").value("RECENTLY_EATEN"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void submitFeedback_validToken_success() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "CUSTOM", null, 5L, "登录用户反馈的自定义菜", "TOO_HEAVY");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reason").value("TOO_HEAVY"));
    }

    // ============ 认证边界：无效 token ============

    @Test
    void submitFeedback_invalidToken_returns1003() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "带了无效token", "OTHER");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .header("Authorization", "Bearer this_is_an_invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    // ============ 参数校验 ============

    @Test
    void submitFeedback_defaultMissingFoodId_returns1001() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", null, null, "缺少foodId", "RECENTLY_EATEN");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_customMissingCustomFoodId_returns1001() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "CUSTOM", null, null, "缺少customFoodId", "RECENTLY_EATEN");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_invalidSource_returns1001() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "INVALID", 1L, null, "无效来源", "RECENTLY_EATEN");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_invalidReason_returns1001() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "无效原因", "BAD_REASON");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_emptyFoodName_returns1001() throws Exception {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "", "RECENTLY_EATEN");

        mockMvc.perform(post("/api/v1/recommendation-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ============ helper ============

    private RecommendationFeedbackRequest buildRequest(
            String source, Long foodId, Long customFoodId, String foodName, String reason) {
        RecommendationFeedbackRequest request = new RecommendationFeedbackRequest();
        request.setSource(source);
        request.setFoodId(foodId);
        request.setCustomFoodId(customFoodId);
        request.setFoodName(foodName);
        request.setReason(reason);
        return request;
    }
}
