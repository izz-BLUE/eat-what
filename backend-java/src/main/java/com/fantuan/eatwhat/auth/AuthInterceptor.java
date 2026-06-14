package com.fantuan.eatwhat.auth;

import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 防御性清理：线程复用时可能残留旧身份
        UserContext.clear();

        // 只处理控制器方法
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 检查是否需要登录
        boolean requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class) != null
                || handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);

        // 获取 Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtTokenService.parseToken(token);

            if (userId == null) {
                // token 无效或过期，无论是否公开接口都返回 1003
                sendError(response, ResultCode.UNAUTHORIZED);
                return false;
            }

            // 设置用户上下文
            UserContext.setUserId(userId);
            return true;
        }

        // 没有 token
        if (requireLogin) {
            sendError(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 允许匿名访问（无 token）
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理用户上下文
        UserContext.clear();
    }

    private void sendError(HttpServletResponse response, ResultCode resultCode) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = ApiResponse.fail(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
