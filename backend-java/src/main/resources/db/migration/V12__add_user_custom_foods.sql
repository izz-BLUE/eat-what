-- =====================================================
-- V12: add_user_custom_foods
-- 1. 创建 user_custom_foods 表
-- 2. 扩展 eat_records 支持自定义菜来源
-- =====================================================

-- 1. 用户自定义菜品表
CREATE TABLE user_custom_foods (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id       BIGINT       NOT NULL COMMENT '用户ID',
  name          VARCHAR(64)  NOT NULL COMMENT '菜品名称',
  category      VARCHAR(32)  NOT NULL COMMENT '分类（派生：第一个cuisine_tag或第一个type_tag）',
  type_tags     VARCHAR(128) NOT NULL DEFAULT '' COMMENT '食物类型，逗号分隔',
  cuisine_tags  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '菜系/风格，逗号分隔',
  meal_types    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '适用餐段，逗号分隔',
  taste_tags    VARCHAR(128) NOT NULL DEFAULT '' COMMENT '口味标签，逗号分隔',
  price_level   TINYINT      DEFAULT NULL COMMENT '价格等级（1-4，可选）',
  enabled       TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_food_name (user_id, name),
  KEY idx_user_enabled (user_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自定义菜品表';

-- 2. eat_records 扩展：food_id 改 nullable + 新增自定义菜和快照字段
ALTER TABLE eat_records MODIFY COLUMN food_id BIGINT NULL COMMENT '食物ID（DEFAULT时为必填，CUSTOM时为NULL）';

ALTER TABLE eat_records
  ADD COLUMN custom_food_id        BIGINT      NULL COMMENT '自定义食物ID' AFTER food_id,
  ADD COLUMN food_source           VARCHAR(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'DEFAULT-系统菜品, CUSTOM-自定义' AFTER custom_food_id,
  ADD COLUMN food_name_snapshot    VARCHAR(64) NULL COMMENT '食物名称快照' AFTER food_source,
  ADD COLUMN category_snapshot     VARCHAR(32) NULL COMMENT '分类快照' AFTER food_name_snapshot,
  ADD COLUMN type_tags_snapshot    VARCHAR(128) NULL COMMENT '类型标签快照' AFTER category_snapshot,
  ADD COLUMN cuisine_tags_snapshot VARCHAR(128) NULL COMMENT '菜系标签快照' AFTER type_tags_snapshot,
  ADD COLUMN meal_types_snapshot   VARCHAR(64)  NULL COMMENT '餐段快照' AFTER cuisine_tags_snapshot,
  ADD COLUMN taste_tags_snapshot   VARCHAR(128) NULL COMMENT '口味标签快照' AFTER meal_types_snapshot,
  ADD COLUMN price_level_snapshot  TINYINT      NULL COMMENT '价格等级快照' AFTER taste_tags_snapshot;
