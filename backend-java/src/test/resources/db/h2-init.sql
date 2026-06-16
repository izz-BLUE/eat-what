-- H2 测试数据库初始化脚本

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(256) DEFAULT '' COMMENT '头像URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_openid` (`openid`)
) COMMENT='用户表';

-- 菜品表
CREATE TABLE IF NOT EXISTS `foods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(64) NOT NULL COMMENT '菜品名称',
  `category` VARCHAR(32) NOT NULL COMMENT '分类（快餐、火锅、川菜等）',
  `type_tags` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '食物类型，逗号分隔',
  `cuisine_tags` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '菜系/风格，逗号分隔',
  `meal_types` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '适用餐段，逗号分隔',
  `taste_tags` VARCHAR(128) DEFAULT '' COMMENT '口味标签（辣、甜、咸等），逗号分隔',
  `price_level` TINYINT DEFAULT 0 COMMENT '价格等级（1-5）',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `image_url` VARCHAR(256) DEFAULT '' COMMENT '菜品图片',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_foods_name` (`name`),
  KEY `idx_foods_category` (`category`)
) COMMENT='菜品表';

-- 吃过记录表
CREATE TABLE IF NOT EXISTS `eat_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `food_id` BIGINT NULL COMMENT '食物ID（DEFAULT时为必填，CUSTOM时为NULL）',
  `custom_food_id` BIGINT NULL COMMENT '自定义食物ID',
  `food_source` VARCHAR(16) NOT NULL DEFAULT 'DEFAULT' COMMENT 'DEFAULT-系统菜品, CUSTOM-自定义',
  `food_name_snapshot` VARCHAR(64) NULL COMMENT '食物名称快照',
  `category_snapshot` VARCHAR(32) NULL COMMENT '分类快照',
  `type_tags_snapshot` VARCHAR(128) NULL COMMENT '类型标签快照',
  `cuisine_tags_snapshot` VARCHAR(128) NULL COMMENT '菜系标签快照',
  `meal_types_snapshot` VARCHAR(64) NULL COMMENT '餐段快照',
  `taste_tags_snapshot` VARCHAR(128) NULL COMMENT '口味标签快照',
  `price_level_snapshot` TINYINT NULL COMMENT '价格等级快照',
  `meal_type` VARCHAR(16) DEFAULT '' COMMENT '餐段：早餐、午餐、晚餐、夜宵',
  `status` VARCHAR(16) NOT NULL DEFAULT 'EATEN' COMMENT '状态：DECIDED-已决定，EATEN-已吃',
  `decided_at` DATETIME NULL COMMENT '决定时间',
  `rating` TINYINT DEFAULT NULL COMMENT '评分（1-5）',
  `note` VARCHAR(256) DEFAULT '' COMMENT '备注',
  `eaten_at` DATETIME NULL COMMENT '吃的时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_eat_records_user_food_eaten_at` (`user_id`, `food_id`, `eaten_at`),
  KEY `idx_eat_records_user_id` (`user_id`),
  KEY `idx_eat_records_food_id` (`food_id`),
  KEY `idx_eat_records_eaten_at` (`eaten_at`),
  KEY `idx_user_status_decided` (`user_id`, `status`, `decided_at`)
) COMMENT='吃过记录表';

-- 用户黑名单表
CREATE TABLE IF NOT EXISTS `user_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `food_id` BIGINT NOT NULL COMMENT '食物ID',
  `reason` VARCHAR(128) DEFAULT '' COMMENT '拉黑原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blacklist_user_food` (`user_id`, `food_id`),
  KEY `idx_user_blacklist_user_id` (`user_id`)
) COMMENT='用户黑名单表';

-- 用户偏好表
CREATE TABLE IF NOT EXISTS `user_prefs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category` VARCHAR(32) NOT NULL COMMENT '食物分类',
  `weight` INT NOT NULL DEFAULT 50 COMMENT '偏好权重（0-100）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_prefs_user_category` (`user_id`, `category`),
  KEY `idx_user_prefs_user_id` (`user_id`)
) COMMENT='用户偏好表';

