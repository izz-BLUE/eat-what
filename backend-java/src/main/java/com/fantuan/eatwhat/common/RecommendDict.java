package com.fantuan.eatwhat.common;

import java.util.List;
import java.util.Set;

/**
 * 推荐分类体系统一词典
 *
 * 唯一数据源，同时用于：请求校验、推荐逻辑、dislike 校验、元数据接口。
 * 当前阶段为代码常量；后续可迁移到数据库或配置文件。
 */
public final class RecommendDict {

    private RecommendDict() {}

    // ==================== 餐段 ====================

    public static final List<String> MEAL_TYPES = List.of("早餐", "午餐", "晚餐", "夜宵");

    // ==================== 预算 ====================

    /** API value（与前端传输值一致） */
    public static final List<String> PRICE_LEVELS = List.of("15以内", "15-25", "25-40", "40以上");

    // ==================== 口味（用户可选，不含"不限"——不限由空值/未选择表达） ====================

    public static final List<String> TASTES = List.of("清淡", "辣", "不辣");

    // ==================== 食物类型（多值标签） ====================

    public static final List<String> TYPE_TAGS = List.of("快餐", "小吃", "面食", "火锅", "烧烤", "甜品");

    // ==================== 菜系/风格（多值标签） ====================

    public static final List<String> CUISINE_TAGS = List.of("家常菜", "川菜", "湘菜", "粤菜", "日料", "西餐");

    // ==================== 底层口味标签（用于 taste_tags 字段校验） ====================

    public static final Set<String> BASE_TASTE_TAGS = Set.of("辣", "麻", "咸", "香", "鲜", "甜", "酸", "清淡");

    // ==================== 旧 category 合法值（兼容不想吃和旧参数） ====================

    public static final Set<String> LEGACY_CATEGORIES = Set.of(
            "快餐", "小吃", "面食", "火锅", "烧烤", "甜品",
            "川菜", "粤菜", "湘菜", "家常菜", "日料", "西餐"
    );

    // ==================== 辅助方法 ====================

    /**
     * 判断一个分类值属于 typeTag 还是 cuisineTag。
     * 用于旧 categories 参数分发。
     *
     * @return "type" | "cuisine" | null（未知值）
     */
    public static String classifyCategory(String value) {
        if (TYPE_TAGS.contains(value)) return "type";
        if (CUISINE_TAGS.contains(value)) return "cuisine";
        return null;
    }

    /**
     * 判断旧 category 值是否合法（兼容不想吃和旧参数校验）。
     */
    public static boolean isValidLegacyCategory(String value) {
        return LEGACY_CATEGORIES.contains(value);
    }

    /**
     * 判断值是否为合法的 typeTag。
     */
    public static boolean isValidTypeTag(String value) {
        return TYPE_TAGS.contains(value);
    }

    /**
     * 判断值是否为合法的 cuisineTag。
     */
    public static boolean isValidCuisineTag(String value) {
        return CUISINE_TAGS.contains(value);
    }

    /**
     * 判断值是否为合法的餐段。
     */
    public static boolean isValidMealType(String value) {
        return MEAL_TYPES.contains(value);
    }

    /**
     * 判断值是否为合法的预算。
     */
    public static boolean isValidPriceLevel(String value) {
        return PRICE_LEVELS.contains(value);
    }

    /**
     * 判断值是否为合法的用户可选口味（不含"不限"）。
     */
    public static boolean isValidTaste(String value) {
        return TASTES.contains(value);
    }

    /**
     * 判断值是否为合法的底层口味标签。
     */
    public static boolean isValidBaseTasteTag(String value) {
        return BASE_TASTE_TAGS.contains(value);
    }
}
