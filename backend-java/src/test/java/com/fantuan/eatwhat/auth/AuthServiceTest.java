package com.fantuan.eatwhat.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.domain.entity.User;
import com.fantuan.eatwhat.dto.request.LoginRequest;
import com.fantuan.eatwhat.dto.response.LoginResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.UserMapper;
import com.fantuan.eatwhat.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private WeChatAuthClient weChatAuthClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_firstTime_createsUser() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCode("test-code");
        request.setNickname("测试用户");
        request.setAvatarUrl("https://example.com/avatar.jpg");

        when(weChatAuthClient.code2Session("test-code")).thenReturn("mock_openid_123");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate auto-generated ID
            return 1;
        });
        when(jwtTokenService.generateToken(1L)).thenReturn("test-token");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(1L, response.getUserId());
        assertEquals("测试用户", response.getNickname());
        assertEquals("https://example.com/avatar.jpg", response.getAvatarUrl());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void login_existingUser_reusesUser() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCode("test-code");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setOpenid("mock_openid_123");
        existingUser.setNickname("已存在用户");

        when(weChatAuthClient.code2Session("test-code")).thenReturn("mock_openid_123");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(jwtTokenService.generateToken(1L)).thenReturn("test-token");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("已存在用户", response.getNickname());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void login_wechatError_throwsException() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCode("invalid-code");

        when(weChatAuthClient.code2Session("invalid-code"))
                .thenThrow(new BusinessException(2009, "微信登录失败"));

        // When & Then
        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void login_concurrentCreation_handlesDuplicateKey() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setCode("test-code");
        request.setNickname("测试用户");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setOpenid("mock_openid_123");
        existingUser.setNickname("已存在用户");

        when(weChatAuthClient.code2Session("test-code")).thenReturn("mock_openid_123");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)  // 第一次查询不存在
                .thenReturn(existingUser);  // 并发创建后重新查询存在
        when(userMapper.insert(any(User.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry"));
        when(jwtTokenService.generateToken(1L)).thenReturn("test-token");
        when(jwtTokenService.getExpirationSeconds()).thenReturn(3600L);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        verify(userMapper).insert(any(User.class));
    }
}
