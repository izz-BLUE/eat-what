# 今天吃啥 - 数据库设计

## 概述

数据库：MySQL 8.0
字符集：utf8mb4
排序规则：utf8mb4_unicode_ci

**核心概念**：`foods` 表存储的是菜品/食物（如猪脚饭、黄焖鸡、麻辣烫），不是餐厅/商家。

## ER 图

```
┌─────────────┐       ┌─────────────────┐       ┌─────────────┐
│   users     │       │  eat_records    │       │    foods    │
├─────────────┤       ├─────────────────┤       ├─────────────┤
│ id          │──┐    │ id              │    ┌──│ id          │
│ openid      │  │    │ user_id         │────┘  │ name        │
│ nickname    │  │    │ food_id         │       │ category    │
│ avatar_url  │  │    │ status          │       │ taste_tags  │
│ created_at  │  │    │ rating          │       │ price_level │
│ updated_at  │  │    │ note            │       │ created_at  │
└─────────────┘  │    │ eaten_at        │       │ updated_at  │
                 │    │ created_at      │       └─────────────┘
                 │    │ updated_at      │
                 │    └─────────────────┘
                 │
                 │    ┌─────────────────┐
                 │    │ user_blacklist  │
                 │    ├─────────────────┤
                 │    │ id              │
                 └────│ user_id         │
                      │ food_id         │
                      │ reason          │
                      │ created_at      │
                      └─────────────────┘

┌─────────────┐       ┌─────────────────┐
│ user_prefs  │       │ user_dislikes   │
├─────────────┤       ├─────────────────┤
│ id          │       │ id              │
│ user_id     │       │ user_id         │
│ category    │       │ category        │
│ weight      │       │ expires_at      │
│ created_at  │       │ created_at      │
│ updated_at  │       └─────────────────┘
└─────────────┘

┌─────────────┐       ┌─────────────────┐
│ votes       │       │ vote_options    │
├─────────────┤       ├─────────────────┤
│ id          │──┐    │ id              │
│ user_id     │  │    │ vote_id         │
│ title       │  │    │ food_id         │
│ status      │  │    │ vote_count      │
│ expires_at  │  └────│ created_at      │
│ created_at  │       └─────────────────┘
│ updated_at  │
└─────────────┘       ┌─────────────────┐
                      │ vote_records    │
                      ├─────────────────┤
                      │ id              │
                      │ vote_id         │
                      │ user_id         │
                      │ option_id       │
                      │ created_at      │
                      └─────────────────┘
```

---

## 表结构详细设计

### 1. users - 用户表

存储用户基本信息，通过微信 openid 唯一标识。JWT token 的 subject 字段存放用户 id。

