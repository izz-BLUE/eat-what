-- =====================================================
-- V15: create_recommendation_feedbacks_table
-- 用户对推荐结果的轻反馈（不喜欢原因记录）
-- =====================================================

CREATE TABLE IF NOT EXISTS `recommendation_feedbacks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '用户ID（匿名提交为NULL）',
  `food_source` VARCHAR(16) NOT NULL COMMENT '食物来源：DEFAULT-系统菜品，CUSTOM-自定义菜品',
  `food_id` BIGINT NULL COMMENT '菜品ID（DEFAULT来源时必填）',
  `custom_food_id` BIGINT NULL COMMENT '自定义菜品ID（CUSTOM来源时必填）',
  `food_name` VARCHAR(100) NOT NULL COMMENT '菜品名称快照',
  `reason` VARCHAR(32) NOT NULL COMMENT '不喜欢原因',
  `meal_type` VARCHAR(32) NULL COMMENT '推荐时的餐段',
  `price_level` VARCHAR(32) NULL COMMENT '推荐时的价格级别',
  `taste` VARCHAR(32) NULL COMMENT '推荐时的口味',
  `type_tags` VARCHAR(255) NULL COMMENT '推荐时的类型标签',
  `cuisine_tags` VARCHAR(255) NULL COMMENT '推荐时的菜系标签',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_rf_user_created` (`user_id`, `created_at`),
  KEY `idx_rf_reason_created` (`reason`, `created_at`),
  KEY `idx_rf_food_source_created` (`food_source`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐反馈表';
