-- 用户反馈表
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '提交人ID (匿名为空)',
    contact_info VARCHAR(100) COMMENT '联系方式',
    content TEXT NOT NULL COMMENT '反馈内容',
    reply_content TEXT COMMENT '管理员回复',
    status INT DEFAULT 0 COMMENT '状态：0-待处理，1-已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 验证表结构
DESC feedback;
