# 菜品数据管线

> 让以后新增或修改菜品主要通过编辑 CSV 完成，而不是手写大量 SQL、Java 特例和逐道测试。

## 概述

```
data/foods.csv           ← 菜品内容的唯一维护来源（人类编辑）
    ↓
scripts/validate-foods.mjs   ← 校验脚本（21 条规则）
    ↓
scripts/generate-food-migration.mjs  ← 生成 Flyway migration SQL（V8+）
    ↓
backend-java/src/main/resources/db/migration/V*.sql  ← 提交 Git，Flyway 执行
    ↓
Java 推荐逻辑              ← 只读数据库，不读 CSV
```

## 文件清单

| 文件 | 用途 |
|------|------|
| `data/foods.csv` | 菜品基线数据，UTF-8 无 BOM，竖线分隔多值字段 |
| `data/recommend-taxonomy.json` | 统一词典，Java RecommendDict 的 JSON 副本 |
| `scripts/lib/foods-data.mjs` | 公共模块：解析、校验、SQL 生成 |
| `scripts/validate-foods.mjs` | CLI 校验入口 |
| `scripts/generate-food-migration.mjs` | CLI 生成入口 |
| `scripts/test/foods-data.test.mjs` | Node 自动化测试 |
| `package.json` | 项目命令 |
| `backend-java/.../common/TestFoodDataInitializer.java` | 测试环境从 CSV 自动导入 H2 |

## 如何新增菜品

1. 编辑 `data/foods.csv`，按 Unicode 顺序插入新行。
2. 运行校验：
   ```bash
   npm run foods:validate
   ```
3. 修复所有校验错误后，运行测试：
   ```bash
   npm run foods:test
   cd backend-java
   .\mvnw.cmd clean test
   ```
4. 生成 migration（**必须使用 V8 及以上版本号**）：
   ```bash
   npm run foods:generate -- V8 add_spicy_snacks
   ```
5. 审查生成的 SQL 文件。
6. 提交 CSV 和生成的 migration 到 Git。
7. 本地启动 MySQL，让 Flyway 执行新 migration。
8. 抽样验证 foods 和 recommend API。

**注意**：新增菜品不需要修改 `h2-init.sql`。测试环境的 H2 数据由 `TestFoodDataInitializer` 从 CSV 自动导入。

## 如何修改菜品

1. 编辑 `data/foods.csv` 中对应菜品的行。
2. 运行 `npm run foods:validate` 确认校验通过。
3. 运行 `npm run foods:test` + Maven test 确认一致性。
4. 生成 migration。
5. 部署后 Flyway 执行 `INSERT ... ON DUPLICATE KEY UPDATE` 更新数据。

## 如何禁用菜品

将目标菜品的 `enabled` 列从 `true` 改为 `false`。

**不要删除行。** 禁用菜品保留在 CSV 和历史数据中，推荐算法会自动排除 `enabled=false` 的菜品。

## 多值字段为什么使用 |

- CSV 标准分隔符是逗号，多值字段嵌套逗号会导致转义复杂度大幅增加。
- 竖线 `|` 在中文菜品数据中几乎不会出现，无需转义。
- 校验脚本检测到字段内逗号时报错退出，防止误用。

数据库内部仍然使用逗号分隔（历史兼容），生成器负责将竖线转换为逗号。

## 如何运行校验

```bash
npm run foods:validate
```

校验通过 → 退出码 0，输出 `✅ 校验通过：N 道菜全部合法`。
校验失败 → 退出码 1，输出所有错误（含行号、菜名、字段、原因）。

## 如何运行测试

```bash
npm run foods:test
cd backend-java
.\mvnw.cmd clean test
```

## 如何生成 migration

```bash
npm run foods:generate -- V8 <description>
```

示例：
```bash
npm run foods:generate -- V8 add_spicy_snacks
npm run foods:generate -- V9 update_noodle_prices
```

**版本号约束**：
- **V7** — 已保留给 `foods.name` 唯一索引（`V7__add_unique_index_to_foods_name.sql`），生成器会拒绝 V7。
- **V8+** — 菜品数据 migration 的合法版本号。使用 `INSERT ... ON DUPLICATE KEY UPDATE` 语法。

生成前会自动运行全套校验，校验失败不会生成文件。
目标文件已存在时拒绝覆盖。

## 为什么不能修改已执行 migration

Flyway 通过 checksum 检测 migration 是否被篡改。修改已执行的 migration 会导致 checksum mismatch，Flyway 启动失败。

正确做法：新增一个 migration，使用 `INSERT ... ON DUPLICATE KEY UPDATE` 覆盖数据。

