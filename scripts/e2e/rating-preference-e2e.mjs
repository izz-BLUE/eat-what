#!/usr/bin/env node
/**
 * rating-preference-e2e.mjs — 评分偏好真实接口回归验收脚本
 *
 * 使用方式：
 *   node scripts/e2e/rating-preference-e2e.mjs
 *   npm run e2e:rating-preference
 *
 * 前置条件：
 *   后端必须已启动（默认 http://localhost:8080）
 *   环境变量 EAT_WHAT_API_BASE 可覆盖
 *
 * 副作用：
 *   创建测试用户 + eat_records + blacklist 记录。
 *   使用唯一 mock code 避免污染已有用户。
 *   没有删除接口，测试数据保留在数据库。
 *
 * 退出码：
 *   0 — 全部通过
 *   1 — 任一失败 / 后端未启动
 */

const API_BASE = process.env.EAT_WHAT_API_BASE || 'http://localhost:8080';
const MOCK_CODE = `e2e_rating_preference_${Date.now()}`;

// ============================================================
// 状态
// ============================================================

let token = null;
let userId = null;

// ============================================================
// 工具函数
// ============================================================

function tagListContains(tagListStr, target) {
  if (!tagListStr) return false;
  return tagListStr.split(',').map(s => s.trim()).filter(Boolean).includes(target);
}

async function apiGet(path, withAuth = false) {
  const headers = {};
  if (withAuth && token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(API_BASE + path, { headers });
  const body = await res.json();
  return { status: res.status, body };
}

async function apiPost(path, data, withAuth = true) {
  const headers = { 'Content-Type': 'application/json' };
  if (withAuth && token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers,
    body: JSON.stringify(data),
  });
  const body = await res.json();
  return { status: res.status, body };
}

// ============================================================
// 测试框架
// ============================================================

const results = [];