```sql
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(256) DEFAULT '' COMMENT '头像URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增，用于 JWT subject |
| openid | VARCHAR(64) | 是 | 微信用户唯一标识，唯一索引 |
| nickname | VARCHAR(64) | 否 | 用户昵称 |
| avatar_url | VARCHAR(256) | 否 | 头像 URL |
| created_at | DATETIME | 是 | 创建时间 |
| updated_at | DATETIME | 是 | 更新时间 |

**认证说明**：
- openid 通过微信 code2Session 接口获取，不下发给前端
- JWT token 包含 userId，有效期默认 7 天
- 前端通过 `Authorization: Bearer {token}` 传递认证信息

---

### 2. foods - 菜品表

存储菜品/食物信息。**注意：是菜品，不是餐厅。**

```sql
CREATE TABLE `foods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '菜品名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类（快餐、火锅、川菜等）',
  `taste_tags` VARCHAR(128) DEFAULT '' COMMENT '口味标签（辣、甜、咸等），逗号分隔',
  `price_level` TINYINT DEFAULT 0 COMMENT '价格等级（1-5）',
  `image_url` VARCHAR(256) DEFAULT '' COMMENT '菜品图片',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| name | VARCHAR(64) | 是 | 菜品名称，如：猪脚饭、黄焖鸡、麻辣烫 |
| category | VARCHAR(32) | 是 | 分类 |
| taste_tags | VARCHAR(128) | 否 | 口味标签，逗号分隔，如：辣,咸,香 |
| price_level | TINYINT | 否 | 价格等级 1-5 |
| image_url | VARCHAR(256) | 否 | 菜品图片 URL |
| created_at | DATETIME | 是 | 创建时间 |
| updated_at | DATETIME | 是 | 更新时间 |

**分类枚举**：
- 快餐（猪脚饭、黄焖鸡、盖浇饭）
- 火锅
- 川菜
- 粤菜
- 湘菜
- 日料
- 韩餐
- 西餐
- 烧烤
- 小吃（麻辣烫、串串、煎饼）
- 甜品
- 面食（拉面、拌面、云吞面）
- 其他

**口味标签示例**：
- 辣、微辣、特辣
- 甜、咸、鲜、酸
- 麻、香、清淡
- 油腻、健康

---

### 3. eat_records - 吃过记录表

记录用户吃过的食物：评分、备注等。用于"我就吃它"功能。

```sql
CREATE TABLE `eat_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `food_id` BIGINT NOT NULL COMMENT '食物ID',
  `rating` TINYINT DEFAULT NULL COMMENT '评分（1-5）',
  `note` VARCHAR(256) DEFAULT '' COMMENT '备注',
  `eaten_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '吃的时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_food_eaten_at` (`user_id`, `food_id`, `eaten_at`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_food_id` (`food_id`),
  KEY `idx_eaten_at` (`eaten_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='吃过记录表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| user_id | BIGINT | 是 | 用户ID，关联 users.id |
| food_id | BIGINT | 是 | 食物ID，关联 foods.id |
| rating | TINYINT | 否 | 评分 1-5 |
| note | VARCHAR(256) | 否 | 备注 |
| eaten_at | DATETIME | 是 | 吃的时间 |
| created_at | DATETIME | 是 | 创建时间 |
| updated_at | DATETIME | 是 | 更新时间 |

**索引说明**：用户可能多次吃同一个食物，吃过记录允许多条。

---

### 4. user_blacklist - 用户黑名单表

存储用户标记为黑名单的食物。

```sql
CREATE TABLE `user_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `food_id` BIGINT NOT NULL COMMENT '食物ID',
  `reason` VARCHAR(128) DEFAULT '' COMMENT '拉黑原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_food` (`user_id`, `food_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户黑名单表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| user_id | BIGINT | 是 | 用户ID，关联 users.id |
| food_id | BIGINT | 是 | 食物ID，关联 foods.id |
| reason | VARCHAR(128) | 否 | 拉黑原因 |
| created_at | DATETIME | 是 | 创建时间 |

**唯一约束**：同一用户对同一食物只能有一条黑名单记录。

---

### 5. user_prefs - 用户偏好表

存储用户对食物分类的偏好权重。

```sql
CREATE TABLE `user_prefs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category` VARCHAR(32) NOT NULL COMMENT '食物分类',
  `weight` INT NOT NULL DEFAULT 50 COMMENT '偏好权重（0-100）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category` (`user_id`, `category`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| user_id | BIGINT | 是 | 用户ID，关联 users.id |
| category | VARCHAR(32) | 是 | 食物分类 |
| weight | INT | 是 | 偏好权重 0-100，默认 50 |
| created_at | DATETIME | 是 | 创建时间 |
| updated_at | DATETIME | 是 | 更新时间 |

**权重说明**：
- 0：非常不喜欢
- 50：中性
- 100：非常喜欢

---

### 6. user_dislikes - 用户不想吃表

存储用户临时不想吃的食物分类，有过期时间。

```sql
CREATE TABLE `user_dislikes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category` VARCHAR(32) NOT NULL COMMENT '食物分类',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category` (`user_id`, `category`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户不想吃表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| user_id | BIGINT | 是 | 用户ID，关联 users.id |
| category | VARCHAR(32) | 是 | 不想吃的食物分类 |
| expires_at | DATETIME | 是 | 过期时间，默认 3 天后 |
| created_at | DATETIME | 是 | 创建时间 |

**业务规则**：
- 默认过期时间为 3 天后
- 过期后自动失效（查询时过滤）
- 同一用户对同一分类只能有一条记录

---

### 7. votes - 饭局投票表

存储饭局投票信息。

```sql
CREATE TABLE `votes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发起人ID',
  `title` VARCHAR(128) NOT NULL COMMENT '饭局标题',
  `status` VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '状态：active-进行中，ended-已结束',
  `expires_at` DATETIME NOT NULL COMMENT '截止时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='饭局投票表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| user_id | BIGINT | 是 | 发起人ID，关联 users.id |
| title | VARCHAR(128) | 是 | 饭局标题，如"周五聚餐" |
| status | VARCHAR(16) | 是 | 状态：active-进行中，ended-已结束 |
| expires_at | DATETIME | 是 | 截止时间 |
| created_at | DATETIME | 是 | 创建时间 |
| updated_at | DATETIME | 是 | 更新时间 |

---

### 8. vote_options - 投票选项表

存储投票的候选食物选项。

```sql
CREATE TABLE `vote_options` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vote_id` BIGINT NOT NULL COMMENT '投票ID',
  `food_id` BIGINT NOT NULL COMMENT '食物ID',
  `vote_count` INT NOT NULL DEFAULT 0 COMMENT '得票数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_vote_id` (`vote_id`),
  CONSTRAINT `fk_vote_options_vote` FOREIGN KEY (`vote_id`) REFERENCES `votes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票选项表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| vote_id | BIGINT | 是 | 投票ID，关联 votes.id |
| food_id | BIGINT | 是 | 食物ID，关联 foods.id |
| vote_count | INT | 是 | 得票数，默认 0 |
| created_at | DATETIME | 是 | 创建时间 |

---

### 9. vote_records - 投票记录表

存储用户的投票记录。

```sql
CREATE TABLE `vote_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vote_id` BIGINT NOT NULL COMMENT '投票ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `option_id` BIGINT NOT NULL COMMENT '选项ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vote_user_option` (`vote_id`, `user_id`, `option_id`),
  KEY `idx_vote_id` (`vote_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_vote_records_vote` FOREIGN KEY (`vote_id`) REFERENCES `votes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vote_records_option` FOREIGN KEY (`option_id`) REFERENCES `vote_options` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票记录表';
```

**字段说明**：
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键，自增 |
| vote_id | BIGINT | 是 | 投票ID，关联 votes.id |
| user_id | BIGINT | 是 | 用户ID，关联 users.id |
| option_id | BIGINT | 是 | 选项ID，关联 vote_options.id |
| created_at | DATETIME | 是 | 创建时间 |

**唯一约束**：同一用户对同一投票的同一选项只能投一次。

**业务规则**：
- 每人最多投 3 票（通过应用层控制）
- 投票后 vote_count +1
- 取消投票后 vote_count -1

---

## 索引说明

### 主要索引

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| users | uk_openid | openid | UNIQUE | 用户唯一标识 |
| eat_records | idx_user_food_eaten_at | user_id, food_id, eaten_at | INDEX | 查询用户吃某食物的记录 |
| user_blacklist | uk_user_food | user_id, food_id | UNIQUE | 防止重复拉黑 |
| user_prefs | uk_user_category | user_id, category | UNIQUE | 用户偏好唯一 |
| user_dislikes | uk_user_category | user_id, category | UNIQUE | 不想吃唯一 |
| vote_records | uk_vote_user_option | vote_id, user_id, option_id | UNIQUE | 防止重复投票 |

### 查询索引

| 表名 | 索引名 | 字段 | 说明 |
|------|--------|------|------|
| foods | idx_category | category | 按分类查询 |
| eat_records | idx_user_id | user_id | 查询用户的记录 |
| eat_records | idx_food_id | food_id | 查询食物的记录 |
| eat_records | idx_eaten_at | eaten_at | 按时间查询 |
| user_blacklist | idx_user_id | user_id | 查询用户的黑名单 |
| user_prefs | idx_user_id | user_id | 查询用户偏好 |
| user_dislikes | idx_user_id | user_id | 查询用户不想吃 |
| user_dislikes | idx_expires_at | expires_at | 查询过期记录 |
| votes | idx_user_id | user_id | 查询用户发起的投票 |
| votes | idx_status | status | 按状态查询 |
| votes | idx_expires_at | expires_at | 查询过期投票 |
| vote_options | idx_vote_id | vote_id | 查询投票选项 |
| vote_records | idx_vote_id | vote_id | 查询投票记录 |
| vote_records | idx_user_id | user_id | 查询用户投票 |

---

## 数据初始化

### 菜品数据初始化

```sql
INSERT INTO `foods` (`name`, `category`, `taste_tags`, `price_level`) VALUES
('猪脚饭', '快餐', '咸,香', 2),
('黄焖鸡米饭', '快餐', '咸,微辣', 2),
('麻辣烫', '小吃', '辣,麻', 2),
('肠粉', '小吃', '清淡,鲜', 1),
('云吞面', '面食', '清淡,鲜', 2),
('兰州拉面', '面食', '清淡', 1),
('沙县小吃', '小吃', '清淡', 1),
('盖浇饭', '快餐', '咸', 1),
('炒饭', '快餐', '咸,香', 1),
('炸鸡', '快餐', '咸,香,油腻', 2),
('汉堡', '快餐', '咸,香', 2),
('披萨', '西餐', '咸,香', 3),
('寿司', '日料', '清淡,鲜', 3),
('拉面', '日料', '咸,鲜', 3),
('烤肉', '烧烤', '咸,香', 3),
('火锅', '火锅', '辣,麻', 4),
('酸菜鱼', '川菜', '酸,辣', 3),
('宫保鸡丁', '川菜', '辣', 2),
('回锅肉', '川菜', '辣,咸', 2),
('红烧肉', '家常菜', '咸,甜', 2),
('糖醋排骨', '家常菜', '甜,酸', 3),
('清蒸鱼', '粤菜', '清淡,鲜', 3),
('白切鸡', '粤菜', '清淡,鲜', 3),
('叉烧', '粤菜', '甜,咸', 3),
('湖南小炒肉', '湘菜', '辣', 2),
('臭豆腐', '小吃', '辣,臭', 1),
('煎饼果子', '小吃', '咸', 1),
('肉夹馍', '小吃', '咸', 1),
('凉皮', '小吃', '辣,酸', 1),
('奶茶', '甜品', '甜', 1);
```

---

## 常用查询

### 查询用户最近 7 天吃过的食物

```sql
SELECT f.id, f.name, f.category, uf.eaten_at
FROM eat_records uf
JOIN foods f ON uf.food_id = f.id
WHERE uf.user_id = ?
  AND uf.status = 'eaten'
  AND uf.eaten_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY uf.eaten_at DESC;
```

### 查询用户黑名单食物

```sql
SELECT f.id, f.name, f.category, ub.reason, ub.created_at
FROM user_blacklist ub
JOIN foods f ON ub.food_id = f.id
WHERE ub.user_id = ?
ORDER BY ub.created_at DESC;
```

### 查询用户不想吃的分类（未过期）

```sql
SELECT category, expires_at
FROM user_dislikes
WHERE user_id = ?
  AND expires_at > NOW()
ORDER BY expires_at ASC;
```

### 查询用户偏好权重

```sql
SELECT category, weight
FROM user_prefs
WHERE user_id = ?
ORDER BY weight DESC;
```

### 查询投票结果

```sql
SELECT 
  ro.id,
  f.name AS food_name,
  f.category,
  ro.vote_count
FROM vote_options ro
JOIN foods f ON ro.food_id = f.id
WHERE ro.vote_id = ?
ORDER BY ro.vote_count DESC;
```

---

## 数据库维护

### 定期清理过期数据

```sql
-- 清理过期的"不想吃"记录
DELETE FROM user_dislikes 
WHERE expires_at < NOW();

-- 清理过期的投票（可选，或标记为 ended）
UPDATE votes 
SET status = 'ended' 
WHERE expires_at < NOW() AND status = 'active';
```

### 数据备份

```bash
# 备份数据库
mysqldump -u root -p eatwhat > backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p eatwhat < backup_20240115.sql
```
