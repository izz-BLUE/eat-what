-- =====================================================
-- V14: expand_popular_food_catalog
-- =====================================================
--
-- 自动生成于 data/foods.csv
-- 生成命令: npm run foods:generate -- V14 expand_popular_food_catalog
--
-- 说明：
--   1. V7 已建立 uk_foods_name 唯一索引，本 migration 不再重复
--   2. 按 name 作为业务唯一键，INSERT ... ON DUPLICATE KEY UPDATE
--   3. CSV 中不存在的菜品不会被自动删除
--   4. 禁用菜品使用 enabled=0，不使用 DELETE
--
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '白切鸡',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '白灼虾',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '百香果茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,酸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '班尼迪克蛋',
  '西餐',
  '',
  '西餐',
  '早餐,午餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '包子',
  '小吃',
  '小吃',
  '',
  '早餐,午餐',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '煲仔饭',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '冰粉',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '冰淇淋',
  '甜品',
  '甜品',
  '',
  '晚餐,夜宵',
  '甜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '波霸奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '菠萝咕咾肉',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '甜,酸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '叉烧',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '甜,咸',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '叉烧饭',
  '粤菜',
  '快餐',
  '粤菜',
  '午餐,晚餐',
  '甜,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '茶碗蒸',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '清淡,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '肠粉',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
  '清淡,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '潮汕牛肉火锅',
  '火锅',
  '火锅',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '炒饭',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '豉油鸡',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '咸,鲜,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '豉汁蒸排骨',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '咸,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '重庆老火锅',
  '火锅',
  '火锅',
  '川菜',
  '午餐,晚餐,夜宵',
  '辣,麻',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '重庆小面',
  '川菜',
  '面食',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '臭豆腐',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '辣',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '川味烤鱼',
  '烧烤',
  '烧烤',
  '川菜',
  '午餐,晚餐,夜宵',
  '辣,麻',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '串串香',
  '火锅',
  '火锅,小吃',
  '川菜',
  '午餐,晚餐,夜宵',
  '辣,麻',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '春卷',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '大盘鸡',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '辣,咸',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '担担面',
  '面食',
  '面食',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '蛋糕',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '刀削面',
  '面食',
  '面食',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '地三鲜',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '丁丁炒面',
  '面食',
  '面食',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '东北乱炖',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '豆腐花',
  '甜品',
  '甜品',
  '',
  '早餐,午餐,晚餐,夜宵',
  '甜,清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '豆浆',
  '小吃',
  '小吃',
  '',
  '早餐',
  '清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '豆乳奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '剁椒鱼头',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '法式吐司',
  '西餐',
  '快餐',
  '西餐',
  '早餐,午餐',
  '甜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '番茄炒蛋',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '甜,酸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '番茄肉酱意面',
  '西餐',
  '面食',
  '西餐',
  '午餐,晚餐',
  '酸,咸',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '番薯糖水',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '饭团',
  '日料',
  '快餐',
  '日料',
  '早餐,午餐',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '夫妻肺片',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '盖浇饭',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '干煸四季豆',
  '家常菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '干炒牛河',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '干锅茶树菇',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '干锅肥肠',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '宫保鸡丁',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '广式皮蛋瘦肉粥',
  '小吃',
  '小吃',
  '粤菜',
  '早餐,午餐,夜宵',
  '清淡,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '广式烧卖',
  '小吃',
  '小吃',
  '粤菜',
  '早餐,午餐',
  '鲜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '龟苓膏',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '锅包肉',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '甜,酸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '过桥米线',
  '面食',
  '面食',
  '',
  '午餐,晚餐',
  '清淡,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '韩式烤肉',
  '烧烤',
  '烧烤',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '汉堡',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '红豆奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '红豆沙',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '红烧茄子',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香,甜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '红烧肉',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,甜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '厚乳拿铁',
  '茶饮',
  '茶饮',
  '',
  '早餐,午餐',
  '香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '厚乳奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '湖南小炒肉',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '黄焖鸡米饭',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸,辣',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '回锅肉',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烩面',
  '面食',
  '面食',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '馄饨',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,晚餐,夜宵',
  '清淡,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '火锅',
  '火锅',
  '火锅',
  '',
  '午餐,晚餐,夜宵',
  '辣,麻',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '鸡蛋仔',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '鸡腿饭',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '家常豆腐',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '煎饼果子',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
  '咸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '姜撞奶',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '焦糖拿铁',
  '茶饮',
  '茶饮',
  '',
  '早餐,午餐',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '咖喱猪排饭',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤串',
  '烧烤',
  '烧烤',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤鸡翅',
  '烧烤',
  '烧烤',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤冷面',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤奶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤南瓜汤',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '甜,清淡',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤肉',
  '烧烤',
  '烧烤',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烤羊排',
  '烧烤',
  '烧烤',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '可乐鸡翅',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '甜,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '口水鸡',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '口味虾',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '拉面',
  '日料',
  '面食',
  '日料',
  '午餐,晚餐',
  '咸,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '腊味合蒸',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '辣椒炒肉',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '辣子鸡',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '兰州拉面',
  '面食',
  '面食',
  '',
  '早餐,午餐,晚餐',
  '清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '老火靓汤',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '凉糕',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '凉皮',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '辣,酸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '卤鸡爪',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '卤鸭脖',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '辣,咸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '绿豆沙',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '螺蛳粉',
  '面食',
  '面食',
  '',
  '午餐,晚餐,夜宵',
  '辣,酸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '麻辣火锅',
  '火锅',
  '火锅',
  '川菜',
  '午餐,晚餐,夜宵',
  '辣,麻',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '麻辣烫',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '辣,麻',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '麻辣香锅',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '麻婆豆腐',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '鳗鱼饭',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '甜,咸',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '芒果绿茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,酸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '毛氏红烧肉',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '咸,香,甜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '毛血旺',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '美式咖啡',
  '茶饮',
  '茶饮',
  '',
  '早餐,午餐',
  '香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '茉莉花茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '清淡,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '茉莉奶绿',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '墨西哥卷饼',
  '西餐',
  '快餐',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '木须肉',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '拿铁',
  '茶饮',
  '茶饮',
  '',
  '早餐,午餐',
  '香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '奶茶',
  '茶饮',
  '茶饮',
  '',
  '晚餐,夜宵',
  '甜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '奶油蘑菇意面',
  '西餐',
  '面食',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '奶油浓汤',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '香,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '柠檬茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,酸,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '牛丼',
  '日料',
  '快餐',
  '日料',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '牛排',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '牛肉面',
  '面食',
  '面食',
  '',
  '早餐,午餐,晚餐',
  '咸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '农家一碗香',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '泡椒牛蛙',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,酸',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '披萨',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '葡萄果茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,酸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '荞麦面',
  '日料',
  '面食',
  '日料',
  '午餐,晚餐',
  '清淡',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '亲子丼',
  '日料',
  '快餐',
  '日料',
  '午餐,晚餐',
  '咸,香,甜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '青椒肉丝',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '轻乳茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '清淡,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '清炒时蔬',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '清汤火锅',
  '火锅',
  '火锅',
  '',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '清蒸排骨',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '清蒸鱼',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '热干面',
  '面食',
  '面食',
  '',
  '早餐,午餐',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '日式咖喱饭',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '日式烤鳗鱼',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '甜,咸',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '日式冷豆腐',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '清淡,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '日式烧肉',
  '烧烤',
  '烧烤',
  '日料',
  '午餐,晚餐,夜宵',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '日式炸猪排',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '肉夹馍',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,晚餐,夜宵',
  '咸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '肉末茄子',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '三明治',
  '西餐',
  '快餐',
  '西餐',
  '早餐,午餐',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '三文鱼刺身',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '清淡,鲜',
  4,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '沙拉',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '清淡',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '沙县小吃',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,晚餐,夜宵',
  '清淡',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烧鹅',
  '粤菜',
  '',
  '粤菜',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烧鸟拼盘',
  '烧烤',
  '烧烤',
  '日料',
  '午餐,晚餐,夜宵',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '生椰拿铁',
  '茶饮',
  '茶饮',
  '',
  '早餐,午餐',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '手打柠檬茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,酸,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '手撕包菜',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,咸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '手抓羊肉',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '寿司',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '薯条',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '双皮奶',
  '甜品',
  '甜品',
  '粤菜',
  '午餐,晚餐,夜宵',
  '甜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '水煮肉片',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '水煮鱼',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '辣,麻',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '四季春茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '清淡,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '酸菜鱼',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '酸,辣',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '酸辣鸡杂',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '酸,辣',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '酸辣土豆丝',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '酸,辣',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '蒜蓉西兰花',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '清淡,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '蒜苔炒肉',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '台式卤肉饭',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '汤圆',
  '甜品',
  '甜品',
  '',
  '早餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '糖醋排骨',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '甜,酸',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '糖水',
  '甜品',
  '甜品',
  '粤菜',
  '午餐,晚餐,夜宵',
  '甜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '桃桃乌龙',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '提拉米苏',
  '甜品',
  '甜品',
  '西餐',
  '午餐,晚餐,夜宵',
  '甜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '天妇罗',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '鲜,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '土豆炖牛肉',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '味噌拉面',
  '日料',
  '面食',
  '日料',
  '午餐,晚餐',
  '咸,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '乌冬面',
  '日料',
  '面食',
  '日料',
  '午餐,晚餐',
  '清淡,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '乌龙奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '西瓜水果茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,酸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '西红柿牛腩',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '酸,咸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '西式烤鸡',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '虾饺',
  '粤菜',
  '小吃',
  '粤菜',
  '早餐,午餐,晚餐',
  '鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '咸豆腐脑',
  '小吃',
  '小吃',
  '',
  '早餐,午餐',
  '咸,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '香干炒肉',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '湘味蒜苗腊肉',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '湘西外婆菜',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,咸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '小炒黄牛肉',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,香',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '小炒鸡',
  '湘菜',
  '',
  '湘菜',
  '午餐,晚餐',
  '辣,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '小鸡炖蘑菇',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '新疆烤包子',
  '小吃',
  '小吃',
  '',
  '早餐,午餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '杏仁豆腐',
  '甜品',
  '甜品',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '燕麦拿铁',
  '茶饮',
  '茶饮',
  '',
  '早餐,午餐',
  '香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '杨枝甘露',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '洋葱圈',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '椰子鸡火锅',
  '火锅',
  '火锅',
  '粤菜',
  '午餐,晚餐',
  '清淡,鲜',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '意大利面',
  '西餐',
  '面食',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '油条',
  '小吃',
  '小吃',
  '',
  '早餐',
  '咸',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '鱼香茄子',
  '川菜',
  '',
  '川菜',
  '午餐,晚餐',
  '咸,甜,酸',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '鱼香肉丝',
  '家常菜',
  '',
  '川菜',
  '午餐,晚餐',
  '甜,酸,辣',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '芋泥奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '云吞',
  '粤菜',
  '小吃',
  '粤菜',
  '早餐,午餐,晚餐',
  '清淡,鲜',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '云吞面',
  '面食',
  '面食',
  '',
  '早餐,午餐,晚餐',
  '清淡,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '炸鸡',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '炸鸡排',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '炸酱面',
  '面食',
  '面食',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '炸鱼薯条',
  '西餐',
  '',
  '西餐',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '章鱼小丸子',
  '小吃',
  '小吃',
  '日料',
  '午餐,晚餐,夜宵',
  '咸,鲜',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '照烧鸡排饭',
  '日料',
  '',
  '日料',
  '午餐,晚餐',
  '甜,咸',
  3,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '珍珠奶茶',
  '茶饮',
  '茶饮',
  '',
  '午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '芝麻糊',
  '甜品',
  '甜品',
  '',
  '早餐,午餐,晚餐,夜宵',
  '甜,香',
  1,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '猪脚饭',
  '快餐',
  '快餐',
  '',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '猪肉炖粉条',
  '家常菜',
  '',
  '家常菜',
  '午餐,晚餐',
  '咸,香',
  2,
  1
)
AS new
ON DUPLICATE KEY UPDATE
  `category` = new.`category`,
  `type_tags` = new.`type_tags`,
  `cuisine_tags` = new.`cuisine_tags`,
  `meal_types` = new.`meal_types`,
  `taste_tags` = new.`taste_tags`,
  `price_level` = new.`price_level`,
  `enabled` = new.`enabled`,
  `updated_at` = CURRENT_TIMESTAMP;