## 为什么 CSV 不在运行时直接导入数据库

1. Flyway 提供可审计的版本历史，每次变更都有记录。
2. CSV 读取可能因文件系统问题失败，运行时依赖文件不如数据库可靠。
3. 推荐服务只需要数据库即可以高性能查询，不需要额外的 CSV 解析开销。
4. CSV → migration → Flyway → DB 的路径保证了开发、测试、生产环境的完全一致性。

## 如何审核生成的 SQL

1. 检查 version 号 >= V8 且 description 正确。
2. 确认所有 INSERT 都包含 8 个字段。
3. 确认使用 `AS new` 行别名 + `new.column` 赋值（MySQL 8 推荐写法）。
4. 确认**不包含** `VALUES(column)` 弃用语法。
5. 确认**不包含** `ALTER TABLE ... ADD UNIQUE KEY`（V7 已建立）。
6. 确认 `enabled=false` 的菜品没有遗漏。
7. 抽样对比 CSV 和 SQL 中的字段值。

## 删除菜品为什么使用 enabled=false

1. 保留历史数据，吃过记录可以继续关联到已禁用的菜品。
2. 避免 DELETE 导致的外键约束问题。
3. 支持「重新上架」场景：改回 `enabled=true` 即可。
4. CSV 中保留该行，维护完整的菜品演进历史。

## 生成后必须执行

```bash
npm run foods:validate
npm run foods:test
cd backend-java
.\mvnw.cmd clean test
```

全部通过后才能提交。

## foods.name 唯一约束

**V7**（`V7__add_unique_index_to_foods_name.sql`）专门建立：

```sql
ALTER TABLE `foods` ADD UNIQUE KEY `uk_foods_name` (`name`);
```

- V7 是独立 migration，不包含任何菜品数据变更。
- V1-V6 不做任何修改。
- H2 测试 schema（`h2-init.sql`）同步包含此约束。
- 如果数据库中存在重复 name，V7 执行时直接失败（预期行为）。

## MySQL 8 upsert 语法

生成器使用 MySQL 8 推荐的**行别名**写法：

```sql
INSERT INTO `foods` (`name`, `category`, ...) VALUES ('白切鸡', '粤菜', ...)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  ...;
```

**弃用**的写法（生成器不再输出）：
```sql
-- ❌ 弃用
ON DUPLICATE KEY UPDATE category = VALUES(category)
```

行别名语法在 MySQL 8.0.19+ 中推荐使用，`VALUES()` 函数已被标记为弃用。

## 新增菜品不需要修改 h2-init.sql

测试环境的 H2 数据由 `TestFoodDataInitializer` 在 Spring Context 启动时从 `data/foods.csv` 自动导入：

1. `h2-init.sql` 只保留 schema DDL 和非菜品必要数据。
2. `TestFoodDataInitializer`（`@Component @Profile("test")` `ApplicationRunner`）读取 CSV 并写入 H2。
3. 每个 Spring ApplicationContext 独立初始化，不使用 static 状态阻止。
4. `FoodDataPipelineConsistencyTest` 验证导入后的 H2 与 CSV 双向一致。
5. `FoodDataPipelineIsolationTest` 验证 @DirtiesContext 重建 Context 后数据仍然完整。

## 校验规则清单

| # | 规则 |
|:---:|------|
| 1 | CSV 表头完全一致，禁止缺列或多列 |
| 2 | 文件必须是 UTF-8 无 BOM |
| 3 | name 非空、唯一、前后无空格 |
| 4 | category 非空且前后无空格 |
| 5 | type_tags 和 cuisine_tags 至少一个非空 |
| 6 | type_tags 全属于词典 |
| 7 | cuisine_tags 全属于词典 |
| 8 | meal_types 全属于词典 |
| 9 | enabled=true 的菜品 meal_types 不得为空（白名单除外） |
| 10 | 空 meal_types 白名单当前只能是奶茶 |
| 11 | taste_tags 非空且全部属于词典 |
| 12 | 多值字段必须使用 `\|` |
| 13 | 多值字段不允许重复值 |
| 14 | 多值字段不允许空元素 |
| 15 | 多值字段每个值不允许前后空格 |
| 16 | price_level 只能是 1-4 |
| 17 | enabled 只能是 true 或 false |
| 18 | 至少 1 道启用菜（不硬编码数量） |
| 19 | 菜品数据与数据库一致（Java 测试验证） |
| 20 | 不允许微辣、重口、韩餐等废弃值 |
| 21 | CSV 必须按 name 的 Unicode 顺序排列 |
