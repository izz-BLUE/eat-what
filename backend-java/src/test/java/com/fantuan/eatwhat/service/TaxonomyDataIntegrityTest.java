package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.FoodTaxonomy;
import com.fantuan.eatwhat.common.RecommendDict;
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
 *
 * H2 菜品数据由 TestFoodDataInitializer 从 data/foods.csv 自动导入。
 * h2-init.sql 只负责 schema DDL，不再包含菜品 INSERT 数据。
 * 因此新增菜品只需修改 CSV + 生成 migration，不再需要手工更新 h2-init.sql。
 */
@SpringBootTest
@ActiveProfiles("test")
class TaxonomyDataIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== 所有启用菜 name 唯一且非空 ====================

    @Test
    void allEnabledFoodNamesUniqueAndNonNull() {
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled = 1 ORDER BY id", String.class);

        assertFalse(names.isEmpty(), "至少应有一道启用菜");
        Set<String> uniqueNames = Set.copyOf(names);
        assertEquals(uniqueNames.size(), names.size(), "存在重复菜名");
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

    // ==================== 所有标签均属于统一词典 ====================

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
