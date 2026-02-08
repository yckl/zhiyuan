-- 方案1：修改 title 字段允许为空
ALTER TABLE feedback MODIFY COLUMN title VARCHAR(255) NULL;

-- 方案2（如果不需要保留旧数据）：
-- DROP TABLE feedback;
-- 然后重新运行 create_feedback.sql
