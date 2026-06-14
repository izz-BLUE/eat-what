# 今天吃啥 - 接口文档

## 概述

- 基础路径：`/api/v1`
- 数据格式：JSON
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
| 1003 | 未登录 |
| 2001 | 用户不存在 |
| 2002 | 食物不存在 |
| 2003 | 投票不存在 |
| 2004 | 投票已结束 |
| 2005 | 已投过票 |
| 2006 | 超过投票上限 |
| 2007 | 黑名单记录不存在 |
| 2008 | 不想吃记录不存在 |
| 5001 | 系统错误 |

---

## 接口总览

### 当前已实现

| 序号 | 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 1 | /api/v1/foods | GET | 无需登录 | 查询菜品列表 |
| 2 | /api/v1/recommend | GET | 无需登录 | 一键推荐 |
| 3 | /api/v1/recommend/swap | GET | 无需登录 | 换一个 |
| 4 | /api/v1/record/eat | POST | 临时 userId | 我就吃它 |
| 5 | /api/v1/record/list | GET | 临时 userId | 吃过记录列表 |
| 6 | /api/v1/blacklist/add | POST | 临时 userId | 加入黑名单 |
| 7 | /api/v1/blacklist/list | GET | 临时 userId | 黑名单列表 |
| 8 | /api/v1/blacklist/{blacklistId} | DELETE | 临时 userId | 移出黑名单 |
| 9 | /api/v1/dislike/add | POST | 临时 userId | 添加不想吃 |
| 10 | /api/v1/dislike/list | GET | 临时 userId | 不想吃列表 |
| 11 | /api/v1/dislike/{dislikeId} | DELETE | 临时 userId | 解除不想吃 |
| 12 | /api/health | GET | 无需登录 | 健康检查 |

### 后续阶段实现

| 序号 | 接口 | 方法 | 说明 | 阶段 |
|------|------|------|------|------|
| 13 | /api/v1/user/login | POST | 微信登录 | M1 |
| 14 | /api/v1/vote/create | POST | 发起投票 | M3 |
| 15 | /api/v1/vote/{id} | GET | 获取投票详情 | M3 |
| 16 | /api/v1/vote/{id}/vote | POST | 投票 | M3 |

---

## 推荐相关

### 1. 一键推荐

**GET** `/api/v1/recommend`

根据参数推荐一道食物。**当前阶段无需登录**。

**推荐逻辑**：
- `userId` 为空：基础推荐（餐段 + 价格 + 口味 + 随机）
- `userId` 不为空：黑名单硬过滤 + 有效不想吃分类硬过滤 + 最近吃过降权

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段：早餐、午餐、晚餐、夜宵 |
| priceLevel | string | 否 | 价格偏好：15以内、15-25、25-40、不限 |
| taste | string | 否 | 口味偏好：清淡、重口、辣、不辣 |
| userId | long | 否 | 用户ID（临时，后续从 token 获取） |

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

### 2. 换一个

**GET** `/api/v1/recommend/swap`

排除已推荐的食物，重新推荐。**当前阶段无需登录**。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段 |
| priceLevel | string | 否 | 价格偏好 |
| taste | string | 否 | 口味偏好 |
| excludeFoodIds | string | 否 | 排除的菜品 ID，逗号分隔，如：1,2,3 |
| userId | long | 否 | 用户ID（临时，后续从 token 获取） |

**响应数据**：与一键推荐相同。

---

## 菜品相关

### 3. 查询菜品列表

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

## 吃过记录相关

### 4. 我就吃它

**POST** `/api/v1/record/eat`

记录用户选择吃某道食物。**当前阶段临时使用 userId 参数，无需 token**。

**请求参数**：
```json
{
  "userId": 1,
  "foodId": 31,
  "mealType": "晚餐",
  "rating": 5,
  "note": "今天加了个蛋，很好吃"
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |
| foodId | long | 是 | 食物ID |
| mealType | string | 是 | 餐段：早餐、午餐、晚餐、夜宵 |
| rating | int | 否 | 评分 1-5 |
| note | string | 否 | 备注，最长 256 字符 |

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
    "rating": 5,
    "note": "今天加了个蛋，很好吃",
    "eatenAt": "2024-01-15T12:30:00"
  }
}
```

---

### 5. 获取吃过记录

**GET** `/api/v1/record/list?userId=1&limit=20`

