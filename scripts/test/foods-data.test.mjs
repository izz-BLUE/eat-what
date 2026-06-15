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
// 辅助：动态计算覆盖统计
// ============================================================

/**
 * 从 parseFoodsCSV() 返回的数据动态计算完整覆盖矩阵。
 * 所有数字均从数据实时计算，严禁手工统计。
 */
function computeCoverageStats(enabledRows) {
  const taxonomy = readTaxonomy();

  const typeTagCounts = {};
  for (const tag of taxonomy.typeTags) {
    typeTagCounts[tag] = { count: 0, names: [] };
  }
  const cuisineTagCounts = {};
  for (const tag of taxonomy.cuisineTags) {
    cuisineTagCounts[tag] = { count: 0, names: [] };
  }
  const mealTypeCounts = {};
  for (const tag of taxonomy.mealTypes) {
    mealTypeCounts[tag] = { count: 0, names: [] };
  }
  const priceLevelCounts = { '1': { count: 0, names: [] }, '2': { count: 0, names: [] }, '3': { count: 0, names: [] }, '4': { count: 0, names: [] } };
  let qingdanCount = 0;
  let laCount = 0;
  let bulaCount = 0;

  for (const row of enabledRows) {
    const types = parseMultiValue(row.type_tags);
    const cuisines = parseMultiValue(row.cuisine_tags);
    const meals = parseMultiValue(row.meal_types);
    const tastes = parseMultiValue(row.taste_tags);

    for (const tag of types) {
      if (typeTagCounts[tag]) {
        typeTagCounts[tag].count++;
        typeTagCounts[tag].names.push(row.name);
      }
    }
    for (const tag of cuisines) {
      if (cuisineTagCounts[tag]) {
        cuisineTagCounts[tag].count++;
        cuisineTagCounts[tag].names.push(row.name);
      }
    }
    for (const tag of meals) {
      if (mealTypeCounts[tag]) {
        mealTypeCounts[tag].count++;
        mealTypeCounts[tag].names.push(row.name);
      }
    }
    if (priceLevelCounts[row.price_level]) {
      priceLevelCounts[row.price_level].count++;
      priceLevelCounts[row.price_level].names.push(row.name);
    }

    if (tastes.includes('清淡')) qingdanCount++;
    if (tastes.includes('辣')) laCount++;
    if (!tastes.includes('辣')) bulaCount++;
  }

  return {
    total: enabledRows.length,
    typeTagCounts,
    cuisineTagCounts,
    mealTypeCounts,
    priceLevelCounts,
    qingdanCount,
    laCount,
    bulaCount,
  };
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

    assert.deepEqual(taxonomy.mealTypes, ['早餐', '午餐', '晚餐', '夜宵']);
    assert.deepEqual(taxonomy.priceLevels, [1, 2, 3, 4]);
    assert.deepEqual(taxonomy.budgetValues, ['15以内', '15-25', '25-40', '40以上']);
    assert.deepEqual(taxonomy.userTasteFilters, ['清淡', '辣', '不辣']);
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

  // --- 5. 奶茶有合理餐段 ---
  it('5. 奶茶具有晚餐|夜宵餐段', () => {
    const milkTea = realRows.find(r => r.name === '奶茶');
    assert.ok(milkTea, '未找到奶茶');
    assert.notEqual(milkTea.meal_types, '', '奶茶 meal_types 不应为空');
    assert.ok(milkTea.meal_types.includes('晚餐'), '奶茶应包含晚餐');
    assert.ok(milkTea.meal_types.includes('夜宵'), '奶茶应包含夜宵');
    assert.equal(taxonomy.emptyMealTypesAllowlist, undefined, 'emptyMealTypesAllowlist 应已删除');
  });

  // --- 6. name 唯一 ---
  it('6. 所有菜品 name 唯一', () => {
    const names = realRows.map(r => r.name);
    const unique = new Set(names);
    assert.equal(unique.size, names.length, `存在重复 name: ${names.length - unique.size} 个重复`);
  });
});

// ============================================================
// 测试：动态覆盖统计 + 一致性断言
// ============================================================

