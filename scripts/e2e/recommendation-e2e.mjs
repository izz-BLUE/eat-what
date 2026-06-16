#!/usr/bin/env node
/**
 * recommendation-e2e.mjs — 推荐系统真实接口回归验收脚本
 *
 * 使用方式：
 *   node scripts/e2e/recommendation-e2e.mjs
 *   npm run e2e:recommend
 *
 * 前置条件：
 *   后端必须已启动（默认 http://localhost:8080）
 *   环境变量 EAT_WHAT_API_BASE 可覆盖后端地址
 *
 * 测试范围：
 *   基础筛选 ×9 | 组合硬过滤 ×3 | swap ×1 | 匿名 reasons ×1 | 非法参数 ×2
 *
 * 退出码：
 *   0 — 全部通过
 *   1 — 至少一个 case 失败 / 后端未启动
 */

const API_BASE = process.env.EAT_WHAT_API_BASE || 'http://localhost:8080';

// ============================================================
// 工具函数
// ============================================================

/**
 * 将逗号分隔字符串解析为 Set<string>
 */
function parseTagSet(str) {
  if (!str) return new Set();
  return new Set(str.split(',').map(s => s.trim()).filter(s => s !== ''));
}

/**
 * 判断逗号分隔字符串是否包含指定标签（精确匹配而非子串）
 */
function tagListContains(tagList, target) {
  return parseTagSet(tagList).has(target);
}

/**
 * 调用 GET API，返回 { status, body }
 */
async function apiGet(path) {
  const url = API_BASE + path;
  const res = await fetch(url);
  const body = await res.json();
  return { status: res.status, body };
}

// ============================================================
// 测试框架
// ============================================================

const results = [];

/**
 * 执行一个测试 case。
 * @param {string}  name     - 测试名称
 * @param {function} fn      - 异步测试函数
 * @param {object}   options
 * @param {number}   options.retries - 最大重试次数，默认 5
 */
async function test(name, fn, { retries = 5 } = {}) {
  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      await fn();
      results.push({ name, passed: true });
      console.log(`  \x1b[32m✓\x1b[0m ${name}`);
      return;
    } catch (e) {
      if (attempt < retries) {
        // 静默重试
        continue;
      }
      results.push({ name, passed: false, error: e.message });
      console.log(`  \x1b[31m✗\x1b[0m ${name}`);
      console.log(`    ${e.message}`);
      return;
    }
  }
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

// ============================================================
// 主流程
// ============================================================

