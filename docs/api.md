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
| 2012 | 反馈不存在 |
| 2013 | 自定义菜品不存在 |
| 2014 | 已存在同名自定义菜品 |
| 3001 | 无权限（管理后台） |
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
$env:ADMIN_TOKEN="dev-admin-token"
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
| 12 | /api/v1/blacklist/add | POST | 必须登录 | 加入黑名单 |
| 13 | /api/v1/blacklist/list | GET | 必须登录 | 黑名单列表 |
| 14 | /api/v1/blacklist/{blacklistId} | DELETE | 必须登录 | 移出黑名单 |
| 15 | /api/v1/dislike/add | POST | 必须登录 | 添加不想吃 |
| 16 | /api/v1/dislike/list | GET | 必须登录 | 不想吃列表 |
| 17 | /api/v1/dislike/{dislikeId} | DELETE | 必须登录 | 解除不想吃 |
| 18 | /api/v1/feedback | POST | 无需登录 | 提交意见反馈 |
| 20 | /api/v1/custom-foods | POST | 必须登录 | 创建自定义菜品 |
| 21 | /api/v1/custom-foods | GET | 必须登录 | 查询我的自定义菜品 |
| 22 | /api/v1/custom-foods/{id} | DELETE | 必须登录 | 删除自定义菜品 |
| 23 | /api/health | GET | 无需登录 | 健康检查 |

### 管理后台（仅供开发者使用）

| 序号 | 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| A1 | /api/v1/admin/feedback | GET | X-Admin-Token | 查询反馈列表 |
| A2 | /api/v1/admin/feedback/{id}/status | PUT | X-Admin-Token | 更新反馈状态 |

> **注意**：管理后台接口仅供开发者使用，小程序端不调用。认证方式为 `X-Admin-Token` 请求头，与普通用户 JWT 无关。

### 后续阶段实现

| 序号 | 接口 | 方法 | 说明 | 阶段 |
|------|------|------|------|------|
| 15 | /api/v1/vote/create | POST | 发起投票 | M3 |
| 16 | /api/v1/vote/{id} | GET | 获取投票详情 | M3 |
| 17 | /api/v1/vote/{id}/vote | POST | 投票 | M3 |

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
- 有 token：
  1. **自定义菜优先**：筛选自定义菜 → 若匹配则只从自定义菜推荐（不混入默认菜）
  2. 自定义菜不匹配 → 回退默认菜逻辑
  3. 默认菜逻辑：黑名单硬过滤 + 有效不想吃分类硬过滤 + 最近吃过降权

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mealType | string | 否 | 餐段：早餐、午餐、晚餐、夜宵 |
| priceLevel | string | 否 | 参考价位：15以内、15-25、25-40、40以上（硬过滤，不选不限） |
| taste | string | 否 | 口味偏好：清淡、辣、不辣 |
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
      "imageUrl": "",
      "source": "DEFAULT",
      "customFoodId": null
    },
    "score": 45,
    "reasons": ["符合偏好分类", "符合参考价位", "最近几天没吃过，换换口味"]
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
| priceLevel | string | 否 | 参考价位：15以内、15-25、25-40、40以上（硬过滤） |
| taste | string | 否 | 口味偏好 |
| excludeFoodIds | string | 否 | 排除的默认菜品 ID，逗号分隔，如：1,2,3 |
| excludeCustomFoodIds | string | 否 | 排除的自定义菜品 ID，逗号分隔，如：1,2 |
| categories | string | 否 | 偏好分类，逗号分隔，最多3个 |

