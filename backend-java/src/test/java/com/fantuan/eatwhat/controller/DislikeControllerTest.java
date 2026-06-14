package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.DislikeAddRequest;
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
class DislikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void add_userIdZero_returnsError() throws Exception {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(0L);
        request.setCategory("火锅");

        mockMvc.perform(post("/api/v1/dislike/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void add_daysZero_returnsError() throws Exception {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(0);

        mockMvc.perform(post("/api/v1/dislike/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void add_days31_returnsError() throws Exception {
        DislikeAddRequest request = new DislikeAddRequest();
        request.setUserId(1L);
        request.setCategory("火锅");
        request.setDays(31);

        mockMvc.perform(post("/api/v1/dislike/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void list_userIdZero_returnsError() throws Exception {
        mockMvc.perform(get("/api/v1/dislike/list")
                        .param("userId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void remove_dislikeIdZero_returnsError() throws Exception {
        mockMvc.perform(delete("/api/v1/dislike/0")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }
}
