package com.fantuan.eatwhat.auth;

import java.lang.annotation.*;

/**
 * 标记接口需要登录
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
