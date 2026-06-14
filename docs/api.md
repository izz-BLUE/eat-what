# 今天吃啥 - 接口文档

## 概述

- 基础路径：`/api/v1`
- 数据格式：JSON
- 认证方式：微信登录 + 后端自定义登录态 token
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
| 5001 | 系统错误 |

**认证说明**：
- 小程序通过 `wx.login()` 获取 code
- 后端用 code 调用微信接口获取 openid
- 后端生成自定义 token 返回给前端
- 前端后续请求通过 `Authorization: Bearer {token}` 传递
- **禁止**前端直接传递 openid 作为可信身份
- token 实现方式后续可选 JWT 或 Redis Session

---

## 接口总览

### 当前已实现（M0 推荐骨架）

| 序号 | 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 1 | /api/v1/foods | GET | 无需登录 | 查询菜品列表 |
| 2 | /api/v1/recommend | GET | 无需登录 | 一键推荐 |
| 3 | /api/v1/recommend/swap | GET | 无需登录 | 换一个 |
| 4 | /api/health | GET | 无需登录 | 健康检查 |

### 后续阶段实现

| 序号 | 接口 | 方法 | 认证 | 说明 | 阶段 |
|------|------|------|------|------|------|
| 5 | /user/login | POST | - | 微信登录 | M1 |
| 6 | /record/eat | POST | 必需 | 我就吃它 | M1 |
| 7 | /record/list | GET | 必需 | 吃过记录列表 | M1 |
| 8 | /blacklist/add | POST | 必需 | 加入黑名单 | M1 |
| 9 | /blacklist/list | GET | 必需 | 黑名单列表 | M1 |
| 10 | /blacklist/{id} | DELETE | 必需 | 移出黑名单 | M1 |
| 11 | /dislike/add | POST | 必需 | 标记不想吃 | M1 |
| 12 | /dislike/list | GET | 必需 | 不想吃列表 | M1 |
| 13 | /dislike/{id} | DELETE | 必需 | 解除不想吃 | M1 |
| 14 | /vote/create | POST | 必需 | 发起投票 | M2 |
| 15 | /vote/{id} | GET | 无需 | 获取投票详情 | M2 |
| 16 | /vote/{id}/vote | POST | 必需 | 投票 | M2 |

---

## 用户相关

### 1. 微信登录

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
    "user_id": 1,
    "token": "自定义token",
    "nickname": "用户昵称",
    "avatar_url": "https://xxx.jpg"
  }
}
```

**业务逻辑**：
1. 使用 code 调用微信接口获取 openid
2. 查询或创建用户
3. 生成自定义 token（JWT 或 Redis Session）
4. 返回 token 和用户信息

---

## 推荐相关（M0 已实现）

### 2. 一键推荐

**GET** `/api/v1/recommend`

根据参数推荐一道食物。**当前阶段无需登录**，只基于 foods 表和请求参数做简单规则打分。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段：早餐、午餐、晚餐、夜宵 |
| priceLevel | string | 否 | 价格偏好：15以内、15-25、25-40、不限 |
| taste | string | 否 | 口味偏好：清淡、重口、辣、不辣 |

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
    "reasons": ["适合当前餐段", "符合预算"]
  }
}
```

**当前业务逻辑**（M0）：
1. 查询所有 enabled=true 的菜品
2. 按 mealType、priceLevel、taste 打分
3. 从 Top 5 中随机选一个
4. 返回推荐结果

**后续阶段**（M1）：
- 接入用户登录
- 读取用户偏好（user_prefs）
- 排除黑名单（user_blacklist）
- 排除不想吃（user_dislikes）
- 读取吃过记录（eat_records）降权

---

### 3. 换一个

**GET** `/api/v1/recommend/swap`

排除已推荐的食物，重新推荐。**当前阶段无需登录**。

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段 |
| priceLevel | string | 否 | 价格偏好 |
| taste | string | 否 | 口味偏好 |
| excludeFoodIds | string | 否 | 排除的菜品 ID，逗号分隔，如：1,2,3 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "food": {
      "id": 4,
      "name": "火锅",
      "category": "火锅",
      "tasteTags": "辣,麻",
      "priceLevel": 4,
      "imageUrl": ""
    },
    "score": 38,
    "reasons": ["适合当前餐段"]
  }
}
```

**业务逻辑**：
1. 查询所有 enabled=true 的菜品
2. 排除 excludeFoodIds 中的菜品
3. 按规则打分，从 Top 5 中随机选一个

**业务逻辑**：
1. 获取 excludeFoodIds
2. 排除这些食物后重新推荐
3. 如果没有候选食物，返回错误提示

---

## 记录相关

### 4. 我就吃它

**POST** `/api/v1/record/eat`

记录用户选择吃某道食物。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "food_id": 1,
  "rating": 5,
  "note": "今天加了个蛋，很好吃"
}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "record_id": 1,
    "food_id": 1,
    "status": "eaten",
    "rating": 5,
    "note": "今天加了个蛋，很好吃",
    "eaten_at": "2024-01-15T12:30:00"
  }
}
```

