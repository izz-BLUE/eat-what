-- =====================================================
-- V9: create_user_feedbacks_table
-- =====================================================

CREATE TABLE IF NOT EXISTS `user_feedbacks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '用户ID（匿名提交为NULL）',
  `type` VARCHAR(32) NOT NULL COMMENT '反馈类型：FEATURE/BUG/RECOMMENDATION/UI/OTHER',
  `rating` TINYINT NULL COMMENT '满意度评分（1-5，可选）',
  `content` VARCHAR(500) NOT NULL COMMENT '反馈内容',
  `contact` VARCHAR(100) NULL COMMENT '联系方式（可选）',
  `page` VARCHAR(128) NULL COMMENT '来源页面路径',
  `system_info` VARCHAR(1000) NULL COMMENT '微信环境信息（JSON）',
  `status` VARCHAR(32) NOT NULL DEFAULT 'NEW' COMMENT '处理状态：NEW/PROCESSING/RESOLVED/CLOSED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意见反馈表';
