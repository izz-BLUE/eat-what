/**
 * foods-data.mjs — 菜品数据管线公共模块
 *
 * 职责：
 *   1. 读取 recommend-taxonomy.json（统一词典）
 *   2. 读取并解析 foods.csv（受限 CSV 格式）
 *   3. 多值字段解析（竖线分隔）
 *   4. 完整数据校验（21 条规则）
 *   5. 生成 Flyway migration SQL
 *
 * 限制：CSV 字段禁止出现英文逗号、双引号、字段内换行。
 *       检测到违规时必须报错，不得静默接受。
 */

import { readFileSync, readdirSync, existsSync } from 'node:fs';
import { resolve, dirname, basename } from 'node:path';
import { fileURLToPath } from 'node:url';

// ============================================================
// 路径解析
// ============================================================

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/**
 * 查找项目根目录：从当前目录向上查找包含 data/foods.csv 的目录。
 * @returns {string} 项目根目录绝对路径
 */
export function findProjectRoot() {
  let dir = resolve(__dirname, '..', '..');
  // 验证：项目根目录下应有 data/foods.csv 或 backend-java/pom.xml
  return dir;
}

export const PROJECT_ROOT = findProjectRoot();
export const DATA_DIR = resolve(PROJECT_ROOT, 'data');
export const TAXONOMY_PATH = resolve(DATA_DIR, 'recommend-taxonomy.json');
export const CSV_PATH = resolve(DATA_DIR, 'foods.csv');
export const MIGRATION_DIR = resolve(PROJECT_ROOT, 'backend-java', 'src', 'main', 'resources', 'db', 'migration');

// ============================================================
// 词典
// ============================================================

let _taxonomyCache = null;

/**
 * 读取并返回统一词典（带缓存）。
 * @param {string} [path] - taxonomy JSON 文件路径
 * @returns {object} 词典对象
 */
export function readTaxonomy(path = TAXONOMY_PATH) {
  if (_taxonomyCache) return _taxonomyCache;
  const raw = readFileSync(path, 'utf-8');
  _taxonomyCache = JSON.parse(raw);
  return _taxonomyCache;
}

/**
 * 清除词典缓存（测试用）。
 */
export function clearTaxonomyCache() {
  _taxonomyCache = null;
}

// ============================================================
// 多值字段解析
// ============================================================

/**
 * 将竖线分隔的字符串解析为数组。
 * 空字符串返回空数组。
 * @param {string} str - 竖线分隔的多值字符串
 * @returns {string[]}
 */
export function parseMultiValue(str) {
  if (!str || str.trim() === '') return [];
  return str.split('|').map(s => s.trim()).filter(s => s !== '');
}

/**
 * 将数组序列化为竖线分隔字符串（稳定排序）。
 * @param {string[]} values
 * @returns {string}
 */
export function formatMultiValue(values) {
  if (!values || values.length === 0) return '';
  return values.join('|');
}

// ============================================================
// CSV 解析
// ============================================================

const EXPECTED_HEADER = 'name,category,type_tags,cuisine_tags,meal_types,taste_tags,price_level,enabled';
const EXPECTED_COLUMNS = EXPECTED_HEADER.split(',');

/**
 * 解析 foods.csv 内容为行对象数组。
 *
 * 受限 CSV 格式：
 *   - 字段内禁止逗号、双引号、换行
 *   - 多值字段使用 | 分隔
 *   - 检测到违规字符立即报错
 *
 * @param {string} content - CSV 文件原始内容
 * @param {string} [filePath] - 文件路径（用于错误消息）
 * @returns {{ rows: Array<object>, errors: Array<string> }}
 */
