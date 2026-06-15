# 今天吃啥 - 接口文档

## 概述

- 基础路径：`/api/v1`
- 数据格式：JSON
- 认证方式：微信登录 + JWT Token
- 统一响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

**错误码定义**：
| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 1001 | 参数错误 |
| 1002 | 参数缺失 |
| 1003 | 未登录（token 缺失、无效或过期） |
| 2001 | 用户不存在 |
| 2002 | 食物不存在 |
| 2003 | 投票不存在 |
| 2004 | 投票已结束 |
| 2005 | 已投过票 |
| 2006 | 超过投票上限 |
| 2007 | 黑名单记录不存在 |
| 2008 | 不想吃记录不存在 |
| 2009 | 微信登录失败 |
| 2010 | 记录不存在 |
| 2011 | 记录状态不允许此操作 |
| 5001 | 系统错误 |

---

## 认证说明

### 微信登录流程

1. 小程序调用 `wx.login()` 获取临时 code
2. 小程序将 code 发送到后端 `POST /api/v1/user/login`
3. 后端使用 code 调用微信 `code2Session` 接口获取 openid
4. 后端根据 openid 查询/创建用户
5. 后端生成 JWT token 返回给小程序
6. 小程序后续请求携带 `Authorization: Bearer {token}`

### 安全边界

- **禁止**前端直接传递 openid 作为可信身份
- openid 和 session_key **不会**下发给前端
- JWT token 包含 userId，有效期默认 7 天
- 无效或过期 token 统一返回 code=1003
- 公开接口携带无效 token 也会返回 1003（不允许静默降级）

### 开发环境 Mock

- 仅在 `dev` 和 `test` Profile 下启用微信 Mock
- 配置 `WECHAT_MOCK_ENABLED=true` 启用 Mock
- Mock 模式下 `code=dev-user-1` 会生成稳定的 mock openid
- **生产环境即使配置 mock-enabled=true 也不会启用 Mock**

### 本地启动配置

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:JWT_SECRET="eat-what-dev-jwt-secret-at-least-32-bytes"
$env:WECHAT_MOCK_ENABLED="true"
```

---

## 接口总览

### 当前已实现

| 序号 | 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 1 | /api/v1/user/login | POST | 无需登录 | 微信登录 |
| 2 | /api/v1/foods | GET | 无需登录 | 查询菜品列表 |
| 3 | /api/v1/recommend | GET | 可选登录 | 一键推荐 |
| 4 | /api/v1/recommend/swap | GET | 可选登录 | 换一个 |
| 5 | /api/v1/record/eat | POST | 必须登录 | 我就吃它（旧接口，保留兼容） |
| 6 | /api/v1/record/decide | POST | 必须登录 | 决定吃什么（创建 DECIDED 记录） |
| 7 | /api/v1/record/{id}/complete | POST | 必须登录 | 完成用餐（DECIDED → EATEN） |
| 8 | /api/v1/record/{id}/review | PUT | 必须登录 | 修改已吃记录的评价 |
| 9 | /api/v1/record/{id}/decision | DELETE | 必须登录 | 取消决定（删除 DECIDED 记录） |
| 10 | /api/v1/record/{id} | GET | 必须登录 | 获取单条记录详情 |
| 11 | /api/v1/record/list | GET | 必须登录 | 吃过记录列表 |
| 7 | /api/v1/blacklist/add | POST | 必须登录 | 加入黑名单 |
| 8 | /api/v1/blacklist/list | GET | 必须登录 | 黑名单列表 |
| 9 | /api/v1/blacklist/{blacklistId} | DELETE | 必须登录 | 移出黑名单 |
| 10 | /api/v1/dislike/add | POST | 必须登录 | 添加不想吃 |
| 11 | /api/v1/dislike/list | GET | 必须登录 | 不想吃列表 |
| 12 | /api/v1/dislike/{dislikeId} | DELETE | 必须登录 | 解除不想吃 |
| 13 | /api/health | GET | 无需登录 | 健康检查 |

### 后续阶段实现

| 序号 | 接口 | 方法 | 说明 | 阶段 |
|------|------|------|------|------|
| 14 | /api/v1/vote/create | POST | 发起投票 | M3 |
| 15 | /api/v1/vote/{id} | GET | 获取投票详情 | M3 |
| 16 | /api/v1/vote/{id}/vote | POST | 投票 | M3 |

---

## 用户相关

### 1. 微信登录

**POST** `/api/v1/user/login`

微信小程序登录，获取用户信息和 token。

**请求参数**：
```json
{
  "code": "wx.login 获取的临时 code",
  "nickname": "可选昵称",
  "avatarUrl": "可选头像"
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | string | 是 | wx.login 获取的临时 code |
| nickname | string | 否 | 用户昵称 |
| avatarUrl | string | 否 | 用户头像 URL |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 604800,
    "userId": 1,
    "nickname": "测试用户",
    "avatarUrl": "https://xxx.jpg"
  }
}
```

**业务逻辑**：
1. 使用 code 调用微信 code2Session 接口获取 openid
2. 根据 openid 查询用户，不存在则创建
3. 生成 JWT token（subject=userId）
4. 返回 token 和用户信息
5. 不返回 openid、session_key 等敏感信息

---

## 推荐相关

### 2. 一键推荐

**GET** `/api/v1/recommend`

根据参数推荐一道食物。

**推荐逻辑**：
- 无 token：基础推荐（餐段 + 价格 + 口味 + 分类 + 随机）
- 有 token：黑名单硬过滤 + 有效不想吃分类硬过滤 + 最近吃过降权

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段：早餐、午餐、晚餐、夜宵 |
| priceLevel | string | 否 | 价格偏好：15以内、15-25、25-40、不限 |
| taste | string | 否 | 口味偏好：清淡、重口、辣、不辣 |
| categories | string | 否 | 偏好分类，逗号分隔，最多3个，如：快餐,面食,粤菜 |

**请求头**（可选）：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "food": {
      "id": 1,
      "name": "猪脚饭",
      "category": "快餐",
      "tasteTags": "咸,香",
      "priceLevel": 2,
      "imageUrl": ""
    },
    "score": 45,
    "reasons": ["适合当前餐段", "符合预算", "最近几天没吃过，换换口味"]
  }
}
```

