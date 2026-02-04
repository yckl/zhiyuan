-- 修复 collection 表缺失的 update_time 列
-- MySQL 版本兼容写法

USE `volunteer_system`;

-- 方法1：直接添加（如果列已存在会报错，忽略即可）
ALTER TABLE `collection` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`;

-- 如果上面报错 "Duplicate column name"，说明列已存在，无需处理