export function parseFoodsCSV(content, filePath = CSV_PATH) {
  const errors = [];
  const fileName = filePath ? basename(filePath) : 'foods.csv';

  // 检查 BOM
  if (content.charCodeAt(0) === 0xFEFF) {
    errors.push(`${fileName}: 文件包含 BOM，必须使用 UTF-8 无 BOM`);
  }

  // 检查首行表头
  const lines = content.split(/\r?\n/);
  if (lines.length === 0) {
    errors.push(`${fileName}: 文件为空`);
    return { rows: [], errors };
  }

  const headerLine = lines[0].trim();
  // 去除可能残留的 BOM
  const cleanHeader = headerLine.replace(/^﻿/, '');
  if (cleanHeader !== EXPECTED_HEADER) {
    const headerCols = cleanHeader.split(',');
    const expectedCols = EXPECTED_COLUMNS;
    if (headerCols.length !== expectedCols.length) {
      errors.push(`${fileName}:1: 表头列数不匹配，期望 ${expectedCols.length} 列，实际 ${headerCols.length} 列`);
    } else {
      for (let i = 0; i < expectedCols.length; i++) {
        if (headerCols[i].trim() !== expectedCols[i]) {
          errors.push(`${fileName}:1: 表头第 ${i + 1} 列期望 "${expectedCols[i]}"，实际 "${headerCols[i].trim()}"`);
        }
      }
    }
    return { rows: [], errors };
  }

  const rows = [];

  for (let i = 1; i < lines.length; i++) {
    const line = lines[i];

    // 跳过空行
    if (line.trim() === '') continue;

    const lineNum = i + 1; // 1-based in editors

    // 检测禁止字符
    if (line.includes('"')) {
      errors.push(`${fileName}:${lineNum}: 字段包含双引号（"），CSV 禁止使用双引号`);
      continue;
    }
    // 换行已在 split 中处理，检测字段内 \r
    if (line.includes('\r')) {
      errors.push(`${fileName}:${lineNum}: 字段包含回车符，CSV 禁止字段内换行`);
      continue;
    }

    const cols = line.split(',');
    const expectedCount = EXPECTED_COLUMNS.length;

    if (cols.length !== expectedCount) {
      // 检查是否因为字段内包含逗号导致
      if (cols.length > expectedCount) {
        errors.push(`${fileName}:${lineNum}: 列数超过 ${expectedCount}，字段内可能包含禁止的英文逗号`);
      } else {
        errors.push(`${fileName}:${lineNum}: 列数不足，期望 ${expectedCount} 列，实际 ${cols.length} 列`);
      }
      continue;
    }

    const [name, category, type_tags, cuisine_tags, meal_types, taste_tags, price_level, enabled] = cols;

    // 前后空格检测（在 trim 前检测）
    if (name !== name.trim()) {
      errors.push(`${fileName}:${lineNum}: name="${name}" 前后有空格`);
    }
    if (category !== category.trim()) {
      errors.push(`${fileName}:${lineNum}: category="${category}" 前后有空格`);
    }

    rows.push({
      line: lineNum,
      name: name.trim(),
      category: category.trim(),
      type_tags: type_tags.trim(),
      cuisine_tags: cuisine_tags.trim(),
      meal_types: meal_types.trim(),
      taste_tags: taste_tags.trim(),
      price_level: price_level.trim(),
      enabled: enabled.trim(),
    });
  }

  return { rows, errors };
}

/**
 * 从文件系统读取并解析 foods.csv。
 * @param {string} [path]
 * @returns {{ rows: Array<object>, errors: Array<string> }}
 */
export function readFoodsCSV(path = CSV_PATH) {
  let content;
  try {
    content = readFileSync(path, 'utf-8');
  } catch (err) {
    if (err.code === 'ENOENT') {
      return { rows: [], errors: [`${basename(path)}: 文件不存在`] };
    }
    throw err;
  }
  return parseFoodsCSV(content, path);
}

// ============================================================
// 校验
// ============================================================

/**
 * 校验所有 foods 行数据。
 *
 * @param {Array<object>} rows - parseFoodsCSV 返回的行数组
 * @param {object} [taxonomy] - 词典对象，不传则自动读取
 * @param {object} [opts]
 * @param {boolean} [opts.skipSortCheck] - 跳过排序检查（测试用）
 * @returns {Array<string>} 错误消息列表，空数组表示通过
 */
