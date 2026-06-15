package com.fantuan.eatwhat.service;

import com.fantuan.eatwhat.common.RecommendDict;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜品数据管线一致性集成测试
 *
 * H2 数据由 TestFoodDataInitializer 从 CSV 导入，本测试验证：
 *   1. CSV 可被测试正常解析
 *   2. H2 中的启用菜与 CSV 双向按 name 对应（不硬编码数量）
 *   3. 逐字段一致：category, typeTags, cuisineTags, mealTypes, tasteTags, priceLevel, enabled
 *   4. RecommendDict 与 recommend-taxonomy.json 词典一致
 */
@SpringBootTest
@ActiveProfiles("test")
class FoodDataPipelineConsistencyTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<FoodRow> csvRows;
    private JsonNode taxonomyJson;

    /**
     * 从项目根目录解析 data/foods.csv 和 data/recommend-taxonomy.json。
     * Maven 测试的 user.dir 通常在 backend-java/，需要向上查找项目根目录。
     */
    private Path findProjectRoot() {
        Path dir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        // 向上查找直到发现 data/foods.csv 或 backend-java/pom.xml
        for (int i = 0; i < 5; i++) {
            if (Files.exists(dir.resolve("data/foods.csv")) && Files.exists(dir.resolve("backend-java/pom.xml"))) {
                return dir;
            }
            if (Files.exists(dir.resolve("pom.xml")) && Files.exists(dir.resolve("../data/foods.csv"))) {
                return dir.resolve("..").normalize();
            }
            dir = dir.getParent();
            if (dir == null) break;
        }
        // 最终 fallback：直接从 user.dir 推算
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        Path candidate = cwd.resolve("../data/foods.csv").normalize();
        if (Files.exists(candidate)) {
            return cwd.resolve("..").normalize();
        }
        return cwd;
    }

    @BeforeEach
    void loadCSVAndTaxonomy() throws Exception {
        Path projectRoot = findProjectRoot();

        // 读取 taxonomy JSON
        Path taxonomyPath = projectRoot.resolve("data/recommend-taxonomy.json");
        assertTrue(Files.exists(taxonomyPath),
                "recommend-taxonomy.json 不存在: " + taxonomyPath.toAbsolutePath());
        taxonomyJson = objectMapper.readTree(taxonomyPath.toFile());

        // 读取并解析 CSV
        Path csvPath = projectRoot.resolve("data/foods.csv");
        assertTrue(Files.exists(csvPath),
                "foods.csv 不存在: " + csvPath.toAbsolutePath());

        csvRows = new ArrayList<>();
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);

        // 检查 BOM
        String firstLine = lines.get(0);
        if (!firstLine.isEmpty() && firstLine.charAt(0) == '﻿') {
            fail("foods.csv 包含 BOM，必须使用 UTF-8 无 BOM");
        }

        // 校验表头
        assertEquals("name,category,type_tags,cuisine_tags,meal_types,taste_tags,price_level,enabled",
                firstLine.trim(), "CSV 表头不匹配");

        // 解析数据行（跳过空行）
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split(",", -1); // -1 保留尾部空串
            assertEquals(8, cols.length,
                    "第 " + (i + 1) + " 行列数不正确: " + line);

            FoodRow row = new FoodRow();
            row.name = cols[0].trim();
            row.category = cols[1].trim();
            row.typeTags = pipeToCsv(cols[2].trim());
            row.cuisineTags = pipeToCsv(cols[3].trim());
            row.mealTypes = pipeToCsv(cols[4].trim());
            row.tasteTags = pipeToCsv(cols[5].trim());
            row.priceLevel = Integer.parseInt(cols[6].trim());
            row.enabled = "true".equals(cols[7].trim());

            csvRows.add(row);
        }

        assertFalse(csvRows.isEmpty(),
                "CSV 应至少包含 1 行数据");
    }

    /**
     * 将竖线分隔的 CSV 字段转换为逗号分隔（数据库格式）。
     */
    private static String pipeToCsv(String val) {
        if (val == null || val.isEmpty()) return "";
        return String.join(",", val.split("\\|"));
    }

    /**
     * 测试 1-3：CSV 30 道菜与 H2 逐字段一致
     */
    @Test
    void csvMatchesH2FieldByField() {
        // 从 H2 获取所有启用菜品（按 name 排序）
        List<Map<String, Object>> h2Foods = jdbcTemplate.queryForList(
                "SELECT name, category, type_tags, cuisine_tags, meal_types, taste_tags, price_level, enabled " +
                "FROM foods WHERE enabled = 1 ORDER BY name");

        // 构建 name → H2 row 映射
        Map<String, Map<String, Object>> h2Map = new LinkedHashMap<>();
        for (Map<String, Object> h2r : h2Foods) {
            h2Map.put((String) h2r.get("name"), h2r);
        }

        // 按 name 排序 CSV 行（与 H2 ORDER BY name 对齐）
        List<FoodRow> sortedCsv = new ArrayList<>(csvRows);
        sortedCsv.sort(Comparator.comparing(r -> r.name));

        for (FoodRow csvRow : sortedCsv) {
            Map<String, Object> h2Row = h2Map.get(csvRow.name);
            assertNotNull(h2Row,
                    "CSV 中的 '" + csvRow.name + "' 在 H2 中不存在");

            assertEquals(csvRow.category, h2Row.get("category"),
                    csvRow.name + " category 不一致");
            assertEquals(csvRow.typeTags, h2Row.get("type_tags"),
                    csvRow.name + " type_tags 不一致");
            assertEquals(csvRow.cuisineTags, h2Row.get("cuisine_tags"),
                    csvRow.name + " cuisine_tags 不一致");
            assertEquals(csvRow.mealTypes, h2Row.get("meal_types"),
                    csvRow.name + " meal_types 不一致");
            assertEquals(csvRow.tasteTags, h2Row.get("taste_tags"),
                    csvRow.name + " taste_tags 不一致");
            assertEquals(csvRow.priceLevel, (int) h2Row.get("price_level"),
                    csvRow.name + " price_level 不一致");
            assertEquals(csvRow.enabled ? 1 : 0, (int) h2Row.get("enabled"),
                    csvRow.name + " enabled 不一致");
        }

        // 反向检查：H2 中的菜在 CSV 中也都存在
        for (String name : h2Map.keySet()) {
            boolean found = sortedCsv.stream().anyMatch(r -> r.name.equals(name));
            assertTrue(found, "H2 中的 '" + name + "' 在 CSV 中不存在");
        }

        System.out.println("✅ CSV 与 H2 逐字段验证通过：" + sortedCsv.size() + " 道菜全部一致");
    }

    /**
     * 测试 4：RecommendDict 与 recommend-taxonomy.json 词典一致
     */
    @Test
    void recommendDictMatchesTaxonomyJson() {
        // mealTypes
        List<String> jsonMealTypes = jsonArrayToList(taxonomyJson.get("mealTypes"));
        assertEquals(RecommendDict.MEAL_TYPES, jsonMealTypes,
                "mealTypes 不一致");

        // budgetValues ↔ RecommendDict.PRICE_LEVELS
        List<String> jsonBudgets = jsonArrayToList(taxonomyJson.get("budgetValues"));
        assertEquals(RecommendDict.PRICE_LEVELS, jsonBudgets,
                "budgetValues / PRICE_LEVELS 不一致");

        // userTasteFilters ↔ RecommendDict.TASTES
        List<String> jsonTastes = jsonArrayToList(taxonomyJson.get("userTasteFilters"));
        assertEquals(RecommendDict.TASTES, jsonTastes,
                "userTasteFilters / TASTES 不一致");

        // typeTags
        List<String> jsonTypeTags = jsonArrayToList(taxonomyJson.get("typeTags"));
        assertEquals(RecommendDict.TYPE_TAGS, jsonTypeTags,
                "typeTags 不一致");

        // cuisineTags
        List<String> jsonCuisineTags = jsonArrayToList(taxonomyJson.get("cuisineTags"));
        assertEquals(RecommendDict.CUISINE_TAGS, jsonCuisineTags,
                "cuisineTags 不一致");

        // tasteTags ↔ RecommendDict.BASE_TASTE_TAGS (Set → sorted List)
        List<String> jsonTasteTags = jsonArrayToList(taxonomyJson.get("tasteTags"));
        List<String> baseTasteTagsSorted = new ArrayList<>(RecommendDict.BASE_TASTE_TAGS);
        baseTasteTagsSorted.sort(Comparator.naturalOrder());
        jsonTasteTags.sort(Comparator.naturalOrder());
        assertEquals(baseTasteTagsSorted, jsonTasteTags,
                "tasteTags / BASE_TASTE_TAGS 不一致");

        System.out.println("✅ RecommendDict 与 recommend-taxonomy.json 词典完全一致");
    }

    /**
     * 测试 5：所有标签精确匹配（CSV 中标签全部在词典中）
     */
    @Test
    void allCsvTagsInTaxonomyDict() {
        Set<String> validTypeTags = new HashSet<>(RecommendDict.TYPE_TAGS);
        Set<String> validCuisineTags = new HashSet<>(RecommendDict.CUISINE_TAGS);
        Set<String> validMealTypes = new HashSet<>(RecommendDict.MEAL_TYPES);
        Set<String> validTasteTags = new HashSet<>(RecommendDict.BASE_TASTE_TAGS);

        for (FoodRow row : csvRows) {
            for (String tag : splitTags(row.typeTags)) {
                if (!tag.isEmpty()) {
                    assertTrue(validTypeTags.contains(tag),
                            row.name + " typeTags 含未知标签: " + tag);
                }
            }
            for (String tag : splitTags(row.cuisineTags)) {
                if (!tag.isEmpty()) {
                    assertTrue(validCuisineTags.contains(tag),
                            row.name + " cuisineTags 含未知标签: " + tag);
                }
            }
            for (String tag : splitTags(row.mealTypes)) {
                if (!tag.isEmpty()) {
                    assertTrue(validMealTypes.contains(tag),
                            row.name + " mealTypes 含未知标签: " + tag);
                }
            }
            for (String tag : splitTags(row.tasteTags)) {
                if (!tag.isEmpty()) {
                    assertTrue(validTasteTags.contains(tag),
                            row.name + " tasteTags 含未知标签: " + tag);
                }
            }
        }

        System.out.println("✅ 全部 CSV 标签均在词典中");
    }

    /**
     * 测试：奶茶空 meal_types 正确
     */
    @Test
    void milkTeaHasEmptyMealTypes() {
        FoodRow milkTea = csvRows.stream()
                .filter(r -> "奶茶".equals(r.name))
                .findFirst()
                .orElse(null);
        assertNotNull(milkTea, "CSV 中未找到奶茶");
        assertEquals("", milkTea.mealTypes, "奶茶 meal_types 应为空");

        // 验证白名单
        JsonNode allowlist = taxonomyJson.get("emptyMealTypesAllowlist");
        assertNotNull(allowlist, "taxonomy JSON 缺少 emptyMealTypesAllowlist");
        List<String> allowlistNames = jsonArrayToList(allowlist);
        assertTrue(allowlistNames.contains("奶茶"),
                "奶茶应在 emptyMealTypesAllowlist 中");

        System.out.println("✅ 奶茶空 meal_types 白名单验证通过");
    }

    /**
     * 测试：type_tags 和 cuisine_tags 至少一个非空
     */
    @Test
    void eachFoodHasTypeOrCuisine() {
        for (FoodRow row : csvRows) {
            boolean hasType = !row.typeTags.isEmpty();
            boolean hasCuisine = !row.cuisineTags.isEmpty();
            assertTrue(hasType || hasCuisine,
                    row.name + " type_tags 和 cuisine_tags 均为空");
        }
        System.out.println("✅ 每道菜至少有一个 type 或 cuisine 标签");
    }

    // ==================== 辅助方法 ====================

    private List<String> jsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private List<String> splitTags(String csv) {
        if (csv == null || csv.isEmpty()) return Collections.emptyList();
        return Arrays.asList(csv.split(","));
    }

    /**
     * CSV 行数据结构。
     */
    private static class FoodRow {
        String name;
        String category;
        String typeTags;
        String cuisineTags;
        String mealTypes;
        String tasteTags;
        int priceLevel;
        boolean enabled;
    }
}
