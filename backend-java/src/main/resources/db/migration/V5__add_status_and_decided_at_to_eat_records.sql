-- V5: 新增用餐记录两阶段生命周期
-- 新增 status 字段（DECIDED-已决定，EATEN-已吃）
ALTER TABLE `eat_records`
    ADD COLUMN `status` VARCHAR(16) NOT NULL DEFAULT 'EATEN' COMMENT '状态：DECIDED-已决定，EATEN-已吃'
    AFTER `meal_type`;

-- 新增 decided_at 字段（决定时间，仅 DECIDED 状态时有值）
ALTER TABLE `eat_records`
    ADD COLUMN `decided_at` DATETIME NULL COMMENT '决定时间'
    AFTER `status`;

-- eaten_at 改为可空（DECIDED 状态时无吃的时间）
ALTER TABLE `eat_records`
    MODIFY COLUMN `eaten_at` DATETIME NULL COMMENT '吃的时间';

-- 新增索引：按用户 + 状态 + 决定时间查询
ALTER TABLE `eat_records`
    ADD INDEX `idx_user_status_decided` (`user_id`, `status`, `decided_at`);
