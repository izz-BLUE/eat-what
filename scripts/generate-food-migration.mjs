#!/usr/bin/env node
/**
 * generate-food-migration.mjs — Migration SQL 生成 CLI 入口
 *
 * 使用方式：
 *   node scripts/generate-food-migration.mjs V8 add_more_foods
 *   npm run foods:generate -- V8 add_more_foods
 *
 * 要求：
 *   1. 版本号必须 >= 8（V7 保留给 foods.name 唯一索引）
 *   2. 生成前必须通过 CSV 校验
 *   3. 目标文件已存在时拒绝覆盖
 *   4. 生成的 SQL 使用 UTF-8 无 BOM
 *   5. SQL 使用 MySQL 8 行别名语法（AS new），不使用弃用的 VALUES()
 *   6. SQL 不含 ALTER TABLE 或 ADD UNIQUE KEY（V7 已建立）
 *
 * 退出码：
 *   0 — 生成成功
 *   1 — 校验失败或文件已存在
 */

import { writeFileSync, existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  readFoodsCSV,
  validateFoods,
  generateMigrationSQL,
  sortRowsByName,
  findExistingMigration,
  migrationOutputPath,
  migrationFileName,
  MIGRATION_DIR,
} from './lib/foods-data.mjs';

/**
 * 执行 migration 生成逻辑（可注入依赖，便于测试）。
 *
 * @param {string[]} args - 命令行参数，如 ['V8', 'add_spicy_snacks']
 * @param {object} [options]
 * @param {string} [options.migrationDir] - 覆盖 migration 输出目录
 * @param {string} [options.csvPath] - 覆盖 CSV 文件路径
 * @param {typeof console.error} [options.stderr] - 错误输出函数（测试可注入）
 * @returns {{ ok: boolean, error?: string, outputPath?: string, count?: number }}
 */
export function run(args, options = {}) {
  const migrationDir = options.migrationDir || MIGRATION_DIR;
  const csvPath = options.csvPath || undefined;
  const stderr = options.stderr || console.error;

  if (args.length < 2) {
    stderr('用法: node scripts/generate-food-migration.mjs <版本号> <描述>');
    stderr('示例: node scripts/generate-food-migration.mjs V8 add_more_foods');
    stderr('');
    stderr('版本号必须是 >= 8 的正整数（V7 保留给 foods.name 唯一索引）。');
    stderr('描述只允许小写字母、数字和下划线，将转为文件名安全格式。');
    return { ok: false, error: '参数不足' };
  }

  const [versionArg, description] = args;

  // 校验版本号
  const versionStr = String(versionArg).replace(/^v/i, 'V');
  const versionNum = parseInt(versionStr.replace(/^V/i, ''), 10);
  if (isNaN(versionNum) || versionNum < 1 || String(versionNum) !== versionStr.replace(/^V/i, '')) {
    const msg = `无效的版本号: "${versionArg}"，必须是正整数（如 8 或 V8）`;
    stderr(`❌ ${msg}`);
    return { ok: false, error: msg };
  }

  if (versionNum < 8) {
    const msg = `版本号 ${versionStr} 不合法：V7 保留给 foods.name 唯一索引，菜品数据 migration 请使用 V8 及以上版本`;
    stderr(`❌ ${msg}`);
    return { ok: false, error: msg };
  }

  // 1. 校验 CSV
  const { rows, errors: parseErrors } = readFoodsCSV(csvPath);

  if (parseErrors.length > 0) {
    stderr('❌ CSV 解析失败，无法生成 migration：');
    for (const err of parseErrors) {
      stderr(`   ${err}`);
    }
    return { ok: false, error: 'CSV 解析失败' };
  }

  const validateErrors = validateFoods(rows);

  if (validateErrors.length > 0) {
    stderr(`❌ CSV 校验失败（${validateErrors.length} 个错误），无法生成 migration：`);
    for (const err of validateErrors) {
      stderr(`   ${err}`);
    }
    return { ok: false, error: 'CSV 校验失败' };
  }

  // 2. 检查目标文件是否已存在（精确路径冲突）
  const outputPath = migrationOutputPath(versionStr, description, migrationDir);
  if (existsSync(outputPath)) {
    const msg = `目标 migration 文件已存在：${outputPath}`;
    stderr(`❌ 目标 migration 文件已存在，禁止覆盖：`);
    stderr(`   ${outputPath}`);
    return { ok: false, error: msg };
  }

  // 2b. 扫描 migration 目录，检查是否有相同版本号的其他文件（不同描述也冲突）
  const conflictingFile = findExistingMigration(versionStr, migrationDir);
  if (conflictingFile) {
    const msg = `版本号 ${versionStr} 已存在 migration 文件: ${conflictingFile}`;
    stderr(`❌ 版本号 ${versionStr} 已存在 migration 文件，禁止生成：`);
    stderr(`   已有文件: ${conflictingFile}`);
    stderr(`   目标文件: ${migrationFileName(versionStr, description)}`);
    stderr(`   版本号必须唯一，请使用下一个版本号`);
    return { ok: false, error: msg };
  }

  // 3. 生成 SQL（按 name 稳定排序）
  const sortedRows = sortRowsByName(rows);
  const sql = generateMigrationSQL(sortedRows, versionStr, description);

  writeFileSync(outputPath, sql, 'utf-8');
  console.log(`✅ Migration 已生成：${outputPath}`);
  console.log(`   共 ${sortedRows.length} 道菜`);
  return { ok: true, outputPath, count: sortedRows.length };
}

/**
 * CLI 入口：解析 process.argv 并调用 run()。
 * 仅负责参数传递和退出码，不包含业务逻辑。
 */
function main() {
  const result = run(process.argv.slice(2));
  process.exit(result.ok ? 0 : 1);
}

// 仅在直接执行时运行 main()，import 时不运行
const currentFile = fileURLToPath(import.meta.url);
const isEntryPoint = process.argv[1] && resolve(process.argv[1]) === currentFile;
if (isEntryPoint) {
  main();
}
