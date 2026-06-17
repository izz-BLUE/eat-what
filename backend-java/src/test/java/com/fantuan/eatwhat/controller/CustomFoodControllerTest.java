package com.fantuan.eatwhat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fantuan.eatwhat.dto.request.CustomFoodCreateRequest;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomFoodControllerTest {

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

    // ==================== POST ====================

    @Test
    void create_success() throws Exception {
        CustomFoodCreateRequest request = buildRequest("测试自定义菜", List.of("快餐"),
                List.of("家常菜"), List.of("午餐"), List.of("咸"), 2);

        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("测试自定义菜"))
                .andExpect(jsonPath("$.data.category").value("家常菜"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void create_duplicateName_returns2014() throws Exception {
        CustomFoodCreateRequest request = buildRequest("重复菜", List.of("快餐"),
                List.of("家常菜"), List.of("午餐"), List.of("咸"), null);

        // 第一次成功
        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 第二次重名
        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2014));
    }

    @Test
    void create_blankTypeAndCuisine_returns1001() throws Exception {
        CustomFoodCreateRequest request = buildRequest("空白标签", List.of(" "),
                List.of(""), List.of("午餐"), List.of("咸"), null);

        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void create_emptyName_returns1001() throws Exception {
        CustomFoodCreateRequest request = buildRequest("", List.of("快餐"),
                List.of("家常菜"), List.of("午餐"), List.of("咸"), null);

        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void create_noAuth_returns1003() throws Exception {
        CustomFoodCreateRequest request = buildRequest("未登录菜", List.of("快餐"),
                List.of("家常菜"), List.of("午餐"), List.of("咸"), null);

        mockMvc.perform(post("/api/v1/custom-foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void create_onlyTypeTags_success() throws Exception {
        CustomFoodCreateRequest request = buildRequest("仅类型", List.of("快餐", "面食"),
                null, List.of("午餐"), List.of("咸"), null);

        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.category").value("快餐"));
    }

    // ==================== GET ====================

    @Test
    void list_emptyInitially() throws Exception {
        mockMvc.perform(get("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void list_afterCreate_returnsItem() throws Exception {
        CustomFoodCreateRequest request = buildRequest("列表测试菜", List.of("快餐"),
                List.of("家常菜"), List.of("午餐"), List.of("咸"), 1);

        mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("列表测试菜"));
    }

    // ==================== DELETE ====================

    @Test
    void delete_success() throws Exception {
        CustomFoodCreateRequest request = buildRequest("待删除菜", List.of("快餐"),
                List.of("家常菜"), List.of("午餐"), List.of("咸"), null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        String resp = createResult.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(resp);
        long id = node.get("data").get("id").asLong();

        // 删除
        mockMvc.perform(delete("/api/v1/custom-foods/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 删除后列表不再包含
        mockMvc.perform(get("/api/v1/custom-foods")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void delete_notFound_returns2013() throws Exception {
        mockMvc.perform(delete("/api/v1/custom-foods/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2013));
    }

    @Test
    void delete_noAuth_returns1003() throws Exception {
        mockMvc.perform(delete("/api/v1/custom-foods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    @Test
    void list_noAuth_returns1003() throws Exception {
        mockMvc.perform(get("/api/v1/custom-foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    // ==================== helpers ====================

    private CustomFoodCreateRequest buildRequest(String name, List<String> typeTags,
                                                  List<String> cuisineTags,
                                                  List<String> mealTypes,
                                                  List<String> tasteTags,
                                                  Integer priceLevel) {
        CustomFoodCreateRequest request = new CustomFoodCreateRequest();
        request.setName(name);
        request.setTypeTags(typeTags);
        request.setCuisineTags(cuisineTags);
        request.setMealTypes(mealTypes);
        request.setTasteTags(tasteTags);
        request.setPriceLevel(priceLevel);
        return request;
    }
}
