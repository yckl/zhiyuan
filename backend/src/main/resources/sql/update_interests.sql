-- 为所有志愿者随机生成兴趣标签

-- 1. 更新 interest_tags
-- 使用 CASE WHEN 和 RAND() 模拟随机选择
-- 标签池: '环境保护', '敬老助残', '大型赛会', '支教助学', '社区服务', '文化宣传'

UPDATE volunteer
SET interest_tags = 
    CASE FLOOR(1 + RAND() * 6)
        WHEN 1 THEN '["环境保护", "社区服务"]'
        WHEN 2 THEN '["敬老助残", "文化宣传"]'
        WHEN 3 THEN '["大型赛会", "支教助学"]'
        WHEN 4 THEN '["环境保护", "大型赛会", "社区服务"]'
        WHEN 5 THEN '["支教助学", "文化宣传"]'
        WHEN 6 THEN '["敬老助残"]'
        ELSE '["社区服务"]'
    END
WHERE is_deleted = 0;

-- 2. 创建浏览记录表 (如果不存在)
CREATE TABLE IF NOT EXISTS `activity_view_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `view_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_activity` (`user_id`,`activity_id`),
  KEY `idx_view_time` (`view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动浏览记录';
