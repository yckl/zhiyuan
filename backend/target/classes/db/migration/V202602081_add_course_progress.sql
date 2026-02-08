-- =====================================================
-- 课程学习进度表
-- 用于记录志愿者的课程学习进度，确保必须完成学习才能参加考试
-- =====================================================
CREATE TABLE IF NOT EXISTS `course_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
    `course_id` BIGINT NOT NULL COMMENT '课程ID',
    `progress` INT DEFAULT 0 COMMENT '学习进度(0-100%)',
    `study_duration` INT DEFAULT 0 COMMENT '已学习时长(秒)',
    `total_duration` INT DEFAULT 0 COMMENT '课程总时长(秒)',
    `last_position` INT DEFAULT 0 COMMENT '最后学习位置(秒)',
    `last_study_time` DATETIME DEFAULT NULL COMMENT '最后学习时间',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否已完成学习',
    `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_volunteer_course` (`volunteer_id`, `course_id`),
    KEY `idx_volunteer_id` (`volunteer_id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学习进度表';