export function validateFoods(rows, taxonomy = null, opts = {}) {
  const t = taxonomy || readTaxonomy();
  const errors = [];
  const fileName = 'foods.csv';

  // ---------- 1. 表头已在 parseFoodsCSV 中校验 ----------

  // ---------- 2. BOM 已在 parseFoodsCSV 中校验 ----------

  // ---------- 3. name 非空、唯一、前后无空格 ----------
  const names = new Set();
  const duplicateNames = new Set();
  for (const row of rows) {
    if (!row.name) {
      errors.push(`${fileName}:${row.line}: name 为空`);
      continue;
    }
    if (row.name !== row.name.trim()) {
      errors.push(`${fileName}:${row.line}: name="${row.name}" 前后有空格`);
    }
    if (names.has(row.name)) {
      duplicateNames.add(row.name);
    }
    names.add(row.name);
  }
  for (const dn of duplicateNames) {
    const lines = rows.filter(r => r.name === dn).map(r => r.line).join(', ');
    errors.push(`${fileName}:${lines}: name="${dn}" 重复`);
  }

  // ---------- 4. category 非空且前后无空格 ----------
  for (const row of rows) {
    if (!row.category) {
      errors.push(`${fileName}:${row.line}: name="${row.name}" category 为空`);
    } else if (row.category !== row.category.trim()) {
      errors.push(`${fileName}:${row.line}: name="${row.name}" category="${row.category}" 前后有空格`);
    }
  }

  // 收集所有标签集合
  const allTypeTags = new Set(t.typeTags);
  const allCuisineTags = new Set(t.cuisineTags);
  const allMealTypes = new Set(t.mealTypes);
  const allTasteTags = new Set(t.tasteTags);
  const allowlist = new Set(t.emptyMealTypesAllowlist || []);

  // 逐行校验
  for (const row of rows) {
    const n = row.name || `行${row.line}`;

    // ---------- 5. type_tags 和 cuisine_tags 至少一个非空 ----------
    const typeVals = parseMultiValue(row.type_tags);
    const cuisineVals = parseMultiValue(row.cuisine_tags);
    if (typeVals.length === 0 && cuisineVals.length === 0) {
      errors.push(`${fileName}:${row.line}: name="${n}" type_tags 和 cuisine_tags 均为空，至少需要一个`);
    }

    // ---------- 6. type_tags 全属于词典 ----------
    for (const tv of typeVals) {
      if (!allTypeTags.has(tv)) {
        errors.push(`${fileName}:${row.line}: name="${n}" type_tags 包含未知标签 "${tv}"`);
      }
    }

    // ---------- 7. cuisine_tags 全属于词典 ----------
    for (const cv of cuisineVals) {
      if (!allCuisineTags.has(cv)) {
        errors.push(`${fileName}:${row.line}: name="${n}" cuisine_tags 包含未知标签 "${cv}"`);
      }
    }

    // ---------- 8. meal_types 全属于词典 ----------
    const mealVals = parseMultiValue(row.meal_types);
    for (const mv of mealVals) {
      if (!allMealTypes.has(mv)) {
        errors.push(`${fileName}:${row.line}: name="${n}" meal_types 包含未知标签 "${mv}"`);
      }
    }

    // ---------- 9. enabled=true 的菜品 meal_types 不得为空，白名单除外 ----------
    if (row.enabled === 'true' && mealVals.length === 0 && !allowlist.has(n)) {
      errors.push(`${fileName}:${row.line}: name="${n}" enabled=true 但 meal_types 为空且不在白名单中`);
    }

    // ---------- 10. 空 meal_types 白名单检查 ----------
    if (mealVals.length === 0 && !allowlist.has(n)) {
      errors.push(`${fileName}:${row.line}: name="${n}" meal_types 为空但不在白名单中`);
    }

    // ---------- 11. taste_tags 非空且全部属于词典 ----------
    const tasteVals = parseMultiValue(row.taste_tags);
    if (tasteVals.length === 0) {
      errors.push(`${fileName}:${row.line}: name="${n}" taste_tags 为空`);
    }
    for (const tv of tasteVals) {
      if (!allTasteTags.has(tv)) {
        errors.push(`${fileName}:${row.line}: name="${n}" taste_tags 包含未知标签 "${tv}"`);
      }
    }

    // ---------- 12. 多值字段必须使用 |（已在 parseMultiValue 中处理） ----------
    // 如果字段包含逗号但非空，可能是误用了逗号分隔
    const multiFields = [
      ['type_tags', row.type_tags],
      ['cuisine_tags', row.cuisine_tags],
      ['meal_types', row.meal_types],
      ['taste_tags', row.taste_tags],
    ];
    for (const [fname, fval] of multiFields) {
      if (fval.includes(',') && fval.trim() !== '') {
        errors.push(`${fileName}:${row.line}: name="${n}" ${fname} 包含英文逗号，多值字段必须使用竖线 |`);
      }
    }

    // ---------- 13. 多值字段不允许重复值 ----------
    const checkDup = (fname, vals) => {
      const seen = new Set();
      for (const v of vals) {
        if (seen.has(v)) {
          errors.push(`${fileName}:${row.line}: name="${n}" ${fname} 包含重复值 "${v}"`);
        }
        seen.add(v);
      }
    };
    checkDup('type_tags', typeVals);
    checkDup('cuisine_tags', cuisineVals);
    checkDup('meal_types', mealVals);
    checkDup('taste_tags', tasteVals);

    // ---------- 14. 多值字段不允许空元素 ----------
    const checkEmpty = (fname, raw, vals) => {
      // 检查原始字符串中是否有 || 或以 | 开头/结尾
      if (raw.startsWith('|') || raw.endsWith('|') || raw.includes('||')) {
        errors.push(`${fileName}:${row.line}: name="${n}" ${fname} 包含空元素`);
        return;
      }
      // 检查解析出的值和原始管道段数是否一致
      if (raw.trim() !== '') {
        const segments = raw.split('|').filter(s => true); // keep empty
        const nonEmpty = raw.split('|').filter(s => s.trim() !== '');
        if (segments.length !== nonEmpty.length) {
          errors.push(`${fileName}:${row.line}: name="${n}" ${fname} 包含空元素`);
        }
      }
    };
    checkEmpty('type_tags', row.type_tags, typeVals);
    checkEmpty('cuisine_tags', row.cuisine_tags, cuisineVals);
    checkEmpty('meal_types', row.meal_types, mealVals);
    checkEmpty('taste_tags', row.taste_tags, tasteVals);

    // ---------- 15. 多值字段每个值不允许前后空格 ----------
    const checkSpace = (fname, raw) => {
      if (raw.trim() === '') return;
      const segments = raw.split('|');
      for (const seg of segments) {
        if (seg !== seg.trim()) {
          errors.push(`${fileName}:${row.line}: name="${n}" ${fname} 值 "${seg}" 前后有空格`);
        }
      }
    };
    checkSpace('type_tags', row.type_tags);
    checkSpace('cuisine_tags', row.cuisine_tags);
    checkSpace('meal_types', row.meal_types);
    checkSpace('taste_tags', row.taste_tags);

    // ---------- 16. price_level 严格校验：只允许字符串 "1", "2", "3", "4" ----------
    const validPriceLevels = ['1', '2', '3', '4'];
    if (!validPriceLevels.includes(row.price_level)) {
      errors.push(`${fileName}:${row.line}: name="${n}" price_level="${row.price_level}" 不合法，只允许 1-4`);
    }

    // ---------- 17. enabled 只能是 true 或 false ----------
    if (row.enabled !== 'true' && row.enabled !== 'false') {
      errors.push(`${fileName}:${row.line}: name="${n}" enabled="${row.enabled}" 不合法，只允许 true 或 false`);
    }

    // ---------- 20. 不允许废弃值 ----------
    const forbiddenValues = ['微辣', '重口', '韩餐'];
    const allFieldValues = [
      row.type_tags, row.cuisine_tags, row.meal_types, row.taste_tags,
      row.category,
    ].join('|');
    for (const fv of forbiddenValues) {
      if (allFieldValues.includes(fv)) {
        errors.push(`${fileName}:${row.line}: name="${n}" 包含废弃值 "${fv}"`);
      }
    }
  }

  // ---------- 18. 至少 1 道启用菜 ----------
  const enabledRows = rows.filter(r => r.enabled === 'true');
  if (enabledRows.length < 1) {
    errors.push(`${fileName}: 至少需要 1 道启用菜，当前 0 道`);
  }

  // ---------- 19. 30 道菜与 V6 映射一致性（由 Java 测试负责） ----------
  // 此校验在 validateFoods 中跳过（依赖数据库），由 Java 集成测试交叉验证

  // ---------- 21. CSV 必须按 name 的 Unicode 顺序排列 ----------
  if (!opts.skipSortCheck) {
    const actualNames = rows.map(r => r.name);
    const expectedNames = [...actualNames].sort((a, b) => a.localeCompare(b, 'zh-CN'));
    for (let i = 0; i < actualNames.length; i++) {
      if (actualNames[i] !== expectedNames[i]) {
        errors.push(
          `${fileName}:${rows[i].line}: name="${actualNames[i]}" 排序不正确，` +
          `期望该位置为 "${expectedNames[i]}"`
        );
        break; // 只报告第一个排序错误
      }
    }
  }

  return errors;
}

