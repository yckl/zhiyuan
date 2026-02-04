-- =====================================================
-- 组织者端扩展模块：通知管理 & 服务评价
-- =====================================================

USE `volunteer_system`;

-- 1. 站内信表
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `type` VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT '类型：SYSTEM-系统, URGENT-紧急, NOTICE-公告, ACTIVITY-活动',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_sender` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内信表';

-- 2. 活动服务评价表
DROP TABLE IF EXISTS `activity_review`;
CREATE TABLE `activity_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `activity_id` BIGINT NOT NULL COMMENT '关联活动',
  `volunteer_id` BIGINT NOT NULL COMMENT '评价人',
  `organizer_id` BIGINT NOT NULL COMMENT '被评价的组织者',
  `score` TINYINT NOT NULL COMMENT '评分 (1-5星)',
  `content` TEXT COMMENT '评价内容',
  `reply_content` TEXT COMMENT '组织者回复内容',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 (0=正常显示, 1=申诉中/隐藏, 2=已屏蔽)',
  `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名 (0-否, 1-是)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_organizer` (`organizer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动服务评价表';
