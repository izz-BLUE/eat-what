package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.FeedbackRequest;
import com.fantuan.eatwhat.dto.request.LoginRequest;
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
class FeedbackControllerTest {

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
        FeedbackRequest request = new FeedbackRequest();
        request.setType("FEATURE");
        request.setContent("匿名反馈测试内容123");

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.type").value("FEATURE"))
                .andExpect(jsonPath("$.data.content").value("匿名反馈测试内容123"))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    void submitFeedback_validToken_success() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("BUG");
        request.setContent("登录用户反馈的问题内容");
        request.setRating(3);

        mockMvc.perform(post("/api/v1/feedback")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.type").value("BUG"))
                .andExpect(jsonPath("$.data.content").value("登录用户反馈的问题内容"))
                .andExpect(jsonPath("$.data.rating").value(3));
    }

    // ============ 认证边界：无效 token ============

    @Test
    void submitFeedback_invalidToken_returns1003() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("UI");
        request.setContent("带了无效token的反馈");

        mockMvc.perform(post("/api/v1/feedback")
                        .header("Authorization", "Bearer this_is_an_invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    // ============ type 校验 ============

    @Test
    void submitFeedback_emptyType_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("");
        request.setContent("反馈内容至少五个字");

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_invalidType_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("INVALID");
        request.setContent("反馈内容至少五个字");

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ============ content 校验 ============

    @Test
    void submitFeedback_contentTooShort_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("OTHER");
        request.setContent("ab");  // 只有2个字，不足5

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_contentTooLong_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("OTHER");
        request.setContent("x".repeat(501));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ============ rating 校验 ============

    @Test
    void submitFeedback_ratingTooLow_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("FEATURE");
        request.setContent("评分低于1的反馈内容");
        request.setRating(0);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_ratingTooHigh_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("FEATURE");
        request.setContent("评分高于5的反馈内容");
        request.setRating(6);

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ============ contact / systemInfo 长度校验 ============

    @Test
    void submitFeedback_contactTooLong_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("FEATURE");
        request.setContent("联系方式超长的反馈内容");
        request.setContact("x".repeat(101));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void submitFeedback_systemInfoTooLong_returns1001() throws Exception {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("FEATURE");
        request.setContent("环境信息超长的反馈内容");
        request.setSystemInfo("x".repeat(1001));

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }
}