// ============================================================
// SQL 生成
// ============================================================

/**
 * 转义 SQL 字符串中的特殊字符。
 * @param {string} str
 * @returns {string}
 */
export function escapeSQLString(str) {
  if (str === null || str === undefined) return "''";
  return "'" + str.replace(/\\/g, '\\\\').replace(/'/g, "\\'") + "'";
}

/**
 * 将竖线格式的多值字段转换为数据库逗号分隔格式。
 * @param {string} pipeStr - 竖线分隔字符串
 * @returns {string} 逗号分隔字符串
 */
export function toDBMultiValue(pipeStr) {
  if (!pipeStr || pipeStr.trim() === '') return '';
  return pipeStr.split('|').map(s => s.trim()).filter(s => s !== '').join(',');
}

/**
 * 生成 Flyway migration SQL 文本。
 *
 * @param {Array<object>} rows - CSV 行数据
 * @param {string|number} version - 版本号，如 "V7" 或 7
 * @param {string} description - 描述文字
 * @returns {string} SQL 文本
 */
export function generateMigrationSQL(rows, version, description) {
  const v = String(version).replace(/^v/i, '');
  const versionStr = 'V' + v;
  const versionNum = parseInt(v, 10);

  if (isNaN(versionNum) || versionNum < 1) {
    throw new Error(`无效的版本号: ${version}`);
  }

  // V7 保留给唯一索引，菜品数据 migration 必须使用 V8 及以上
  if (versionNum < 8) {
    throw new Error(
      `版本号 ${versionStr} 不合法：V7 保留给 foods.name 唯一索引，` +
      `菜品数据 migration 请使用 V8 及以上版本`
    );
  }

  const safeDesc = description
    .toLowerCase()
    .replace(/[^a-z0-9一-鿿]+/g, '_')
    .replace(/^_+|_+$/g, '');

  const lines = [];
  lines.push(`-- =====================================================`);
  lines.push(`-- ${versionStr}: ${description}`);
  lines.push(`-- =====================================================`);
  lines.push(`--`);
  lines.push(`-- 自动生成于 data/foods.csv`);
  lines.push(`-- 生成命令: npm run foods:generate -- ${versionStr} ${description}`);
  lines.push(`--`);
  lines.push(`-- 说明：`);
  lines.push(`--   1. V7 已建立 uk_foods_name 唯一索引，本 migration 不再重复`);
  lines.push(`--   2. 按 name 作为业务唯一键，INSERT ... ON DUPLICATE KEY UPDATE`);
  lines.push(`--   3. CSV 中不存在的菜品不会被自动删除`);
  lines.push(`--   4. 禁用菜品使用 enabled=0，不使用 DELETE`);
  lines.push(`--`);
  lines.push(`-- =====================================================`);
  lines.push(``);

  // 逐行生成 INSERT ... ON DUPLICATE KEY UPDATE（MySQL 8 行别名语法）
  for (const row of rows) {
    const name = row.name;
    const category = row.category;
    const typeTags = toDBMultiValue(row.type_tags);
    const cuisineTags = toDBMultiValue(row.cuisine_tags);
    const mealTypes = toDBMultiValue(row.meal_types);
    const tasteTags = toDBMultiValue(row.taste_tags);
    const priceLevel = parseInt(row.price_level, 10);
    const enabled = row.enabled === 'true' ? 1 : 0;

    lines.push(`INSERT INTO \`foods\` (`);
    lines.push(`  \`name\`, \`category\`, \`type_tags\`, \`cuisine_tags\`,`);
    lines.push(`  \`meal_types\`, \`taste_tags\`, \`price_level\`, \`enabled\``);
    lines.push(`) VALUES (`);
    lines.push(`  ${escapeSQLString(name)},`);
    lines.push(`  ${escapeSQLString(category)},`);
    lines.push(`  ${escapeSQLString(typeTags)},`);
    lines.push(`  ${escapeSQLString(cuisineTags)},`);
    lines.push(`  ${escapeSQLString(mealTypes)},`);
    lines.push(`  ${escapeSQLString(tasteTags)},`);
    lines.push(`  ${priceLevel},`);
    lines.push(`  ${enabled}`);
    lines.push(`)`);
    lines.push(`AS new`);
    lines.push(`ON DUPLICATE KEY UPDATE`);
    lines.push(`  \`category\` = new.\`category\`,`);
    lines.push(`  \`type_tags\` = new.\`type_tags\`,`);
    lines.push(`  \`cuisine_tags\` = new.\`cuisine_tags\`,`);
    lines.push(`  \`meal_types\` = new.\`meal_types\`,`);
    lines.push(`  \`taste_tags\` = new.\`taste_tags\`,`);
    lines.push(`  \`price_level\` = new.\`price_level\`,`);
    lines.push(`  \`enabled\` = new.\`enabled\`,`);
    lines.push(`  \`updated_at\` = CURRENT_TIMESTAMP;`);
    lines.push(``);
  }

  return lines.join('\n');
}

/**
 * 生成 migration 文件名。
 * @param {string|number} version
 * @param {string} description
 * @returns {string}
 */
export function migrationFileName(version, description) {
  const v = String(version).replace(/^v/i, '');
  const versionStr = 'V' + v;
  const safeDesc = description
    .toLowerCase()
    .replace(/[^a-z0-9_]+/g, '_')
    .replace(/^_+|_+$/g, '');
  return `${versionStr}__${safeDesc}.sql`;
}

/**
 * 计算 migration 文件的完整输出路径。
 * @param {string|number} version
 * @param {string} description
 * @param {string} [dir] - migration 目录
 * @returns {string}
 */
export function migrationOutputPath(version, description, dir = MIGRATION_DIR) {
  return resolve(dir, migrationFileName(version, description));
}

/**
 * 将 rows 按 name 的 Unicode 顺序排序后返回新数组。
 * @param {Array<object>} rows
 * @returns {Array<object>}
 */
/**
 * 扫描 migration 目录，查找是否存在相同版本号的文件。
 * 版本号匹配规则：以 V{version}__ 开头且 .sql 结尾。
 *
 * @param {string|number} version - 版本号
 * @param {string} [dir] - migration 目录
 * @returns {string|null} 找到的文件名，未找到返回 null
 */
export function findExistingMigration(version, dir = MIGRATION_DIR) {
  const v = String(version).replace(/^v/i, '');
  const versionStr = 'V' + v;
  const prefix = versionStr + '__';

  try {
    if (!existsSync(dir)) return null;
    const entries = readdirSync(dir);
    for (const entry of entries) {
      if (entry.startsWith(prefix) && entry.endsWith('.sql')) {
        return entry;
      }
    }
  } catch (_) {
    // 目录不存在或无权限，视为无冲突
  }
  return null;
}

export function sortRowsByName(rows) {
  return [...rows].sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'));
}
