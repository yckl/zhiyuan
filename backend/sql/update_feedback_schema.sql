-- 增加缺失的 contact_info 列
ALTER TABLE feedback ADD COLUMN contact_info VARCHAR(100) COMMENT '联系方式';

-- 如果还有其他列缺失，也可以一并补全（以防万一）
-- ALTER TABLE feedback ADD COLUMN reply_content TEXT COMMENT '管理员回复';
-- ALTER TABLE feedback ADD COLUMN user_id BIGINT COMMENT '提交人ID';
