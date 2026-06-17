# 管理反馈 CLI 工具

> 今天吃啥 MVP 阶段无管理后台 UI，通过 PowerShell 脚本调用 admin feedback API 查看和处理用户反馈。

---

## 一、使用方法

### 1.1 设置认证 Token

```powershell
# 方式一：环境变量（推荐，避免每次输入）
$env:ADMIN_TOKEN = "your-admin-token-here"

# 方式二：每次传参
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Token "your-admin-token" -Action list
```

Token 即为 `deploy/.env` 中配置的 `ADMIN_TOKEN`，上线前由 `openssl rand -base64 24` 生成。

### 1.2 基本命令

```powershell
# 查看所有反馈（默认每页 20 条）
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action list

# 按状态筛选
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action list -Status NEW

# 搜索关键词（匹配 content 和 contact 字段）
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action list -Keyword 推荐

# 翻页
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action list -Page 2 -Size 50

# 查看单条详情
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action show -Id 1

# 更新状态
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action status -Id 1 -NewStatus REVIEWED
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action status -Id 1 -NewStatus RESOLVED
.\scripts\admin\feedback.ps1 -BaseUrl https://api.example.com -Action status -Id 1 -NewStatus IGNORED
```

---

## 二、参数说明

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `-BaseUrl` | 是 | — | 后端 API 地址，自动去掉末尾 `/` |
| `-Token` | 否 | `$env:ADMIN_TOKEN` | 管理后台认证 Token，参数优先于环境变量 |
| `-Action` | 是 | — | 操作类型：`list` / `show` / `status` |
| `-Status` | 否 | — | 按状态筛选：`NEW` / `REVIEWED` / `RESOLVED` / `IGNORED` |
| `-Keyword` | 否 | — | 搜索关键词，匹配 content 和 contact |
| `-Page` | 否 | `1` | 页码 |
| `-Size` | 否 | `20` | 每页条数，最大 100 |
| `-Id` | 条件 | — | 反馈 ID，`show` / `status` 时必填 |
| `-NewStatus` | 条件 | — | 新状态，`status` 时必填 |

---

## 三、输出格式

### 3.1 list — 反馈列表

表格字段：`id`、`type`、`rating`、`status`、`createdAt`、`content`（前 40 字）、`contact`

```
id type          rating status  createdAt             content                                   contact
-- ----          ------ ------  ---------             -------                                   -------
 1 FEATURE       4/5    NEW     2026-06-17 10:30:00  推荐结果总是一样的，能不能换...            test@example.com
 2 BUG           2/5    NEW     2026-06-17 09:15:00  点击"我就吃它"后页面闪...                 -
```

### 3.2 show — 单条详情

输出所有可用字段：
- `id`、`type`、`rating`、`status`、`content`、`contact`、`page`（来源页面）、`systemInfo`（系统信息 JSON）、`createdAt`

> 注意：`userId` 和 `updatedAt` 字段当前 API 版本未返回。

### 3.3 status — 状态更新

输出更新后的 `id`、`status`、`createdAt`。

---

## 四、Token 安全

- Token 读取优先级：`-Token` 参数 > 环境变量 `ADMIN_TOKEN`
- Token 缺失时明确报错并退出（exit 1）
- **Token 不会出现在任何输出中**
- `ADMIN_TOKEN` 应与 `deploy/.env` 中的值一致

---

## 五、错误处理

| 场景 | 行为 |
|------|------|
| Token 缺失 | `[ERROR] 缺少认证 Token` → exit 1 |
| 网络错误 | `[ERROR] 网络错误: ...` → exit 1 |
| API 返回 `code != 0` | `[ERROR] API 返回错误: code=..., message=...` → exit 1 |
| show 未找到 ID | `[WARN] 未找到反馈 id=xxx` → exit 1 |
| 缺少必填参数 | PowerShell 自动提示 → exit 1 |

---

## 六、后端 API 参考

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/v1/admin/feedback` | 查询反馈列表，支持 `status` / `keyword` / `page` / `size` |
| `PUT` | `/api/v1/admin/feedback/{id}/status` | 更新反馈状态，Body: `{ "status": "..." }` |

认证方式：请求头 `X-Admin-Token`。

---

## 七、相关工作流建议

```
# 1. 查看待处理反馈
.\scripts\admin\feedback.ps1 -BaseUrl https://api.your-domain.com -Action list -Status NEW

# 2. 查看某条详情
.\scripts\admin\feedback.ps1 -BaseUrl https://api.your-domain.com -Action show -Id 5

# 3. 标记为已查看
.\scripts\admin\feedback.ps1 -BaseUrl https://api.your-domain.com -Action status -Id 5 -NewStatus REVIEWED

# 4. 处理完毕
.\scripts\admin\feedback.ps1 -BaseUrl https://api.your-domain.com -Action status -Id 5 -NewStatus RESOLVED
```

---

## 八、已知限制

1. **无后端管理 UI**：MVP 阶段仅有此 CLI 工具，无 Web 后台页面
2. **反馈无通知**：用户提交反馈后无自动通知（如邮件/站内信），需主动查询
3. **`userId` / `updatedAt` 未返回**：当前 API DTO 未映射这两个字段，后续版本可扩展
4. **`show` 通过 list 遍历查找**：无单条详情接口，按 id 在 list 中逐页搜索，反馈量大时较慢