> **排除规则说明**：`excludeFoodIds` 只排除默认菜品（`foods` 表），`excludeCustomFoodIds` 只排除自定义菜品（`user_custom_foods` 表），两者不混用。已登录用户使用 swap 时，若当前在自定义菜推荐阶段，应传 `excludeCustomFoodIds`；自定义菜耗尽后回退默认菜推荐，应传 `excludeFoodIds`。

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
      "imageUrl": "",
      "source": "DEFAULT",
      "customFoodId": null
    }
  ]
}
```

---

## 用户自定义菜品

用户可以在默认菜品库中找不到想吃的菜时，自行添加自定义菜品。自定义菜仅创建者可见，推荐时优先级高于系统菜品。

**核心规则：**
- 自定义菜仅创建者可见，不影响其他用户
- 所有接口必须登录
- 仅返回和操作 `enabled=true` 的自定义菜
- 删除为软删除（`enabled=false`），Phase 1 不支持编辑
- 自定义菜不可加入黑名单（用户可直接删除自己创建的菜）

### 18. 创建自定义菜品

**POST** `/api/v1/custom-foods`

创建当前用户的自定义菜品。

**请求头**：
```
Authorization: Bearer {token}
```

**请求参数**：
```json
{
  "name": "妈妈做的炒饭",
  "typeTags": ["快餐"],
  "cuisineTags": ["家常菜"],
  "mealTypes": ["午餐", "晚餐"],
  "tasteTags": ["咸", "香"],
  "priceLevel": 1
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 菜品名称，1-64 字符 |
| mealTypes | string[] | 是 | 适用餐段，至少一个：早餐、午餐、晚餐、夜宵 |
| tasteTags | string[] | 是 | 口味标签，至少一个：酸、甜、苦、辣、咸、清淡、麻、鲜、香 |
| typeTags | string[] | 否 | 食物类型标签，与 cuisineTags 至少一个非空 |
| cuisineTags | string[] | 否 | 菜系标签，与 typeTags 至少一个非空 |
| priceLevel | int | 否 | 参考价位：1（15以内）、2（15-25）、3（25-40）、4（40以上） |

**标签校验规则**：
- typeTags、cuisineTags、mealTypes、tasteTags 中的值必须在系统允许的标签范围内
- 每个多值字段内部自动去重
- typeTags 和 cuisineTags 至少一个非空——都能为空会被拒绝（1001）

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "妈妈做的炒饭",
    "category": "家常菜",
    "typeTags": "快餐",
    "cuisineTags": "家常菜",
    "mealTypes": "午餐,晚餐",
    "tasteTags": "咸,香",
    "priceLevel": 1,
    "enabled": true,
    "createdAt": "2026-06-17T12:00:00",
    "updatedAt": "2026-06-17T12:00:00"
  }
}
```

**响应字段说明**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 自定义菜品 ID |
| name | string | 菜品名称 |
| category | string | 派生分类：优先取 cuisineTags[0]，否则取 typeTags[0] |
| typeTags | string | 类型标签，逗号分隔 |
| cuisineTags | string | 菜系标签，逗号分隔 |
| mealTypes | string | 适用餐段，逗号分隔 |
| tasteTags | string | 口味标签，逗号分隔 |
| priceLevel | int | 参考价位 1-4，null 表示不限 |
| enabled | boolean | 是否启用 |
| createdAt | string | 创建时间 |
| updatedAt | string | 更新时间 |

**错误**：
| code | 说明 |
|------|------|
| 1001 | 参数错误（name 为空、mealTypes/tasteTags 为空、typeTags 和 cuisineTags 都为空、标签不合法） |
| 1003 | 未登录 |
| 2014 | 已存在同名自定义菜品（同一用户下 name 唯一） |

---

### 19. 查询我的自定义菜品

**GET** `/api/v1/custom-foods`

查询当前用户所有启用的自定义菜品，按更新时间倒序。

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
      "name": "妈妈做的炒饭",
      "category": "家常菜",
      "typeTags": "快餐",
      "cuisineTags": "家常菜",
      "mealTypes": "午餐,晚餐",
      "tasteTags": "咸,香",
      "priceLevel": 1,
      "enabled": true,
      "createdAt": "2026-06-17T12:00:00",
      "updatedAt": "2026-06-17T12:00:00"
    }
  ]
}
```

**说明**：只返回 `enabled=true` 的自定义菜，按 `updatedAt DESC` 排列。已删除（`enabled=false`）的不在列表中。

---

### 20. 删除自定义菜品

**DELETE** `/api/v1/custom-foods/{id}`

软删除自定义菜品（设置 `enabled=false`），仅允许删除自己创建的。

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

**错误**：
| code | 说明 |
|------|------|
| 1003 | 未登录 |
| 2013 | 自定义菜品不存在或不属于当前用户 |

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
| foodId | long | 与 customFoodId 二选一 | 系统菜品ID（DEFAULT 来源） |
| customFoodId | long | 与 foodId 二选一 | 自定义菜品ID（CUSTOM 来源） |
| mealType | string | 是 | 餐段：早餐、午餐、晚餐、夜宵 |
| rating | int | 否 | 评分 1-5 |
| note | string | 否 | 备注，最长 256 字符 |