---

### 3. 换一个

**GET** `/api/v1/recommend/swap`

排除已推荐的食物，重新推荐。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段 |
| priceLevel | string | 否 | 价格偏好 |
| taste | string | 否 | 口味偏好 |
| excludeFoodIds | string | 否 | 排除的食物 ID，逗号分隔，如：1,2,3 |
| categories | string | 否 | 偏好分类，逗号分隔，最多3个 |

**请求头**（可选）：
```
Authorization: Bearer {token}
```

**响应数据**：与一键推荐相同。

---

## 菜品相关

### 4. 查询菜品列表

**GET** `/api/v1/foods`

查询启用的菜品列表。**无需登录**。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | 分类筛选 |
| priceLevel | int | 否 | 价格等级筛选 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "猪脚饭",
      "category": "快餐",
      "tasteTags": "咸,香",
      "priceLevel": 2,
      "imageUrl": ""
    }
  ]
}
```

---

## 吃过记录相关（两阶段生命周期）

用餐记录采用两阶段生命周期：
1. **DECIDED（已决定）**：用户点击"我就吃它"时创建，表示"今天决定吃这个"
2. **EATEN（已完成）**：用户吃完后评价，从 DECIDED 转为 EATEN

### 5. 我就吃它（旧接口，保留兼容）

**POST** `/api/v1/record/eat`

直接创建 EATEN 记录，保留兼容旧版本。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "foodId": 31,
  "mealType": "晚餐",
  "rating": 5,
  "note": "很好吃"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| foodId | long | 是 | 食物ID |
| mealType | string | 是 | 餐段：早餐、午餐、晚餐、夜宵 |
| rating | int | 否 | 评分 1-5 |
| note | string | 否 | 备注，最长 256 字符 |

---

### 6. 决定吃什么

**POST** `/api/v1/record/decide`

创建 DECIDED 记录。事务+行锁保证并发下每个用户最多一条 DECIDED。
同一菜品重复决定幂等返回原记录；不同菜品决定时替换旧 DECIDED。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "foodId": 31,
  "mealType": "晚餐"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| foodId | long | 是 | 食物ID |
| mealType | string | 是 | 餐段：早餐、午餐、晚餐、夜宵 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "foodId": 31,
    "foodName": "猪脚饭",
    "mealType": "晚餐",
    "status": "DECIDED",
    "rating": null,
    "note": null,
    "eatenAt": null,
    "decidedAt": "2024-01-15T12:30:00",
    "category": "快餐"
  }
}
```

---

### 7. 完成用餐

**POST** `/api/v1/record/{recordId}/complete`

将 DECIDED 记录转为 EATEN。要求记录属于当前用户且状态为 DECIDED。

