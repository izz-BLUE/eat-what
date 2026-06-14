package com.fantuan.eatwhat.auth;

/**
 * 用户认证上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    /**
     * 设置当前用户 ID
     */
    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
