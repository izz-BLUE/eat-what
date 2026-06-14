package com.fantuan.eatwhat.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeChatAuthClientMockTest {

    @Test
    void code2Session_sameCode_returnsSameOpenid() throws Exception {
        // Given
        WeChatAuthClientMock mock = new WeChatAuthClientMock();

        // When
        String openid1 = mock.code2Session("dev-user-1");
        String openid2 = mock.code2Session("dev-user-1");

        // Then
        assertNotNull(openid1);
        assertEquals(openid1, openid2);
        assertTrue(openid1.startsWith("mock_"));
    }

    @Test
    void code2Session_differentCode_returnsDifferentOpenid() throws Exception {
        // Given
        WeChatAuthClientMock mock = new WeChatAuthClientMock();

        // When
        String openid1 = mock.code2Session("dev-user-1");
        String openid2 = mock.code2Session("dev-user-2");

        // Then
        assertNotNull(openid1);
        assertNotNull(openid2);
        assertNotEquals(openid1, openid2);
    }
}
