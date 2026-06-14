-- =====================================================
-- V4: 为 eat_records 表添加 meal_type 字段
-- =====================================================

ALTER TABLE `eat_records`
ADD COLUMN `meal_type` VARCHAR(16) DEFAULT '' COMMENT '餐段：早餐、午餐、晚餐、夜宵' AFTER `food_id`;