describe('动态覆盖统计与一致性断言', () => {
  let taxonomy;
  let enabledRows;
  let stats;

  before(() => {
    clearTaxonomyCache();
    taxonomy = readTaxonomy();
    const result = readFoodsCSV();
    enabledRows = result.rows.filter(r => r.enabled === 'true');
    stats = computeCoverageStats(enabledRows);
  });

  // ==================== 一致性断言 ====================

  it('一致性：各 priceLevel 数量之和等于启用菜总数', () => {
    const sum = Object.values(stats.priceLevelCounts).reduce((s, v) => s + v.count, 0);
    assert.equal(sum, stats.total,
      `priceLevel 合计 ${sum} ≠ 启用菜总数 ${stats.total}`);
  });

  it('一致性：每个 mealType 数量等于 CSV 中包含该餐段的实际行数', () => {
    for (const [tag, info] of Object.entries(stats.mealTypeCounts)) {
      const actual = enabledRows.filter(r => parseMultiValue(r.meal_types).includes(tag)).length;
      assert.equal(info.count, actual,
        `mealType "${tag}" 动态计数 ${info.count} ≠ 实际行数 ${actual}`);
    }
  });

  it('一致性：覆盖报告中的菜名列表长度等于对应数量', () => {
    for (const [tag, info] of Object.entries(stats.typeTagCounts)) {
      assert.equal(info.names.length, info.count,
        `typeTag "${tag}" names列表 ${info.names.length} ≠ count ${info.count}`);
    }
    for (const [tag, info] of Object.entries(stats.cuisineTagCounts)) {
      assert.equal(info.names.length, info.count,
        `cuisineTag "${tag}" names列表 ${info.names.length} ≠ count ${info.count}`);
    }
    for (const [tag, info] of Object.entries(stats.mealTypeCounts)) {
      assert.equal(info.names.length, info.count,
        `mealType "${tag}" names列表 ${info.names.length} ≠ count ${info.count}`);
    }
    for (const [level, info] of Object.entries(stats.priceLevelCounts)) {
      assert.equal(info.names.length, info.count,
        `priceLevel ${level} names列表 ${info.names.length} ≠ count ${info.count}`);
    }
  });

  it('一致性：typeTag 和 cuisineTag 计数无空标签干扰', () => {
    for (const row of enabledRows) {
      const types = parseMultiValue(row.type_tags);
      const cuisines = parseMultiValue(row.cuisine_tags);
      for (const tag of types) {
        assert.ok(taxonomy.typeTags.includes(tag),
          `${row.name} type_tag "${tag}" 不在词典中，不应计入统计`);
      }
      for (const tag of cuisines) {
        assert.ok(taxonomy.cuisineTags.includes(tag),
          `${row.name} cuisine_tag "${tag}" 不在词典中，不应计入统计`);
      }
    }
  });

  // ==================== 覆盖矩阵（动态计算） ====================

  it('每个 typeTag 至少 5 道启用菜（动态统计）', () => {
    for (const [tag, info] of Object.entries(stats.typeTagCounts)) {
      assert.ok(info.count >= 5,
        `typeTag "${tag}" 仅 ${info.count} 道菜（${info.names.join(', ')}），需要至少 5 道`);
    }
  });

  it('每个 cuisineTag 至少 5 道启用菜（动态统计）', () => {
    for (const [tag, info] of Object.entries(stats.cuisineTagCounts)) {
      assert.ok(info.count >= 5,
        `cuisineTag "${tag}" 仅 ${info.count} 道菜（${info.names.join(', ')}），需要至少 5 道`);
    }
  });

  it('每个 mealType 至少 10 道候选（动态统计）', () => {
    for (const [tag, info] of Object.entries(stats.mealTypeCounts)) {
      assert.ok(info.count >= 10,
        `mealType "${tag}" 仅 ${info.count} 道菜（${info.names.join(', ')}），需要至少 10 道`);
    }
  });

  it('清淡、辣、不辣各至少 10 道（动态统计）', () => {
    assert.ok(stats.qingdanCount >= 10, `"清淡" 仅 ${stats.qingdanCount} 道，需要至少 10 道`);
    assert.ok(stats.laCount >= 10, `"辣" 仅 ${stats.laCount} 道，需要至少 10 道`);
    assert.ok(stats.bulaCount >= 10, `"不辣" 仅 ${stats.bulaCount} 道，需要至少 10 道`);
  });

  it('甜品至少 5 道且均有餐段（动态统计）', () => {
    const desserts = enabledRows.filter(r => parseMultiValue(r.type_tags).includes('甜品'));
    assert.ok(desserts.length >= 5, `甜品仅 ${desserts.length} 道，需要至少 5 道`);
    for (const d of desserts) {
      const meals = parseMultiValue(d.meal_types);
      assert.ok(meals.length > 0, `甜品 "${d.name}" meal_types 为空`);
    }
  });

  it('所有启用菜 meal_types 非空（动态统计）', () => {
    for (const row of enabledRows) {
      const meals = parseMultiValue(row.meal_types);
      assert.ok(meals.length > 0, `"${row.name}" 是启用菜但 meal_types 为空`);
    }
  });

  it('priceLevel 1-4 各至少 3 道（动态统计）', () => {
    for (const [level, info] of Object.entries(stats.priceLevelCounts)) {
      assert.ok(info.count >= 3,
        `priceLevel ${level} 仅 ${info.count} 道（${info.names.join(', ')}），需要至少 3 道`);
    }
  });
});

