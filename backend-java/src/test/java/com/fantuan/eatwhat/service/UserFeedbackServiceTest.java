package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.domain.entity.UserFeedback;
import com.fantuan.eatwhat.dto.request.FeedbackRequest;
import com.fantuan.eatwhat.dto.response.FeedbackResponse;
import com.fantuan.eatwhat.mapper.UserFeedbackMapper;
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
class UserFeedbackServiceTest {

    @Mock
    private UserFeedbackMapper userFeedbackMapper;

    @InjectMocks
    private UserFeedbackService userFeedbackService;

    @Captor
    private ArgumentCaptor<UserFeedback> feedbackCaptor;

    @Test
    void createFeedback_withUserId_success() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("FEATURE");
        request.setContent("建议增加更多菜系分类");

        FeedbackResponse response = userFeedbackService.createFeedback(1L, request);

        assertNotNull(response);
        assertEquals("FEATURE", response.getType());
        assertEquals("建议增加更多菜系分类", response.getContent());
        assertEquals("NEW", response.getStatus());
        assertNotNull(response.getCreatedAt());

        verify(userFeedbackMapper).insert(feedbackCaptor.capture());
        UserFeedback entity = feedbackCaptor.getValue();
        assertEquals(1L, entity.getUserId());
        assertEquals("FEATURE", entity.getType());
        assertEquals("建议增加更多菜系分类", entity.getContent());
        assertEquals("NEW", entity.getStatus());
        assertNull(entity.getRating());
    }

    @Test
    void createFeedback_nullUserId_success() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("BUG");
        request.setContent("首页推荐按钮偶尔无响应");

        FeedbackResponse response = userFeedbackService.createFeedback(null, request);

        assertNotNull(response);
        assertEquals("BUG", response.getType());

        verify(userFeedbackMapper).insert(feedbackCaptor.capture());
        assertNull(feedbackCaptor.getValue().getUserId());
    }

    @Test
    void createFeedback_allOptionalFields_success() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("RECOMMENDATION");
        request.setRating(4);
        request.setContent("推荐结果偏向火锅类，希望更均衡");
        request.setContact("wechat: testuser");
        request.setPage("/pages/index/index");
        request.setSystemInfo("{\"model\":\"iPhone 14\",\"platform\":\"ios\"}");

        FeedbackResponse response = userFeedbackService.createFeedback(2L, request);

        assertNotNull(response);
        assertEquals("RECOMMENDATION", response.getType());
        assertEquals(Integer.valueOf(4), response.getRating());
        assertEquals("wechat: testuser", response.getContact());
        assertEquals("/pages/index/index", response.getPage());
        assertEquals("{\"model\":\"iPhone 14\",\"platform\":\"ios\"}", response.getSystemInfo());

        verify(userFeedbackMapper).insert(feedbackCaptor.capture());
        UserFeedback entity = feedbackCaptor.getValue();
        assertEquals(Integer.valueOf(4), entity.getRating());
        assertEquals("wechat: testuser", entity.getContact());
        assertEquals("/pages/index/index", entity.getPage());
    }

    @Test
    void createFeedback_minimalFields_success() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("UI");
        request.setContent("颜色对比度偏低看不清");

        FeedbackResponse response = userFeedbackService.createFeedback(null, request);

        assertNotNull(response);
        assertNull(response.getRating());
        assertNull(response.getContact());
        assertNull(response.getPage());
        assertNull(response.getSystemInfo());

        verify(userFeedbackMapper).insert(feedbackCaptor.capture());
        UserFeedback entity = feedbackCaptor.getValue();
        assertNull(entity.getContact());
        assertNull(entity.getPage());
        assertNull(entity.getSystemInfo());
    }

    @Test
    void createFeedback_emptyString_becomesNull() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("OTHER");
        request.setContent("其他类型的反馈内容");
        request.setContact("");
        request.setPage("");
        request.setSystemInfo("");

        FeedbackResponse response = userFeedbackService.createFeedback(null, request);

        assertNull(response.getContact());
        assertNull(response.getPage());
        assertNull(response.getSystemInfo());

        verify(userFeedbackMapper).insert(feedbackCaptor.capture());
        UserFeedback entity = feedbackCaptor.getValue();
        assertNull(entity.getContact());
        assertNull(entity.getPage());
        assertNull(entity.getSystemInfo());
    }
}
