package com.fantuan.eatwhat.controller;

import com.fantuan.eatwhat.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查接口
 */
@RestController
public class HealthController {

    /**
     * GET /api/health
     */
    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "eat-what-backend",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
