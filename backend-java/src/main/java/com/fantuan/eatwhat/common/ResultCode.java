package com.fantuan.eatwhat.common;

import lombok.Getter;

/**
 * 统一错误码定义
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(0, "success"),

    // 参数错误 1xxx
    PARAM_ERROR(1001, "参数错误"),
    PARAM_MISSING(1002, "参数缺失"),
    UNAUTHORIZED(1003, "未登录"),

    // 业务错误 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    FOOD_NOT_FOUND(2002, "食物不存在"),
    VOTE_NOT_FOUND(2003, "投票不存在"),
    VOTE_ENDED(2004, "投票已结束"),
    VOTE_ALREADY_VOTED(2005, "已投过票"),
    VOTE_LIMIT_EXCEEDED(2006, "超过投票上限"),
    BLACKLIST_NOT_FOUND(2007, "黑名单记录不存在"),
    DISLIKE_NOT_FOUND(2008, "不想吃记录不存在"),
    WECHAT_LOGIN_FAILED(2009, "微信登录失败"),

    // 系统错误 5xxx
    SYSTEM_ERROR(5001, "系统错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
