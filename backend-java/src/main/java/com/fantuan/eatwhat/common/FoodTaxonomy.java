package com.fantuan.eatwhat.common;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜品分类体系工具类
 *
 * 统一标签解析逻辑，确保 RecommendService 和 UserDislikeService
 * 都使用精确的 Set.contains 匹配，而非 LIKE/子串模糊匹配。
 */
public final class FoodTaxonomy {

    private FoodTaxonomy() {}

    /**
     * 将逗号分隔的标签字符串解析为 Set。
     * 空值/null 返回空 Set，每个标签 trim 处理，过滤空白标签。
     *
     * @param tags 逗号分隔的标签字符串，如 "面食,小吃" 或 null
     * @return 不可变的标签集合
     */
    public static Set<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Set.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 判断目标标签集合是否包含给定值（精确匹配）。
     */
    public static boolean containsTag(Set<String> tagSet, String value) {
        return tagSet.contains(value);
    }

    /**
     * 判断 food 的 typeTags 或 cuisineTags 是否包含给定值（精确匹配）。
     *
     * @param typeTags     food.typeTags 逗号分隔字符串
     * @param cuisineTags  food.cuisineTags 逗号分隔字符串
     * @param value        要匹配的值
     * @return 任一维度命中即返回 true
     */
    public static boolean matchesTypeOrCuisine(String typeTags, String cuisineTags, String value) {
        return parseTags(typeTags).contains(value) || parseTags(cuisineTags).contains(value);
    }

    /**
     * 判断 food 的 category、typeTags 或 cuisineTags 是否包含给定值（精确匹配）。
     * 用于 dislike 三向匹配。
     *
     * @param category     food.category
     * @param typeTags     food.typeTags 逗号分隔字符串
     * @param cuisineTags  food.cuisineTags 逗号分隔字符串
     * @param value        要匹配的值（dislike 分类）
     * @return 任一维度命中即返回 true
     */
    public static boolean matchesAny(String category, String typeTags, String cuisineTags, String value) {
        if (value.equals(category)) return true;
        return matchesTypeOrCuisine(typeTags, cuisineTags, value);
    }
}
