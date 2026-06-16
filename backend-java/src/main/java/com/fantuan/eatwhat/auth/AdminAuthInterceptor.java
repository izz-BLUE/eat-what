package com.fantuan.eatwhat.auth;

import com.fantuan.eatwhat.common.ApiResponse;
import com.fantuan.eatwhat.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 管理后台认证拦截器
 * <p>
 * 拦截 /api/v1/admin/** 路径，校验 X-Admin-Token 请求头。
 * <ul>
 *   <li>配置了 admin.token 时：请求头必须匹配，否则返回 3001</li>
 *   <li>未配置 admin.token 时：dev/test Profile 放行，prod Profile 拒绝</li>
 * </ul>
 */
@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Value("${admin.token:}")
    private String adminToken;

    private final Environment environment;
    private final ObjectMapper objectMapper;

    public AdminAuthInterceptor(Environment environment, ObjectMapper objectMapper) {
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestToken = request.getHeader("X-Admin-Token");

        // 配置了 admin token：必须匹配
        if (adminToken != null && !adminToken.isEmpty()) {
            if (adminToken.equals(requestToken)) {
                return true;
            }
            log.warn("管理后台 token 无效: {}", request.getRequestURI());
            sendError(response, ResultCode.FORBIDDEN, "管理 token 无效");
            return false;
        }

        // 未配置 admin token：检查环境
        boolean isDevOrTest = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(p -> p.equals("dev") || p.equals("test"));

        if (isDevOrTest) {
            log.debug("未配置 admin.token，dev/test 环境放行");
            return true;
        }

        log.error("生产环境未配置 admin.token，拒绝管理后台请求");
        sendError(response, ResultCode.FORBIDDEN, "管理 token 未配置");
        return false;
    }

    private void sendError(HttpServletResponse response, ResultCode resultCode, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = ApiResponse.fail(resultCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