> `foodId` 和 `customFoodId` 互斥，详见"决定吃什么"接口的双来源说明。

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

**双来源请求说明**：

`foodId` 和 `customFoodId` 互斥（有且仅有一个非 null），按菜品来源选择：

| 来源 | 传参 | 说明 |
|------|------|------|
| DEFAULT（系统菜品） | `foodId` | 对应 `foods` 表的主键 |
| CUSTOM（自定义菜品） | `customFoodId` | 对应 `user_custom_foods` 表的主键 |

自定义菜示例：
```json
{
  "customFoodId": 5,
  "mealType": "午餐"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| foodId | long | 与 customFoodId 二选一 | 系统菜品ID（DEFAULT 来源） |
| customFoodId | long | 与 foodId 二选一 | 自定义菜品ID（CUSTOM 来源） |
| mealType | string | 是 | 餐段：早餐、午餐、晚餐、夜宵 |

> **互斥校验**：两者都为 null 或两者都非 null 均返回 1001。

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "foodId": 31,
    "customFoodId": null,
    "foodSource": "DEFAULT",
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

**响应字段说明**（新增字段）：
| 字段 | 类型 | 说明 |
|------|------|------|
| foodId | long | 系统菜品ID（CUSTOM 来源时为 null） |
| customFoodId | long | 自定义菜品ID（DEFAULT 来源时为 null） |
| foodSource | string | 来源："DEFAULT"（系统菜品）/ "CUSTOM"（自定义菜品） |
| foodName | string | 菜品名称（优先取快照，空时回查对应表） |
| category | string | 菜品分类（优先取快照，空时回查对应表） |

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
      "customFoodId": null,
      "foodSource": "DEFAULT",
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
      "customFoodId": null,
      "foodSource": "DEFAULT",
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

## 意见反馈相关

### 18. 提交意见反馈

**POST** `/api/v1/feedback`

提交用户意见反馈，支持匿名提交和已登录用户提交。

**认证说明**：无需登录即可提交。登录用户提交会记录 userId，方便后续联系。携带无效 token 会返回 code=1003。

**请求参数**：
```json
{
  "type": "FEATURE",
  "rating": 4,
  "content": "建议增加更多菜系分类",
  "contact": "wechat: user1",
  "page": "/pages/index/index",
  "systemInfo": "{\"model\":\"iPhone 14\",\"system\":\"iOS 16.0\"}"
}
```

**请求参数说明**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | string | 是 | 反馈类型：FEATURE-功能建议、BUG-问题反馈、RECOMMENDATION-推荐不准、UI-界面体验、OTHER-其他 |
| rating | int | 否 | 满意度评分 1-5，不填也可提交 |
| content | string | 是 | 反馈内容，5-500 字 |
| contact | string | 否 | 联系方式（微信号/手机/邮箱），最长 100 字 |
| page | string | 否 | 来源页面路径，最长 128 字 |
| systemInfo | string | 否 | 微信环境信息（JSON），最长 1000 字 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "type": "FEATURE",
    "rating": 4,
    "content": "建议增加更多菜系分类",
    "contact": "wechat: user1",
    "page": "/pages/index/index",
    "systemInfo": "{\"model\":\"iPhone 14\",\"system\":\"iOS 16.0\"}",
    "status": "NEW",
    "createdAt": "2026-06-16T12:00:00"
  }
}
```

---

## 管理后台 - 反馈管理

> **适用范围**：仅供开发/管理用途，小程序端不调用。
> **认证方式**：请求头 `X-Admin-Token`，值需等于配置项 `admin.token`。
> **权限说明**：
> - 配置了 `admin.token`（dev/test 默认已配）：必须携带正确的 `X-Admin-Token`，否则返回 3001。
> - 未配置 `admin.token`（手动设为空）：dev/test Profile 放行所有请求；prod Profile 拒绝所有请求。
> - 生产环境必须配置 `admin.token`，否则管理接口不可用。

### A1. 查询反馈列表

**GET** `/api/v1/admin/feedback`

查询用户意见反馈列表，支持状态筛选、关键词搜索和分页。

