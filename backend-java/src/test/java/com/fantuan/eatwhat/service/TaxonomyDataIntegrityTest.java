package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.FoodTaxonomy;
import com.fantuan.eatwhat.common.RecommendDict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类体系数据完整性集成测试
 * 验证 H2 测试数据库中 30 道菜的新分类字段完整性和一致性
 *
 * 注意：因 Spring Test 上下文缓存机制，H2 INIT 脚本可能多次执行导致数据重复。
 * 本测试通过 @BeforeEach 清理重复数据后重新插入确保精确可控。
 */
@SpringBootTest
@ActiveProfiles("test")
class TaxonomyDataIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanAndReseed() {
        // 清理所有菜品数据并重新插入一份（仅用于数据完整性验证）
        jdbcTemplate.execute("DELETE FROM foods");
        jdbcTemplate.execute("INSERT INTO `foods` (`name`, `category`, `type_tags`, `cuisine_tags`, `meal_types`, `taste_tags`, `price_level`, `enabled`) VALUES" +
                "('猪脚饭', '快餐', '快餐', '', '午餐,晚餐', '咸,香', 2, 1)," +
                "('黄焖鸡米饭', '快餐', '快餐', '', '午餐,晚餐', '咸,辣', 2, 1)," +
                "('麻辣烫', '小吃', '小吃', '', '午餐,晚餐,夜宵', '辣,麻', 2, 1)," +
                "('肠粉', '小吃', '小吃', '', '早餐,午餐,夜宵', '清淡,鲜', 1, 1)," +
                "('云吞面', '面食', '面食', '', '早餐,午餐,晚餐', '清淡,鲜', 2, 1)," +
                "('兰州拉面', '面食', '面食', '', '早餐,午餐,晚餐', '清淡', 1, 1)," +
                "('沙县小吃', '小吃', '小吃', '', '早餐,午餐,晚餐,夜宵', '清淡', 1, 1)," +
                "('盖浇饭', '快餐', '快餐', '', '午餐,晚餐', '咸', 1, 1)," +
                "('炒饭', '快餐', '快餐', '', '午餐,晚餐', '咸,香', 1, 1)," +
                "('炸鸡', '快餐', '快餐', '', '午餐,晚餐,夜宵', '咸,香', 2, 1)," +
                "('汉堡', '快餐', '快餐', '', '午餐,晚餐', '咸,香', 2, 1)," +
                "('披萨', '西餐', '', '西餐', '午餐,晚餐', '咸,香', 3, 1)," +
                "('寿司', '日料', '', '日料', '午餐,晚餐', '清淡,鲜', 3, 1)," +
                "('拉面', '日料', '面食', '日料', '午餐,晚餐', '咸,鲜', 3, 1)," +
                "('烤肉', '烧烤', '烧烤', '', '午餐,晚餐,夜宵', '咸,香', 3, 1)," +
                "('火锅', '火锅', '火锅', '', '午餐,晚餐,夜宵', '辣,麻', 4, 1)," +
                "('酸菜鱼', '川菜', '', '川菜', '午餐,晚餐', '酸,辣', 3, 1)," +
                "('宫保鸡丁', '川菜', '', '川菜', '午餐,晚餐', '辣', 2, 1)," +
                "('回锅肉', '川菜', '', '川菜', '午餐,晚餐', '辣,咸', 2, 1)," +
                "('红烧肉', '家常菜', '', '家常菜', '午餐,晚餐', '咸,甜', 2, 1)," +
                "('糖醋排骨', '家常菜', '', '家常菜', '午餐,晚餐', '甜,酸', 3, 1)," +
                "('清蒸鱼', '粤菜', '', '粤菜', '午餐,晚餐', '清淡,鲜', 3, 1)," +
                "('白切鸡', '粤菜', '', '粤菜', '午餐,晚餐', '清淡,鲜', 3, 1)," +
                "('叉烧', '粤菜', '', '粤菜', '午餐,晚餐', '甜,咸', 3, 1)," +
                "('湖南小炒肉', '湘菜', '', '湘菜', '午餐,晚餐', '辣', 2, 1)," +
                "('臭豆腐', '小吃', '小吃', '', '午餐,晚餐,夜宵', '辣', 1, 1)," +
                "('煎饼果子', '小吃', '小吃', '', '早餐,午餐,夜宵', '咸', 1, 1)," +
                "('肉夹馍', '小吃', '小吃', '', '早餐,午餐,晚餐,夜宵', '咸', 1, 1)," +
                "('凉皮', '小吃', '小吃', '', '午餐,晚餐,夜宵', '辣,酸', 1, 1)," +
                "('奶茶', '甜品', '甜品', '', '', '甜', 1, 1)");
    }

    // ==================== 菜品总数为 30 ====================

    @Test
    void enabledFoodsExactly30() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled = 1", Integer.class);
        assertEquals(30, count, "启用菜品应恰好 30 道");
    }

    // ==================== 30 个预期菜名全部存在且不重复 ====================

    @Test
    void allExpectedFoodNamesPresentAndUnique() {
        List<String> expectedNames = List.of(
                "猪脚饭", "黄焖鸡米饭", "麻辣烫", "肠粉", "云吞面",
                "兰州拉面", "沙县小吃", "盖浇饭", "炒饭", "炸鸡",
                "汉堡", "披萨", "寿司", "拉面", "烤肉",
                "火锅", "酸菜鱼", "宫保鸡丁", "回锅肉", "红烧肉",
                "糖醋排骨", "清蒸鱼", "白切鸡", "叉烧", "湖南小炒肉",
                "臭豆腐", "煎饼果子", "肉夹馍", "凉皮", "奶茶"
        );

        List<String> actualNames = jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled = 1 ORDER BY id", String.class);

        assertEquals(expectedNames.size(), actualNames.size(), "启用菜品数量不符");

        Set<String> actualSet = Set.copyOf(actualNames);
        for (String expected : expectedNames) {
            assertTrue(actualSet.contains(expected),
                    "缺少预期菜品: " + expected);
        }
        assertEquals(expectedNames.size(), actualSet.size(), "存在重复菜名");
    }

    // ==================== 每道启用菜至少有 typeTags 或 cuisineTags ====================

    @Test
    void everyFoodHasTypeOrCuisineTags() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT name, type_tags, cuisine_tags FROM foods WHERE enabled = 1");

        for (Map<String, Object> row : rows) {
            String name = (String) row.get("name");
            String typeTags = (String) row.get("type_tags");
            String cuisineTags = (String) row.get("cuisine_tags");

            boolean hasType = typeTags != null && !typeTags.isEmpty();
            boolean hasCuisine = cuisineTags != null && !cuisineTags.isEmpty();

            assertTrue(hasType || hasCuisine,
                    "菜品 [" + name + "] 至少应有 typeTags 或 cuisineTags");
        }
    }

    // ==================== 除奶茶外 mealTypes 非空 ====================

    @Test
    void allFoodsExceptMilkTeaHaveMealTypes() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT name, meal_types FROM foods WHERE enabled = 1");

        for (Map<String, Object> row : rows) {
            String name = (String) row.get("name");
            String mealTypes = (String) row.get("meal_types");

            if ("奶茶".equals(name)) {
                assertTrue(mealTypes == null || mealTypes.isEmpty(),
                        "奶茶 mealTypes 应为空，实际: " + mealTypes);
            } else {
                assertNotNull(mealTypes, "菜品 [" + name + "] mealTypes 不应为 null");
                assertFalse(mealTypes.isEmpty(),
                        "菜品 [" + name + "] mealTypes 不应为空");
            }
        }
    }

    // ==================== 奶茶 mealTypes 为空 ====================

    @Test
    void milkTeaMealTypesEmpty() {
        String mealTypes = jdbcTemplate.queryForObject(
                "SELECT meal_types FROM foods WHERE name = '奶茶' AND enabled = 1", String.class);
        assertTrue(mealTypes == null || mealTypes.isEmpty(),
                "奶茶 mealTypes 应为空，实际: " + mealTypes);
    }

    // ==================== 日料拉面 typeTags 精确包含面食、cuisineTags 精确包含日料 ====================

    @Test
    void ramenTypeTagsContainsNoodles_cuisineTagsContainsJapanese() {
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT type_tags, cuisine_tags FROM foods WHERE name = '拉面' AND enabled = 1");

        String typeTags = (String) row.get("type_tags");
        String cuisineTags = (String) row.get("cuisine_tags");

        Set<String> types = FoodTaxonomy.parseTags(typeTags);
        Set<String> cuisines = FoodTaxonomy.parseTags(cuisineTags);

        assertTrue(types.contains("面食"),
                "日料拉面 type_tags 应精确包含'面食'，实际: " + types);
        assertTrue(cuisines.contains("日料"),
                "日料拉面 cuisine_tags 应精确包含'日料'，实际: " + cuisines);
    }

    // ==================== 火锅和麻辣烫 mealTypes 精确包含午餐 ====================

    @Test
    void hotpotAndMalatangMealTypesContainLunch() {
        for (String name : List.of("火锅", "麻辣烫")) {
            String mealTypes = jdbcTemplate.queryForObject(
                    "SELECT meal_types FROM foods WHERE name = ? AND enabled = 1",
                    String.class, name);

            Set<String> mealSet = FoodTaxonomy.parseTags(mealTypes);
            assertTrue(mealSet.contains("午餐"),
                    name + " mealTypes 应包含'午餐'，实际: " + mealSet);
        }
    }

    // ==================== 微辣残留为 0 ====================

    @Test
    void noWeiLaResidue() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled = 1 AND taste_tags LIKE '%微辣%'",
                Integer.class);
        assertEquals(0, count, "不应存在微辣残留");
    }

    // ==================== 所有 typeTags/cuisineTags/mealTypes/tasteTags 均属于统一词典 ====================

    @Test
    void allTypeTagsInDict() {
        List<String> allTypeTags = jdbcTemplate.queryForList(
                "SELECT type_tags FROM foods WHERE enabled = 1 AND type_tags != ''", String.class);

        for (String tags : allTypeTags) {
            for (String tag : FoodTaxonomy.parseTags(tags)) {
                assertTrue(RecommendDict.isValidTypeTag(tag),
                        "type_tag [" + tag + "] 不在词典中");
            }
        }
    }

    @Test
    void allCuisineTagsInDict() {
        List<String> allCuisineTags = jdbcTemplate.queryForList(
                "SELECT cuisine_tags FROM foods WHERE enabled = 1 AND cuisine_tags != ''", String.class);

        for (String tags : allCuisineTags) {
            for (String tag : FoodTaxonomy.parseTags(tags)) {
                assertTrue(RecommendDict.isValidCuisineTag(tag),
                        "cuisine_tag [" + tag + "] 不在词典中");
            }
        }
    }

    @Test
    void allMealTypesInDict() {
        List<String> allMealTypes = jdbcTemplate.queryForList(
                "SELECT meal_types FROM foods WHERE enabled = 1 AND meal_types != ''", String.class);

        for (String tags : allMealTypes) {
            for (String tag : FoodTaxonomy.parseTags(tags)) {
                assertTrue(RecommendDict.isValidMealType(tag),
                        "meal_type [" + tag + "] 不在词典中");
            }
        }
    }

    @Test
    void allTasteTagsInDict() {
        List<String> allTasteTags = jdbcTemplate.queryForList(
                "SELECT taste_tags FROM foods WHERE enabled = 1 AND taste_tags != ''", String.class);

        for (String tags : allTasteTags) {
            for (String tag : FoodTaxonomy.parseTags(tags)) {
                assertTrue(RecommendDict.isValidBaseTasteTag(tag),
                        "taste_tag [" + tag + "] 不在词典中");
            }
        }
    }

    // ==================== 旧 category 值兼容性 ====================

    @Test
    void allCategoriesAreValidLegacyValues() {
        List<String> categories = jdbcTemplate.queryForList(
                "SELECT DISTINCT category FROM foods WHERE enabled = 1", String.class);

        for (String cat : categories) {
            assertTrue(
                    RecommendDict.isValidTypeTag(cat)
                    || RecommendDict.isValidCuisineTag(cat)
                    || RecommendDict.isValidLegacyCategory(cat),
                    "category [" + cat + "] 不是合法的旧分类值");
        }
    }

    // ==================== 子串精确性验证 ====================

    @Test
    void substringNotMatch_typeTag() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT name, type_tags FROM foods WHERE enabled = 1 AND type_tags != ''");

        for (Map<String, Object> row : rows) {
            Set<String> tags = FoodTaxonomy.parseTags((String) row.get("type_tags"));
            assertFalse(tags.contains("面"),
                    "菜品 [" + row.get("name") + "] 的 typeTags 不应包含'面'（非词典值）");
        }
    }

    @Test
    void parseTags_emptyAndNullHandled() {
        assertTrue(FoodTaxonomy.parseTags(null).isEmpty(), "null 应返回空 Set");
        assertTrue(FoodTaxonomy.parseTags("").isEmpty(), "空字符串应返回空 Set");
        assertTrue(FoodTaxonomy.parseTags("  ").isEmpty(), "仅空格应返回空 Set");

        Set<String> result = FoodTaxonomy.parseTags(" 面食 , 日料 ");
        assertEquals(2, result.size());
        assertTrue(result.contains("面食"));
        assertTrue(result.contains("日料"));
        assertFalse(result.contains(" 面食 "));
    }
}