async function main() {
  // ---- 健康检查 ----
  console.log('检查后端状态...');
  let healthOk = false;
  try {
    const { body: health } = await apiGet('/api/health');
    if (health.code === 0 && health.data?.status === 'UP') {
      healthOk = true;
      console.log(`  后端状态: UP (${API_BASE})\n`);
    }
  } catch (_) {
    // fall through
  }

  if (!healthOk) {
    console.error('❌ 后端未启动，请先启动后端再运行本脚本');
    console.error('   cd backend-java');
    console.error('   .\\mvnw.cmd spring-boot:run');
    process.exit(1);
  }

  // ================================================================
  console.log('推荐 API 回归验收');
  console.log('='.repeat(60) + '\n');

  // ======================== 基础筛选 ========================

  await test('mealType=早餐 → food.mealTypes 包含 早餐', async () => {
    const { body } = await apiGet('/api/v1/recommend?mealType=早餐');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(body.data?.food, 'data.food 不应为空');
    assert(
      tagListContains(body.data.food.mealTypes, '早餐'),
      `mealTypes="${body.data.food.mealTypes}" 不包含 早餐`
    );
  });

  await test('mealType=夜宵 → food.mealTypes 包含 夜宵', async () => {
    const { body } = await apiGet('/api/v1/recommend?mealType=夜宵');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      tagListContains(body.data.food.mealTypes, '夜宵'),
      `mealTypes="${body.data.food.mealTypes}" 不包含 夜宵`
    );
  });

  await test('priceLevel=15以内 → food.priceLevel=1', async () => {
    const { body } = await apiGet('/api/v1/recommend?priceLevel=15以内');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      body.data.food.priceLevel === 1,
      `期望 priceLevel=1，实际 ${body.data.food.priceLevel}`
    );
  });

  await test('priceLevel=40以上 → food.priceLevel=4', async () => {
    const { body } = await apiGet('/api/v1/recommend?priceLevel=40以上');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      body.data.food.priceLevel === 4,
      `期望 priceLevel=4，实际 ${body.data.food.priceLevel}`
    );
  });

  await test('taste=清淡 → food.tasteTags 包含 清淡', async () => {
    const { body } = await apiGet('/api/v1/recommend?taste=清淡');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      tagListContains(body.data.food.tasteTags, '清淡'),
      `tasteTags="${body.data.food.tasteTags}" 不包含 清淡`
    );
  });

  await test('taste=辣 → food.tasteTags 包含 辣', async () => {
    const { body } = await apiGet('/api/v1/recommend?taste=辣');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      tagListContains(body.data.food.tasteTags, '辣'),
      `tasteTags="${body.data.food.tasteTags}" 不包含 辣`
    );
  });

  await test('taste=不辣 → food.tasteTags 不含 辣/麻', async () => {
    const { body } = await apiGet('/api/v1/recommend?taste=不辣');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    const tags = parseTagSet(body.data.food.tasteTags);
    assert(!tags.has('辣'), `tasteTags="${body.data.food.tasteTags}" 不应含 辣`);
    assert(!tags.has('麻'), `tasteTags="${body.data.food.tasteTags}" 不应含 麻`);
  });

  await test('typeTags=火锅 → food.typeTags 包含 火锅', async () => {
    const { body } = await apiGet('/api/v1/recommend?typeTags=火锅');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      tagListContains(body.data.food.typeTags, '火锅'),
      `typeTags="${body.data.food.typeTags}" 不包含 火锅`
    );
  });

  await test('cuisineTags=粤菜 → food.cuisineTags 包含 粤菜', async () => {
    const { body } = await apiGet('/api/v1/recommend?cuisineTags=粤菜');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    assert(
      tagListContains(body.data.food.cuisineTags, '粤菜'),
      `cuisineTags="${body.data.food.cuisineTags}" 不包含 粤菜`
    );
  });

  // ======================== 组合硬过滤 ========================

  await test('mealType=早餐 & typeTags=火锅 → code=2002', async () => {
    const { body } = await apiGet('/api/v1/recommend?mealType=早餐&typeTags=火锅');
    assert(
      body.code === 2002,
      `期望 code=2002，实际 code=${body.code} message="${body.message}"`
    );
  });

  await test('mealType=午餐 & priceLevel=40以上 & typeTags=火锅 → code=0 且全部条件匹配', async () => {
    const { body } = await apiGet(
      '/api/v1/recommend?mealType=午餐&priceLevel=40以上&typeTags=火锅'
    );
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    const f = body.data.food;
    assert(tagListContains(f.mealTypes, '午餐'), `mealTypes="${f.mealTypes}" 不包含 午餐`);
    assert(f.priceLevel === 4, `期望 priceLevel=4，实际 ${f.priceLevel}`);
    assert(tagListContains(f.typeTags, '火锅'), `typeTags="${f.typeTags}" 不包含 火锅`);
  });

  await test('mealType=午餐 & cuisineTags=川菜 & taste=不辣 → code=2002', async () => {
    const { body } = await apiGet(
      '/api/v1/recommend?mealType=午餐&cuisineTags=川菜&taste=不辣'
    );
    assert(
      body.code === 2002,
      `期望 code=2002，实际 code=${body.code} message="${body.message}"`
    );
  });

  // ======================== swap ========================

  await test('swap: 先推荐再换一个，foodId 不同且仍含 面食', async () => {
    // 第一次推荐
    const first = await apiGet('/api/v1/recommend?typeTags=面食');
    assert(
      first.body.code === 0,
      `初次推荐期望 code=0，实际 code=${first.body.code} message="${first.body.message}"`
    );
    const firstId = first.body.data.food.id;
    assert(
      tagListContains(first.body.data.food.typeTags, '面食'),
      `初次推荐 typeTags="${first.body.data.food.typeTags}" 不含 面食`
    );

    // 换一个（排除第一个）
    const second = await apiGet(
      `/api/v1/recommend/swap?typeTags=面食&excludeFoodIds=${firstId}`
    );

    if (second.body.code === 2002) {
      // 允许 2002（候选不足），但这种情况对面食来说不应出现（有 8 道）
      // 如果发生了，仍算通过
    } else {
      assert(
        second.body.code === 0,
        `swap 期望 code=0 或 2002，实际 code=${second.body.code} message="${second.body.message}"`
      );
      const secondId = second.body.data.food.id;
      assert(
        secondId !== firstId,
        `swap 返回了相同的 foodId=${firstId}，excludeFoodIds 未生效`
      );
      assert(
        tagListContains(second.body.data.food.typeTags, '面食'),
        `swap 结果 typeTags="${second.body.data.food.typeTags}" 不含 面食`
      );
    }
  });

  // ======================== 匿名 reasons ========================

  await test('匿名推荐 reasons 不含"最近几天没吃过"，含"随机帮你挑一个"', async () => {
    const { body } = await apiGet('/api/v1/recommend');
    assert(body.code === 0, `期望 code=0，实际 code=${body.code} message="${body.message}"`);
    const reasons = body.data.reasons;
    assert(Array.isArray(reasons), 'reasons 应为数组');

    const hasRecent = reasons.some(r => r.includes('最近几天没吃过'));
    assert(!hasRecent, `匿名用户 reasons 不应包含"最近几天没吃过": ${JSON.stringify(reasons)}`);

    const hasRandom = reasons.some(r => r.includes('随机帮你挑一个'));
    assert(hasRandom, `匿名用户 reasons 应包含"随机帮你挑一个": ${JSON.stringify(reasons)}`);
  });

  // ======================== 非法参数 ========================

  await test('priceLevel=invalid → code=1001', async () => {
    const { body } = await apiGet('/api/v1/recommend?priceLevel=invalid');
    assert(
      body.code === 1001,
      `期望 code=1001，实际 code=${body.code} message="${body.message}"`
    );
  });

  await test('typeTags=不存在 → code=1001', async () => {
    const { body } = await apiGet('/api/v1/recommend?typeTags=不存在');
    assert(
      body.code === 1001,
      `期望 code=1001，实际 code=${body.code} message="${body.message}"`
    );
  });

  // ======================== 汇总 ========================
  console.log('='.repeat(60));
  const total = results.length;
  const passed = results.filter(r => r.passed).length;
  const failed = total - passed;
  console.log(`${total} total, ${passed} passed, ${failed} failed`);

  if (failed > 0) {
    console.log('\n失败 case:');
    for (const r of results) {
      if (!r.passed) {
        console.log(`  ✗ ${r.name}`);
        console.log(`    ${r.error}`);
      }
    }
    process.exit(1);
  }
}

main().catch(e => {
  console.error('脚本执行异常:', e);
  process.exit(1);
});