**业务逻辑**：
1. 创建 eat_records 记录，status=eaten
2. 自动调整用户偏好权重（+5）
3. 返回记录详情

---

### 5. 获取吃过记录

**GET** `/api/v1/record/list`

获取用户吃过的食物记录列表。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 20 |
| category | string | 否 | 分类筛选 |
| minRating | int | 否 | 最低评分筛选 |
| keyword | string | 否 | 食物名称搜索 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 50,
    "page": 1,
    "pageSize": 20,
    "list": [
      {
        "record_id": 1,
        "food_id": 1,
        "food_name": "猪脚饭",
        "category": "快餐",
        "rating": 5,
        "note": "今天加了个蛋，很好吃",
        "eaten_at": "2024-01-15T12:30:00"
      },
      {
        "record_id": 2,
        "food_id": 2,
        "food_name": "黄焖鸡",
        "category": "快餐",
        "rating": 4,
        "note": "",
        "eaten_at": "2024-01-10T18:00:00"
      }
    ]
  }
}
```

---

## 黑名单相关

### 6. 加入黑名单

**POST** `/api/v1/blacklist/add`

将食物加入黑名单。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "food_id": 1,
  "reason": "太难吃了"
}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "blacklist_id": 1,
    "food_id": 1,
    "reason": "太难吃了",
    "created_at": "2024-01-15T12:30:00"
  }
}
```

**业务逻辑**：
1. 创建黑名单记录
2. 自动调整用户偏好权重（-20）
3. 返回黑名单详情

---

### 7. 获取黑名单列表

**GET** `/api/v1/blacklist/list`

获取用户的黑名单列表。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 20 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 5,
    "page": 1,
    "pageSize": 20,
    "list": [
      {
        "blacklist_id": 1,
        "food_id": 1,
        "food_name": "猪脚饭",
        "category": "快餐",
        "reason": "太难吃了",
        "created_at": "2024-01-15T12:30:00"
      }
    ]
  }
}
```

---

### 8. 移出黑名单

**DELETE** `/api/v1/blacklist/{blacklistId}`

将食物从黑名单中移除。

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

### 9. 标记不想吃

**POST** `/api/v1/dislike/add`

标记不想吃的食物分类。

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

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "dislike_id": 1,
    "category": "火锅",
    "expires_at": "2024-01-18T12:30:00"
  }
}
```

**业务逻辑**：
1. 创建或更新 user_dislikes 记录
2. 设置过期时间（默认 3 天）
3. 自动调整用户偏好权重（-10）
4. 返回详情

---

### 10. 获取不想吃列表

**GET** `/api/v1/dislike/list`

获取用户标记的不想吃分类（未过期）。

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
      "dislike_id": 1,
      "category": "火锅",
      "expires_at": "2024-01-18T12:30:00"
    },
    {
      "dislike_id": 2,
      "category": "川菜",
      "expires_at": "2024-01-20T12:30:00"
    }
  ]
}
```

---

### 11. 解除不想吃

**DELETE** `/api/v1/dislike/{dislikeId}`

解除不想吃的食物分类。

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

## 饭局投票相关

### 12. 发起投票

**POST** `/api/v1/vote/create`

发起一个新的饭局投票。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "title": "周五聚餐",
  "food_ids": [1, 2, 3, 4, 5],
  "expires_hours": 2
}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "vote_id": 1,
    "title": "周五聚餐",
    "status": "active",
    "expires_at": "2024-01-15T14:30:00",
    "options": [
      {
        "option_id": 1,
        "food_id": 1,
        "food_name": "猪脚饭",
        "vote_count": 0
      },
      {
        "option_id": 2,
        "food_id": 2,
        "food_name": "黄焖鸡",
        "vote_count": 0
      }
    ],
    "share_url": "https://xxx.com/vote/1",
    "qr_code": "https://xxx.com/qrcode/1"
  }
}
```

**业务逻辑**：
1. 创建投票记录
2. 创建投票选项
3. 生成分享链接和二维码
4. 返回投票详情

---

### 13. 获取投票详情

**GET** `/api/v1/vote/{voteId}`

获取投票详情和当前结果。

**请求头**：
```
Authorization: Bearer {token}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "vote_id": 1,
    "title": "周五聚餐",
    "status": "active",
    "expires_at": "2024-01-15T14:30:00",
    "creator_id": 1,
    "options": [
      {
        "option_id": 1,
        "food_id": 1,
        "food_name": "猪脚饭",
        "category": "快餐",
        "vote_count": 3,
        "is_voted": true
      },
      {
        "option_id": 2,
        "food_id": 2,
        "food_name": "黄焖鸡",
        "category": "快餐",
        "vote_count": 2,
        "is_voted": false
      }
    ],
    "total_votes": 5,
    "my_votes": 1,
    "max_votes": 3
  }
}
```

