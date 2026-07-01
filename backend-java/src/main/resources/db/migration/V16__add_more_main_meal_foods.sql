-- =====================================================
-- V16: add_more_main_meal_foods
-- =====================================================
--
-- 补充主餐类菜品，重点覆盖以下缺口：
--   - 饺子/锅贴/小笼包（6）
--   - 粥类（9，含用户指定的艇仔粥、及第粥）
--   - 面食/粉类（5，排除已有）
--   - 东南亚菜（5）
--   - 韩式菜（4）
--   - 冒菜/麻辣（2）
--   - 卤味/熟食（4）
--   - 铁板类（3）
--   - 轻食健康（5）
--   - 粤式高频（4，排除已有云吞面、干炒牛河、叉烧饭）
--   - 其他（3：茶树菇炒腊肉、香辣虾、羊肉泡馍）
--
-- 共计 50 个新菜品。
-- 使用与 V8~V14 相同的 INSERT ... ON DUPLICATE KEY UPDATE 幂等写法。
-- 所有主餐类菜品 type_tags 不含"甜品"/"茶饮"，确保指定餐段时不会被排除。
-- 番茄蛋汤为配汤，不属于主餐，已移除。
--
-- =====================================================

-- =====================================================
-- 一、饺子/锅贴/小笼包类（6）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '水饺',
  '小吃',
  '小吃,面食',
  '',
  '早餐,午餐,晚餐,夜宵',
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
  '蒸饺',
  '小吃',
  '小吃,面食',
  '',
  '早餐,午餐,晚餐',
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
  '煎饺',
  '小吃',
  '小吃,面食',
  '',
  '早餐,午餐,晚餐,夜宵',
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
  '锅贴',
  '小吃',
  '小吃,面食',
  '',
  '早餐,午餐,晚餐,夜宵',
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
  '小笼包',
  '小吃',
  '小吃,面食',
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
  '生煎包',
  '小吃',
  '小吃,面食',
  '',
  '早餐,午餐,晚餐,夜宵',
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

-- =====================================================
-- 二、粥类（9）
-- type_tags 使用"小吃"（与已有皮蛋瘦肉粥保持一致，且不被 isSideItemType 过滤）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '小米粥',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
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
  '白粥',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
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
  '八宝粥',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
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
  '生滚鱼片粥',
  '小吃',
  '小吃',
  '粤菜',
  '早餐,午餐,夜宵',
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
  '南瓜粥',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
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
  '艇仔粥',
  '小吃',
  '小吃',
  '粤菜',
  '早餐,午餐,夜宵',
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
  '及第粥',
  '小吃',
  '小吃',
  '粤菜',
  '早餐,午餐,夜宵',
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
  '红枣粥',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
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
  '山药排骨粥',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,夜宵',
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

-- =====================================================
-- 三、面食/粉类（5，排除已有面食）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '酸辣粉',
  '面食',
  '面食',
  '川菜',
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
  '桂林米粉',
  '面食',
  '面食',
  '',
  '早餐,午餐,晚餐',
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
  '拌面',
  '面食',
  '面食',
  '',
  '早餐,午餐,晚餐',
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
  '炒面',
  '快餐',
  '快餐',
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
  '鸭血粉丝汤',
  '小吃',
  '小吃',
  '',
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

-- =====================================================
-- 四、东南亚菜（5）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '越南河粉',
  '东南亚菜',
  '面食',
  '东南亚菜',
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
  '冬阴功汤',
  '东南亚菜',
  '',
  '东南亚菜',
  '午餐,晚餐',
  '辣,酸,鲜',
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
  '泰式炒河粉',
  '东南亚菜',
  '面食',
  '东南亚菜',
  '午餐,晚餐',
  '酸,甜,辣',
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
  '叻沙',
  '东南亚菜',
  '面食',
  '东南亚菜',
  '午餐,晚餐',
  '辣,鲜,香',
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
  '椰浆饭',
  '东南亚菜',
  '快餐',
  '东南亚菜',
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

-- =====================================================
-- 五、韩式菜（4）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '石锅拌饭',
  '韩式',
  '快餐',
  '韩式',
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
  '部队锅',
  '韩式',
  '火锅',
  '韩式',
  '午餐,晚餐,夜宵',
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
  '韩式炸鸡',
  '韩式',
  '快餐',
  '韩式',
  '午餐,晚餐,夜宵',
  '甜,辣,香',
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
  '泡菜汤',
  '韩式',
  '',
  '韩式',
  '午餐,晚餐',
  '辣,酸',
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

-- =====================================================
-- 六、冒菜/麻辣类（2）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '冒菜',
  '小吃',
  '小吃',
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
  '钵钵鸡',
  '小吃',
  '小吃',
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

-- =====================================================
-- 七、卤味/熟食（4）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '卤牛肉',
  '小吃',
  '小吃',
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
  '卤猪蹄',
  '小吃',
  '小吃',
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
  '卤味拼盘',
  '小吃',
  '小吃',
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
  '烧腊饭',
  '快餐',
  '快餐',
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

-- =====================================================
-- 八、铁板类（3）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '铁板牛柳',
  '家常菜',
  '',
  '',
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
  '铁板豆腐',
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
  '铁板鱿鱼',
  '小吃',
  '小吃',
  '',
  '午餐,晚餐,夜宵',
  '咸,辣,香',
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

-- =====================================================
-- 九、轻食健康（5）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '谷物碗',
  '轻食',
  '快餐',
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
  'Poke Bowl',
  '轻食',
  '快餐',
  '西餐',
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
  '轻食便当',
  '轻食',
  '快餐',
  '',
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
  '鸡胸肉饭',
  '轻食',
  '快餐',
  '',
  '午餐,晚餐',
  '清淡,咸',
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
  '牛油果鸡肉沙拉',
  '轻食',
  '',
  '西餐',
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

-- =====================================================
-- 十、粤式高频补充（4，排除已有云吞面、干炒牛河、叉烧饭）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '烧腊双拼饭',
  '快餐',
  '快餐',
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
  '豉油鸡饭',
  '快餐',
  '快餐',
  '粤菜',
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
  '滑蛋牛肉饭',
  '快餐',
  '快餐',
  '粤菜',
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
  '猪杂汤粉',
  '面食',
  '面食',
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

-- =====================================================
-- 十一、其他主餐补充（3）
-- =====================================================

INSERT INTO `foods` (
  `name`, `category`, `type_tags`, `cuisine_tags`,
  `meal_types`, `taste_tags`, `price_level`, `enabled`
) VALUES (
  '茶树菇炒腊肉',
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
  '香辣虾',
  '川菜',
  '',
  '川菜',
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
  '羊肉泡馍',
  '小吃',
  '小吃',
  '',
  '早餐,午餐,晚餐',
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
