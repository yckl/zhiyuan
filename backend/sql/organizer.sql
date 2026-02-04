CREATE TABLE IF NOT EXISTS `organizer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `org_name` varchar(100) DEFAULT NULL COMMENT '组织名称',
  `org_type` varchar(50) DEFAULT NULL COMMENT '组织类型',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `description` text COMMENT '组织简介',
  `logo` varchar(255) DEFAULT NULL COMMENT '组织Logo',
  `address` varchar(255) DEFAULT NULL COMMENT '办公地址',
  `verified` int(1) DEFAULT '0' COMMENT '是否认证：0-未认证，1-已认证',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织者信息表';