获取用户吃过的食物记录列表。**当前阶段临时使用 userId 参数，无需 token**。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |
| limit | int | 否 | 返回数量，默认 20，范围 1-100 |

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
      "mealType": "晚餐",
      "rating": 5,
      "note": "今天加了个蛋，很好吃",
      "eatenAt": "2024-01-15T12:30:00"
    }
  ]
}
```

---

## 黑名单相关

### 6. 加入黑名单

**POST** `/api/v1/blacklist/add`

将食物加入黑名单。**当前阶段临时使用 userId 参数，无需 token**。重复添加幂等，如果 reason 变化则更新。

**请求参数**：
```json
{
  "userId": 1,
  "foodId": 31,
  "reason": "不喜欢"
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |
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

### 7. 获取黑名单列表

**GET** `/api/v1/blacklist/list?userId=1`

获取用户的黑名单列表。**当前阶段临时使用 userId 参数，无需 token**。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |

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

### 8. 移出黑名单

**DELETE** `/api/v1/blacklist/{blacklistId}?userId=1`

将食物从黑名单中移除。**当前阶段临时使用 userId 参数，无需 token**。只能删除自己的黑名单记录。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| blacklistId | long | 是 | 路径参数，黑名单记录ID |
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |

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

### 9. 添加不想吃

**POST** `/api/v1/dislike/add`

添加或更新不想吃的分类。**当前阶段临时使用 userId 参数，无需 token**。同一 userId + category 幂等，已过期记录重新添加会恢复生效。

**请求参数**：
```json
{
  "userId": 1,
  "category": "火锅",
  "days": 3
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |
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

### 10. 获取不想吃列表

**GET** `/api/v1/dislike/list?userId=1`

获取用户有效的不想吃分类列表。**当前阶段临时使用 userId 参数，无需 token**。只返回未过期记录，按 expiresAt 升序。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |

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

### 11. 解除不想吃

**DELETE** `/api/v1/dislike/{dislikeId}?userId=1`

解除不想吃的分类。**当前阶段临时使用 userId 参数，无需 token**。只能删除自己的记录。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dislikeId | long | 是 | 路径参数，记录ID |
| userId | long | 是 | 用户ID（临时，后续从 token 获取） |

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

### 13. 微信登录

**POST** `/api/v1/user/login`

微信小程序登录，获取用户信息和 token。

**请求参数**：
```json
{
  "code": "微信登录code"
}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 1,
    "token": "自定义token",
    "nickname": "用户昵称",
    "avatarUrl": "https://xxx.jpg"
  }
}
```

---

### 14. 发起投票（后续阶段）

**POST** `/api/v1/vote/create`

发起一个新的饭局投票。

---

### 15. 获取投票详情（后续阶段）

**GET** `/api/v1/vote/{voteId}`

获取投票详情和当前结果。

---

### 16. 投票（后续阶段）

**POST** `/api/v1/vote/{voteId}/vote`

为投票选项投票。

---

## 接口验证示例

### PowerShell 验证（推荐，中文显示正常）

```powershell
# ========== 推荐接口 ==========

# 一键推荐（无 userId，基础推荐）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐" | ConvertTo-Json -Depth 8

# 一键推荐（有 userId，黑名单过滤 + 不想吃分类过滤 + 最近吃过降权）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐&userId=1" | ConvertTo-Json -Depth 8

# 换一个（blacklist + dislike + excludeFoodIds 同时生效）
Invoke-RestMethod "http://localhost:8080/api/v1/recommend/swap?mealType=晚餐&excludeFoodIds=31,32&userId=1" | ConvertTo-Json -Depth 8

# ========== 吃过记录接口 ==========

# 我就吃它
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/record/eat" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"userId":1,"foodId":31,"mealType":"晚餐","rating":5,"note":"很好吃"}')) `
  | ConvertTo-Json -Depth 8

# 查询吃过记录
Invoke-RestMethod "http://localhost:8080/api/v1/record/list?userId=1" | ConvertTo-Json -Depth 8

# ========== 黑名单接口 ==========

# 加入黑名单
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/blacklist/add" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"userId":1,"foodId":31,"reason":"不喜欢"}')) `
  | ConvertTo-Json -Depth 8

# 查询黑名单列表
Invoke-RestMethod "http://localhost:8080/api/v1/blacklist/list?userId=1" | ConvertTo-Json -Depth 8

# 移出黑名单
Invoke-RestMethod -Method DELETE -Uri "http://localhost:8080/api/v1/blacklist/1?userId=1" | ConvertTo-Json -Depth 8

# ========== 不想吃接口 ==========

# 添加不想吃（默认 3 天）
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/dislike/add" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"userId":1,"category":"火锅"}')) `
  | ConvertTo-Json -Depth 8

# 添加不想吃（自定义 5 天）
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/dislike/add" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"userId":1,"category":"川菜","days":5}')) `
  | ConvertTo-Json -Depth 8

# 查询不想吃列表（只返回未过期记录）
Invoke-RestMethod "http://localhost:8080/api/v1/dislike/list?userId=1" | ConvertTo-Json -Depth 8

# 解除不想吃
Invoke-RestMethod -Method DELETE -Uri "http://localhost:8080/api/v1/dislike/1?userId=1" | ConvertTo-Json -Depth 8

# ========== 健康检查 ==========

Invoke-RestMethod "http://localhost:8080/api/health" | ConvertTo-Json -Depth 8
```

---

## 未来接口（MVP 不做）

以下接口在 v1.0 中不做，放到未来版本：

| 接口 | 说明 | 计划版本 |
|------|------|----------|
| /record/{id} | 更新/删除记录 | v1.1 |
| /preference/list | 获取用户偏好 | v1.1 |
| /preference/update | 更新用户偏好 | v1.1 |
| /vote/{id}/end | 结束投票 | v1.1 |
| /vote/list | 我的投票列表 | v1.1 |
| /vote/{id}/vote/{optionId} | 取消投票 | v1.1 |
| /restaurant/search | 搜索餐厅 | v1.1 |
| /restaurant/{id} | 餐厅详情 | v1.1 |
| /stats/user | 用户统计 | v1.1 |
