-- =====================================================
-- 添加用户点赞表
-- =====================================================

USE `volunteer_system`;

-- 用户点赞表
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` VARCHAR(30) NOT NULL COMMENT '目标类型：EXPERIENCE,ACTIVITY,COMMENT',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞表';
