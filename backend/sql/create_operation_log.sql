-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人用户名',
    role VARCHAR(50) COMMENT '操作人角色',
    operation_type VARCHAR(20) COMMENT '操作类型：CREATE, UPDATE, DELETE, QUERY, LOGIN, OTHER',
    operation VARCHAR(255) COMMENT '操作描述',
    path VARCHAR(255) COMMENT '请求路径',
    method VARCHAR(10) COMMENT '请求方法：GET, POST, PUT, DELETE',
    params TEXT COMMENT '请求参数 (JSON)',
    ip VARCHAR(50) COMMENT 'IP地址',
    result VARCHAR(20) COMMENT '操作结果：SUCCESS, FAIL',
    error_msg TEXT COMMENT '错误信息',
    cost_time BIGINT COMMENT '耗时（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operator_name (operator_name),
    INDEX idx_create_time (create_time),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 验证表结构
DESC operation_log;
