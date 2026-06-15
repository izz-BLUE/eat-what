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
  `taste_tags` VARCHAR(128) DEFAULT '' COMMENT '口味标签（辣、甜、咸等），逗号分隔',
  `price_level` TINYINT DEFAULT 0 COMMENT '价格等级（1-5）',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `image_url` VARCHAR(256) DEFAULT '' COMMENT '菜品图片',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_foods_category` (`category`)
) COMMENT='菜品表';

-- 吃过记录表
CREATE TABLE IF NOT EXISTS `eat_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `food_id` BIGINT NOT NULL COMMENT '食物ID',
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

-- 初始化测试数据
INSERT INTO `foods` (`name`, `category`, `taste_tags`, `price_level`, `enabled`) VALUES
('猪脚饭', '快餐', '咸,香', 2, 1),
('黄焖鸡米饭', '快餐', '咸,微辣', 2, 1),
('麻辣烫', '小吃', '辣,麻', 2, 1),
('肠粉', '小吃', '清淡,鲜', 1, 1),
('云吞面', '面食', '清淡,鲜', 2, 1),
('兰州拉面', '面食', '清淡', 1, 1),
('沙县小吃', '小吃', '清淡', 1, 1),
('盖浇饭', '快餐', '咸', 1, 1),
('炒饭', '快餐', '咸,香', 1, 1),
('炸鸡', '快餐', '咸,香', 2, 1),
('汉堡', '快餐', '咸,香', 2, 1),
('披萨', '西餐', '咸,香', 3, 1),
('寿司', '日料', '清淡,鲜', 3, 1),
('拉面', '日料', '咸,鲜', 3, 1),
('烤肉', '烧烤', '咸,香', 3, 1),
('火锅', '火锅', '辣,麻', 4, 1),
('酸菜鱼', '川菜', '酸,辣', 3, 1),
('宫保鸡丁', '川菜', '辣', 2, 1),
('回锅肉', '川菜', '辣,咸', 2, 1),
('红烧肉', '家常菜', '咸,甜', 2, 1),
('糖醋排骨', '家常菜', '甜,酸', 3, 1),
('清蒸鱼', '粤菜', '清淡,鲜', 3, 1),
('白切鸡', '粤菜', '清淡,鲜', 3, 1),
('叉烧', '粤菜', '甜,咸', 3, 1),
('湖南小炒肉', '湘菜', '辣', 2, 1),
('臭豆腐', '小吃', '辣', 1, 1),
('煎饼果子', '小吃', '咸', 1, 1),
('肉夹馍', '小吃', '咸', 1, 1),
('凉皮', '小吃', '辣,酸', 1, 1),
('奶茶', '甜品', '甜', 1, 1);
