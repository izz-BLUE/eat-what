package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.BlacklistAddRequest;
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
class BlacklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void add_userIdZero_returnsError() throws Exception {
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setUserId(0L);
        request.setFoodId(1L);

        mockMvc.perform(post("/api/v1/blacklist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void add_foodIdZero_returnsError() throws Exception {
        BlacklistAddRequest request = new BlacklistAddRequest();
        request.setUserId(1L);
        request.setFoodId(0L);

        mockMvc.perform(post("/api/v1/blacklist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void list_userIdZero_returnsError() throws Exception {
        mockMvc.perform(get("/api/v1/blacklist/list")
                        .param("userId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void remove_blacklistIdZero_returnsError() throws Exception {
        mockMvc.perform(delete("/api/v1/blacklist/0")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }
}
