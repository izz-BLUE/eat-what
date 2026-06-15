package com.fantuan.eatwhat.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 测试环境 H2 菜品数据自动初始化器。
 *
 * 读取 data/foods.csv 并将数据写入 H2 内存数据库。
 * CSV 是测试菜品内容的唯一来源 —— 不再需要手工维护 h2-init.sql 中的菜品 INSERT。
 *
 * 仅在 "test" profile 下激活。使用 ApplicationRunner 确保在 Spring 上下文完全就绪后、
 * 任何测试方法执行前运行。
 *
 * 每个 Spring ApplicationContext 拥有独立的 H2 内存实例和独立的 Bean 实例，
 * ApplicationRunner 在每个 Context 启动时各执行一次。不使用 static 状态阻止初始化，
 * 确保不同 Context（例如 @DirtiesContext 重建的 Context）都能得到完整的 CSV 数据。
 */
@Component
@Profile("test")
public class TestFoodDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestFoodDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    /** 当前 Bean 实例是否已执行（单 Context 内防止重复，不使用 static） */
    private boolean initialized = false;

    public TestFoodDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (initialized) {
            log.debug("TestFoodDataInitializer 当前实例已执行过，跳过");
            return;
        }
        initialized = true;

        try {
            Path projectRoot = findProjectRoot();
            Path csvPath = projectRoot.resolve("data/foods.csv");

            if (!Files.exists(csvPath)) {
                log.warn("foods.csv 未找到: {}，跳过 H2 数据初始化", csvPath.toAbsolutePath());
                return;
            }

            List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
            if (lines.size() < 2) {
                log.warn("foods.csv 没有数据行，跳过 H2 数据初始化");
                return;
            }

            // 清空现有数据
            jdbcTemplate.execute("DELETE FROM foods");

            // 跳过表头，逐行插入
            int count = 0;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 8) continue;

                String name = cols[0].trim();
                String category = cols[1].trim();
                String typeTags = pipeToCsv(cols[2].trim());
                String cuisineTags = pipeToCsv(cols[3].trim());
                String mealTypes = pipeToCsv(cols[4].trim());
                String tasteTags = pipeToCsv(cols[5].trim());
                int priceLevel = Integer.parseInt(cols[6].trim());
                int enabled = "true".equals(cols[7].trim()) ? 1 : 0;

                jdbcTemplate.update(
                    "INSERT INTO foods (name, category, type_tags, cuisine_tags, meal_types, taste_tags, price_level, enabled) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    name, category, typeTags, cuisineTags, mealTypes, tasteTags, priceLevel, enabled
                );
                count++;
            }

            log.info("TestFoodDataInitializer: 从 {} 导入了 {} 道菜到 H2", csvPath.getFileName(), count);

        } catch (Exception e) {
            log.error("TestFoodDataInitializer 执行失败", e);
            throw new RuntimeException("无法从 CSV 初始化 H2 测试数据", e);
        }
    }

    /**
     * 将竖线分隔的 CSV 字段转换为逗号分隔（数据库格式）。
     */
    private static String pipeToCsv(String val) {
        if (val == null || val.isEmpty()) return "";
        return String.join(",", val.split("\\|"));
    }

    /**
     * 查找项目根目录。Maven 测试的 user.dir 通常是 backend-java/。
     */
    private static Path findProjectRoot() {
        Path dir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();

        for (int i = 0; i < 5; i++) {
            if (Files.exists(dir.resolve("data/foods.csv"))
                    && Files.exists(dir.resolve("backend-java/pom.xml"))) {
                return dir;
            }
            if (Files.exists(dir.resolve("pom.xml"))
                    && Files.exists(dir.resolve("../data/foods.csv"))) {
                return dir.resolve("..").normalize();
            }
            dir = dir.getParent();
            if (dir == null) break;
        }

        // fallback
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        return cwd.resolve("..").normalize();
    }
}
