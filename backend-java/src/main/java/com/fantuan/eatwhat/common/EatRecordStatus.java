package com.fantuan.eatwhat.common;

/**
 * 用餐记录状态常量
 */
public final class EatRecordStatus {

    /** 已决定（待用餐） */
    public static final String DECIDED = "DECIDED";

    /** 已吃（已完成） */
    public static final String EATEN = "EATEN";

    private EatRecordStatus() {
        // 工具类，禁止实例化
    }
}
