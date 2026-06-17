package com.fantuan.eatwhat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fantuan.eatwhat.auth.JwtTokenService;
import com.fantuan.eatwhat.auth.WeChatAuthClient;
import com.fantuan.eatwhat.common.ResultCode;
import com.fantuan.eatwhat.domain.entity.User;
import com.fantuan.eatwhat.dto.request.LoginRequest;
import com.fantuan.eatwhat.dto.response.LoginResponse;
import com.fantuan.eatwhat.exception.BusinessException;
import com.fantuan.eatwhat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WeChatAuthClient weChatAuthClient;
    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;

    /**
     * 微信登录
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 调用微信接口获取 openid
        String openid;
        try {
            openid = weChatAuthClient.code2Session(request.getCode());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常: {}", e.getClass().getSimpleName());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }

        // 2. 查询或创建用户
        User user = findOrCreateUser(openid, request.getNickname(), request.getAvatarUrl());

        // 3. 生成 JWT token
        String token = jwtTokenService.generateToken(user.getId());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getExpirationSeconds())
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    private User findOrCreateUser(String openid, String nickname, String avatarUrl) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            // 用户存在，更新昵称和头像（如果提供）
            boolean needUpdate = false;
            if (nickname != null && !nickname.isEmpty() && !nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
                needUpdate = true;
            }
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
                needUpdate = true;
            }
            if (needUpdate) {
                userMapper.updateById(user);
            }
            return user;
        }

        // 用户不存在，创建
        user = new User();
        user.setOpenid(openid);
        user.setNickname(nickname != null ? nickname : "");
        user.setAvatarUrl(avatarUrl != null ? avatarUrl : "");
        user.setCreatedAt(LocalDateTime.now());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发创建，重新查询（不打印 openid 避免日志泄露）
            log.warn("并发创建用户，重新查询用户");
            user = userMapper.selectOne(wrapper);
            if (user == null) {
                throw new RuntimeException("用户创建失败");
            }
        }

        return user;
    }
}
