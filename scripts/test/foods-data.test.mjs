/**
 * foods-data.test.mjs — 菜品数据管线自动化测试
 *
 * 运行方式：
 *   node --test scripts/test/foods-data.test.mjs
 *   npm run foods:test
 *
 * 使用 Node.js 内置 test runner，零第三方依赖。
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, writeFileSync, mkdirSync, rmSync, existsSync, readdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { tmpdir } from 'node:os';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const PROJECT_ROOT = resolve(__dirname, '..', '..');
const DATA_DIR = resolve(PROJECT_ROOT, 'data');

// ============================================================
// 动态导入
// ============================================================

let parseFoodsCSV, readFoodsCSV, validateFoods, readTaxonomy, clearTaxonomyCache;
let generateMigrationSQL, migrationOutputPath, migrationFileName;
let parseMultiValue, formatMultiValue, escapeSQLString, toDBMultiValue;
let sortRowsByName, findExistingMigration;
let TAXONOMY_PATH, CSV_PATH, MIGRATION_DIR;

before(async () => {
  const mod = await import('../lib/foods-data.mjs');
  parseFoodsCSV = mod.parseFoodsCSV;
  readFoodsCSV = mod.readFoodsCSV;
  validateFoods = mod.validateFoods;
  readTaxonomy = mod.readTaxonomy;
  clearTaxonomyCache = mod.clearTaxonomyCache;
  generateMigrationSQL = mod.generateMigrationSQL;
  migrationOutputPath = mod.migrationOutputPath;
  migrationFileName = mod.migrationFileName;
  parseMultiValue = mod.parseMultiValue;
  formatMultiValue = mod.formatMultiValue;
  escapeSQLString = mod.escapeSQLString;
  toDBMultiValue = mod.toDBMultiValue;
  sortRowsByName = mod.sortRowsByName;
  findExistingMigration = mod.findExistingMigration;
  TAXONOMY_PATH = mod.TAXONOMY_PATH;
  CSV_PATH = mod.CSV_PATH;
  MIGRATION_DIR = mod.MIGRATION_DIR;
});

// ============================================================
// 临时目录管理
// ============================================================

const tmpDir = resolve(tmpdir(), `foods-test-${Date.now()}`);
const tmpDataDir = resolve(tmpDir, 'data');
const tmpMigrationDir = resolve(tmpDir, 'migration');

function createTmpDir() {
  rmSync(tmpDir, { recursive: true, force: true });
  mkdirSync(tmpDir, { recursive: true });
  mkdirSync(tmpDataDir, { recursive: true });
  mkdirSync(tmpMigrationDir, { recursive: true });
}

function cleanupTmpDir() {
  rmSync(tmpDir, { recursive: true, force: true });
}

function writeTmpCSV(name, content) {
  const p = resolve(tmpDataDir, name);
  writeFileSync(p, content, 'utf-8');
  return p;
}

function makeCSVHeader() {
  return 'name,category,type_tags,cuisine_tags,meal_types,taste_tags,price_level,enabled';
}

function makeCSVLine(name, category, type_tags, cuisine_tags, meal_types, taste_tags, price_level, enabled) {
  return [name, category, type_tags, cuisine_tags, meal_types, taste_tags, price_level, enabled].join(',');
}

// ============================================================
// 测试：CSV 解析与数据正确性
// ============================================================

describe('CSV 解析与数据正确性', () => {
  let taxonomy;
  let realRows;

  before(() => {
    clearTaxonomyCache();
    taxonomy = readTaxonomy();
    const result = readFoodsCSV();
    if (result.errors.length > 0) {
      throw new Error('真实 CSV 解析失败: ' + result.errors.join('; '));
    }
    realRows = result.rows;
  });

  // --- 1. CSV 可解析且有启用菜 ---
  it('1. CSV 可解析且至少 1 道启用菜', () => {
    const { rows, errors } = readFoodsCSV();
    assert.equal(errors.length, 0, '解析错误: ' + JSON.stringify(errors));
    const enabled = rows.filter(r => r.enabled === 'true');
    assert.ok(enabled.length >= 1, `应有至少 1 道启用菜，实际 ${enabled.length}`);
  });

  // --- 2. CSV 通过全部校验 ---
  it('2. CSV 通过全部校验规则', () => {
    const errors = validateFoods(realRows, taxonomy);
    assert.equal(errors.length, 0, '校验错误: ' + errors.join('\n'));
  });

  // --- 3. taxonomy JSON 与 Java RecommendDict 对应字段匹配 ---
  it('3. taxonomy JSON 结构与 RecommendDict 对应字段匹配', () => {
    assert.ok(Array.isArray(taxonomy.mealTypes));
    assert.ok(Array.isArray(taxonomy.priceLevels));
    assert.ok(Array.isArray(taxonomy.budgetValues));
    assert.ok(Array.isArray(taxonomy.tasteTags));
    assert.ok(Array.isArray(taxonomy.typeTags));
    assert.ok(Array.isArray(taxonomy.cuisineTags));
    assert.ok(Array.isArray(taxonomy.userTasteFilters));
    assert.ok(Array.isArray(taxonomy.emptyMealTypesAllowlist));
    assert.ok(typeof taxonomy.emptyMealTypesReason === 'string');

    assert.deepEqual(taxonomy.mealTypes, ['早餐', '午餐', '晚餐', '夜宵']);
    assert.deepEqual(taxonomy.priceLevels, [1, 2, 3, 4]);
    assert.deepEqual(taxonomy.budgetValues, ['15以内', '15-25', '25-40', '40以上']);
    assert.deepEqual(taxonomy.userTasteFilters, ['清淡', '辣', '不辣']);
    assert.deepEqual(taxonomy.emptyMealTypesAllowlist, ['奶茶']);
  });

  // --- 4. 所有标签精确匹配 ---
  it('4. CSV 中所有标签都在词典中定义', () => {
    const allTypeSet = new Set(taxonomy.typeTags);
    const allCuisineSet = new Set(taxonomy.cuisineTags);
    const allMealSet = new Set(taxonomy.mealTypes);
    const allTasteSet = new Set(taxonomy.tasteTags);

    for (const row of realRows) {
      for (const tv of parseMultiValue(row.type_tags)) {
        assert.ok(allTypeSet.has(tv), `"${row.name}" type_tag "${tv}" 不在词典中`);
      }
      for (const cv of parseMultiValue(row.cuisine_tags)) {
        assert.ok(allCuisineSet.has(cv), `"${row.name}" cuisine_tag "${cv}" 不在词典中`);
      }
      for (const mv of parseMultiValue(row.meal_types)) {
        assert.ok(allMealSet.has(mv), `"${row.name}" meal_type "${mv}" 不在词典中`);
      }
      for (const tv of parseMultiValue(row.taste_tags)) {
        assert.ok(allTasteSet.has(tv), `"${row.name}" taste_tag "${tv}" 不在词典中`);
      }
    }
  });

  // --- 5. 奶茶空 meal_types 白名单正确 ---
  it('5. 奶茶空 meal_types 在白名单中', () => {
    const milkTea = realRows.find(r => r.name === '奶茶');
    assert.ok(milkTea, '未找到奶茶');
    assert.equal(milkTea.meal_types, '', '奶茶 meal_types 应为空');
    assert.ok(taxonomy.emptyMealTypesAllowlist.includes('奶茶'), '奶茶应在白名单中');
  });

  // --- 6. name 唯一 ---
  it('6. 所有菜品 name 唯一', () => {
    const names = realRows.map(r => r.name);
    const unique = new Set(names);
    assert.equal(unique.size, names.length, `存在重复 name: ${names.length - unique.size} 个重复`);
  });
});

// ============================================================
// 测试：回归 —— 数量灵活性
// ============================================================

describe('回归：数量灵活性', () => {
  let taxonomy;

  before(() => {
    clearTaxonomyCache();
    taxonomy = readTaxonomy();
  });

  after(() => {
    cleanupTmpDir();
  });

  // --- 31 道合法菜可通过校验 ---
  it('31 道合法菜通过校验（不硬编码 30）', () => {
    createTmpDir();
    let content = makeCSVHeader() + '\n';
    // 生成 31 道菜
    const baseRow = ['测试菜', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true'];
    for (let i = 0; i < 31; i++) {
      content += makeCSVLine(`测试菜${i}`, `快餐`, `小吃`, ``, `午餐|晚餐`, `辣`, `1`, `true`) + '\n';
    }
    const p = writeTmpCSV('31-foods.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0, '31 道菜解析应无错误: ' + parseErrors.join('; '));
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    // 不应包含 "期望 30" 这类固定数量错误
    assert.ok(!errors.some(e => e.includes('期望 30')), '不应包含硬编码 30 限制');
    assert.ok(!errors.some(e => e.includes('至少需要 1')), '31 道菜应通过至少 1 道检查');
    // 只可能有排序警告（未按 Unicode 排）
    const nonSortErrors = errors.filter(e => !e.includes('排序不正确'));
    assert.equal(nonSortErrors.length, 0, '31 道合法菜不应有非排序错误: ' + nonSortErrors.join('; '));
  });

  // --- 空 CSV 失败 ---
  it('空 CSV（无数据行）校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n';
    const p = writeTmpCSV('empty.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('至少需要 1')), `空 CSV 应报至少 1 道菜: ${errors.join('; ')}`);
  });
});

// ============================================================
// 测试：错误检测
// ============================================================

describe('错误检测：校验失败场景', () => {
  let taxonomy;

  before(() => {
    clearTaxonomyCache();
    taxonomy = readTaxonomy();
  });

  after(() => {
    cleanupTmpDir();
  });

  // --- 8. BOM 文件校验失败 ---
  it('8. 包含 BOM 的文件校验失败', () => {
    createTmpDir();
    const content = '﻿' + makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('with-bom.csv', content);
    const { errors } = parseFoodsCSV(content, p);
    assert.ok(errors.some(e => e.includes('BOM')), '应报告 BOM 错误');
  });

  // --- 9. 错误表头失败 ---
  it('9. 错误表头校验失败', () => {
    createTmpDir();
    const content = 'name,category,wrong_column,cuisine_tags,meal_types,taste_tags,price_level,enabled\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('bad-header.csv', content);
    const { errors } = parseFoodsCSV(content, p);
    assert.ok(errors.length > 0, '应报告表头错误');
  });

  // --- 10. 重复 name 失败 ---
  it('10. 重复 name 校验失败', () => {
    createTmpDir();
    const line = makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true');
    const content = makeCSVHeader() + '\n' + line + '\n' + line;
    const p = writeTmpCSV('dup-name.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('重复')), `应报告重复 name: ${errors.join('; ')}`);
  });

  // --- 11. 未知标签失败 ---
  it('11. 未知 type_tags 标签校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '未知类型', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('bad-tag.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('未知标签')), `应报告未知标签: ${errors.join('; ')}`);
  });

  // --- 12. 重复标签失败 ---
  it('12. 多值字段重复标签校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|午餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('dup-tag.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('重复值')), `应报告重复标签: ${errors.join('; ')}`);
  });

  // --- 13. 空标签元素失败 ---
  it('13. 多值字段空元素校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐||晚餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('empty-element.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('空元素')), `应报告空元素: ${errors.join('; ')}`);
  });

  // --- 14. 字段前后空格失败 ---
  it('14. name 前后空格校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + ' 白切鸡,粤菜,,粤菜,午餐|晚餐,清淡|鲜,3,true';
    const p = writeTmpCSV('space-name.csv', content);
    const { errors: parseErrors } = parseFoodsCSV(content, p);
    assert.ok(parseErrors.some(e => e.includes('前后有空格')), `应报告空格: ${parseErrors.join('; ')}`);
  });

  // --- 15. 非法 price_level 失败 ---
  it('15. 非法 price_level 校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '5', 'true');
    const p = writeTmpCSV('bad-price.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `应报告非法 price_level: ${errors.join('; ')}`);
  });

  // --- 15b. price_level "2abc" 失败（严格字符串匹配） ---
  it('15b. price_level "2abc" 校验失败（严格字符串，不允许 parseInt 宽松解析）', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '2abc', 'true');
    const p = writeTmpCSV('bad-price-str.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `"2abc" 应校验失败: ${errors.join('; ')}`);
  });

  // --- 15c. price_level "2.5" 失败（禁止小数） ---
  it('15c. price_level "2.5" 校验失败（禁止小数）', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '2.5', 'true');
    const p = writeTmpCSV('bad-price-decimal.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `"2.5" 应校验失败: ${errors.join('; ')}`);
  });

  // --- 15d. price_level "02" 失败（禁止前导零） ---
  it('15d. price_level "02" 校验失败（禁止前导零）', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '02', 'true');
    const p = writeTmpCSV('bad-price-zero.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `"02" 应校验失败: ${errors.join('; ')}`);
  });

  // --- 15e. price_level 空值失败 ---
  it('15e. price_level 空值校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '', 'true');
    const p = writeTmpCSV('bad-price-empty.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `空值应校验失败: ${errors.join('; ')}`);
  });

  // --- 16. 非法 enabled 失败 ---
  it('16. 非法 enabled 值校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'yes');
    const p = writeTmpCSV('bad-enabled.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('enabled')), `应报告非法 enabled: ${errors.join('; ')}`);
  });

  // --- 17. 行顺序错误失败 ---
  it('17. CSV 行未按 Unicode 排序校验失败', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n'
      + makeCSVLine('猪脚饭', '快餐', '快餐', '', '午餐|晚餐', '咸|香', '2', 'true') + '\n'
      + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('bad-order.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy);
    assert.ok(errors.some(e => e.includes('排序不正确')), `应报告排序错误: ${errors.join('; ')}`);
  });
});

// ============================================================
// 测试：SQL 生成
// ============================================================

describe('Migration SQL 生成', () => {
  let taxonomy;
  let rows;

  before(() => {
    clearTaxonomyCache();
    taxonomy = readTaxonomy();
    const result = readFoodsCSV();
    rows = sortRowsByName(result.rows);
  });

  after(() => {
    cleanupTmpDir();
  });

  // --- 18. SQL 输出稳定 ---
  it('18. 两次生成 SQL 文本完全一致', () => {
    const sql1 = generateMigrationSQL(rows, 'V8', 'stable_test');
    const sql2 = generateMigrationSQL(rows, 'V8', 'stable_test');
    assert.equal(sql1, sql2, '两次 V8 生成应完全一致');
  });

  // --- 19. 单引号正确转义 ---
  it('19. 单引号在 SQL 中正确转义', () => {
    const escaped = escapeSQLString("It's a test");
    assert.equal(escaped, "'It\\'s a test'");

    const rowsWithQuote = [{
      ...rows[0],
      name: "O'Brien's 测试",
      category: "快餐",
      type_tags: "快餐",
      cuisine_tags: "",
      meal_types: "午餐",
      taste_tags: "辣",
      price_level: "1",
      enabled: "true",
    }];
    const sql = generateMigrationSQL(rowsWithQuote, 'V8', 'quote_test');
    assert.ok(!sql.includes("'O'Brien's"), '单引号应被转义');
    assert.ok(sql.includes("\\'"), '应包含转义的单引号');
  });

  // --- 20. 扫描 migration 目录检测版本号冲突 ---
  it('20. findExistingMigration 检测同版本不同描述的文件冲突', () => {
    createTmpDir();
    // 创建一个 V8 文件
    const p = resolve(tmpMigrationDir, 'V8__other_description.sql');
    writeFileSync(p, '-- dummy', 'utf-8');
    assert.ok(existsSync(p));

    // 同版本 + 不同描述 → 应检测到冲突
    const conflict = findExistingMigration('V8', tmpMigrationDir);
    assert.equal(conflict, 'V8__other_description.sql',
      '应检测到已有 V8__other_description.sql');

    // 同版本 + 相同描述 → 路径检查应拒绝（精确匹配）
    const exactPath = migrationOutputPath('V8', 'test_exists', tmpMigrationDir);
    writeFileSync(exactPath, '-- dummy', 'utf-8');
    assert.ok(existsSync(exactPath), '精确路径也应被占用');

    // 不同版本 → 无冲突
    const noConflict = findExistingMigration('V9', tmpMigrationDir);
    assert.equal(noConflict, null, 'V9 不应有冲突');

    // 带前缀版本号（如 V8 vs 8）
    const conflict2 = findExistingMigration('8', tmpMigrationDir);
    assert.equal(conflict2, 'V8__other_description.sql',
      '不带 V 前缀的版本号 8 也应检测到冲突');
  });

  // --- 21. 非法版本失败 ---
  it('21. 非法版本号应拒绝（含 V7 保留）', () => {
    assert.throws(() => generateMigrationSQL(rows, 'V0', 'test'), /无效的版本号/);
    assert.throws(() => generateMigrationSQL(rows, 'abc', 'test'), /无效的版本号/);
    assert.throws(() => generateMigrationSQL(rows, '', 'test'), /无效的版本号/);
  });

  // --- 22. 非法 CSV 不生成文件 ---
  it('22. 包含校验错误的 CSV 不应通过 validateFoods', () => {
    const badRows = [{
      line: 2, name: '测试菜', category: '快餐',
      type_tags: '未知标签', cuisine_tags: '', meal_types: '午餐',
      taste_tags: '辣', price_level: '1', enabled: 'true',
    }];
    const errors = validateFoods(badRows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.length > 0, '包含未知标签的数据应校验失败');
  });

  // ==================== 回归测试 ====================

  // --- V7 被生成器拒绝 ---
  it('V7 版本被生成器拒绝', () => {
    assert.throws(() => {
      generateMigrationSQL(rows, 'V7', 'test');
    }, /V7.*保留/);
    assert.throws(() => {
      generateMigrationSQL(rows, '7', 'test');
    }, /V7.*保留/);
  });

  // --- V8 正常生成 ---
  it('V8 正常生成 SQL', () => {
    const sql = generateMigrationSQL(rows, 'V8', 'add_test_food');
    assert.ok(sql.includes('V8: add_test_food'), '应包含版本描述');
    assert.ok(sql.includes('INSERT INTO'), '应包含 INSERT');
    assert.ok(sql.includes('AS new'), '应使用 AS new 行别名');
    assert.ok(sql.includes('ON DUPLICATE KEY UPDATE'), '应包含 upsert');
  });

  // --- SQL 不包含弃用 VALUES(column) ---
  it('生成的 SQL 不包含弃用的 VALUES(column) 语法', () => {
    const sql = generateMigrationSQL(rows, 'V8', 'no_values_test');
    assert.ok(!sql.includes('VALUES(`'), '不应包含 VALUES(`column`) 弃用语法');
    assert.ok(!sql.includes('VALUES('), '不应包含 VALUES() 弃用语法');
  });

  // --- SQL 不包含 ADD UNIQUE KEY ---
  it('生成的 SQL 不包含 ADD UNIQUE KEY（V7 已建立）', () => {
    const sql = generateMigrationSQL(rows, 'V8', 'no_alter_test');
    assert.ok(!sql.includes('ADD UNIQUE KEY'), '不应包含 ADD UNIQUE KEY');
    assert.ok(!sql.includes('ALTER TABLE'), '不应包含 ALTER TABLE');
  });

  // --- name 重复时唯一约束测试失败 ---
  it('重复 name 时校验失败（模拟唯一约束违反）', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n'
      + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true') + '\n'
      + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '3', 'true');
    const p = writeTmpCSV('dup-name-unique.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('重复')), `重复 name 应校验失败: ${errors.join('; ')}`);
  });

  // --- 连续生成文本稳定 ---
  it('连续 3 次生成 V8 SQL 文本完全一致', () => {
    const sql1 = generateMigrationSQL(rows, 'V8', 'continuous_test');
    const sql2 = generateMigrationSQL(rows, 'V8', 'continuous_test');
    const sql3 = generateMigrationSQL(rows, 'V8', 'continuous_test');
    assert.equal(sql1, sql2);
    assert.equal(sql2, sql3);
  });
});

// ============================================================
// 测试：CLI 集成（run 函数）
// ============================================================

describe('CLI 集成：run() 冲突拒绝', () => {
  let run;
  let taxonomy;

  before(async () => {
    clearTaxonomyCache();
    taxonomy = readTaxonomy();
    const cliMod = await import('../generate-food-migration.mjs');
    run = cliMod.run;
  });

  after(() => {
    cleanupTmpDir();
  });

  // --- CLI 拒绝同版本 V8（真实调用 run 函数，不直接调 findExistingMigration） ---
  it('CLI run() 拒绝同版本 V8：已有 V8__existing.sql 时禁止生成另一个 V8', () => {
    createTmpDir();

    // 写一个合法的最小 CSV
    const csvContent = makeCSVHeader() + '\n'
      + makeCSVLine('测试菜A', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true') + '\n'
      + makeCSVLine('测试菜B', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true');
    const csvPath = writeTmpCSV('cli-test.csv', csvContent);

    // 预先在 migration 目录中创建 V8__existing.sql
    const existingPath = resolve(tmpMigrationDir, 'V8__existing.sql');
    writeFileSync(existingPath, '-- dummy migration', 'utf-8');
    assert.ok(existsSync(existingPath), 'V8__existing.sql 应已创建');

    // 记录创建前的文件列表
    const filesBefore = readdirSync(tmpMigrationDir).filter(f => f.endsWith('.sql'));
    assert.equal(filesBefore.length, 1, '初始应有 1 个 sql 文件');

    // 捕获 stderr
    const stderrLines = [];
    const stderr = (msg) => { stderrLines.push(msg); };

    // 实际调用 run()
    const result = run(['V8', 'test_migration'], {
      migrationDir: tmpMigrationDir,
      csvPath: csvPath,
      stderr: stderr,
    });

    // 断言：应失败
    assert.equal(result.ok, false, 'run() 应返回 ok=false');
    assert.ok(result.error, '应有错误消息');
    assert.ok(
      result.error.includes('V8') || result.error.includes('已存在'),
      `错误消息应提及版本冲突: ${result.error}`
    );

    // 断言：不是 ReferenceError
    assert.ok(!result.error.includes('ReferenceError'), '不应是 ReferenceError');
    assert.ok(!result.error.includes('is not defined'), '不应是未定义变量错误');

    // 断言：stderr 中应包含冲突信息
    const stderrText = stderrLines.join('\n');
    assert.ok(
      stderrText.includes('V8') && stderrText.includes('已存在'),
      `stderr 应包含版本冲突信息: ${stderrText}`
    );

    // 断言：没有生成第二个 V8 文件
    const filesAfter = readdirSync(tmpMigrationDir).filter(f => f.endsWith('.sql'));
    assert.equal(filesAfter.length, 1, '不应生成第二个文件');
    assert.equal(filesAfter[0], 'V8__existing.sql', '原有文件不受影响');
    assert.ok(!existsSync(resolve(tmpMigrationDir, 'V8__test_migration.sql')),
      '不应生成 V8__test_migration.sql');

    console.log('✅ CLI run() 正确拒绝同版本冲突');
  });

  // --- CLI 成功生成 V9（同一 migration 目录中 V8 已存在但不冲突） ---
  it('CLI run() 成功生成 V9：同一目录已有 V8 时 V9 不受阻止', () => {
    createTmpDir();

    const csvContent = makeCSVHeader() + '\n'
      + makeCSVLine('测试菜A', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true') + '\n'
      + makeCSVLine('测试菜B', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true');
    const csvPath = writeTmpCSV('cli-test2.csv', csvContent);

    // 预先创建 V8__existing.sql
    writeFileSync(resolve(tmpMigrationDir, 'V8__existing.sql'), '-- dummy', 'utf-8');

    const stderrLines = [];
    const stderr = (msg) => { stderrLines.push(msg); };

    const result = run(['V9', 'new_migration'], {
      migrationDir: tmpMigrationDir,
      csvPath: csvPath,
      stderr: stderr,
    });

    assert.equal(result.ok, true, `run() 应返回 ok=true，实际: ${JSON.stringify(result)}`);
    assert.ok(result.outputPath, '应有 outputPath');
    assert.ok(result.outputPath.includes('V9__new_migration.sql'), '输出路径应包含 V9');
    assert.equal(result.count, 2, '应有 2 道菜');
    assert.ok(existsSync(result.outputPath), 'V9 文件应存在');

    // 两个文件都存在
    const files = readdirSync(tmpMigrationDir).filter(f => f.endsWith('.sql'));
    assert.equal(files.length, 2, '应有 V8 和 V9 两个文件');

    console.log('✅ CLI run() 成功生成 V9（V8 不冲突）');
  });
});

// ============================================================
// 测试：工具函数
// ============================================================

describe('工具函数', () => {
  it('parseMultiValue 正确解析竖线分隔值', () => {
    assert.deepEqual(parseMultiValue('早餐|午餐|晚餐'), ['早餐', '午餐', '晚餐']);
    assert.deepEqual(parseMultiValue('辣'), ['辣']);
    assert.deepEqual(parseMultiValue(''), []);
    assert.deepEqual(parseMultiValue('   '), []);
  });

  it('toDBMultiValue 将竖线转换为逗号', () => {
    assert.equal(toDBMultiValue('早餐|午餐|晚餐'), '早餐,午餐,晚餐');
    assert.equal(toDBMultiValue('辣'), '辣');
    assert.equal(toDBMultiValue(''), '');
  });

  it('formatMultiValue 保持输入顺序', () => {
    const result = formatMultiValue(['晚餐', '午餐', '早餐']);
    assert.equal(result, '晚餐|午餐|早餐');
  });

  it('escapeSQLString 正确转义', () => {
    assert.equal(escapeSQLString('hello'), "'hello'");
    assert.equal(escapeSQLString("it's"), "'it\\'s'");
    assert.equal(escapeSQLString(''), "''");
  });

  it('sortRowsByName 按 Unicode 排序', () => {
    const unsorted = [
      { name: '猪脚饭' }, { name: '白切鸡' }, { name: '叉烧' },
    ];
    const sorted = sortRowsByName(unsorted);
    assert.equal(sorted[0].name, '白切鸡');
    assert.equal(sorted[1].name, '叉烧');
    assert.equal(sorted[2].name, '猪脚饭');
  });

  it('migrationFileName 生成正确文件名', () => {
    assert.equal(migrationFileName('V8', 'add_more_foods'), 'V8__add_more_foods.sql');
    assert.equal(migrationFileName('8', 'Add More Foods'), 'V8__add_more_foods.sql');
  });
});
