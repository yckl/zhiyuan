-- =====================================================
-- 校园志愿者管理系统 - 最终完整版 (包含建库语句)
-- MySQL 8.0+
-- Created: 2026-01-17
-- =====================================================

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `volunteer_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 使用该数据库
USE `volunteer_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 一、核心用户认证模块
-- =====================================================

-- 1. 系统用户表（统一认证）
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（学号/工号/管理员账号）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'VOLUNTEER' COMMENT '角色类型：ADMIN-管理员，VOLUNTEER-志愿者，ORGANIZER-组织者',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常，2-锁定',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 2. 志愿者信息表
DROP TABLE IF EXISTS `volunteer`;
CREATE TABLE `volunteer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '志愿者ID',
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `student_no` VARCHAR(30) DEFAULT NULL COMMENT '学号',
  `college` VARCHAR(100) DEFAULT NULL COMMENT '学院',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `class_name` VARCHAR(50) DEFAULT NULL COMMENT '班级',
  `grade` VARCHAR(20) DEFAULT NULL COMMENT '年级',
  `total_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计志愿时长（小时）',
  `total_points` INT NOT NULL DEFAULT 0 COMMENT '累计积分',
  `available_points` INT NOT NULL DEFAULT 0 COMMENT '可用积分',
  `level` INT NOT NULL DEFAULT 1 COMMENT '志愿者等级',
  `interest_tags` JSON DEFAULT NULL COMMENT '兴趣标签（JSON数组）',
  `skills` VARCHAR(500) DEFAULT NULL COMMENT '技能特长',
  `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `id_card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号（加密存储）',
  `emergency_contact` VARCHAR(50) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) DEFAULT NULL COMMENT '紧急联系电话',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_student_no` (`student_no`),
  KEY `idx_college` (`college`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者信息表';

-- 3. 组织者信息表
DROP TABLE IF EXISTS `organizer`;
CREATE TABLE `organizer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '组织者ID',
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `org_name` VARCHAR(100) NOT NULL COMMENT '组织名称',
  `org_type` VARCHAR(50) DEFAULT NULL COMMENT '组织类型：学院/社团/部门',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `description` TEXT DEFAULT NULL COMMENT '组织简介',
  `logo` VARCHAR(500) DEFAULT NULL COMMENT '组织Logo',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '办公地址',
  `verified` TINYINT NOT NULL DEFAULT 0 COMMENT '是否认证：0-未认证，1-已认证',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_org_name` (`org_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织者信息表';

-- =====================================================
-- 二、活动管理模块
-- =====================================================

