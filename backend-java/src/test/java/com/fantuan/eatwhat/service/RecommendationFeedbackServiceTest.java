package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.RecommendationFeedback;
import com.fantuan.eatwhat.dto.request.RecommendationFeedbackRequest;
import com.fantuan.eatwhat.dto.response.RecommendationFeedbackResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.RecommendationFeedbackMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecommendationFeedbackServiceTest {

    @Mock
    private RecommendationFeedbackMapper recommendationFeedbackMapper;

    @InjectMocks
    private RecommendationFeedbackService recommendationFeedbackService;

    @Captor
    private ArgumentCaptor<RecommendationFeedback> feedbackCaptor;

    // ==================== 成功场景 ====================

    @Test
    void createFeedback_anonymous_success() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "宫保鸡丁", "RECENTLY_EATEN");

        RecommendationFeedbackResponse response =
                recommendationFeedbackService.createFeedback(null, request);

        assertNotNull(response);
        assertEquals("RECENTLY_EATEN", response.getReason());
        assertNotNull(response.getCreatedAt());

        verify(recommendationFeedbackMapper).insert(feedbackCaptor.capture());
        RecommendationFeedback entity = feedbackCaptor.getValue();
        assertNull(entity.getUserId());
        assertEquals("DEFAULT", entity.getFoodSource());
        assertEquals(1L, entity.getFoodId());
        assertNull(entity.getCustomFoodId());
        assertEquals("宫保鸡丁", entity.getFoodName());
        assertEquals("RECENTLY_EATEN", entity.getReason());
    }

    @Test
    void createFeedback_loggedIn_success() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 2L, null, "红烧肉", "TOO_EXPENSIVE");

        RecommendationFeedbackResponse response =
                recommendationFeedbackService.createFeedback(1L, request);

        assertNotNull(response);
        assertEquals("TOO_EXPENSIVE", response.getReason());

        verify(recommendationFeedbackMapper).insert(feedbackCaptor.capture());
        assertEquals(1L, feedbackCaptor.getValue().getUserId());
    }

    @Test
    void createFeedback_custom_success() {
        RecommendationFeedbackRequest request = buildRequest(
                "CUSTOM", null, 10L, "妈妈做的炒饭", "OTHER");

        RecommendationFeedbackResponse response =
                recommendationFeedbackService.createFeedback(2L, request);

        assertNotNull(response);
        assertEquals("OTHER", response.getReason());

        verify(recommendationFeedbackMapper).insert(feedbackCaptor.capture());
        RecommendationFeedback entity = feedbackCaptor.getValue();
        assertEquals("CUSTOM", entity.getFoodSource());
        assertNull(entity.getFoodId());
        assertEquals(10L, entity.getCustomFoodId());
        assertEquals("妈妈做的炒饭", entity.getFoodName());
    }

    @Test
    void createFeedback_allReasons_valid() {
        String[] allReasons = {"RECENTLY_EATEN", "NOT_IN_MOOD", "TOO_EXPENSIVE",
                "TOO_HEAVY", "WRONG_TASTE", "WRONG_CATEGORY", "OTHER"};
        for (String reason : allReasons) {
            RecommendationFeedbackRequest request = buildRequest(
                    "DEFAULT", 1L, null, "测试菜", reason);
            assertNotNull(recommendationFeedbackService.createFeedback(null, request),
                    "reason " + reason + " should be valid");
        }
    }

    @Test
    void createFeedback_withOptionalFields_success() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 3L, null, "火锅", "TOO_HEAVY");
        request.setMealType("晚餐");
        request.setPriceLevel("25-40");
        request.setTaste("辣");
        request.setTypeTags("火锅,川菜");
        request.setCuisineTags("家常菜");

        RecommendationFeedbackResponse response =
                recommendationFeedbackService.createFeedback(null, request);

        assertNotNull(response);

        verify(recommendationFeedbackMapper).insert(feedbackCaptor.capture());
        RecommendationFeedback entity = feedbackCaptor.getValue();
        assertEquals("晚餐", entity.getMealType());
        assertEquals("25-40", entity.getPriceLevel());
        assertEquals("辣", entity.getTaste());
        assertEquals("火锅,川菜", entity.getTypeTags());
        assertEquals("家常菜", entity.getCuisineTags());
    }

    // ==================== 校验失败场景 ====================

    @Test
    void createFeedback_defaultMissingFoodId_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", null, null, "测试菜", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_customMissingCustomFoodId_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "CUSTOM", null, null, "测试菜", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_defaultWithExtraCustomFoodId_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, 5L, "测试菜", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_customWithExtraFoodId_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "CUSTOM", 1L, 5L, "测试菜", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_invalidSource_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "INVALID", 1L, null, "测试菜", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_nullSource_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                null, 1L, null, "测试菜", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_invalidReason_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "测试菜", "INVALID_REASON");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_nullReason_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "测试菜", null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_emptyFoodName_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "", "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_nullFoodName_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, null, "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void createFeedback_tooLongFoodName_throwsError() {
        RecommendationFeedbackRequest request = buildRequest(
                "DEFAULT", 1L, null, "x".repeat(101), "RECENTLY_EATEN");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> recommendationFeedbackService.createFeedback(null, request));
        assertEquals(ResultCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    // ==================== helper ====================

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
