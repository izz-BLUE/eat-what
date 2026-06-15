-- =====================================================
-- V6: 扩展菜品分类体系
-- =====================================================
--
-- 变更：
--   1. 新增 type_tags（食物类型，多值）、cuisine_tags（菜系/风格，多值）、
--      meal_types（适用餐段，多值）三个字段
--   2. 保留 category 字段用于展示和旧接口兼容
--   3. 规范化 taste_tags：微辣→辣
--   4. 按菜名匹配，为全部 30 道菜填充新字段
--
-- =====================================================

-- 1. 新增字段
ALTER TABLE `foods`
  ADD COLUMN `type_tags` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '食物类型，逗号分隔：快餐,小吃,面食,火锅,烧烤,甜品' AFTER `category`,
  ADD COLUMN `cuisine_tags` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '菜系/风格，逗号分隔：家常菜,川菜,湘菜,粤菜,日料,西餐' AFTER `type_tags`,
  ADD COLUMN `meal_types` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '适用餐段，逗号分隔：早餐,午餐,晚餐,夜宵' AFTER `cuisine_tags`;

-- 2. 规范化 taste_tags：微辣→辣
UPDATE `foods` SET `taste_tags` = REPLACE(`taste_tags`, '微辣', '辣') WHERE `taste_tags` LIKE '%微辣%';

-- 3. 按菜名逐条填充新字段（按菜名匹配，不依赖 ID）
-- 格式：UPDATE foods SET type_tags='...', cuisine_tags='...', meal_types='...' WHERE name='...';

-- 快餐类
UPDATE `foods` SET `type_tags`='快餐', `cuisine_tags`='', `meal_types`='午餐,晚餐' WHERE `name`='猪脚饭';
UPDATE `foods` SET `type_tags`='快餐', `cuisine_tags`='', `meal_types`='午餐,晚餐' WHERE `name`='黄焖鸡米饭';
UPDATE `foods` SET `type_tags`='快餐', `cuisine_tags`='', `meal_types`='午餐,晚餐' WHERE `name`='盖浇饭';
UPDATE `foods` SET `type_tags`='快餐', `cuisine_tags`='', `meal_types`='午餐,晚餐' WHERE `name`='炒饭';
UPDATE `foods` SET `type_tags`='快餐', `cuisine_tags`='', `meal_types`='午餐,晚餐,夜宵' WHERE `name`='炸鸡';
UPDATE `foods` SET `type_tags`='快餐', `cuisine_tags`='', `meal_types`='午餐,晚餐' WHERE `name`='汉堡';

-- 小吃类
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='午餐,晚餐,夜宵' WHERE `name`='麻辣烫';
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='早餐,午餐,夜宵' WHERE `name`='肠粉';
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='早餐,午餐,晚餐,夜宵' WHERE `name`='沙县小吃';
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='午餐,晚餐,夜宵' WHERE `name`='臭豆腐';
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='早餐,午餐,夜宵' WHERE `name`='煎饼果子';
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='早餐,午餐,晚餐,夜宵' WHERE `name`='肉夹馍';
UPDATE `foods` SET `type_tags`='小吃', `cuisine_tags`='', `meal_types`='午餐,晚餐,夜宵' WHERE `name`='凉皮';

-- 面食类
UPDATE `foods` SET `type_tags`='面食', `cuisine_tags`='', `meal_types`='早餐,午餐,晚餐' WHERE `name`='云吞面';
UPDATE `foods` SET `type_tags`='面食', `cuisine_tags`='', `meal_types`='早餐,午餐,晚餐' WHERE `name`='兰州拉面';

-- 日料拉面：type=面食 + cuisine=日料
UPDATE `foods` SET `type_tags`='面食', `cuisine_tags`='日料', `meal_types`='午餐,晚餐' WHERE `name`='拉面';

-- 火锅/烧烤/甜品（食物类型）
UPDATE `foods` SET `type_tags`='火锅', `cuisine_tags`='', `meal_types`='午餐,晚餐,夜宵' WHERE `name`='火锅';
UPDATE `foods` SET `type_tags`='烧烤', `cuisine_tags`='', `meal_types`='午餐,晚餐,夜宵' WHERE `name`='烤肉';
UPDATE `foods` SET `type_tags`='甜品', `cuisine_tags`='', `meal_types`='' WHERE `name`='奶茶';

-- 西餐/日料/家常菜（菜系）
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='西餐', `meal_types`='午餐,晚餐' WHERE `name`='披萨';
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='日料', `meal_types`='午餐,晚餐' WHERE `name`='寿司';

-- 川菜类
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='川菜', `meal_types`='午餐,晚餐' WHERE `name`='酸菜鱼';
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='川菜', `meal_types`='午餐,晚餐' WHERE `name`='宫保鸡丁';
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='川菜', `meal_types`='午餐,晚餐' WHERE `name`='回锅肉';

-- 家常菜类
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='家常菜', `meal_types`='午餐,晚餐' WHERE `name`='红烧肉';
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='家常菜', `meal_types`='午餐,晚餐' WHERE `name`='糖醋排骨';

-- 粤菜类
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='粤菜', `meal_types`='午餐,晚餐' WHERE `name`='清蒸鱼';
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='粤菜', `meal_types`='午餐,晚餐' WHERE `name`='白切鸡';
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='粤菜', `meal_types`='午餐,晚餐' WHERE `name`='叉烧';

-- 湘菜类
UPDATE `foods` SET `type_tags`='', `cuisine_tags`='湘菜', `meal_types`='午餐,晚餐' WHERE `name`='湖南小炒肉';