-- 4. 活动分类表
DROP TABLE IF EXISTS `activity_category`;
CREATE TABLE `activity_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动分类表';

-- 5. 活动表
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `organizer_id` BIGINT DEFAULT NULL COMMENT '发布组织者ID',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `images` JSON DEFAULT NULL COMMENT '活动图片列表',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '活动摘要',
  `content` LONGTEXT DEFAULT NULL COMMENT '活动详情（富文本）',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
  `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
  `register_start` DATETIME DEFAULT NULL COMMENT '报名开始时间',
  `register_end` DATETIME DEFAULT NULL COMMENT '报名截止时间',
  `max_participants` INT NOT NULL DEFAULT 0 COMMENT '最大参与人数（0-不限）',
  `current_participants` INT NOT NULL DEFAULT 0 COMMENT '当前报名人数',
  `min_participants` INT NOT NULL DEFAULT 0 COMMENT '最小开展人数',
  `service_hours` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '志愿时长（小时）',
  `points_reward` INT NOT NULL DEFAULT 0 COMMENT '积分奖励',
  `requirements` TEXT DEFAULT NULL COMMENT '参与要求',
  `contact_info` VARCHAR(200) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-已发布，3-进行中，4-已结束，5-已取消',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-拒绝',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_featured` TINYINT NOT NULL DEFAULT 0 COMMENT '是否精选推荐',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `tags` JSON DEFAULT NULL COMMENT '活动标签',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_organizer` (`organizer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿活动表';

-- 6. 活动报名表
DROP TABLE IF EXISTS `activity_registration`;
CREATE TABLE `activity_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-已报名，1-已确认，2-已签到，3-已完成，4-已取消，5-缺席',
  `sign_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `sign_out_time` DATETIME DEFAULT NULL COMMENT '签退时间',
  `actual_hours` DECIMAL(5,2) DEFAULT NULL COMMENT '实际服务时长',
  `actual_points` INT DEFAULT NULL COMMENT '实际获得积分',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `cancel_reason` VARCHAR(500) DEFAULT NULL COMMENT '取消原因',
  `qr_code` VARCHAR(200) DEFAULT NULL COMMENT '签到二维码',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_volunteer` (`activity_id`, `volunteer_id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- =====================================================
-- 三、通知公告模块
-- =====================================================

-- 7. 通知公告表
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` LONGTEXT DEFAULT NULL COMMENT '通知内容（富文本）',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0-公告，1-通知，2-提醒',
  `target_type` TINYINT NOT NULL DEFAULT 0 COMMENT '目标类型：0-全体，1-志愿者，2-组织者，3-指定用户',
  `target_ids` JSON DEFAULT NULL COMMENT '目标用户ID列表',
  `publisher_id` BIGINT DEFAULT NULL COMMENT '发布者ID',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已撤回',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知公告表';

-- 8. 消息通知表（站内信）
DROP TABLE IF EXISTS `message_notice`;
CREATE TABLE `message_notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT DEFAULT NULL COMMENT '消息内容',
  `type` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '消息类型：SYSTEM-系统，ACTIVITY-活动，POINTS-积分，ORDER-订单',
  `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- =====================================================
-- 四、心得与评论模块
-- =====================================================

-- 9. 志愿心得表
DROP TABLE IF EXISTS `experience`;
CREATE TABLE `experience` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '心得ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `activity_id` BIGINT DEFAULT NULL COMMENT '关联活动ID',
  `title` VARCHAR(200) NOT NULL COMMENT '心得标题',
  `content` LONGTEXT NOT NULL COMMENT '心得内容（富文本）',
  `images` JSON DEFAULT NULL COMMENT '图片列表',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已发布，2-已拒绝',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `is_featured` TINYINT NOT NULL DEFAULT 0 COMMENT '是否精选',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿心得表';

-- 10. 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `target_type` VARCHAR(30) NOT NULL COMMENT '目标类型：ACTIVITY,EXPERIENCE,COURSE',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID（0为顶级评论）',
  `reply_to_id` BIGINT DEFAULT NULL COMMENT '回复的评论ID',
  `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复的用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =====================================================
-- 四、通知公告模块
-- =====================================================

-- 11. 公告表
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT COMMENT '内容',
  `type` VARCHAR(30) DEFAULT 'NOTICE' COMMENT '类型：NOTICE-公告, SYSTEM-系统通知, ACTIVITY-活动通知',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '置顶：0-否，1-是',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 插入示例公告
INSERT INTO `notice` (`title`, `content`, `type`, `status`, `is_top`, `publish_time`, `creator_id`) VALUES
('关于2026年寒假志愿服务活动报名的通知', '各位志愿者们好：\n\n为丰富同学们的寒假生活，培养社会责任感，现开放2026年寒假志愿服务活动报名...', 'NOTICE', 1, 1, NOW(), 1),
('志愿者管理系统维护公告', '为保证系统稳定运行，系统将于本周六凌晨2:00至6:00进行维护升级，届时系统将暂停服务。', 'SYSTEM', 1, 0, NOW(), 1),
('新增积分商城功能上线通知', '亲爱的志愿者们：\n\n积分商城功能已正式上线！您可以使用志愿服务积累的积分兑换精美礼品。', 'NOTICE', 1, 0, NOW(), 1);

-- =====================================================
-- 五、收藏与点赞模块
-- =====================================================

-- 12. 收藏表
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` VARCHAR(30) NOT NULL COMMENT '目标类型：ACTIVITY,EXPERIENCE,NOTICE,COURSE',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 12. 点赞表
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` VARCHAR(30) NOT NULL COMMENT '目标类型：ACTIVITY,EXPERIENCE,COMMENT',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- =====================================================
-- 六、积分与商城模块
-- =====================================================

-- 13. 积分记录表
DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `points` INT NOT NULL COMMENT '积分变动（正-增加，负-减少）',
  `balance` INT NOT NULL COMMENT '变动后余额',
  `type` VARCHAR(30) NOT NULL COMMENT '类型：ACTIVITY-活动,SIGNIN-签到,EXCHANGE-兑换,LOTTERY-抽奖,ADMIN-管理员调整',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- 14. 签到记录表
DROP TABLE IF EXISTS `signin_record`;
CREATE TABLE `signin_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '签到ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `signin_date` DATE NOT NULL COMMENT '签到日期',
  `continuous_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `points_earned` INT NOT NULL DEFAULT 0 COMMENT '获得积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_volunteer_date` (`volunteer_id`, `signin_date`),
  KEY `idx_signin_date` (`signin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

-- 15. 积分商城商品表
DROP TABLE IF EXISTS `mall_goods`;
CREATE TABLE `mall_goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '商品分类',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `images` JSON DEFAULT NULL COMMENT '商品图片列表',
  `description` TEXT DEFAULT NULL COMMENT '商品描述',
  `points_price` INT NOT NULL COMMENT '积分价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价（元）',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量（-1为无限）',
  `sold_count` INT NOT NULL DEFAULT 0 COMMENT '已售数量',
  `goods_type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0-实物，1-虚拟，2-优惠券',
  `virtual_content` TEXT DEFAULT NULL COMMENT '虚拟商品内容（卡密等）',
  `limit_per_user` INT NOT NULL DEFAULT 0 COMMENT '每人限购（0-不限）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商城商品表';

-- 16. 兑换订单表
DROP TABLE IF EXISTS `mall_order`;
CREATE TABLE `mall_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `goods_name` VARCHAR(100) NOT NULL COMMENT '商品名称（冗余）',
  `goods_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（冗余）',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '兑换数量',
  `points_cost` INT NOT NULL COMMENT '消耗积分',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待发货，1-已发货，2-已完成，3-已取消',
  `pickup_code` VARCHAR(20) DEFAULT NULL COMMENT '取货码',
  `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
  `receiver_address` VARCHAR(200) DEFAULT NULL COMMENT '收货地址',
  `express_company` VARCHAR(50) DEFAULT NULL COMMENT '快递公司',
  `express_no` VARCHAR(50) DEFAULT NULL COMMENT '快递单号',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分兑换订单表';

-- 17. 用户道具/资产表
DROP TABLE IF EXISTS `user_props`;
CREATE TABLE `user_props` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `prop_type` VARCHAR(50) NOT NULL COMMENT '道具类型：COUPON-优惠券,CARD_KEY-卡密,BADGE-徽章',
  `prop_id` BIGINT DEFAULT NULL COMMENT '关联道具ID',
  `prop_name` VARCHAR(100) NOT NULL COMMENT '道具名称',
  `prop_image` VARCHAR(500) DEFAULT NULL COMMENT '道具图片',
  `prop_content` TEXT DEFAULT NULL COMMENT '道具内容（卡密等）',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `source` VARCHAR(50) DEFAULT NULL COMMENT '来源：EXCHANGE-兑换,LOTTERY-抽奖,ACTIVITY-活动',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_prop_type` (`prop_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户道具资产表';

-- =====================================================
-- 七、抽奖模块
-- =====================================================

-- 18. 抽奖奖品表
DROP TABLE IF EXISTS `lottery_prize`;
CREATE TABLE `lottery_prize` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '奖品名称',
  `image` VARCHAR(500) DEFAULT NULL COMMENT '奖品图片',
  `prize_type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型：0-积分，1-实物，2-虚拟',
  `prize_value` INT NOT NULL DEFAULT 0 COMMENT '奖品价值（积分数/商品ID）',
  `probability` DECIMAL(5,4) NOT NULL DEFAULT 0.0000 COMMENT '中奖概率（0-1）',
  `daily_limit` INT NOT NULL DEFAULT 0 COMMENT '每日限量（0-不限）',
  `total_limit` INT NOT NULL DEFAULT 0 COMMENT '总限量（0-不限）',
  `won_count` INT NOT NULL DEFAULT 0 COMMENT '已中奖次数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（转盘位置）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖奖品表';

-- 19. 抽奖记录表
DROP TABLE IF EXISTS `lottery_record`;
CREATE TABLE `lottery_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `prize_id` BIGINT DEFAULT NULL COMMENT '奖品ID',
  `prize_name` VARCHAR(100) DEFAULT NULL COMMENT '奖品名称',
  `prize_type` TINYINT DEFAULT NULL COMMENT '奖品类型',
  `prize_value` INT DEFAULT NULL COMMENT '奖品价值',
  `points_cost` INT NOT NULL DEFAULT 0 COMMENT '消耗积分',
  `is_won` TINYINT NOT NULL DEFAULT 0 COMMENT '是否中奖',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待领取，1-已领取，2-已过期',
  `claim_time` DATETIME DEFAULT NULL COMMENT '领取时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_prize` (`prize_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖记录表';

-- =====================================================
-- 八、培训课程模块
-- =====================================================

-- 20. 课程表
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '课程分类',
  `instructor` VARCHAR(50) DEFAULT NULL COMMENT '讲师/主讲人',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '课程摘要',
  `content` LONGTEXT DEFAULT NULL COMMENT '课程内容',
  `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频地址',
  `duration` INT NOT NULL DEFAULT 0 COMMENT '时长(分钟)',
  `credit_hours` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '学分/学时',
  `difficulty` TINYINT NOT NULL DEFAULT 1 COMMENT '难度:1-初级,2-中级,3-高级',
  `is_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必修',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '观看次数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-下架,1-上架',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='培训课程表';

-- 21. 课程学习记录表
DROP TABLE IF EXISTS `course_learning`;
CREATE TABLE `course_learning` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '学习进度(0-100)',
  `last_position` INT NOT NULL DEFAULT 0 COMMENT '上次播放位置(秒)',
  `completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_volunteer_course` (`volunteer_id`, `course_id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程学习记录表';

-- =====================================================
-- 九、爱心捐赠模块
-- =====================================================

-- 22. 捐赠项目表
DROP TABLE IF EXISTS `donation_project`;
CREATE TABLE `donation_project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `title` VARCHAR(200) NOT NULL COMMENT '项目标题',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `images` JSON DEFAULT NULL COMMENT '项目图片',
  `description` LONGTEXT DEFAULT NULL COMMENT '项目描述',
  `target_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '目标金额',
  `current_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '已筹金额',
  `donor_count` INT NOT NULL DEFAULT 0 COMMENT '捐赠人数',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-筹备中,1-进行中,2-已结束,3-已取消',
  `organizer_id` BIGINT DEFAULT NULL COMMENT '发起组织ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捐赠项目表';

-- 23. 捐赠记录表
DROP TABLE IF EXISTS `donation_record`;
CREATE TABLE `donation_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `donor_id` BIGINT DEFAULT NULL COMMENT '捐赠者ID(可为空)',
  `donor_name` VARCHAR(50) DEFAULT NULL COMMENT '捐赠者姓名',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '捐赠金额',
  `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名',
  `message` VARCHAR(500) DEFAULT NULL COMMENT '留言',
  `payment_method` VARCHAR(30) DEFAULT NULL COMMENT '支付方式',
  `payment_no` VARCHAR(100) DEFAULT NULL COMMENT '支付流水号',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-未支付,1-已支付,2-已退款',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_donor` (`donor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捐赠记录表';

-- =====================================================
-- 十、反馈与评价模块
-- =====================================================

-- 24. 反馈表
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(30) NOT NULL DEFAULT 'SUGGESTION' COMMENT '类型:SUGGESTION-建议,BUG-报障,COMPLAINT-投诉',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `images` JSON DEFAULT NULL COMMENT '图片列表',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0-待处理,1-处理中,2-已回复,3-已关闭',
  `reply` TEXT DEFAULT NULL COMMENT '回复内容',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `replier_id` BIGINT DEFAULT NULL COMMENT '回复人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 25. 活动评价表
DROP TABLE IF EXISTS `activity_evaluation`;
CREATE TABLE `activity_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
  `content` TEXT DEFAULT NULL COMMENT '评价内容',
  `tags` JSON DEFAULT NULL COMMENT '评价标签',
  `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_volunteer` (`activity_id`, `volunteer_id`),
  KEY `idx_volunteer` (`volunteer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动评价表';

-- 26. 志愿者评价表(组织者对志愿者评价)
DROP TABLE IF EXISTS `volunteer_evaluation`;
CREATE TABLE `volunteer_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `evaluator_id` BIGINT NOT NULL COMMENT '评价者ID',
  `rating` TINYINT NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
  `content` TEXT DEFAULT NULL COMMENT '评价内容',
  `tags` JSON DEFAULT NULL COMMENT '评价标签',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_volunteer` (`volunteer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿者评价表';

-- =====================================================
-- 十一、系统配置模块
-- =====================================================

-- 27. 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `config_type` VARCHAR(30) NOT NULL DEFAULT 'STRING' COMMENT '类型:STRING,NUMBER,BOOLEAN,JSON',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `group_name` VARCHAR(50) DEFAULT NULL COMMENT '分组',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 28. 轮播图表
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮播ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '标题',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片地址',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
  `link_type` VARCHAR(30) DEFAULT NULL COMMENT '链接类型:ACTIVITY,NOTICE,URL',
  `link_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `position` VARCHAR(30) NOT NULL DEFAULT 'HOME' COMMENT '位置:HOME-首页,ACTIVITY-活动页',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始展示时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束展示时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- =====================================================
-- 十二、日志监控模块
-- =====================================================

-- 29. 操作日志表
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '模块',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作',
  `method` VARCHAR(200) DEFAULT NULL COMMENT '方法名',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
  `response_data` TEXT DEFAULT NULL COMMENT '响应数据',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` INT DEFAULT NULL COMMENT '执行时间(ms)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-失败,1-成功',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 30. 登录日志表
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `login_type` VARCHAR(20) DEFAULT NULL COMMENT '登录类型:PASSWORD,WECHAT,SMS',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
  `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
  `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-失败,1-成功',
  `msg` VARCHAR(200) DEFAULT NULL COMMENT '提示信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- =====================================================
-- 十三、志愿团队模块
-- =====================================================

-- 31. 志愿团队表
DROP TABLE IF EXISTS `volunteer_team`;
CREATE TABLE `volunteer_team` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '团队ID',
  `name` VARCHAR(100) NOT NULL COMMENT '团队名称',
  `logo` VARCHAR(500) DEFAULT NULL COMMENT '团队Logo',
  `description` TEXT DEFAULT NULL COMMENT '团队简介',
  `leader_id` BIGINT NOT NULL COMMENT '队长ID',
  `member_count` INT NOT NULL DEFAULT 1 COMMENT '成员数量',
  `total_hours` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '团队总时长',
  `total_activities` INT NOT NULL DEFAULT 0 COMMENT '参与活动数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-解散,1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_leader` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='志愿团队表';

-- 32. 团队成员表
DROP TABLE IF EXISTS `team_member`;
CREATE TABLE `team_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `team_id` BIGINT NOT NULL COMMENT '团队ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色:LEADER-队长,ADMIN-管理员,MEMBER-成员',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-已退出,1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_volunteer` (`team_id`, `volunteer_id`),
  KEY `idx_volunteer` (`volunteer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

-- =====================================================
-- 十四、证书荣誉模块
-- =====================================================

-- 33. 证书模板表
DROP TABLE IF EXISTS `certificate_template`;
CREATE TABLE `certificate_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `type` VARCHAR(30) NOT NULL COMMENT '类型:ACTIVITY-活动,HONOR-荣誉,TRAINING-培训',
  `background_image` VARCHAR(500) DEFAULT NULL COMMENT '背景图片',
  `template_content` LONGTEXT DEFAULT NULL COMMENT '模板内容(HTML)',
  `fields` JSON DEFAULT NULL COMMENT '可替换字段',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书模板表';

-- 34. 证书记录表
DROP TABLE IF EXISTS `certificate`;
CREATE TABLE `certificate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证书ID',
  `certificate_no` VARCHAR(50) NOT NULL COMMENT '证书编号',
  `template_id` BIGINT DEFAULT NULL COMMENT '模板ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `activity_id` BIGINT DEFAULT NULL COMMENT '关联活动ID',
  `title` VARCHAR(200) NOT NULL COMMENT '证书标题',
  `content` TEXT DEFAULT NULL COMMENT '证书内容',
  `issue_date` DATE NOT NULL COMMENT '颁发日期',
  `issuer` VARCHAR(100) DEFAULT NULL COMMENT '颁发机构',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '证书文件URL',
  `qr_code` VARCHAR(500) DEFAULT NULL COMMENT '验证二维码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-已撤销,1-有效',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_certificate_no` (`certificate_no`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书记录表';

-- =====================================================
-- 十五、文件管理模块
-- =====================================================

-- 35. 文件表
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `original_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
  `file_name` VARCHAR(200) NOT NULL COMMENT '存储文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  `storage_type` VARCHAR(20) NOT NULL DEFAULT 'LOCAL' COMMENT '存储方式:LOCAL,MINIO,OSS',
  `uploader_id` BIGINT DEFAULT NULL COMMENT '上传者ID',
  `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  `biz_id` BIGINT DEFAULT NULL COMMENT '业务ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_uploader` (`uploader_id`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件管理表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入超级管理员 (密码: 123456, BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `role`, `status`, `email`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 1, 'admin@volunteer.edu.cn');

-- 插入默认活动分类
INSERT INTO `activity_category` (`name`, `icon`, `sort_order`, `status`) VALUES
('环境保护', 'environmental', 1, 1),
('社区服务', 'community', 2, 1),
('敬老助残', 'elderly-care', 3, 1),
('支教助学', 'education', 4, 1),
('文化艺术', 'culture', 5, 1),
('应急救援', 'emergency', 6, 1),
('体育赛事', 'sports', 7, 1),
('其他', 'other', 99, 1);

-- 插入默认系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`, `group_name`) VALUES
('site_name', '校园志愿者管理系统', 'STRING', '网站名称', 'basic'),
('site_logo', '/logo.png', 'STRING', '网站Logo', 'basic'),
('signin_base_points', '5', 'NUMBER', '签到基础积分', 'points'),
('signin_continuous_bonus', '2', 'NUMBER', '连续签到额外奖励', 'points'),
('lottery_cost_points', '10', 'NUMBER', '每次抽奖消耗积分', 'lottery'),
('activity_cancel_hours', '24', 'NUMBER', '活动取消提前小时数', 'activity');