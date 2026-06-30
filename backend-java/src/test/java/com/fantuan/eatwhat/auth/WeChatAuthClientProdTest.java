package com.fantuan.eatwhat.auth;

import com.fantuan.eatwhat.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeChatAuthClientProdTest {

    private WeChatAuthClientProd client;

    @Mock
    private RestClient restClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RequestHeadersSpec requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @BeforeEach
    void setUp() throws Exception {
        client = new WeChatAuthClientProd();

        // 通过反射注入 mock RestClient，避免真实 HTTP 请求
        Field field = WeChatAuthClientProd.class.getDeclaredField("restClient");
        field.setAccessible(true);
        field.set(client, restClient);

        // 注入测试用的 appId / appSecret
        Field appIdField = WeChatAuthClientProd.class.getDeclaredField("appId");
        appIdField.setAccessible(true);
        appIdField.set(client, "test_app_id");

        Field appSecretField = WeChatAuthClientProd.class.getDeclaredField("appSecret");
        appSecretField.setAccessible(true);
        appSecretField.set(client, "test_app_secret");
    }

    @Test
    void code2Session_normalResponse_returnsOpenid() throws Exception {
        // Given
        String json = "{\"openid\":\"test_openid\",\"session_key\":\"test_key\",\"errcode\":0}";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(json);

        // When
        String openid = client.code2Session("test_code");

        // Then
        assertEquals("test_openid", openid);
    }

    @Test
    void code2Session_wechatError_throwsBusinessException() {
        // Given
        String json = "{\"errcode\":40029,\"errmsg\":\"invalid code\"}";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(json);

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> client.code2Session("bad_code"));
        assertEquals(2009, ex.getCode());
    }

    @Test
    void code2Session_nonJsonContentType_parsesSuccessfully() throws Exception {
        // Given: 微信可能返回 Content-Type: text/plain，但 body 仍然是合法 JSON
        // String.class 接收不受 Content-Type 影响，这里测试 JSON 解析本身
        String json = "{\"openid\":\"plain_openid\",\"session_key\":\"plain_key\"}";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(json);

        // When
        String openid = client.code2Session("test_code");

        // Then
        assertEquals("plain_openid", openid);
    }

    @Test
    void code2Session_emptyBody_throwsBusinessException() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("");

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> client.code2Session("test_code"));
        assertEquals(2009, ex.getCode());
    }

    @Test
    void code2Session_nullBody_throwsBusinessException() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(null);

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> client.code2Session("test_code"));
        assertEquals(2009, ex.getCode());
    }

    @Test
    void code2Session_malformedJson_throwsBusinessException() {
        // Given
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("not json at all");

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> client.code2Session("test_code"));
        assertEquals(2009, ex.getCode());
    }

    @Test
    void code2Session_emptyOpenid_throwsBusinessException() {
        // Given
        String json = "{\"openid\":\"\",\"session_key\":\"test_key\",\"errcode\":0}";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(json);

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> client.code2Session("test_code"));
        assertEquals(2009, ex.getCode());
    }
}