**请求参数**：
```json
{
  "rating": 5,
  "note": "很好吃"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| rating | int | 否 | 评分 1-5 |
| note | string | 否 | 备注，最长 256 字符 |

---

### 8. 修改评价

**PUT** `/api/v1/record/{recordId}/review`

修改已吃记录的评分和备注。要求记录属于当前用户且状态为 EATEN。

**请求参数**：
```json
{
  "rating": 4,
  "note": "还不错"
}
```

---

### 9. 取消决定

**DELETE** `/api/v1/record/{recordId}/decision`

删除 DECIDED 记录。要求记录属于当前用户且状态为 DECIDED。EATEN 记录不可删除。

---

### 10. 获取单条记录

**GET** `/api/v1/record/{recordId}`

获取单条记录详情（含 foodName、category）。要求记录属于当前用户。

---

### 11. 获取吃过记录列表

**GET** `/api/v1/record/list`

获取用户用餐记录列表。**需要登录**。排序：DECIDED 排最前，然后按时间倒序。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | int | 否 | 返回数量，默认 20，范围 1-100 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 2,
      "foodId": 32,
      "foodName": "黄焖鸡",
      "mealType": "午餐",
      "status": "DECIDED",
      "rating": null,
      "note": null,
      "eatenAt": null,
      "decidedAt": "2024-01-15T12:30:00",
      "category": "快餐"
    },
    {
      "id": 1,
      "foodId": 31,
      "foodName": "猪脚饭",
      "mealType": "晚餐",
      "status": "EATEN",
      "rating": 5,
      "note": "很好吃",
      "eatenAt": "2024-01-14T18:30:00",
      "decidedAt": null,
      "category": "快餐"
    }
  ]
}
```

---

## 黑名单相关

### 12. 加入黑名单

**POST** `/api/v1/blacklist/add`

将食物加入黑名单。**需要登录**。重复添加幂等，如果 reason 变化则更新。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "foodId": 31,
  "reason": "不喜欢"
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| foodId | long | 是 | 食物ID |
| reason | string | 否 | 拉黑原因，最长 128 字符 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "foodId": 31,
    "foodName": "猪脚饭",
    "category": "快餐",
    "reason": "不喜欢",
    "createdAt": "2024-01-15T12:30:00"
  }
}
```

---

### 13. 获取黑名单列表

**GET** `/api/v1/blacklist/list`

获取用户的黑名单列表。**需要登录**。

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "foodId": 31,
      "foodName": "猪脚饭",
      "category": "快餐",
      "reason": "不喜欢",
      "createdAt": "2024-01-15T12:30:00"
    }
  ]
}
```

---

### 14. 移出黑名单

**DELETE** `/api/v1/blacklist/{blacklistId}`

将食物从黑名单中移除。**需要登录**。只能删除自己的黑名单记录。

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

## 不想吃相关

### 15. 添加不想吃

**POST** `/api/v1/dislike/add`

添加或更新不想吃的分类。**需要登录**。同一用户 + category 幂等，已过期记录重新添加会恢复生效。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "category": "火锅",
  "days": 3
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 是 | 食物分类，最长 32 字符 |
| days | int | 否 | 有效天数，默认 3，范围 1-30 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "category": "火锅",
    "expiresAt": "2024-01-18T12:30:00",
    "createdAt": "2024-01-15T12:30:00"
  }
}
```

---

### 16. 获取不想吃列表

**GET** `/api/v1/dislike/list`

获取用户有效的不想吃分类列表。**需要登录**。只返回未过期记录，按 expiresAt 升序。

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "category": "火锅",
      "expiresAt": "2024-01-18T12:30:00",
      "createdAt": "2024-01-15T12:30:00"
    }
  ]
}
```

---

### 17. 解除不想吃

**DELETE** `/api/v1/dislike/{dislikeId}`

解除不想吃的分类。**需要登录**。只能删除自己的记录。

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

## 后续阶段接口（需要登录）

### 13. 发起投票（后续阶段）

**POST** `/api/v1/vote/create`

发起一个新的饭局投票。

---

### 14. 获取投票详情（后续阶段）

**GET** `/api/v1/vote/{voteId}`

获取投票详情和当前结果。

---

### 15. 投票（后续阶段）

**POST** `/api/v1/vote/{voteId}/vote`

为投票选项投票。

---

## 接口验证示例

### PowerShell 验证（推荐，中文显示正常）

