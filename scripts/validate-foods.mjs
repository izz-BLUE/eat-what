#!/usr/bin/env node
/**
 * validate-foods.mjs — CSV 校验 CLI 入口
 *
 * 使用方式：
 *   node scripts/validate-foods.mjs
 *   npm run foods:validate
 *
 * 校验规则：详见 validateFoods() 函数，共 21 条规则。
 *
 * 退出码：
 *   0 — 校验通过
 *   1 — 校验失败（有错误输出）
 */

import { readFoodsCSV, validateFoods } from './lib/foods-data.mjs';

function main() {
  const { rows, errors: parseErrors } = readFoodsCSV();

  if (parseErrors.length > 0) {
    console.error('❌ CSV 解析失败：');
    for (const err of parseErrors) {
      console.error(`   ${err}`);
    }
    process.exit(1);
  }

  const validateErrors = validateFoods(rows);

  if (validateErrors.length > 0) {
    console.error(`❌ 校验失败：${validateErrors.length} 个错误`);
    for (const err of validateErrors) {
      console.error(`   ${err}`);
    }
    process.exit(1);
  }

  console.log(`✅ 校验通过：${rows.length} 道菜全部合法`);
  process.exit(0);
}

main();
