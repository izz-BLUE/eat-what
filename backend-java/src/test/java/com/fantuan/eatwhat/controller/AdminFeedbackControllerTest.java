package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.AdminFeedbackStatusRequest;
import com.fantuan.eatwhat.dto.request.FeedbackRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "admin.token=test-admin-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_TOKEN = "test-admin-token";
    private static final String BASE_URL = "/api/v1/admin/feedback";

    /**
     * 初始化测试数据：提交几条反馈，供列表查询测试使用
     */
    @BeforeEach
    void setUp() throws Exception {
        // 提交反馈 1：FEATURE 类型
        FeedbackRequest req1 = new FeedbackRequest();
        req1.setType("FEATURE");
        req1.setContent("建议增加更多菜系分类");
        req1.setContact("wxid_001");
        mockMvc.perform(post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)));

        // 提交反馈 2：BUG 类型
        FeedbackRequest req2 = new FeedbackRequest();
        req2.setType("BUG");
        req2.setContent("首页推荐按钮偶发无响应，需要排查");
        req2.setContact("13800001111");
        mockMvc.perform(post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)));

        // 提交反馈 3：UI 类型
        FeedbackRequest req3 = new FeedbackRequest();
        req3.setType("UI");
        req3.setContent("颜色对比度偏低看不清文字");
        mockMvc.perform(post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req3)));

        // 提交反馈 4：RECOMMENDATION 类型（带评分）
        FeedbackRequest req4 = new FeedbackRequest();
        req4.setType("RECOMMENDATION");
        req4.setRating(2);
        req4.setContent("推荐结果一直偏向火锅，需要调优");
        req4.setContact("user@example.com");
        mockMvc.perform(post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req4)));
    }

    // ============ 认证测试 ============

    @Test
    void listFeedbacks_noAdminToken_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001));
    }

    @Test
    void listFeedbacks_wrongAdminToken_returnsForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", "wrong-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001));
    }

    @Test
    void updateStatus_noAdminToken_returnsForbidden() throws Exception {
        AdminFeedbackStatusRequest req = new AdminFeedbackStatusRequest();
        req.setStatus("REVIEWED");

        mockMvc.perform(put(BASE_URL + "/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(3001));
    }

    // ============ 正确 token 可查询 ============

    @Test
    void listFeedbacks_validToken_success() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    // ============ 状态过滤 ============

    @Test
    void listFeedbacks_filterByStatus() throws Exception {
        // 所有新提交的反馈状态都是 NEW
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].status").value("NEW"));
    }

    @Test
    void listFeedbacks_filterByStatus_emptyResult() throws Exception {
        // 没有 RESOLVED 状态的反馈（其他测试可能改出 REVIEWED，这里用 RESOLVED 确保空结果）
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // ============ 关键词搜索 ============

    @Test
    void listFeedbacks_keywordMatchesContent() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("keyword", "火锅"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[0].content").value(containsString("火锅")));
    }

    @Test
    void listFeedbacks_keywordMatchesContact() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("keyword", "13800001111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[0].contact").value("13800001111"));
    }

    @Test
    void listFeedbacks_keywordNoMatch() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("keyword", "zzz_no_such_keyword_zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void listFeedbacks_keywordWhitespaceOnly_treatedAsNull() throws Exception {
        // 纯空格 keyword 应视为无 keyword，不会过滤，total >= 4
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(4)));
    }

    @Test
    void listFeedbacks_keywordWithSurroundingSpaces_trimmed() throws Exception {
        // "  火锅  " trim 后 = "火锅"，应能匹配
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("keyword", "  火锅  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[0].content").value(containsString("火锅")));
    }

    // ============ 分页 ============

    @Test
    void listFeedbacks_pagination() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(lessThanOrEqualTo(2)));
    }

    @Test
    void listFeedbacks_sizeExceedsMax_clampedTo100() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.size").value(100));   // controller clamps to 100
    }

    // ============ 更新状态 ============

    @Test
    void updateStatus_success() throws Exception {
        // 先查一条反馈的 id
        String listJson = mockMvc.perform(get(BASE_URL)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(listJson).get("data").get("items").get(0).get("id").asInt();

        // 更新状态
        AdminFeedbackStatusRequest req = new AdminFeedbackStatusRequest();
        req.setStatus("REVIEWED");

        mockMvc.perform(put(BASE_URL + "/" + id + "/status")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.status").value("REVIEWED"));
    }

    // ============ 非法状态 ============

    @Test
    void updateStatus_invalidStatus_returns1001() throws Exception {
        AdminFeedbackStatusRequest req = new AdminFeedbackStatusRequest();
        req.setStatus("INVALID_STATUS");

        mockMvc.perform(put(BASE_URL + "/1/status")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ============ 不存在的反馈 ============

    @Test
    void updateStatus_notFound_returnsBusinessError() throws Exception {
        AdminFeedbackStatusRequest req = new AdminFeedbackStatusRequest();
        req.setStatus("RESOLVED");

        mockMvc.perform(put(BASE_URL + "/99999/status")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2012));
    }
}