// ============================================================
// 测试：高风险菜品逐道期望映射
// ============================================================

describe('高风险菜品逐道期望映射', () => {
  let enabledRows;
  let rowMap;

  before(() => {
    const result = readFoodsCSV();
    enabledRows = result.rows.filter(r => r.enabled === 'true');
    rowMap = new Map(enabledRows.map(r => [r.name, r]));
  });

  // ==================== 正断言：精确分类 ====================

  it('包子：typeTags=小吃，无 cuisineTags', () => {
    const r = rowMap.get('包子');
    assert.ok(r, '包子应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['小吃']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), []);
  });

  it('烤串：tasteTags=咸|香，不含辣', () => {
    const r = rowMap.get('烤串');
    assert.ok(r, '烤串应存在');
    const tastes = parseMultiValue(r.taste_tags);
    assert.ok(tastes.includes('咸'));
    assert.ok(tastes.includes('香'));
    assert.ok(!tastes.includes('辣'), '烤串 taste 不应含辣');
  });

  it('西式烤鸡：无 typeTags，cuisineTags=西餐', () => {
    const r = rowMap.get('西式烤鸡');
    assert.ok(r, '西式烤鸡应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), []);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['西餐']);
  });

  it('台式卤肉饭：typeTags=快餐，无 cuisineTags', () => {
    const r = rowMap.get('台式卤肉饭');
    assert.ok(r, '台式卤肉饭应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['快餐']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), []);
  });

  it('鳗鱼饭：无 typeTags，cuisineTags=日料', () => {
    const r = rowMap.get('鳗鱼饭');
    assert.ok(r, '鳗鱼饭应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), []);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['日料']);
  });

  it('牛肉面：typeTags=面食，无 cuisineTags', () => {
    const r = rowMap.get('牛肉面');
    assert.ok(r, '牛肉面应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['面食']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), []);
  });

  it('馄饨：typeTags=小吃，无 cuisineTags', () => {
    const r = rowMap.get('馄饨');
    assert.ok(r, '馄饨应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['小吃']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), []);
  });

  it('广式皮蛋瘦肉粥：小吃 + 粤菜', () => {
    const r = rowMap.get('广式皮蛋瘦肉粥');
    assert.ok(r, '广式皮蛋瘦肉粥应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['小吃']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['粤菜']);
  });

  it('广式烧卖：小吃 + 粤菜', () => {
    const r = rowMap.get('广式烧卖');
    assert.ok(r, '广式烧卖应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['小吃']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['粤菜']);
  });

  it('川味烤鱼：烧烤 + 川菜，taste=辣|麻', () => {
    const r = rowMap.get('川味烤鱼');
    assert.ok(r, '川味烤鱼应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), ['烧烤']);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['川菜']);
    const tastes = parseMultiValue(r.taste_tags);
    assert.ok(tastes.includes('辣'));
    assert.ok(tastes.includes('麻'));
  });

  it('湘味蒜苗腊肉：无 typeTags，cuisineTags=湘菜', () => {
    const r = rowMap.get('湘味蒜苗腊肉');
    assert.ok(r, '湘味蒜苗腊肉应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), []);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['湘菜']);
  });

  it('农家一碗香：无 typeTags，cuisineTags=湘菜，taste=辣|咸', () => {
    const r = rowMap.get('农家一碗香');
    assert.ok(r, '农家一碗香应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), []);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['湘菜']);
    const tastes = parseMultiValue(r.taste_tags);
    assert.ok(tastes.includes('辣'));
    assert.ok(tastes.includes('咸'));
  });

  it('剁椒鱼头：tasteTags=辣|鲜，不含酸', () => {
    const r = rowMap.get('剁椒鱼头');
    assert.ok(r, '剁椒鱼头应存在');
    const tastes = parseMultiValue(r.taste_tags);
    assert.ok(tastes.includes('辣'));
    assert.ok(tastes.includes('鲜'));
    assert.ok(!tastes.includes('酸'), '剁椒鱼头 taste 不应含酸');
  });

  it('油条：mealTypes 仅早餐', () => {
    const r = rowMap.get('油条');
    assert.ok(r, '油条应存在');
    assert.deepEqual(parseMultiValue(r.meal_types), ['早餐']);
  });

  it('豆浆：tasteTags=清淡，不含甜', () => {
    const r = rowMap.get('豆浆');
    assert.ok(r, '豆浆应存在');
    const tastes = parseMultiValue(r.taste_tags);
    assert.deepEqual(tastes, ['清淡'], '豆浆 taste 应为清淡');
  });

  it('咸豆腐脑：tasteTags=咸|鲜，不含清淡', () => {
    const r = rowMap.get('咸豆腐脑');
    assert.ok(r, '咸豆腐脑应存在');
    const tastes = parseMultiValue(r.taste_tags);
    assert.ok(tastes.includes('咸'));
    assert.ok(tastes.includes('鲜'));
    assert.ok(!tastes.includes('清淡'), '咸豆腐脑不应含清淡');
  });

  it('家常豆腐：无 typeTags，cuisineTags=家常菜', () => {
    const r = rowMap.get('家常豆腐');
    assert.ok(r, '家常豆腐应存在');
    assert.deepEqual(parseMultiValue(r.type_tags), []);
    assert.deepEqual(parseMultiValue(r.cuisine_tags), ['家常菜']);
  });

  // ==================== 负断言：关键误分类标签不存在 ====================

  it('负断言：包子不含家常菜', () => {
    const r = rowMap.get('包子');
    assert.ok(!parseMultiValue(r.cuisine_tags).includes('家常菜'),
      '包子不应含 cuisineTag=家常菜');
  });

  it('负断言：台式卤肉饭不含家常菜', () => {
    const r = rowMap.get('台式卤肉饭');
    assert.ok(!parseMultiValue(r.cuisine_tags).includes('家常菜'),
      '台式卤肉饭不应含 cuisineTag=家常菜');
  });

  it('负断言：牛肉面不含家常菜', () => {
    const r = rowMap.get('牛肉面');
    assert.ok(!parseMultiValue(r.cuisine_tags).includes('家常菜'),
      '牛肉面不应含 cuisineTag=家常菜');
  });

  it('负断言：馄饨不含家常菜', () => {
    const r = rowMap.get('馄饨');
    assert.ok(!parseMultiValue(r.cuisine_tags).includes('家常菜'),
      '馄饨不应含 cuisineTag=家常菜');
  });

  it('负断言：鳗鱼饭不含快餐', () => {
    const r = rowMap.get('鳗鱼饭');
    assert.ok(!parseMultiValue(r.type_tags).includes('快餐'),
      '鳗鱼饭不应含 typeTag=快餐');
  });

  it('负断言：烤串不含辣', () => {
    const r = rowMap.get('烤串');
    assert.ok(!parseMultiValue(r.taste_tags).includes('辣'),
      '烤串不应含辣');
  });

  it('负断言：西式烤鸡不含烧烤', () => {
    const r = rowMap.get('西式烤鸡');
    assert.ok(!parseMultiValue(r.type_tags).includes('烧烤'),
      '西式烤鸡不应含烧烤 typeTag');
  });

  it('负断言：普通烤制食品不会自动包含烧烤', () => {
    const r = rowMap.get('西式烤鸡');
    assert.ok(r, '西式烤鸡应存在');
    assert.ok(!parseMultiValue(r.type_tags).includes('烧烤'),
      '西式烤鸡（烤制但不属烧烤消费场景）不应含烧烤');
  });

  it('负断言：干锅牛蛙已不存在', () => {
    assert.ok(!rowMap.has('干锅牛蛙'), '干锅牛蛙应已被替换为农家一碗香');
  });

  // ==================== 甜品餐段审计 ====================

  it('甜品餐段审计：冰淇淋仅晚餐|夜宵', () => {
    const r = rowMap.get('冰淇淋');
    const meals = parseMultiValue(r.meal_types);
    assert.deepEqual(meals, ['晚餐', '夜宵'], '冰淇淋应在晚餐和夜宵，不应在午餐');
    assert.ok(!meals.includes('午餐'), '冰淇淋不应含午餐——冰淇淋不宜作为午餐推荐');
  });

  it('甜品餐段审计：奶茶仅晚餐|夜宵', () => {
    const r = rowMap.get('奶茶');
    const meals = parseMultiValue(r.meal_types);
    assert.deepEqual(meals, ['晚餐', '夜宵'], '奶茶应在晚餐和夜宵，不应在午餐');
    assert.ok(!meals.includes('午餐'), '奶茶不应含午餐——奶茶不宜作为午餐正餐推荐');
  });

  it('甜品餐段审计：蛋糕、双皮奶、糖水、提拉米苏均含午餐|晚餐|夜宵', () => {
    for (const name of ['蛋糕', '双皮奶', '糖水', '提拉米苏']) {
      const r = rowMap.get(name);
      assert.ok(r, `${name} 应存在`);
      const meals = parseMultiValue(r.meal_types);
      assert.ok(meals.includes('午餐'), `${name} 应含午餐（餐后甜品属真实消费场景）`);
      assert.ok(meals.includes('晚餐'), `${name} 应含晚餐`);
      assert.ok(meals.includes('夜宵'), `${name} 应含夜宵`);
    }
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
  it('31 道合法菜通过校验（不硬编码数量）', () => {
    createTmpDir();
    let content = makeCSVHeader() + '\n';
    for (let i = 0; i < 31; i++) {
      content += makeCSVLine(`测试菜${i}`, `快餐`, `小吃`, ``, `午餐|晚餐`, `辣`, `1`, `true`) + '\n';
    }
    const p = writeTmpCSV('31-foods.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0, '31 道菜解析应无错误: ' + parseErrors.join('; '));
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(!errors.some(e => e.includes('期望 30')), '不应包含硬编码数量限制');
    assert.ok(!errors.some(e => e.includes('至少需要 1')), '31 道菜应通过至少 1 道检查');
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

  // --- 15b. price_level "2abc" 失败 ---
  it('15b. price_level "2abc" 校验失败（严格字符串，不允许 parseInt 宽松解析）', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '2abc', 'true');
    const p = writeTmpCSV('bad-price-str.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `"2abc" 应校验失败: ${errors.join('; ')}`);
  });

  // --- 15c. price_level "2.5" 失败 ---
  it('15c. price_level "2.5" 校验失败（禁止小数）', () => {
    createTmpDir();
    const content = makeCSVHeader() + '\n' + makeCSVLine('白切鸡', '粤菜', '', '粤菜', '午餐|晚餐', '清淡|鲜', '2.5', 'true');
    const p = writeTmpCSV('bad-price-decimal.csv', content);
    const { rows, errors: parseErrors } = parseFoodsCSV(content, p);
    assert.equal(parseErrors.length, 0);
    const errors = validateFoods(rows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.some(e => e.includes('price_level')), `"2.5" 应校验失败: ${errors.join('; ')}`);
  });

  // --- 15d. price_level "02" 失败 ---
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
    const p = resolve(tmpMigrationDir, 'V8__other_description.sql');
    writeFileSync(p, '-- dummy', 'utf-8');
    assert.ok(existsSync(p));

    const conflict = findExistingMigration('V8', tmpMigrationDir);
    assert.equal(conflict, 'V8__other_description.sql',
      '应检测到已有 V8__other_description.sql');

    const exactPath = migrationOutputPath('V8', 'test_exists', tmpMigrationDir);
    writeFileSync(exactPath, '-- dummy', 'utf-8');
    assert.ok(existsSync(exactPath), '精确路径也应被占用');

    const noConflict = findExistingMigration('V9', tmpMigrationDir);
    assert.equal(noConflict, null, 'V9 不应有冲突');

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

  // --- 22. 非法 CSV 不通过 validateFoods ---
  it('22. 包含校验错误的 CSV 不应通过 validateFoods', () => {
    const badRows = [{
      line: 2, name: '测试菜', category: '快餐',
      type_tags: '未知标签', cuisine_tags: '', meal_types: '午餐',
      taste_tags: '辣', price_level: '1', enabled: 'true',
    }];
    const errors = validateFoods(badRows, taxonomy, { skipSortCheck: true });
    assert.ok(errors.length > 0, '包含未知标签的数据应校验失败');
  });

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

  // --- name 重复时校验失败 ---
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

  it('CLI run() 拒绝同版本 V8：已有 V8__existing.sql 时禁止生成另一个 V8', () => {
    createTmpDir();

    const csvContent = makeCSVHeader() + '\n'
      + makeCSVLine('测试菜A', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true') + '\n'
      + makeCSVLine('测试菜B', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true');
    const csvPath = writeTmpCSV('cli-test.csv', csvContent);

    const existingPath = resolve(tmpMigrationDir, 'V8__existing.sql');
    writeFileSync(existingPath, '-- dummy migration', 'utf-8');
    assert.ok(existsSync(existingPath), 'V8__existing.sql 应已创建');

    const filesBefore = readdirSync(tmpMigrationDir).filter(f => f.endsWith('.sql'));
    assert.equal(filesBefore.length, 1, '初始应有 1 个 sql 文件');

    const stderrLines = [];
    const stderr = (msg) => { stderrLines.push(msg); };

    const result = run(['V8', 'test_migration'], {
      migrationDir: tmpMigrationDir,
      csvPath: csvPath,
      stderr: stderr,
    });

    assert.equal(result.ok, false, 'run() 应返回 ok=false');
    assert.ok(result.error, '应有错误消息');
    assert.ok(
      result.error.includes('V8') || result.error.includes('已存在'),
      `错误消息应提及版本冲突: ${result.error}`
    );

    assert.ok(!result.error.includes('ReferenceError'), '不应是 ReferenceError');
    assert.ok(!result.error.includes('is not defined'), '不应是未定义变量错误');

    const stderrText = stderrLines.join('\n');
    assert.ok(
      stderrText.includes('V8') && stderrText.includes('已存在'),
      `stderr 应包含版本冲突信息: ${stderrText}`
    );

    const filesAfter = readdirSync(tmpMigrationDir).filter(f => f.endsWith('.sql'));
    assert.equal(filesAfter.length, 1, '不应生成第二个文件');
    assert.equal(filesAfter[0], 'V8__existing.sql', '原有文件不受影响');
    assert.ok(!existsSync(resolve(tmpMigrationDir, 'V8__test_migration.sql')),
      '不应生成 V8__test_migration.sql');
  });

  it('CLI run() 成功生成 V9：同一目录已有 V8 时 V9 不受阻止', () => {
    createTmpDir();

    const csvContent = makeCSVHeader() + '\n'
      + makeCSVLine('测试菜A', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true') + '\n'
      + makeCSVLine('测试菜B', '快餐', '小吃', '', '午餐|晚餐', '辣', '1', 'true');
    const csvPath = writeTmpCSV('cli-test2.csv', csvContent);

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

    const files = readdirSync(tmpMigrationDir).filter(f => f.endsWith('.sql'));
    assert.equal(files.length, 2, '应有 V8 和 V9 两个文件');
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
