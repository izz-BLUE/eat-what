package com.fantuan.eatwhat.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 TestFoodDataInitializer 在不同 Spring ApplicationContext 中独立运行。
 *
 * 测试顺序由 @Order 明确保证：
 *   - @Order(1) 方法在 Context-1 中运行（初始 Context）
 *   - @DirtiesContext 标记 Context-1 为脏
 *   - @Order(2) 方法在 Context-2 中运行（全新 Context，TestFoodDataInitializer 再次执行）
 *   - @Order(3) 验证唯一性
 *
 * 两个 Context 各有独立的 H2 内存实例，应各自拥有完整的 CSV 数据。
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FoodDataPipelineIsolationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Context-1：首次初始化，验证全量数据。
     * @DirtiesContext 标记当前 Context 脏，确保下一个方法获得全新 Context。
     */
    @Test
    @Order(1)
    @DirtiesContext
    void context1_hasFullCsvData() {
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled = 1 ORDER BY name");

        assertFalse(foods.isEmpty(), "Context-1 应包含 CSV 全量数据");
        assertTrue(foods.size() >= 30, "Context-1 期望 >=30 道菜，实际 " + foods.size());

        // 验证关键菜品存在（不硬编码全量，抽样验证即可）
        Set<String> names = foods.stream().map(r -> (String) r.get("name")).collect(java.util.stream.Collectors.toSet());
        assertTrue(names.contains("奶茶"), "应包含奶茶");
        assertTrue(names.contains("拉面"), "应包含拉面");
        assertTrue(names.contains("火锅"), "应包含火锅");
        assertTrue(names.contains("白切鸡"), "应包含白切鸡");
        assertTrue(names.contains("猪脚饭"), "应包含猪脚饭");

        System.out.println("✅ Context-1 验证通过：" + foods.size() + " 道菜");
    }

    /**
     * Context-2：因为 Context-1 被 @DirtiesContext 标记为脏，Spring 重建新 Context。
     * TestFoodDataInitializer 在 Context-2 中再次执行，应重新导入 CSV 全量数据。
     */
    @Test
    @Order(2)
    void context2_alsoHasFullCsvData() {
        List<Map<String, Object>> foods = jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled = 1 ORDER BY name");

        assertFalse(foods.isEmpty(), "Context-2 应包含 CSV 全量数据（不应被 static flag 阻止初始化）");
        assertTrue(foods.size() >= 30, "Context-2 期望 >=30 道菜，实际 " + foods.size());

        Set<String> names = foods.stream().map(r -> (String) r.get("name")).collect(java.util.stream.Collectors.toSet());
        assertTrue(names.contains("奶茶"), "应包含奶茶");
        assertTrue(names.contains("拉面"), "应包含拉面");
        assertTrue(names.contains("火锅"), "应包含火锅");
        assertTrue(names.contains("白切鸡"), "应包含白切鸡");
        assertTrue(names.contains("猪脚饭"), "应包含猪脚饭");

        System.out.println("✅ Context-2 验证通过：" + foods.size() + " 道菜（独立于 Context-1）");
    }

    /**
     * 验证 name 唯一性在独立 Context 中也成立。
     */
    @Test
    @Order(3)
    void namesAreUniqueInIsolatedContext() {
        List<String> names = jdbcTemplate.queryForList(
                "SELECT name FROM foods WHERE enabled = 1", String.class);

        Set<String> unique = Set.copyOf(names);
        assertEquals(unique.size(), names.size(),
                "独立 Context 中不应有重复 name: " + unique.size() + " vs " + names.size());

        System.out.println("✅ name 唯一性验证通过：" + unique.size() + " 个唯一名称");
    }
}
