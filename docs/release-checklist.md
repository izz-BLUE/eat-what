# 发布前检查清单

> 今天吃啥 微信小程序 MVP v1.0 发布前质量收口
> 生成日期：2026-06-16

---

## 一、启动命令

### 后端

```powershell
# 1. 环境变量
$env:SPRING_PROFILES_ACTIVE="dev"
$env:JWT_SECRET="eat-what-dev-jwt-secret-at-least-32-bytes"
$env:WECHAT_MOCK_ENABLED="true"
$env:ADMIN_TOKEN="dev-admin-token"

# 2. 启动 MySQL
docker compose up -d

# 3. 运行测试
cd backend-java
.\mvnw.cmd clean test

# 4. 启动后端
.\mvnw.cmd spring-boot:run

# 5. 验证健康检查
Invoke-RestMethod http://localhost:8080/api/health
```

### 前端

```bash
# 1. 微信开发者工具打开 miniprogram 目录
# 2. 详情 → 本地设置 → 勾选"不校验合法域名"
# 3. 确认后端已启动（默认 http://localhost:8080）
# 4. 点击"编译"预览
```

### 菜品数据管线

```bash
npm run foods:validate   # 校验 foods.csv
npm run foods:generate   # 生成 Flyway 迁移
npm run foods:test       # 数据管线测试
```

---

## 二、验收路径

### 用户端核心流程

1. **打开小程序** → 首页加载，筛选区域可见
2. **选择偏好** → 餐段 / 预算 / 口味 / 分类
3. **一键推荐** → 获取推荐菜品（无需登录也可使用）
4. **换一个** → 排除已推荐，重新推荐（最多 10 次）
5. **重置筛选** → 筛选区域提供一键重置
6. **我就吃它** → 需要登录，创建 DECIDED 记录
7. **完成用餐** → 从 DECIDED 转为 EATEN，填写评分和备注
8. **查看历史** → 吃过记录列表（DECIDED 排最前）
9. **黑名单** → 加入/移出黑名单
10. **不想吃** → 添加/解除临时不想吃分类
11. **意见反馈** → 无需登录即可提交

### 管理端流程

12. **查询反馈列表** → 需要 `X-Admin-Token`
13. **更新反馈状态** → NEW → REVIEWED → RESOLVED / IGNORED

---

## 三、必测页面

| 页面 | 路径 | 关键验证 |
|------|------|---------|
| 首页 | `pages/index/index` | 推荐筛选、一键推荐、换一个、我就吃它 |
| 登录 | `pages/login/login` | 微信登录（Mock 模式） |
| 个人中心 | `pages/profile/profile` | 用户信息、菜单跳转 |
| 历史记录 | `pages/history/history` | 记录列表、DECIDED 置顶 |
| 黑名单 | `pages/blacklist/blacklist` | 加入/移出 |
| 不想吃 | `pages/dislike/dislike` | 添加/解除 |
| 用餐记录 | `pages/record/record` | 完成用餐、评价 |
| 意见反馈 | `pages/feedback/feedback` | 类型选择、星级评分、匿名提交 |

---

## 四、必测接口

| 方法 | 路径 | 认证 | 验证点 |
|------|------|------|--------|
| POST | `/api/v1/user/login` | 无 | 登录成功返回 token |
| GET | `/api/v1/foods` | 无 | 返回 73 道菜品 |
| GET | `/api/v1/recommend` | 可选 | 匿名/登录均可推荐 |
| GET | `/api/v1/recommend/swap` | 可选 | 排除已推荐 |
| POST | `/api/v1/record/decide` | 必须 | 创建 DECIDED 记录 |
| POST | `/api/v1/record/{id}/complete` | 必须 | DECIDED → EATEN |
| PUT | `/api/v1/record/{id}/review` | 必须 | 修改评价 |
| DELETE | `/api/v1/record/{id}/decision` | 必须 | 取消决定 |
| GET | `/api/v1/record/list` | 必须 | 记录列表 |
| POST | `/api/v1/blacklist/add` | 必须 | 加入黑名单 |
| GET | `/api/v1/blacklist/list` | 必须 | 黑名单列表 |
| DELETE | `/api/v1/blacklist/{id}` | 必须 | 移出黑名单 |
| POST | `/api/v1/dislike/add` | 必须 | 添加不想吃 |
| GET | `/api/v1/dislike/list` | 必须 | 不想吃列表 |
| DELETE | `/api/v1/dislike/{id}` | 必须 | 解除不想吃 |
| POST | `/api/v1/feedback` | 无 | 匿名/登录提交 |
| GET | `/api/v1/admin/feedback` | Admin | 查询反馈列表 |
| PUT | `/api/v1/admin/feedback/{id}/status` | Admin | 更新反馈状态 |
| GET | `/api/health` | 无 | 健康检查 |

### 安全接口验证

| 场景 | 预期 |
|------|------|
| 无 token 访问 record/list | 返回 1003 |
| 错误 token 访问 record/list | 返回 1003 |
| 无 X-Admin-Token 访问 admin/feedback | 返回 3001 |
| 错误 X-Admin-Token 访问 admin/feedback | 返回 3001 |
| 反馈内容不足 5 字 | 返回 1001 |
| 更新不存在反馈状态 | 返回 2012 |

---

## 五、已知限制

1. **饭局投票未实现**：接口已设计但 MVP 未实现（后续阶段 v1.1）
2. **管理反馈无前端 UI**：`/api/v1/admin/*` 只有 API，需通过 curl/PowerShell 操作，无后台管理页面
3. **反馈无通知**：用户提交反馈后无自动通知（如邮件/站内信），需主动查询 admin API
4. **偏好不跨设备**：筛选偏好使用小程序本地存储（`wx.setStorageSync`），换设备后丢失
5. **微信 Mock 依赖**：dev/test 环境使用 Mock 微信登录，生产环境需真实微信 AppID 和 AppSecret
6. **发布前需备案**：小程序正式发布前需完成微信审核、配置合法域名（生产 API 地址）、配置业务域名

---

## 六、发布前检查命令

```bash
# 1. 后端测试
cd backend-java
.\mvnw.cmd clean test
# 预期：318 tests, BUILD SUCCESS

# 2. 小程序类型检查
cd ..\miniprogram
npx tsc --noEmit
# 预期：零错误

# 3. 菜品数据校验
cd ..
npm run foods:validate
# 预期：✅ 校验通过：73 道菜全部合法

# 4. 菜品数据测试
npm run foods:test
# 预期：81 pass, 0 fail

# 5. 推荐 API 回归验收
npm run e2e:recommend
# 预期：16 total, 16 passed, 0 failed

# 6. 评分偏好 API 验收（会创建测试用户和 eat_records）
npm run e2e:rating-preference
# 预期：4 total, 4 passed, 0 failed
# 注意：本脚本会写入测试数据，仅适合 dev/test 环境，不建议生产运行

# 7. 代码冲突检查
git diff --check
# 预期：无输出（无冲突）
```

---

## 七、环境变量速查

| 变量 | 默认值（dev） | 说明 |
|------|-------------|------|
| `SPRING_PROFILES_ACTIVE` | `dev` | 激活 dev Profile |
| `JWT_SECRET` | `eat-what-dev-jwt-secret-at-least-32-bytes` | JWT 密钥，≥32 字节 |
| `WECHAT_MOCK_ENABLED` | `true` | 微信 Mock 模式 |
| `ADMIN_TOKEN` | `dev-admin-token` | 管理后台认证 token |
| `DB_PASSWORD` | `eatwhat_dev` | MySQL 密码 |