-- 用户不想吃表
CREATE TABLE IF NOT EXISTS `user_dislikes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `category` VARCHAR(32) NOT NULL COMMENT '食物分类',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_dislikes_user_category` (`user_id`, `category`),
  KEY `idx_user_dislikes_user_id` (`user_id`),
  KEY `idx_user_dislikes_expires_at` (`expires_at`)
) COMMENT='用户不想吃表';

-- 饭局投票表
CREATE TABLE IF NOT EXISTS `votes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发起人ID',
  `title` VARCHAR(128) NOT NULL COMMENT '饭局标题',
  `status` VARCHAR(16) NOT NULL DEFAULT 'active' COMMENT '状态：active-进行中，ended-已结束',
  `expires_at` DATETIME NOT NULL COMMENT '截止时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_votes_user_id` (`user_id`),
  KEY `idx_votes_status` (`status`),
  KEY `idx_votes_expires_at` (`expires_at`)
) COMMENT='饭局投票表';

-- 投票选项表
CREATE TABLE IF NOT EXISTS `vote_options` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vote_id` BIGINT NOT NULL COMMENT '投票ID',
  `food_id` BIGINT NOT NULL COMMENT '食物ID',
  `vote_count` INT NOT NULL DEFAULT 0 COMMENT '得票数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_vote_options_vote_id` (`vote_id`),
  CONSTRAINT `fk_vote_options_vote` FOREIGN KEY (`vote_id`) REFERENCES `votes` (`id`) ON DELETE CASCADE
) COMMENT='投票选项表';

-- 投票记录表
CREATE TABLE IF NOT EXISTS `vote_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `vote_id` BIGINT NOT NULL COMMENT '投票ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `option_id` BIGINT NOT NULL COMMENT '选项ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vote_records_vote_user_option` (`vote_id`, `user_id`, `option_id`),
  KEY `idx_vote_records_vote_id` (`vote_id`),
  KEY `idx_vote_records_user_id` (`user_id`),
  CONSTRAINT `fk_vote_records_vote` FOREIGN KEY (`vote_id`) REFERENCES `votes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_vote_records_option` FOREIGN KEY (`option_id`) REFERENCES `vote_options` (`id`) ON DELETE CASCADE
) COMMENT='投票记录表';

-- 意见反馈表
CREATE TABLE IF NOT EXISTS `user_feedbacks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NULL COMMENT '用户ID（匿名提交为NULL）',
  `type` VARCHAR(32) NOT NULL COMMENT '反馈类型',
  `rating` TINYINT NULL COMMENT '满意度评分',
  `content` VARCHAR(500) NOT NULL COMMENT '反馈内容',
  `contact` VARCHAR(100) NULL COMMENT '联系方式',
  `page` VARCHAR(128) NULL COMMENT '来源页面路径',
  `system_info` VARCHAR(1000) NULL COMMENT '微信环境信息',
  `status` VARCHAR(32) NOT NULL DEFAULT 'NEW' COMMENT '处理状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_created` (`user_id`, `created_at`),
  KEY `idx_status_created` (`status`, `created_at`)
) COMMENT='意见反馈表';

-- 用户自定义菜品表
CREATE TABLE IF NOT EXISTS user_custom_foods (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  name          VARCHAR(64)  NOT NULL,
  category      VARCHAR(32)  NOT NULL,
  type_tags     VARCHAR(128) NOT NULL DEFAULT '',
  cuisine_tags  VARCHAR(128) NOT NULL DEFAULT '',
  meal_types    VARCHAR(64)  NOT NULL DEFAULT '',
  taste_tags    VARCHAR(128) NOT NULL DEFAULT '',
  price_level   TINYINT      DEFAULT NULL,
  enabled       TINYINT(1)   NOT NULL DEFAULT 1,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_food_name (user_id, name)
);
