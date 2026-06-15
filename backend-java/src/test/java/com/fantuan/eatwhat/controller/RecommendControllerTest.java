package com.fantuan.eatwhat.controller;

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
 * RecommendController 参数校验与新旧参数合并测试
 * 通过 MockMvc 真实调用，验证 Controller 层的校验逻辑
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== typeTags 合法 ====================

    @Test
    void recommend_typeTagsValid_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("typeTags", "面食,快餐"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== cuisineTags 合法 ====================

    @Test
    void recommend_cuisineTagsValid_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("cuisineTags", "川菜,日料"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 旧 categories 合法并正确分类 ====================

    @Test
    void recommend_oldCategoriesValidType_returnsSuccess() throws Exception {
        // "面食" 是 typeTag
        mockMvc.perform(get("/api/v1/recommend")
                        .param("categories", "面食"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void recommend_oldCategoriesValidCuisine_returnsSuccess() throws Exception {
        // "日料" 是 cuisineTag
        mockMvc.perform(get("/api/v1/recommend")
                        .param("categories", "日料"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void recommend_oldCategoriesMixed_returnsSuccess() throws Exception {
        // "面食"(type) + "日料"(cuisine) 各一个
        mockMvc.perform(get("/api/v1/recommend")
                        .param("categories", "面食,日料"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 新旧参数同时存在时合并去重 ====================

    @Test
    void recommend_newAndOldMergeDeduplicate_returnsSuccess() throws Exception {
        // typeTags=面食 + categories=面食→合并去重后仍只有1个typeTag
        mockMvc.perform(get("/api/v1/recommend")
                        .param("typeTags", "面食")
                        .param("categories", "面食"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void recommend_newAndOldMergeDifferent_returnsSuccess() throws Exception {
        // typeTags=面食 + categories=快餐→合并后2个typeTag
        mockMvc.perform(get("/api/v1/recommend")
                        .param("typeTags", "面食")
                        .param("categories", "快餐"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 新旧分类合计超过 3 个返回 1001 ====================

    @Test
    void recommend_totalExceeds3_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("typeTags", "快餐,小吃,面食")
                        .param("cuisineTags", "川菜"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void recommend_oldCategoriesTotalExceeds3_returns1001() throws Exception {
        // "快餐,小吃,面食,火锅" 都是 typeTag，合计4
        mockMvc.perform(get("/api/v1/recommend")
                        .param("categories", "快餐,小吃,面食,火锅"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ==================== 未知分类返回 1001 ====================

    @Test
    void recommend_unknownTypeTag_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("typeTags", "不存在的分类"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void recommend_unknownCuisineTag_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("cuisineTags", "东北菜"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void recommend_unknownOldCategory_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("categories", "韩餐"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ==================== mealType 非法返回 1001 ====================

    @Test
    void recommend_invalidMealType_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("mealType", "下午茶"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ==================== priceLevel 非法返回 1001 ====================

    @Test
    void recommend_invalidPriceLevel_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("priceLevel", "100以上"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ==================== taste=重口 返回 1001 ====================

    @Test
    void recommend_tasteZhongKou_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("taste", "重口"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ==================== taste=清淡/辣/不辣 通过 ====================

    @Test
    void recommend_tasteQingDan_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("taste", "清淡"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void recommend_tasteLa_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("taste", "辣"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void recommend_tasteBuLa_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("taste", "不辣"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== priceLevel=40以上 通过 ====================

    @Test
    void recommend_priceLevel40Above_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend")
                        .param("priceLevel", "40以上"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== swap 端点同样校验 ====================

    @Test
    void swap_invalidTaste_returns1001() throws Exception {
        mockMvc.perform(get("/api/v1/recommend/swap")
                        .param("taste", "重口"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    @Test
    void swap_validParams_returnsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/recommend/swap")
                        .param("typeTags", "面食")
                        .param("excludeFoodIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