**请求头**：
```
X-Admin-Token: dev-admin-token
```

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 否 | 状态筛选：NEW / REVIEWED / RESOLVED / IGNORED |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20，最大 100 |
| keyword | string | 否 | 关键词搜索，匹配 content 和 contact 字段 |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "type": "FEATURE",
        "rating": 4,
        "content": "建议增加更多菜系分类",
        "contact": "wechat: user1",
        "page": "/pages/feedback/feedback",
        "systemInfo": "{\"model\":\"iPhone 14\"}",
        "status": "NEW",
        "createdAt": "2026-06-16T12:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

**错误**：
| code | 说明 |
|------|------|
| 3001 | X-Admin-Token 缺失或不正确 |

---

### A2. 更新反馈状态

**PUT** `/api/v1/admin/feedback/{id}/status`

更新指定反馈的处理状态。

**请求头**：
```
X-Admin-Token: dev-admin-token
```

**请求参数**：
```json
{
  "status": "REVIEWED"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | string | 是 | 新状态：NEW / REVIEWED / RESOLVED / IGNORED |

**响应数据**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "type": "FEATURE",
    "rating": 4,
    "content": "建议增加更多菜系分类",
    "contact": "wechat: user1",
    "page": "/pages/feedback/feedback",
    "systemInfo": "{\"model\":\"iPhone 14\"}",
    "status": "REVIEWED",
    "createdAt": "2026-06-16T12:00:00"
  }
}
```

**错误**：
| code | 说明 |
|------|------|
| 1001 | status 为空或值不合法 |
| 2012 | 反馈不存在 |
| 3001 | X-Admin-Token 缺失或不正确 |

---

## 后续阶段接口（需要登录）

### 19. 发起投票（后续阶段）

**POST** `/api/v1/vote/create`

发起一个新的饭局投票。

---

### 20. 获取投票详情（后续阶段）

**GET** `/api/v1/vote/{voteId}`

获取投票详情和当前结果。

---

### 21. 投票（后续阶段）

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

# ========== 意见反馈（无需登录，支持匿名） ==========

# 匿名提交反馈
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/feedback" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"type":"FEATURE","content":"匿名反馈测试，建议增加分类"}')) `
  | ConvertTo-Json -Depth 8

# 已登录用户提交反馈（带评分和联系方式）
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/feedback" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{Authorization="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"type":"BUG","rating":2,"content":"推荐页面偶现白屏问题","contact":"wechat: testuser","page":"/pages/index/index"}')) `
  | ConvertTo-Json -Depth 8

# 匿名提交（内容不足5字会被拒绝）
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/v1/feedback" `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"type":"OTHER","content":"ab"}')) `
  | ConvertTo-Json -Depth 8

# ========== 管理后台 - 反馈管理（需要 X-Admin-Token） ==========

# 无 token 会被拒绝（3001）
Invoke-RestMethod "http://localhost:8080/api/v1/admin/feedback" | ConvertTo-Json -Depth 8

# 错误 token 会被拒绝（3001）
Invoke-RestMethod "http://localhost:8080/api/v1/admin/feedback" `
  -Headers @{"X-Admin-Token"="wrong-token"} | ConvertTo-Json -Depth 8

# 查询反馈列表
Invoke-RestMethod "http://localhost:8080/api/v1/admin/feedback" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} | ConvertTo-Json -Depth 8

# 按状态筛选
Invoke-RestMethod "http://localhost:8080/api/v1/admin/feedback?status=NEW" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} | ConvertTo-Json -Depth 8

# 关键词搜索
Invoke-RestMethod "http://localhost:8080/api/v1/admin/feedback?keyword=火锅" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} | ConvertTo-Json -Depth 8

# 分页查询
Invoke-RestMethod "http://localhost:8080/api/v1/admin/feedback?page=1&size=5" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} | ConvertTo-Json -Depth 8

# 更新反馈状态
Invoke-RestMethod -Method PUT -Uri "http://localhost:8080/api/v1/admin/feedback/1/status" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"status":"REVIEWED"}')) `
  | ConvertTo-Json -Depth 8

# 非法状态会被拒绝（1001）
Invoke-RestMethod -Method PUT -Uri "http://localhost:8080/api/v1/admin/feedback/1/status" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"status":"INVALID"}')) `
  | ConvertTo-Json -Depth 8

# 不存在的反馈（2012）
Invoke-RestMethod -Method PUT -Uri "http://localhost:8080/api/v1/admin/feedback/99999/status" `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{"X-Admin-Token"="dev-admin-token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"status":"RESOLVED"}')) `
  | ConvertTo-Json -Depth 8
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
