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
import {
  readFoodsCSV,
  validateFoods,
  generateMigrationSQL,
  sortRowsByName,
  findExistingMigration,
  migrationOutputPath,
} from './lib/foods-data.mjs';

function main() {
  const args = process.argv.slice(2);

  if (args.length < 2) {
    console.error('用法: node scripts/generate-food-migration.mjs <版本号> <描述>');
    console.error('示例: node scripts/generate-food-migration.mjs V8 add_more_foods');
    console.error('');
    console.error('版本号必须是 >= 8 的正整数（V7 保留给 foods.name 唯一索引）。');
    console.error('描述只允许小写字母、数字和下划线，将转为文件名安全格式。');
    process.exit(1);
  }

  const [versionArg, description] = args;

  // 校验版本号
  const versionStr = String(versionArg).replace(/^v/i, 'V');
  const versionNum = parseInt(versionStr.replace(/^V/i, ''), 10);
  if (isNaN(versionNum) || versionNum < 1 || String(versionNum) !== versionStr.replace(/^V/i, '')) {
    console.error(`❌ 无效的版本号: "${versionArg}"，必须是正整数（如 8 或 V8）`);
    process.exit(1);
  }

  if (versionNum < 8) {
    console.error(`❌ 版本号 ${versionStr} 不合法：V7 保留给 foods.name 唯一索引`);
    console.error(`   菜品数据 migration 请使用 V8 及以上版本`);
    process.exit(1);
  }

  // 1. 校验 CSV
  const { rows, errors: parseErrors } = readFoodsCSV();

  if (parseErrors.length > 0) {
    console.error('❌ CSV 解析失败，无法生成 migration：');
    for (const err of parseErrors) {
      console.error(`   ${err}`);
    }
    process.exit(1);
  }

  const validateErrors = validateFoods(rows);

  if (validateErrors.length > 0) {
    console.error(`❌ CSV 校验失败（${validateErrors.length} 个错误），无法生成 migration：`);
    for (const err of validateErrors) {
      console.error(`   ${err}`);
    }
    process.exit(1);
  }

  // 2. 检查目标文件是否已存在（精确路径冲突）
  const outputPath = migrationOutputPath(versionStr, description);
  if (existsSync(outputPath)) {
    console.error(`❌ 目标 migration 文件已存在，禁止覆盖：`);
    console.error(`   ${outputPath}`);
    process.exit(1);
  }

  // 2b. 扫描 migration 目录，检查是否有相同版本号的其他文件（不同描述也冲突）
  const conflictingFile = findExistingMigration(versionStr);
  if (conflictingFile) {
    console.error(`❌ 版本号 ${versionStr} 已存在 migration 文件，禁止生成：`);
    console.error(`   已有文件: ${conflictingFile}`);
    console.error(`   目标文件: ${migrationFileName(versionStr, description)}`);
    console.error(`   版本号必须唯一，请使用下一个版本号`);
    process.exit(1);
  }

  // 3. 生成 SQL（按 name 稳定排序）
  const sortedRows = sortRowsByName(rows);
  const sql = generateMigrationSQL(sortedRows, versionStr, description);

  writeFileSync(outputPath, sql, 'utf-8');
  console.log(`✅ Migration 已生成：${outputPath}`);
  console.log(`   共 ${sortedRows.length} 道菜`);
  process.exit(0);
}

main();
