package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.common.RecommendDict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MetaController 元数据接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== 无 token 可访问 ====================

    @Test
    void getRecommendOptions_noToken_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 各维度全部存在 ====================

    @Test
    void getRecommendOptions_mealTypesPresent() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mealTypes").isArray())
                .andExpect(jsonPath("$.data.mealTypes.length()").value(RecommendDict.MEAL_TYPES.size()));
    }

    @Test
    void getRecommendOptions_priceLevelsPresent() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priceLevels").isArray())
                .andExpect(jsonPath("$.data.priceLevels.length()").value(RecommendDict.PRICE_LEVELS.size()));
    }

    @Test
    void getRecommendOptions_tastesPresent() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tastes").isArray())
                .andExpect(jsonPath("$.data.tastes.length()").value(RecommendDict.TASTES.size()));
    }

    @Test
    void getRecommendOptions_typeTagsPresent() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.typeTags").isArray())
                .andExpect(jsonPath("$.data.typeTags.length()").value(RecommendDict.TYPE_TAGS.size()));
    }

    @Test
    void getRecommendOptions_cuisineTagsPresent() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cuisineTags").isArray())
                .andExpect(jsonPath("$.data.cuisineTags.length()").value(RecommendDict.CUISINE_TAGS.size()));
    }

    // ==================== value 与统一词典一致 ====================

    @Test
    void getRecommendOptions_mealTypesMatchDict() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(jsonPath("$.data.mealTypes[0].value").value("早餐"))
                .andExpect(jsonPath("$.data.mealTypes[1].value").value("午餐"))
                .andExpect(jsonPath("$.data.mealTypes[2].value").value("晚餐"))
                .andExpect(jsonPath("$.data.mealTypes[3].value").value("夜宵"));
    }

    @Test
    void getRecommendOptions_typeTagsMatchDict() throws Exception {
        String[] expected = RecommendDict.TYPE_TAGS.toArray(new String[0]);
        for (int i = 0; i < expected.length; i++) {
            int idx = i;
            mockMvc.perform(get("/api/v1/meta/recommend-options"))
                    .andExpect(jsonPath("$.data.typeTags[" + idx + "].value").value(expected[idx]));
        }
    }

    @Test
    void getRecommendOptions_cuisineTagsMatchDict() throws Exception {
        String[] expected = RecommendDict.CUISINE_TAGS.toArray(new String[0]);
        for (int i = 0; i < expected.length; i++) {
            int idx = i;
            mockMvc.perform(get("/api/v1/meta/recommend-options"))
                    .andExpect(jsonPath("$.data.cuisineTags[" + idx + "].value").value(expected[idx]));
        }
    }

    // ==================== sortOrder 唯一且递增 ====================

    @Test
    void getRecommendOptions_mealTypesSortOrderUniqueAndAscending() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(jsonPath("$.data.mealTypes[0].sortOrder").value(1))
                .andExpect(jsonPath("$.data.mealTypes[1].sortOrder").value(2))
                .andExpect(jsonPath("$.data.mealTypes[2].sortOrder").value(3))
                .andExpect(jsonPath("$.data.mealTypes[3].sortOrder").value(4));
    }

    // ==================== price API value 为 15以内/15-25/25-40/40以上 ====================

    @Test
    void getRecommendOptions_priceLevelsApiValues() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(jsonPath("$.data.priceLevels[0].value").value("15以内"))
                .andExpect(jsonPath("$.data.priceLevels[1].value").value("15-25"))
                .andExpect(jsonPath("$.data.priceLevels[2].value").value("25-40"))
                .andExpect(jsonPath("$.data.priceLevels[3].value").value("40以上"));
    }

    // ==================== 不返回"不限" ====================

    @Test
    void getRecommendOptions_noUnlimitedInTastes() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(jsonPath("$.data.tastes[*].value").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("不限"))));
    }

    // ==================== 不返回"重口" ====================

    @Test
    void getRecommendOptions_noZhongKouInTastes() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(jsonPath("$.data.tastes[*].value").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("重口"))));
    }

    // ==================== 不返回 price value 1/2/3/4 ====================

    @Test
    void getRecommendOptions_noNumericPriceValues() throws Exception {
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(jsonPath("$.data.priceLevels[*].value").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("1"))))
                .andExpect(jsonPath("$.data.priceLevels[*].value").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("2"))))
                .andExpect(jsonPath("$.data.priceLevels[*].value").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("3"))))
                .andExpect(jsonPath("$.data.priceLevels[*].value").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("4"))));
    }

    // ==================== 辣口味有 hint ====================

    @Test
    void getRecommendOptions_tasteLaHasHint() throws Exception {
        // "辣" 的 hint 应为 "含麻辣"
        mockMvc.perform(get("/api/v1/meta/recommend-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tastes[?(@.value=='辣')].hint").value("含麻辣"));
    }
}