```powershell
# ========== 环境变量设置 ==========
$env:SPRING_PROFILES_ACTIVE="dev"
$env:JWT_SECRET="eat-what-dev-jwt-secret-at-least-32-bytes"
$env:WECHAT_MOCK_ENABLED="true"

# ========== 登录获取 token ==========
# 微信登录（Mock 模式）
$loginResult = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/user/login" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"code":"dev-user-1","nickname":"测试用户"}'))
$loginResult | ConvertTo-Json -Depth 8

# 提取 token
$token = $loginResult.data.token

# ========== 推荐接口（可选登录） ==========

# 一键推荐（无 token，基础推荐）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐" | ConvertTo-Json -Depth 8

# 一键推荐（有 token，个性化推荐：黑名单 + 不想吃 + 最近吃过降权）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# 换一个（无 token）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend/swap?mealType=晚餐&excludeFoodIds=31,32" | ConvertTo-Json -Depth 8

# 换一个（有 token）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend/swap?mealType=晚餐&excludeFoodIds=31,32" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# ========== 菜品接口（无需登录） ==========

# 查询菜品列表
Invoke-RestMethod "http://localhost:8080/api/v1/foods" | ConvertTo-Json -Depth 8

# 按分类筛选
Invoke-RestMethod "http://localhost:8080/api/v1/foods?category=快餐" | ConvertTo-Json -Depth 8

# ========== 吃过记录接口（需要登录） ==========

# 我就吃它（旧接口，保留兼容）
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/record/eat" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"foodId":31,"mealType":"晚餐","rating":5,"note":"很好吃"}')) `
  | ConvertTo-Json -Depth 8

# 决定吃什么（新接口）
$decideResult = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/record/decide" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"foodId":31,"mealType":"晚餐"}'))
$recordId = $decideResult.data.id

# 查询单条记录
Invoke-RestMethod "http://localhost:8080/api/v1/record/$recordId" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# 完成用餐
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/record/$recordId/complete" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"rating":5,"note":"很好吃"}'))

# 查询吃过记录列表
Invoke-RestMethod "http://localhost:8080/api/v1/record/list" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# 修改评价
Invoke-RestMethod -Method PUT -Uri "http://localhost:8080/api/v1/record/$recordId/review" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"rating":4,"note":"还不错"}'))

# 取消决定（先重新决定一个再取消）
$decideResult2 = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/record/decide" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"foodId":32,"mealType":"午餐"}'))
Invoke-RestMethod -Method DELETE -Uri "http://localhost:8080/api/v1/record/$($decideResult2.data.id)/decision" `
  -Headers @{Authorization="Bearer $token"}

# ========== 黑名单接口（需要登录） ==========

# 加入黑名单
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/blacklist/add" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"foodId":31,"reason":"不喜欢"}')) `
  | ConvertTo-Json -Depth 8

# 查询黑名单列表
Invoke-RestMethod "http://localhost:8080/api/v1/blacklist/list" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# 移出黑名单
Invoke-RestMethod -Method DELETE -Uri "http://localhost:8080/api/v1/blacklist/1" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# ========== 不想吃接口（需要登录） ==========

# 添加不想吃（默认 3 天）
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/dislike/add" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"category":"火锅"}')) `
  | ConvertTo-Json -Depth 8

# 查询不想吃列表
Invoke-RestMethod "http://localhost:8080/api/v1/dislike/list" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# 解除不想吃
Invoke-RestMethod -Method DELETE -Uri "http://localhost:8080/api/v1/dislike/1" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 8

# ========== 安全验证 ==========

# 不带 token 访问受保护接口，确认返回 1003
Invoke-RestMethod "http://localhost:8080/api/v1/record/list" | ConvertTo-Json -Depth 8

# 携带伪造 token，确认返回 1003
Invoke-RestMethod "http://localhost:8080/api/v1/record/list" `
  -Headers @{Authorization="Bearer fake-invalid-token"} | ConvertTo-Json -Depth 8

# ========== 健康检查 ==========

Invoke-RestMethod "http://localhost:8080/api/health" | ConvertTo-Json -Depth 8
```

---

## 未来接口（MVP 不做）

以下接口在 v1.0 中不做，放到未来版本：

| 接口 | 说明 | 计划版本 |
|------|------|----------|
| /preference/list | 获取用户偏好 | v1.1 |
| /preference/update | 更新用户偏好 | v1.1 |
| /vote/{id}/end | 结束投票 | v1.1 |
| /vote/list | 我的投票列表 | v1.1 |
| /vote/{id}/vote/{optionId} | 取消投票 | v1.1 |
| /restaurant/search | 搜索餐厅 | v1.1 |
| /restaurant/{id} | 餐厅详情 | v1.1 |
| /stats/user | 用户统计 | v1.1 |
