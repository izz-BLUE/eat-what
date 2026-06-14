-- =====================================================
-- V2: 为 foods 表添加 enabled 字段
-- =====================================================

ALTER TABLE `foods`
ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用' AFTER `price_level`;

-- 默认所有菜品都是启用状态
UPDATE `foods` SET `enabled` = 1 WHERE `enabled` IS NULL;