async function test(name, fn, { retries = 5 } = {}) {
  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      await fn();
      results.push({ name, passed: true });
      console.log(`  \x1b[32m✓\x1b[0m ${name}`);
      return;
    } catch (e) {
      if (attempt < retries) {
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

// Sleep helper
const sleep = ms => new Promise(r => setTimeout(r, ms));

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
  } catch (_) { /* fall through */ }

  if (!healthOk) {
    console.error('❌ 后端未启动，请先启动后端');
    console.error('   cd backend-java && .\\mvnw.cmd spring-boot:run');
    process.exit(1);
  }

  // ---- 登录 ----
  console.log('创建测试用户...');
  const loginRes = await apiPost('/api/v1/user/login', { code: MOCK_CODE }, false);
  assert(loginRes.body.code === 0, `登录失败: code=${loginRes.body.code} ${loginRes.body.message}`);
  token = loginRes.body.data.token;
  userId = loginRes.body.data.userId;
  assert(token, 'token 不应为空');
  assert(userId, 'userId 不应为空');
  console.log(`  userId=${userId}  token=${token.substring(0, 20)}...\n`);

  // ================================================================
  console.log('评分偏好回归验收');
  console.log('='.repeat(60) + '\n');

  // ==================== Case A：正反馈影响 ====================

  await test('正反馈: 湘菜 5星 → 推荐含"符合你以往喜欢的口味/类型"', async () => {
    // 1. 获取一个湘菜 foodId
    const r1 = await apiGet('/api/v1/recommend?cuisineTags=湘菜');
    assert(r1.body.code === 0, `获取湘菜食物失败: code=${r1.body.code}`);
    const foodA = r1.body.data.food;
    console.log(`    选中: ${foodA.name} (id=${foodA.id}, typeTags="${foodA.typeTags}", cuisineTags="${foodA.cuisineTags}", tasteTags="${foodA.tasteTags}")`);

    // 2. 给 5 星
    const eatRes = await apiPost('/api/v1/record/eat', {
      foodId: foodA.id,
      mealType: '午餐',
      rating: 5,
    });
    assert(eatRes.body.code === 0, `创建 EATEN 记录失败: code=${eatRes.body.code} ${eatRes.body.message}`);
    console.log(`    记录已创建: recordId=${eatRes.body.data?.id} rating=${eatRes.body.data?.rating} status=${eatRes.body.data?.status}`);

    // 3. 重新推荐（同一 cuisineTags），应出现偏好理由
    //    因为候选全被 cuisineTags=湘菜 硬过滤，全部命中评分记录，必然 ratingBonus>0
    let reasonFound = false;
    for (let i = 0; i < 10; i++) {
      const r2 = await apiGet('/api/v1/recommend?cuisineTags=湘菜', true);
      assert(r2.body.code === 0, `推荐失败: code=${r2.body.code}`);
      const reasons = r2.body.data.reasons || [];
      // 首次尝试打印完整 reasons 以便调试
      if (i === 0) {
        console.log(`    第1次推荐 reasons: ${JSON.stringify(reasons)}`);
      }
      if (reasons.some(r => r.includes('符合你以往喜欢的口味/类型'))) {
        reasonFound = true;
        break;
      }
      if (i < 9) await sleep(50);
    }
    assert(reasonFound,
      '10 次推荐均未出现"符合你以往喜欢的口味/类型"。' +
      ' 可能原因：后端未加载评分偏好代码（需重启后端）或 userId 未正确传递。' +
      ` 已创建 ${foodA.name}(id=${foodA.id}) 的 rating=5 记录。`);
  }, { retries: 3 });

  // ==================== Case B：负反馈影响 ====================

  await test('负反馈: 小吃 1星 → 同类仍可推荐且无负面用户文案', async () => {
    // 1. 获取一个小吃 foodId（用 swap 确保和之前的不是同一个）
    const r1 = await apiGet('/api/v1/recommend?typeTags=小吃', true);
    assert(r1.body.code === 0, `获取小吃食物失败: code=${r1.body.code}`);
    const foodB = r1.body.data.food;
    console.log(`    选中: ${foodB.name} (id=${foodB.id})`);

    // 2. 给 1 星
    const eatRes = await apiPost('/api/v1/record/eat', {
      foodId: foodB.id,
      mealType: '午餐',
      rating: 1,
    });
    assert(eatRes.body.code === 0, `创建 EATEN 记录失败: code=${eatRes.body.code}`);

    // 3. 重新推荐小吃 —— 不应被硬过滤（code=0）
    const r2 = await apiGet('/api/v1/recommend?typeTags=小吃', true);
    assert(r2.body.code === 0, `小吃推荐被错误过滤: code=${r2.body.code} ${r2.body.message}`);

    // 4. 不应出现任何面向用户的负面文案
    const reasons = r2.body.data.reasons || [];
    const negativeTexts = ['你可能不喜欢', '你给了差评', '低分', '难吃', '不符合你的口味'];
    for (const neg of negativeTexts) {
      const found = reasons.some(r => r.includes(neg));
      assert(!found, `reasons 不应包含负面文案 "${neg}": ${JSON.stringify(reasons)}`);
    }
  });

  // ==================== Case C：匿名不启用评分偏好 ====================

  await test('匿名推荐 reasons 不含"符合你以往喜欢的口味/类型"', async () => {
    const r = await apiGet('/api/v1/recommend'); // 不带 token
    assert(r.body.code === 0, `匿名推荐失败: code=${r.body.code}`);
    const reasons = r.body.data.reasons || [];
    const hasPreferenceReason = reasons.some(r => r.includes('符合你以往喜欢的口味/类型'));
    assert(!hasPreferenceReason, `匿名用户不应出现评分偏好理由: ${JSON.stringify(reasons)}`);
  });

  // ==================== Case D：黑名单硬过滤优先 ====================

  await test('黑名单硬过滤优先: 高分菜加入黑名单后不再出现', async () => {
    // 1. 获取一个火锅 foodId
    const r1 = await apiGet('/api/v1/recommend?typeTags=火锅', true);
    assert(r1.body.code === 0, `获取火锅食物失败: code=${r1.body.code}`);
    const foodD = r1.body.data.food;
    console.log(`    选中: ${foodD.name} (id=${foodD.id})`);

    // 2. 给 5 星（高分菜）
    const eatRes = await apiPost('/api/v1/record/eat', {
      foodId: foodD.id,
      mealType: '午餐',
      rating: 5,
    });
    assert(eatRes.body.code === 0, `创建 EATEN 记录失败: code=${eatRes.body.code}`);

    // 3. 加入黑名单
    const blRes = await apiPost('/api/v1/blacklist/add', {
      foodId: foodD.id,
      reason: 'e2e 测试：验证黑名单优先于评分偏好',
    });
    assert(blRes.body.code === 0, `加入黑名单失败: code=${blRes.body.code} ${blRes.body.message}`);

    // 4. 多次推荐，验证该 foodId 永不出现
    //    黑名单是硬过滤，所以一定不会出现
    for (let i = 0; i < 5; i++) {
      const r2 = await apiGet('/api/v1/recommend?typeTags=火锅', true);
      if (r2.body.code === 0) {
        const returnedId = r2.body.data.food.id;
        assert(returnedId !== foodD.id,
          `第 ${i + 1} 次推荐返回了已黑名单食物 id=${foodD.id}`);
      } else {
        // 2002 也接受（其他候选都不符合）
        assert(r2.body.code === 2002,
          `期望 code=0 或 2002，实际 ${r2.body.code}: ${r2.body.message}`);
      }
    }

    // 5. swap 也应排除黑名单食物
    const swapRes = await apiGet(`/api/v1/recommend/swap?typeTags=火锅&excludeFoodIds=99999`, true);
    if (swapRes.body.code === 0) {
      assert(swapRes.body.data.food.id !== foodD.id,
        `swap 返回了已黑名单食物 id=${foodD.id}`);
    }
    // 2002 也接受（黑名单排除后无可推荐）
  });

  // ======================== 汇总 ========================
  console.log('');
  console.log('='.repeat(60));
  const total = results.length;
  const passed = results.filter(r => r.passed).length;
  const failed = total - passed;
  console.log(`${total} total, ${passed} passed, ${failed} failed`);

  console.log('');
  console.log(`测试用户 userId=${userId}，其数据保留在数据库中（未自动清理）`);

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