---

### 14. 投票

**POST** `/api/v1/vote/{voteId}/vote`

为投票选项投票。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "option_ids": [1, 2]
}
```

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "vote_id": 1,
    "my_votes": 2,
    "options": [
      {
        "option_id": 1,
        "vote_count": 4
      },
      {
        "option_id": 2,
        "vote_count": 3
      }
    ]
  }
}
```

**业务逻辑**：
1. 检查投票是否过期
2. 检查用户是否超过投票上限（最多 3 票）
3. 创建投票记录
4. 更新选项得票数
5. 返回更新后的投票结果

---

## 接口调用示例

### 微信小程序调用示例

```typescript
// services/api.ts

const BASE_URL = 'https://api.example.com/api/v1'

// ============================================
// M0 阶段接口（无需登录）
// ============================================

// 一键推荐（M0 无需登录）
export async function getRecommend(mealType?: string, priceLevel?: string, taste?: string) {
  const params = new URLSearchParams()
  if (mealType) params.append('mealType', mealType)
  if (priceLevel) params.append('priceLevel', priceLevel)
  if (taste) params.append('taste', taste)

  const res = await wx.request({
    url: `${BASE_URL}/recommend?${params.toString()}`,
    method: 'GET'
  })
  return res.data
}

// 换一个（M0 无需登录）
export async function swapRecommend(mealType?: string, priceLevel?: string, taste?: string, excludeFoodIds?: number[]) {
  const params = new URLSearchParams()
  if (mealType) params.append('mealType', mealType)
  if (priceLevel) params.append('priceLevel', priceLevel)
  if (taste) params.append('taste', taste)
  if (excludeFoodIds && excludeFoodIds.length > 0) {
    params.append('excludeFoodIds', excludeFoodIds.join(','))
  }

  const res = await wx.request({
    url: `${BASE_URL}/recommend/swap?${params.toString()}`,
    method: 'GET'
  })
  return res.data
}

// 查询菜品列表（M0 无需登录）
export async function getFoods(category?: string, priceLevel?: number) {
  const params = new URLSearchParams()
  if (category) params.append('category', category)
  if (priceLevel) params.append('priceLevel', priceLevel.toString())

  const res = await wx.request({
    url: `${BASE_URL}/foods?${params.toString()}`,
    method: 'GET'
  })
  return res.data
}

// ============================================
// 后续阶段接口（需要登录）
// ============================================

// 微信登录（M1 阶段）
export async function login(code: string) {
  const res = await wx.request({
    url: `${BASE_URL}/user/login`,
    method: 'POST',
    data: { code }
  })
  return res.data
}

// 我就吃它（M1 阶段，需要登录）
export async function eatFood(foodId: number, rating?: number, note?: string) {
  const token = wx.getStorageSync('token')
  const res = await wx.request({
    url: `${BASE_URL}/record/eat`,
    method: 'POST',
    header: {
      'Authorization': `Bearer ${token}`
    },
    data: {
      food_id: foodId,
      rating,
      note
    }
  })
  return res.data
}
```

---

## 接口测试

### curl 测试示例

#### M0 阶段接口（无需登录）

```bash
# 健康检查
curl http://localhost:8080/api/health

# 查询菜品列表
curl http://localhost:8080/api/v1/foods

# 按分类筛选
curl "http://localhost:8080/api/v1/foods?category=快餐"

# 一键推荐
curl "http://localhost:8080/api/v1/recommend?mealType=晚餐&priceLevel=15-25&taste=重口"

# 换一个（排除指定菜品）
curl "http://localhost:8080/api/v1/recommend/swap?mealType=晚餐&priceLevel=15-25&excludeFoodIds=1,2,3"
```

#### PowerShell 测试示例（推荐，中文显示正常）

```powershell
# 健康检查
Invoke-RestMethod http://localhost:8080/api/health | ConvertTo-Json -Depth 8

# 查询菜品列表
Invoke-RestMethod http://localhost:8080/api/v1/foods | ConvertTo-Json -Depth 8

# 一键推荐
Invoke-RestMethod "http://localhost:8080/api/v1/recommend?mealType=晚餐&priceLevel=15-25&taste=重口" | ConvertTo-Json -Depth 8

# 换一个
Invoke-RestMethod "http://localhost:8080/api/v1/recommend/swap?mealType=晚餐&priceLevel=15-25&excludeFoodIds=1,2" | ConvertTo-Json -Depth 8
```

#### 后续阶段接口示例（需要登录，M1 阶段实现）

```bash
# 微信登录
curl -X POST http://localhost:8080/api/v1/user/login \
  -H "Content-Type: application/json" \
  -d '{"code": "test_code"}'

# 我就吃它（需要 token）
curl -X POST http://localhost:8080/api/v1/record/eat \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"food_id": 1, "rating": 5, "note": "很好吃"}'
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
