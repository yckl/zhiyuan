-- 活动收藏表
CREATE TABLE IF NOT EXISTS `activity_favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动收藏表';
