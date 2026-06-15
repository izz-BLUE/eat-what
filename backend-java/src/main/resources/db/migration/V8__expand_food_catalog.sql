-- =====================================================
-- V8: expand_food_catalog
-- =====================================================
--
-- 自动生成于 data/foods.csv
-- 生成命令: npm run foods:generate -- V8 expand_food_catalog
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
  '奶茶',
  '甜品',
  '甜品',
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
