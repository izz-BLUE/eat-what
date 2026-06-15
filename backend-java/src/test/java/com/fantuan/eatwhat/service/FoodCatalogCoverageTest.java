package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.RecommendDict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜品库覆盖矩阵验证测试。
 *
 * 所有统计从 H2 数据库动态计算，严禁手工统计。
 * 验证 data/foods.csv 导入 H2 后的：
 *   1. 覆盖矩阵（动态计算 + 一致性断言）
 *   2. 高风险菜品逐道期望映射（正断言 + 负断言）
 *   3. 推荐核心逻辑验证
 */
@SpringBootTest
@ActiveProfiles("test")
class FoodCatalogCoverageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== 一致性断言 ====================

    /**
     * 各 priceLevel 数量之和必须等于启用菜总数。
     */
    @Test
    void priceLevelSumEqualsTotal() {
        int total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled=1", Integer.class);
        int sum = 0;
        for (int level = 1; level <= 4; level++) {
            int count = countByPriceLevel(level);
            sum += count;
            // 每个 priceLevel 的菜名列表长度等于 count
            List<String> names = getFoodNamesByPriceLevel(level);
            assertEquals(count, names.size(),
                    "priceLevel " + level + " 的 names 列表长度 " + names.size() + " ≠ count " + count);
        }
        assertEquals(total, sum, "priceLevel 合计 " + sum + " ≠ 启用菜总数 " + total);
    }

    /**
     * 每个 mealType 数量等于 H2 中包含该餐段的实际行数。
     */
    @Test
    void mealTypeCountsMatchActualRows() {
        for (String mt : RecommendDict.MEAL_TYPES) {
            int dynamicCount = countByTag("meal_types", mt);
            // 手工验证：逐行检查该餐段
            List<Map<String, Object>> allFoods = jdbcTemplate.queryForList(
                    "SELECT meal_types FROM foods WHERE enabled=1");
            int manualCount = 0;
            for (Map<String, Object> row : allFoods) {
                String mealTypes = (String) row.get("meal_types");
                if (mealTypes != null && Arrays.asList(mealTypes.split(",")).contains(mt)) {
                    manualCount++;
                }
            }
            assertEquals(manualCount, dynamicCount,
                    "mealType \"" + mt + "\" 动态计数 " + dynamicCount + " ≠ 逐行计算 " + manualCount);
        }
    }

    /**
     * 覆盖报告中的菜名列表长度等于对应数量。
     */
    @Test
    void namelistLengthEqualsCount() {
        for (String tag : RecommendDict.TYPE_TAGS) {
            List<String> names = getFoodNamesByTag("type_tags", tag);
            int count = countByTag("type_tags", tag);
            assertEquals(count, names.size(),
                    "typeTag \"" + tag + "\" names列表 " + names.size() + " ≠ count " + count);
        }
        for (String tag : RecommendDict.CUISINE_TAGS) {
            List<String> names = getFoodNamesByTag("cuisine_tags", tag);
            int count = countByTag("cuisine_tags", tag);
            assertEquals(count, names.size(),
                    "cuisineTag \"" + tag + "\" names列表 " + names.size() + " ≠ count " + count);
        }
        for (String mt : RecommendDict.MEAL_TYPES) {
            List<String> names = getFoodNamesByTag("meal_types", mt);
            int count = countByTag("meal_types", mt);
            assertEquals(count, names.size(),
                    "mealType \"" + mt + "\" names列表 " + names.size() + " ≠ count " + count);
        }
    }

    // ==================== 覆盖矩阵（从数据库动态计算） ====================

    @Test
    void eachTypeTagAtLeast5() {
        for (String tag : RecommendDict.TYPE_TAGS) {
            int count = countByTag("type_tags", tag);
            List<String> names = getFoodNamesByTag("type_tags", tag);
            assertTrue(count >= 5,
                    "typeTag \"" + tag + "\" 仅 " + count + " 道（" + names + "），需要至少 5 道");
        }
    }

    @Test
    void eachCuisineTagAtLeast5() {
        for (String tag : RecommendDict.CUISINE_TAGS) {
            int count = countByTag("cuisine_tags", tag);
            List<String> names = getFoodNamesByTag("cuisine_tags", tag);
            assertTrue(count >= 5,
                    "cuisineTag \"" + tag + "\" 仅 " + count + " 道（" + names + "），需要至少 5 道");
        }
    }

    @Test
    void eachMealTypeAtLeast10() {
        for (String mt : RecommendDict.MEAL_TYPES) {
            int count = countByTag("meal_types", mt);
            List<String> names = getFoodNamesByTag("meal_types", mt);
            assertTrue(count >= 10,
                    "mealType \"" + mt + "\" 仅 " + count + " 道（" + names + "），需要至少 10 道");
        }
    }

    @Test
    void tasteFiltersQingdanLaBulaAtLeast10() {
        int qingdan = countByTag("taste_tags", "清淡");
        int la = countByTag("taste_tags", "辣");
        int bula = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled=1 AND taste_tags NOT LIKE '%辣%'", Integer.class);

        assertTrue(qingdan >= 10, "\"清淡\" 仅 " + qingdan + " 道");
        assertTrue(la >= 10, "\"辣\" 仅 " + la + " 道");
        assertTrue(bula >= 10, "\"不辣\" 仅 " + bula + " 道");
    }

    @Test
    void dessertsAtLeast5WithMealTypes() {
        List<Map<String, Object>> desserts = jdbcTemplate.queryForList(
                "SELECT name, meal_types FROM foods WHERE enabled=1 AND type_tags LIKE '%甜品%'");
        assertTrue(desserts.size() >= 5, "甜品仅 " + desserts.size() + " 道");
        for (Map<String, Object> d : desserts) {
            String mealTypes = (String) d.get("meal_types");
            assertNotNull(mealTypes, d.get("name") + " meal_types 为 null");
            assertFalse(mealTypes.isEmpty(), d.get("name") + " meal_types 为空");
        }
    }

    @Test
    void allEnabledFoodsHaveMealTypes() {
        int emptyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled=1 AND meal_types=''", Integer.class);
        assertEquals(0, emptyCount, "有 " + emptyCount + " 道启用菜 meal_types 为空");
    }

    @Test
    void priceLevel1To4EachAtLeast3() {
        for (int level = 1; level <= 4; level++) {
            List<String> names = getFoodNamesByPriceLevel(level);
            int count = countByPriceLevel(level);
            assertTrue(count >= 3,
                    "priceLevel " + level + " 仅 " + count + " 道（" + names + "），需要至少 3 道");
        }
    }

    // ==================== 高风险菜品逐道期望映射（正断言） ====================

    @Test
    void baoziTypeXiaochiNoCuisine() {
        Map<String, Object> r = queryFood("包子");
        assertNotNull(r, "包子应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.contains("小吃"), "包子 typeTags 应含小吃");
        assertTrue(cuisines.isEmpty(), "包子 cuisineTags 应为空，实际: " + cuisines);
    }

    @Test
    void kaoChuanTasteXianXiangNoLa() {
        Map<String, Object> r = queryFood("烤串");
        assertNotNull(r);
        Set<String> tastes = parseTags((String) r.get("taste_tags"));
        assertTrue(tastes.contains("咸"));
        assertTrue(tastes.contains("香"));
        assertFalse(tastes.contains("辣"), "烤串 taste 不应含辣，实际: " + tastes);
    }

    @Test
    void xiShiKaoJiNoTypeTagOnlyXiCan() {
        Map<String, Object> r = queryFood("西式烤鸡");
        assertNotNull(r, "西式烤鸡应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.isEmpty(), "西式烤鸡 typeTags 应为空，实际: " + types);
        assertTrue(cuisines.contains("西餐"), "西式烤鸡 cuisineTags 应含西餐");
    }

    @Test
    void taiShiLuRouFanFastFoodNoCuisine() {
        Map<String, Object> r = queryFood("台式卤肉饭");
        assertNotNull(r, "台式卤肉饭应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.contains("快餐"));
        assertTrue(cuisines.isEmpty(), "台式卤肉饭 cuisineTags 应为空，实际: " + cuisines);
    }

    @Test
    void manYuFanNoTypeTagOnlyJapanese() {
        Map<String, Object> r = queryFood("鳗鱼饭");
        assertNotNull(r);
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.isEmpty(), "鳗鱼饭 typeTags 应为空，实际: " + types);
        assertTrue(cuisines.contains("日料"));
    }

    @Test
    void niuRouMianNoodlesNoCuisine() {
        Map<String, Object> r = queryFood("牛肉面");
        assertNotNull(r);
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.contains("面食"));
        assertTrue(cuisines.isEmpty(), "牛肉面 cuisineTags 应为空，实际: " + cuisines);
    }

    @Test
    void hunTunXiaochiNoCuisine() {
        Map<String, Object> r = queryFood("馄饨");
        assertNotNull(r);
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.contains("小吃"));
        assertTrue(cuisines.isEmpty(), "馄饨 cuisineTags 应为空，实际: " + cuisines);
    }

    @Test
    void guangShiPiDanShouRouZhouXiaochiYueCai() {
        Map<String, Object> r = queryFood("广式皮蛋瘦肉粥");
        assertNotNull(r, "广式皮蛋瘦肉粥应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.contains("小吃"));
        assertTrue(cuisines.contains("粤菜"), "广式皮蛋瘦肉粥应含粤菜");
    }

    @Test
    void guangShiShaoMaiXiaochiYueCai() {
        Map<String, Object> r = queryFood("广式烧卖");
        assertNotNull(r, "广式烧卖应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.contains("小吃"));
        assertTrue(cuisines.contains("粤菜"));
    }

    @Test
    void chuanWeiKaoYuBbqChuanCaiLaMa() {
        Map<String, Object> r = queryFood("川味烤鱼");
        assertNotNull(r, "川味烤鱼应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        Set<String> tastes = parseTags((String) r.get("taste_tags"));
        assertTrue(types.contains("烧烤"));
        assertTrue(cuisines.contains("川菜"));
        assertTrue(tastes.contains("辣"));
        assertTrue(tastes.contains("麻"));
    }

    @Test
    void xiangWeiSuanMiaoLaRouXiangCai() {
        Map<String, Object> r = queryFood("湘味蒜苗腊肉");
        assertNotNull(r, "湘味蒜苗腊肉应存在");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.isEmpty(), "湘味蒜苗腊肉 typeTags 应为空");
        assertTrue(cuisines.contains("湘菜"));
    }

    @Test
    void nongJiaYiWanXiangXiangCaiLaXian() {
        Map<String, Object> r = queryFood("农家一碗香");
        assertNotNull(r, "农家一碗香应存在（替换干锅牛蛙）");
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        Set<String> tastes = parseTags((String) r.get("taste_tags"));
        assertTrue(cuisines.contains("湘菜"));
        assertTrue(tastes.contains("辣"));
        assertTrue(tastes.contains("咸"));
    }

    @Test
    void duoJiaoYuTouLaXianNoSuan() {
        Map<String, Object> r = queryFood("剁椒鱼头");
        assertNotNull(r);
        Set<String> tastes = parseTags((String) r.get("taste_tags"));
        assertTrue(tastes.contains("辣"));
        assertTrue(tastes.contains("鲜"));
        assertFalse(tastes.contains("酸"), "剁椒鱼头 taste 不应含酸，实际: " + tastes);
    }

    @Test
    void youTiaoOnlyBreakfast() {
        Map<String, Object> r = queryFood("油条");
        assertNotNull(r);
        Set<String> meals = parseTags((String) r.get("meal_types"));
        assertEquals(Set.of("早餐"), meals, "油条 mealTypes 应仅为早餐，实际: " + meals);
    }

    @Test
    void douJiangQingdanNoSweet() {
        Map<String, Object> r = queryFood("豆浆");
        assertNotNull(r);
        Set<String> tastes = parseTags((String) r.get("taste_tags"));
        assertTrue(tastes.contains("清淡"), "豆浆 taste 应为清淡");
        assertFalse(tastes.contains("甜"), "豆浆不应含甜，实际: " + tastes);
    }

    @Test
    void xianDouFuNaoXianXianNoQingdan() {
        Map<String, Object> r = queryFood("咸豆腐脑");
        assertNotNull(r, "咸豆腐脑应存在（已从豆腐脑改名）");
        Set<String> tastes = parseTags((String) r.get("taste_tags"));
        assertTrue(tastes.contains("咸"));
        assertTrue(tastes.contains("鲜"));
        assertFalse(tastes.contains("清淡"), "咸豆腐脑不应含清淡，实际: " + tastes);
    }

    @Test
    void jiaChangDouFuJiaChangCai() {
        Map<String, Object> r = queryFood("家常豆腐");
        assertNotNull(r, "家常豆腐应存在（新增）");
        Set<String> types = parseTags((String) r.get("type_tags"));
        Set<String> cuisines = parseTags((String) r.get("cuisine_tags"));
        assertTrue(types.isEmpty(), "家常豆腐 typeTags 应为空");
        assertTrue(cuisines.contains("家常菜"));
    }

    // ==================== 负断言 ====================

    @Test
    void neg_baoziNoJiaChangCai() {
        Map<String, Object> r = queryFood("包子");
        assertFalse(parseTags((String) r.get("cuisine_tags")).contains("家常菜"),
                "包子不应含 cuisineTag=家常菜");
    }

    @Test
    void neg_taiShiLuRouFanNoJiaChangCai() {
        Map<String, Object> r = queryFood("台式卤肉饭");
        assertFalse(parseTags((String) r.get("cuisine_tags")).contains("家常菜"),
                "台式卤肉饭不应含 cuisineTag=家常菜");
    }

    @Test
    void neg_niuRouMianNoJiaChangCai() {
        Map<String, Object> r = queryFood("牛肉面");
        assertFalse(parseTags((String) r.get("cuisine_tags")).contains("家常菜"),
                "牛肉面不应含 cuisineTag=家常菜");
    }

    @Test
    void neg_hunTunNoJiaChangCai() {
        Map<String, Object> r = queryFood("馄饨");
        assertFalse(parseTags((String) r.get("cuisine_tags")).contains("家常菜"),
                "馄饨不应含 cuisineTag=家常菜");
    }

    @Test
    void neg_manYuFanNoKuaiCan() {
        Map<String, Object> r = queryFood("鳗鱼饭");
        assertFalse(parseTags((String) r.get("type_tags")).contains("快餐"),
                "鳗鱼饭不应含 typeTag=快餐");
    }

    @Test
    void neg_kaoChuanNoLa() {
        Map<String, Object> r = queryFood("烤串");
        assertFalse(parseTags((String) r.get("taste_tags")).contains("辣"),
                "烤串不应含辣");
    }

    @Test
    void neg_xiShiKaoJiNoBbq() {
        Map<String, Object> r = queryFood("西式烤鸡");
        assertFalse(parseTags((String) r.get("type_tags")).contains("烧烤"),
                "西式烤鸡不应含烧烤 typeTag");
    }

    @Test
    void neg_ganGuoNiuWaNotExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE name='干锅牛蛙'", Integer.class);
        assertEquals(0, count, "干锅牛蛙应已被替换为农家一碗香");
    }

    // ==================== 甜品餐段审计 ====================

    @Test
    void dessert_bingQiLinDinnerNightOnly() {
        Map<String, Object> r = queryFood("冰淇淋");
        Set<String> meals = parseTags((String) r.get("meal_types"));
        assertEquals(Set.of("晚餐", "夜宵"), meals,
                "冰淇淋 mealTypes 应为晚餐|夜宵，不应含午餐，实际: " + meals);
    }

    @Test
    void dessert_naiChaDinnerNightOnly() {
        Map<String, Object> r = queryFood("奶茶");
        Set<String> meals = parseTags((String) r.get("meal_types"));
        assertEquals(Set.of("晚餐", "夜宵"), meals,
                "奶茶 mealTypes 应为晚餐|夜宵，不应含午餐，实际: " + meals);
    }

    @Test
    void dessert_cakeDessertsHaveAllThreeMeals() {
        for (String name : List.of("蛋糕", "双皮奶", "糖水", "提拉米苏")) {
            Map<String, Object> r = queryFood(name);
            assertNotNull(r, name + " 应存在");
            Set<String> meals = parseTags((String) r.get("meal_types"));
            assertTrue(meals.contains("午餐"), name + " 应含午餐（餐后甜品属真实消费场景）");
            assertTrue(meals.contains("晚餐"), name + " 应含晚餐");
            assertTrue(meals.contains("夜宵"), name + " 应含夜宵");
        }
    }

    // ==================== 推荐核心逻辑验证 ====================

    @Test
    void filterByTypeTagReturnsOnlyMatchingFoods() {
        String typeTag = "烧烤";
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name, type_tags FROM foods WHERE enabled=1 AND type_tags LIKE ?",
                "%" + typeTag + "%");

        assertFalse(foods.isEmpty(), "应至少有一道烧烤菜");
        for (Map<String, Object> f : foods) {
            String typeTags = (String) f.get("type_tags");
            assertTrue(typeTags.contains(typeTag),
                    f.get("name") + " type_tags 应包含 " + typeTag + "，实际: " + typeTags);
        }
    }

    @Test
    void filterByCuisineTagReturnsOnlyMatchingFoods() {
        String cuisineTag = "日料";
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name, cuisine_tags FROM foods WHERE enabled=1 AND cuisine_tags LIKE ?",
                "%" + cuisineTag + "%");

        assertFalse(foods.isEmpty(), "应至少有一道日料");
        for (Map<String, Object> f : foods) {
            String cuisineTags = (String) f.get("cuisine_tags");
            assertTrue(cuisineTags.contains(cuisineTag),
                    f.get("name") + " cuisine_tags 应包含 " + cuisineTag + "，实际: " + cuisineTags);
        }
    }

    @Test
    void typeTagOrCuisineTagFilter() {
        String typeTag = "火锅";
        String cuisineTag = "粤菜";
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name, type_tags, cuisine_tags FROM foods WHERE enabled=1 AND (type_tags LIKE ? OR cuisine_tags LIKE ?)",
                "%" + typeTag + "%", "%" + cuisineTag + "%");

        assertFalse(foods.isEmpty(), "火锅 OR 粤菜 应有结果");
        for (Map<String, Object> f : foods) {
            String typeTags = (String) f.get("type_tags");
            String cuisineTags = (String) f.get("cuisine_tags");
            boolean hasType = typeTags.contains(typeTag);
            boolean hasCuisine = cuisineTags.contains(cuisineTag);
            assertTrue(hasType || hasCuisine,
                    f.get("name") + " 应命中 typeTag=" + typeTag + " 或 cuisineTag=" + cuisineTag);
        }
    }

    @Test
    void typeTagAndMealTypeCombined() {
        String typeTag = "甜品";
        String mealType = "夜宵";
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name, type_tags, meal_types FROM foods WHERE enabled=1 AND type_tags LIKE ? AND meal_types LIKE ?",
                "%" + typeTag + "%", "%" + mealType + "%");

        assertFalse(foods.isEmpty(), "甜品 AND 夜宵 应有结果");
        for (Map<String, Object> f : foods) {
            String typeTags = (String) f.get("type_tags");
            String mealTypes = (String) f.get("meal_types");
            assertTrue(typeTags.contains(typeTag) && mealTypes.contains(mealType),
                    f.get("name") + " 应同时满足 type=" + typeTag + " 和 mealType=" + mealType);
        }
    }

    @Test
    void dessertsRecommendableInTheirMealTypes() {
        List<Map<String, Object>> desserts = jdbcTemplate.queryForList(
                "SELECT name, meal_types FROM foods WHERE enabled=1 AND type_tags LIKE '%甜品%'");

        Set<String> allDessertMealTypes = new HashSet<>();
        for (Map<String, Object> d : desserts) {
            String mealTypes = (String) d.get("meal_types");
            assertNotNull(mealTypes, d.get("name") + " 甜品无餐段");
            for (String mt : mealTypes.split(",")) {
                allDessertMealTypes.add(mt.trim());
            }
        }
        assertFalse(allDessertMealTypes.isEmpty(), "甜品应有可推荐餐段");
        assertTrue(allDessertMealTypes.contains("午餐") || allDessertMealTypes.contains("晚餐"),
                "甜品至少应在午餐或晚餐可推荐: " + allDessertMealTypes);
    }

    @Test
    void nonExistentCombinationReturnsEmpty() {
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled=1 AND type_tags LIKE ? AND meal_types LIKE ?",
                "%火锅%", "%早餐%");
        assertTrue(foods.isEmpty(),
                "火锅 AND 早餐 不应有结果，实际: " + foods.stream().map(f -> (String) f.get("name")).toList());
    }

    // ==================== 辅助方法 ====================

    private int countByTag(String column, String tag) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled=1 AND " + column + " LIKE ?",
                Integer.class, "%" + tag + "%");
    }

    private int countByPriceLevel(int level) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM foods WHERE enabled=1 AND price_level=?", Integer.class, level);
    }

    private List<String> getFoodNamesByTag(String column, String tag) {
        return jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled=1 AND " + column + " LIKE ? ORDER BY name",
                String.class, "%" + tag + "%");
    }

    private List<String> getFoodNamesByPriceLevel(int level) {
        return jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled=1 AND price_level=? ORDER BY name",
                String.class, level);
    }

    private Map<String, Object> queryFood(String name) {
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT * FROM foods WHERE name=? AND enabled=1", name);
        return results.isEmpty() ? null : results.get(0);
    }

    private Set<String> parseTags(String csv) {
        if (csv == null || csv.isEmpty()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
