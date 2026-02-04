-- =====================================================
-- 培训考试模块补充表
-- =====================================================

-- 课程题目表
DROP TABLE IF EXISTS `course_question`;
CREATE TABLE `course_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `course_id` BIGINT NOT NULL COMMENT '所属课程ID',
  `question_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型:1-单选,2-多选,3-判断',
  `content` TEXT NOT NULL COMMENT '题目内容',
  `options` JSON DEFAULT NULL COMMENT '选项(JSON数组)',
  `correct_answer` VARCHAR(50) NOT NULL COMMENT '正确答案(如A,AB)',
  `score` INT NOT NULL DEFAULT 10 COMMENT '分值',
  `difficulty` TINYINT NOT NULL DEFAULT 1 COMMENT '难度:1-简单,2-中等,3-困难',
  `explanation` TEXT DEFAULT NULL COMMENT '答案解析',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程题目表';

-- 考试记录表
DROP TABLE IF EXISTS `course_exam_record`;
CREATE TABLE `course_exam_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
  `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
  `correct_count` INT NOT NULL DEFAULT 0 COMMENT '正确题数',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '总题数',
  `passed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否通过:0-否,1-是',
  `points_reward` INT NOT NULL DEFAULT 0 COMMENT '获得积分',
  `answers` JSON DEFAULT NULL COMMENT '答题详情(JSON)',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `time_spent` INT DEFAULT NULL COMMENT '用时(秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_passed` (`passed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程考试记录表';
