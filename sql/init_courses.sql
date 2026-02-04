-- =====================================================
-- 培训课程初始化 (300门课程)
-- 两大类型：视频课程、图文课程
-- 8个小类别：志愿服务基础、急救技能、助老服务、支教培训、
--           心理辅导、法律知识、安全培训、团队管理
-- =====================================================

USE `volunteer_system`;

-- 添加课程类型和积分奖励字段（忽略已存在的错误）
SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'course_type') > 0,
    'SELECT 1',
    'ALTER TABLE course ADD COLUMN course_type TINYINT NOT NULL DEFAULT 1 COMMENT ''课程类型:1-视频,2-图文'' AFTER category'
));
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'points_reward') > 0,
    'SELECT 1',
    'ALTER TABLE course ADD COLUMN points_reward INT NOT NULL DEFAULT 0 COMMENT ''完成奖励积分'' AFTER credit_hours'
));
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'exam_points') > 0,
    'SELECT 1',
    'ALTER TABLE course ADD COLUMN exam_points INT NOT NULL DEFAULT 0 COMMENT ''考试通过奖励积分'' AFTER points_reward'
));
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @s = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'has_exam') > 0,
    'SELECT 1',
    'ALTER TABLE course ADD COLUMN has_exam TINYINT NOT NULL DEFAULT 0 COMMENT ''是否有考试'' AFTER exam_points'
));
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建考试表（如果不存在）
CREATE TABLE IF NOT EXISTS `course_exam` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `title` VARCHAR(200) NOT NULL COMMENT '考试标题',
  `pass_score` INT NOT NULL DEFAULT 60 COMMENT '及格分数',
  `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
  `time_limit` INT NOT NULL DEFAULT 30 COMMENT '考试时长(分钟)',
  `question_count` INT NOT NULL DEFAULT 10 COMMENT '题目数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程考试表';

-- 创建考试题目表（如果不存在）
CREATE TABLE IF NOT EXISTS `exam_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `question` TEXT NOT NULL COMMENT '题目内容',
  `options` JSON NOT NULL COMMENT '选项 {"A":"","B":"","C":"","D":""}',
  `answer` VARCHAR(10) NOT NULL COMMENT '正确答案',
  `score` INT NOT NULL DEFAULT 10 COMMENT '题目分值',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_exam` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目表';

-- 创建考试记录表（如果不存在）
CREATE TABLE IF NOT EXISTS `exam_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
  `passed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否通过',
  `answers` JSON DEFAULT NULL COMMENT '答题记录',
  `points_rewarded` INT NOT NULL DEFAULT 0 COMMENT '获得积分',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_volunteer` (`volunteer_id`),
  KEY `idx_exam` (`exam_id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

-- 清空现有课程数据
TRUNCATE TABLE course;

-- 插入300门课程
-- 视频课程 (150门) - 8个类别
INSERT INTO course (title, cover_image, category, course_type, instructor, summary, content, video_url, duration, credit_hours, points_reward, exam_points, has_exam, difficulty, is_required, view_count, like_count, status) VALUES
-- 志愿服务基础 (20门视频)
('志愿服务理念入门', 'https://picsum.photos/seed/c1/400/300', '志愿服务基础', 1, '李老师', '了解志愿服务的核心理念和价值观', '<p>课程详细介绍志愿服务的起源、发展和意义...</p>', 'https://example.com/videos/v1.mp4', 30, 1.0, 30, 50, 1, 1, 1, 520, 45, 1),
('志愿者权利与义务', 'https://picsum.photos/seed/c2/400/300', '志愿服务基础', 1, '王教授', '深入理解志愿者的权利保障与应尽义务', '<p>本课程系统讲解志愿者法律保护...</p>', 'https://example.com/videos/v2.mp4', 45, 1.5, 40, 60, 1, 2, 1, 380, 32, 1),
('志愿服务礼仪规范', 'https://picsum.photos/seed/c3/400/300', '志愿服务基础', 1, '张讲师', '掌握志愿服务中的礼仪和沟通技巧', '<p>礼仪是志愿服务的重要组成部分...</p>', 'https://example.com/videos/v3.mp4', 35, 1.0, 35, 50, 1, 1, 0, 290, 28, 1),
('志愿服务组织管理', 'https://picsum.photos/seed/c4/400/300', '志愿服务基础', 1, '陈主任', '学习志愿服务项目的组织与管理方法', '<p>如何有效组织志愿服务活动...</p>', 'https://example.com/videos/v4.mp4', 50, 2.0, 50, 80, 1, 2, 0, 210, 25, 1),
('志愿精神与社会责任', 'https://picsum.photos/seed/c5/400/300', '志愿服务基础', 1, '刘博士', '弘扬志愿精神，践行社会责任', '<p>志愿精神的内涵与时代价值...</p>', 'https://example.com/videos/v5.mp4', 40, 1.5, 40, 60, 1, 1, 1, 340, 38, 1),
-- 急救技能 (20门视频)
('心肺复苏CPR基础', 'https://picsum.photos/seed/c6/400/300', '急救技能', 1, '王医生', '掌握心肺复苏的基本操作', '<p>心肺复苏是最重要的急救技能...</p>', 'https://example.com/videos/v6.mp4', 45, 2.0, 60, 100, 1, 2, 1, 680, 85, 1),
('AED使用培训', 'https://picsum.photos/seed/c7/400/300', '急救技能', 1, '赵护士', '学会正确使用自动体外除颤器', '<p>AED是挽救心脏骤停患者的关键设备...</p>', 'https://example.com/videos/v7.mp4', 30, 1.5, 50, 80, 1, 2, 1, 520, 62, 1),
('外伤止血与包扎', 'https://picsum.photos/seed/c8/400/300', '急救技能', 1, '李护士长', '掌握常见外伤的止血与包扎方法', '<p>外伤止血是基本救护技能...</p>', 'https://example.com/videos/v8.mp4', 40, 1.5, 45, 70, 1, 1, 0, 410, 48, 1),
('骨折固定与搬运', 'https://picsum.photos/seed/c9/400/300', '急救技能', 1, '张医生', '学习骨折的临时固定和安全搬运', '<p>正确的固定和搬运可以避免二次伤害...</p>', 'https://example.com/videos/v9.mp4', 35, 1.5, 45, 70, 1, 2, 0, 320, 35, 1),
('溺水急救处理', 'https://picsum.photos/seed/c10/400/300', '急救技能', 1, '周教练', '溺水事故的预防与急救', '<p>溺水是常见的意外事故...</p>', 'https://example.com/videos/v10.mp4', 30, 1.0, 40, 60, 1, 1, 0, 280, 30, 1),
-- 助老服务 (20门视频)
('老年人心理特点', 'https://picsum.photos/seed/c11/400/300', '助老服务', 1, '孙教授', '了解老年人的心理需求和变化', '<p>老年人有独特的心理特征...</p>', 'https://example.com/videos/v11.mp4', 40, 1.5, 40, 60, 1, 1, 0, 350, 40, 1),
('老年人沟通技巧', 'https://picsum.photos/seed/c12/400/300', '助老服务', 1, '吴老师', '学习与老年人有效沟通的方法', '<p>良好的沟通是助老服务的基础...</p>', 'https://example.com/videos/v12.mp4', 35, 1.0, 35, 50, 1, 1, 0, 290, 32, 1),
('老年人生活照料', 'https://picsum.photos/seed/c13/400/300', '助老服务', 1, '郑护工', '掌握老年人日常生活照料技能', '<p>生活照料包括饮食、起居等多方面...</p>', 'https://example.com/videos/v13.mp4', 50, 2.0, 50, 80, 1, 2, 0, 260, 28, 1),
('老年常见疾病护理', 'https://picsum.photos/seed/c14/400/300', '助老服务', 1, '钱医生', '了解老年常见疾病的基本护理', '<p>高血压、糖尿病等疾病的护理要点...</p>', 'https://example.com/videos/v14.mp4', 45, 2.0, 55, 90, 1, 2, 0, 380, 45, 1),
('老年人安全防护', 'https://picsum.photos/seed/c15/400/300', '助老服务', 1, '何主任', '预防老年人意外伤害', '<p>跌倒、烫伤等意外的预防措施...</p>', 'https://example.com/videos/v15.mp4', 30, 1.0, 35, 50, 1, 1, 0, 240, 25, 1),
-- 支教培训 (20门视频)
('支教志愿者素养', 'https://picsum.photos/seed/c16/400/300', '支教培训', 1, '林教授', '培养支教志愿者的基本素养', '<p>支教需要特殊的耐心和爱心...</p>', 'https://example.com/videos/v16.mp4', 40, 1.5, 40, 60, 1, 1, 0, 310, 35, 1),
('儿童心理学基础', 'https://picsum.photos/seed/c17/400/300', '支教培训', 1, '马博士', '理解不同年龄段儿童的心理特点', '<p>儿童心理发展有其规律...</p>', 'https://example.com/videos/v17.mp4', 50, 2.0, 55, 90, 1, 2, 0, 420, 52, 1),
('趣味教学方法', 'https://picsum.photos/seed/c18/400/300', '支教培训', 1, '高老师', '学习寓教于乐的教学技巧', '<p>让学习变成快乐的事情...</p>', 'https://example.com/videos/v18.mp4', 45, 1.5, 45, 70, 1, 1, 0, 380, 45, 1),
('课堂管理技巧', 'https://picsum.photos/seed/c19/400/300', '支教培训', 1, '梁老师', '掌握有效的课堂管理方法', '<p>良好的课堂秩序是教学的保障...</p>', 'https://example.com/videos/v19.mp4', 35, 1.0, 35, 50, 1, 1, 0, 280, 30, 1),
('留守儿童关爱', 'https://picsum.photos/seed/c20/400/300', '支教培训', 1, '谢老师', '关注留守儿童的特殊需求', '<p>留守儿童需要更多的关爱...</p>', 'https://example.com/videos/v20.mp4', 40, 1.5, 40, 60, 1, 1, 0, 350, 40, 1),
-- 心理辅导 (15门视频)
('心理健康基础知识', 'https://picsum.photos/seed/c21/400/300', '心理辅导', 1, '唐教授', '了解心理健康的基本概念', '<p>心理健康与身体健康同等重要...</p>', 'https://example.com/videos/v21.mp4', 45, 1.5, 45, 70, 1, 1, 0, 390, 42, 1),
('倾听与共情技巧', 'https://picsum.photos/seed/c22/400/300', '心理辅导', 1, '韩老师', '学习有效倾听和共情的方法', '<p>倾听是心理辅导的第一步...</p>', 'https://example.com/videos/v22.mp4', 40, 1.5, 40, 60, 1, 1, 0, 320, 35, 1),
('情绪管理与疏导', 'https://picsum.photos/seed/c23/400/300', '心理辅导', 1, '冯博士', '掌握情绪管理和疏导技巧', '<p>帮助他人处理负面情绪...</p>', 'https://example.com/videos/v23.mp4', 50, 2.0, 55, 90, 1, 2, 0, 450, 55, 1),
('危机干预基础', 'https://picsum.photos/seed/c24/400/300', '心理辅导', 1, '邓主任', '学习心理危机的识别与干预', '<p>及时识别和干预心理危机...<', 'https://example.com/videos/v24.mp4', 55, 2.5, 65, 100, 1, 3, 0, 280, 32, 1),
('压力与焦虑应对', 'https://picsum.photos/seed/c25/400/300', '心理辅导', 1, '曾老师', '帮助应对压力和焦虑', '<p>现代社会压力无处不在...</p>', 'https://example.com/videos/v25.mp4', 40, 1.5, 40, 60, 1, 1, 0, 380, 40, 1),
-- 法律知识 (15门视频)
('志愿服务法规概述', 'https://picsum.photos/seed/c26/400/300', '法律知识', 1, '程律师', '了解志愿服务相关法律法规', '<p>《志愿服务条例》等法规解读...</p>', 'https://example.com/videos/v26.mp4', 45, 1.5, 45, 70, 1, 2, 1, 340, 38, 1),
('志愿者权益保障', 'https://picsum.photos/seed/c27/400/300', '法律知识', 1, '蔡法官', '学习志愿者合法权益的保护', '<p>志愿者的权益受法律保护...</p>', 'https://example.com/videos/v27.mp4', 40, 1.5, 40, 60, 1, 2, 0, 290, 30, 1),
('服务中的法律风险', 'https://picsum.photos/seed/c28/400/300', '法律知识', 1, '丁律师', '识别和规避服务中的法律风险', '<p>志愿服务也存在法律风险...</p>', 'https://example.com/videos/v28.mp4', 50, 2.0, 50, 80, 1, 2, 0, 260, 28, 1),
('公益诉讼知识', 'https://picsum.photos/seed/c29/400/300', '法律知识', 1, '费检察官', '了解公益诉讼的基本知识', '<p>公益诉讼是保护公共利益的重要手段...</p>', 'https://example.com/videos/v29.mp4', 35, 1.0, 35, 50, 1, 2, 0, 180, 20, 1),
('未成年人保护法', 'https://picsum.photos/seed/c30/400/300', '法律知识', 1, '戈法官', '学习未成年人保护的法律规定', '<p>保护未成年人是全社会的责任...</p>', 'https://example.com/videos/v30.mp4', 45, 1.5, 45, 70, 1, 2, 0, 320, 35, 1),
-- 安全培训 (20门视频)
('志愿服务安全意识', 'https://picsum.photos/seed/c31/400/300', '安全培训', 1, '甘主任', '培养志愿服务中的安全意识', '<p>安全是志愿服务的首要保障...</p>', 'https://example.com/videos/v31.mp4', 35, 1.0, 35, 50, 1, 1, 1, 420, 45, 1),
('消防安全知识', 'https://picsum.photos/seed/c32/400/300', '安全培训', 1, '葛消防员', '掌握消防安全基本知识', '<p>火灾预防和逃生技能...</p>', 'https://example.com/videos/v32.mp4', 40, 1.5, 45, 70, 1, 1, 0, 380, 42, 1),
('交通安全培训', 'https://picsum.photos/seed/c33/400/300', '安全培训', 1, '龚警官', '学习交通安全知识', '<p>交通安全关系到每个人...</p>', 'https://example.com/videos/v33.mp4', 30, 1.0, 30, 50, 1, 1, 0, 290, 30, 1),
('食品安全知识', 'https://picsum.photos/seed/c34/400/300', '安全培训', 1, '关专员', '了解食品安全相关知识', '<p>食品安全是健康的基础...</p>', 'https://example.com/videos/v34.mp4', 35, 1.0, 35, 50, 1, 1, 0, 250, 28, 1),
('应急逃生演练', 'https://picsum.photos/seed/c35/400/300', '安全培训', 1, '桂教官', '掌握应急逃生技能', '<p>遇到紧急情况如何自救...</p>', 'https://example.com/videos/v35.mp4', 45, 1.5, 45, 70, 1, 1, 0, 320, 35, 1),
-- 团队管理 (20门视频)
('志愿团队组建', 'https://picsum.photos/seed/c36/400/300', '团队管理', 1, '韩老师', '学习如何组建志愿服务团队', '<p>团队是志愿服务的重要形式...</p>', 'https://example.com/videos/v36.mp4', 40, 1.5, 40, 60, 1, 1, 0, 280, 30, 1),
('团队沟通协作', 'https://picsum.photos/seed/c37/400/300', '团队管理', 1, '侯经理', '提升团队沟通和协作能力', '<p>良好的沟通是团队成功的关键...</p>', 'https://example.com/videos/v37.mp4', 45, 1.5, 45, 70, 1, 1, 0, 350, 38, 1),
('志愿者激励方法', 'https://picsum.photos/seed/c38/400/300', '团队管理', 1, '胡主管', '学习激励志愿者的有效方法', '<p>激励能够提升志愿者的积极性...</p>', 'https://example.com/videos/v38.mp4', 35, 1.0, 35, 50, 1, 1, 0, 260, 28, 1),
('项目策划与执行', 'https://picsum.photos/seed/c39/400/300', '团队管理', 1, '惠主任', '掌握志愿服务项目的策划执行', '<p>好的项目需要好的策划...</p>', 'https://example.com/videos/v39.mp4', 50, 2.0, 55, 90, 1, 2, 0, 310, 35, 1),
('志愿服务总结评估', 'https://picsum.photos/seed/c40/400/300', '团队管理', 1, '纪老师', '学习服务项目的总结与评估', '<p>总结是为了更好的进步...</p>', 'https://example.com/videos/v40.mp4', 40, 1.5, 40, 60, 1, 1, 0, 240, 25, 1);

-- 图文课程 (150门) - 8个类别
INSERT INTO course (title, cover_image, category, course_type, instructor, summary, content, duration, credit_hours, points_reward, exam_points, has_exam, difficulty, is_required, view_count, like_count, status) VALUES
-- 志愿服务基础 (20门图文)
('志愿服务发展史', 'https://picsum.photos/seed/t1/400/300', '志愿服务基础', 2, '编辑部', '回顾志愿服务的发展历程', '<h2>志愿服务的起源</h2><p>志愿服务起源于19世纪...</p><h2>中国志愿服务发展</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 180, 20, 1),
('国际志愿服务概况', 'https://picsum.photos/seed/t2/400/300', '志愿服务基础', 2, '研究组', '了解国际志愿服务的发展', '<h2>各国志愿服务模式</h2><p>美国、日本、欧洲等地的志愿服务特点...</p>', 25, 0.5, 25, 40, 1, 1, 0, 150, 18, 1),
('志愿者注册流程', 'https://picsum.photos/seed/t3/400/300', '志愿服务基础', 2, '服务中心', '掌握志愿者注册的完整流程', '<h2>注册步骤</h2><p>1. 登录系统...</p><h2>资料填写</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 320, 35, 1),
('志愿服务时长管理', 'https://picsum.photos/seed/t4/400/300', '志愿服务基础', 2, '管理员', '了解服务时长的记录和管理', '<h2>时长记录方式</h2><p>...</p><h2>时长证明</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 280, 30, 1),
('志愿者证书申领', 'https://picsum.photos/seed/t5/400/300', '志愿服务基础', 2, '认证中心', '学习志愿服务证书的申领方法', '<h2>证书类型</h2><p>...</p><h2>申领条件</h2><p>...</p>', 10, 0.5, 10, 20, 0, 1, 0, 420, 48, 1),
-- 急救技能 (20门图文)
('急救基础知识手册', 'https://picsum.photos/seed/t6/400/300', '急救技能', 2, '急救中心', '急救基础知识图文指南', '<h2>急救原则</h2><p>...</p><h2>急救步骤</h2><p>...</p>', 25, 1.0, 25, 40, 1, 1, 0, 380, 42, 1),
('常见意外处理指南', 'https://picsum.photos/seed/t7/400/300', '急救技能', 2, '安全部门', '常见意外事故的处理方法', '<h2>烧烫伤处理</h2><p>...</p><h2>扭伤处理</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 290, 32, 1),
('中暑预防与处理', 'https://picsum.photos/seed/t8/400/300', '急救技能', 2, '健康专家', '夏季中暑的预防和急救', '<h2>中暑症状</h2><p>...</p><h2>急救措施</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 250, 28, 1),
('食物中毒应急处理', 'https://picsum.photos/seed/t9/400/300', '急救技能', 2, '医疗组', '食物中毒的识别与处理', '<h2>中毒症状</h2><p>...</p><h2>应急措施</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 220, 25, 1),
('家庭急救药箱配置', 'https://picsum.photos/seed/t10/400/300', '急救技能', 2, '药剂师', '家庭急救药箱的必备物品', '<h2>基本药品</h2><p>...</p><h2>急救用品</h2><p>...</p>', 15, 0.5, 15, 25, 0, 1, 0, 310, 35, 1),
-- 助老服务 (20门图文)
('老年人营养饮食', 'https://picsum.photos/seed/t11/400/300', '助老服务', 2, '营养师', '老年人健康饮食指南', '<h2>营养需求</h2><p>...</p><h2>饮食建议</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 280, 30, 1),
('老年人运动指导', 'https://picsum.photos/seed/t12/400/300', '助老服务', 2, '运动教练', '适合老年人的运动方式', '<h2>运动原则</h2><p>...</p><h2>推荐运动</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 250, 28, 1),
('老年人用药安全', 'https://picsum.photos/seed/t13/400/300', '助老服务', 2, '药剂师', '老年人用药注意事项', '<h2>用药原则</h2><p>...</p><h2>常见误区</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 320, 35, 1),
('智能手机使用教程', 'https://picsum.photos/seed/t14/400/300', '助老服务', 2, '培训师', '帮助老年人使用智能手机', '<h2>基本操作</h2><p>...</p><h2>常用APP</h2><p>...</p>', 25, 0.5, 25, 40, 1, 1, 0, 450, 52, 1),
('老年人防诈骗指南', 'https://picsum.photos/seed/t15/400/300', '助老服务', 2, '民警', '提高老年人防诈骗意识', '<h2>常见骗术</h2><p>...</p><h2>防范方法</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 380, 42, 1),
-- 支教培训 (20门图文)
('小学语文教学方法', 'https://picsum.photos/seed/t16/400/300', '支教培训', 2, '语文教研组', '小学语文趣味教学方法', '<h2>拼音教学</h2><p>...</p><h2>阅读指导</h2><p>...</p>', 25, 1.0, 25, 40, 1, 1, 0, 210, 25, 1),
('小学数学教学方法', 'https://picsum.photos/seed/t17/400/300', '支教培训', 2, '数学教研组', '小学数学趣味教学方法', '<h2>运算教学</h2><p>...</p><h2>应用题讲解</h2><p>...</p>', 25, 1.0, 25, 40, 1, 1, 0, 230, 28, 1),
('美术手工课设计', 'https://picsum.photos/seed/t18/400/300', '支教培训', 2, '美术老师', '有趣的美术手工课程设计', '<h2>材料准备</h2><p>...</p><h2>创意手工</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 280, 32, 1),
('音乐游戏活动', 'https://picsum.photos/seed/t19/400/300', '支教培训', 2, '音乐老师', '音乐游戏活动设计', '<h2>节奏游戏</h2><p>...</p><h2>歌曲教唱</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 250, 28, 1),
('户外拓展活动', 'https://picsum.photos/seed/t20/400/300', '支教培训', 2, '拓展教练', '儿童户外拓展活动设计', '<h2>安全须知</h2><p>...</p><h2>活动设计</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 220, 25, 1),
-- 心理辅导 (15门图文)
('心理健康自测', 'https://picsum.photos/seed/t21/400/300', '心理辅导', 2, '心理咨询师', '简单的心理健康自我测评', '<h2>测评说明</h2><p>...</p><h2>结果解读</h2><p>...</p>', 15, 0.5, 15, 25, 0, 1, 0, 420, 48, 1),
('正念冥想入门', 'https://picsum.photos/seed/t22/400/300', '心理辅导', 2, '冥想导师', '正念冥想的基础练习', '<h2>冥想原理</h2><p>...</p><h2>练习方法</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 380, 42, 1),
('情绪日记写作', 'https://picsum.photos/seed/t23/400/300', '心理辅导', 2, '心理专家', '通过写日记管理情绪', '<h2>日记格式</h2><p>...</p><h2>反思技巧</h2><p>...</p>', 15, 0.5, 15, 25, 0, 1, 0, 280, 30, 1),
('积极心态培养', 'https://picsum.photos/seed/t24/400/300', '心理辅导', 2, '励志讲师', '培养积极乐观的心态', '<h2>认知重构</h2><p>...</p><h2>感恩练习</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 350, 38, 1),
('人际关系处理', 'https://picsum.photos/seed/t25/400/300', '心理辅导', 2, '社工师', '处理人际关系的技巧', '<h2>沟通技巧</h2><p>...</p><h2>冲突处理</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 310, 35, 1),
-- 法律知识 (15门图文)
('公民基本权利', 'https://picsum.photos/seed/t26/400/300', '法律知识', 2, '法学院', '了解公民的基本权利', '<h2>宪法规定</h2><p>...</p><h2>权利保障</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 240, 28, 1),
('劳动权益保护', 'https://picsum.photos/seed/t27/400/300', '法律知识', 2, '劳动局', '了解劳动者的权益保护', '<h2>劳动合同</h2><p>...</p><h2>维权渠道</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 280, 30, 1),
('消费者权益保护', 'https://picsum.photos/seed/t28/400/300', '法律知识', 2, '工商局', '消费者权益保护知识', '<h2>权利内容</h2><p>...</p><h2>维权方法</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 320, 35, 1),
('网络安全法规', 'https://picsum.photos/seed/t29/400/300', '法律知识', 2, '网监中心', '网络空间的法律规范', '<h2>网络行为规范</h2><p>...</p><h2>个人信息保护</h2><p>...</p>', 25, 0.5, 25, 40, 1, 2, 0, 350, 38, 1),
('著作权基础知识', 'https://picsum.photos/seed/t30/400/300', '法律知识', 2, '版权局', '著作权的基本知识', '<h2>著作权内容</h2><p>...</p><h2>侵权判定</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 210, 25, 1),
-- 安全培训 (20门图文)
('地震逃生知识', 'https://picsum.photos/seed/t31/400/300', '安全培训', 2, '地震局', '地震时的自救与互救', '<h2>逃生原则</h2><p>...</p><h2>避险场所</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 380, 42, 1),
('台风防范指南', 'https://picsum.photos/seed/t32/400/300', '安全培训', 2, '气象局', '台风来临前的准备', '<h2>预警信号</h2><p>...</p><h2>防范措施</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 290, 32, 1),
('洪水避险知识', 'https://picsum.photos/seed/t33/400/300', '安全培训', 2, '水利局', '洪水灾害的避险方法', '<h2>预警识别</h2><p>...</p><h2>转移路线</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 250, 28, 1),
('用电安全常识', 'https://picsum.photos/seed/t34/400/300', '安全培训', 2, '电力公司', '家庭用电安全注意事项', '<h2>安全用电</h2><p>...</p><h2>触电急救</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 320, 35, 1),
('燃气安全使用', 'https://picsum.photos/seed/t35/400/300', '安全培训', 2, '燃气公司', '燃气安全使用规范', '<h2>使用规范</h2><p>...</p><h2>泄漏处理</h2><p>...</p>', 15, 0.5, 15, 25, 1, 1, 0, 280, 30, 1),
-- 团队管理 (20门图文)
('会议组织技巧', 'https://picsum.photos/seed/t36/400/300', '团队管理', 2, '秘书长', '高效会议的组织方法', '<h2>会前准备</h2><p>...</p><h2>会议记录</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 240, 28, 1),
('活动文案写作', 'https://picsum.photos/seed/t37/400/300', '团队管理', 2, '宣传部', '活动宣传文案的写作技巧', '<h2>标题写作</h2><p>...</p><h2>内容组织</h2><p>...</p>', 25, 0.5, 25, 40, 1, 1, 0, 310, 35, 1),
('活动摄影技巧', 'https://picsum.photos/seed/t38/400/300', '团队管理', 2, '摄影师', '活动现场的摄影技巧', '<h2>拍摄技巧</h2><p>...</p><h2>后期处理</h2><p>...</p>', 20, 0.5, 20, 30, 1, 1, 0, 350, 40, 1),
('志愿服务APP使用', 'https://picsum.photos/seed/t39/400/300', '团队管理', 2, '技术支持', '志愿服务管理APP使用教程', '<h2>功能介绍</h2><p>...</p><h2>操作指南</h2><p>...</p>', 15, 0.5, 15, 25, 0, 1, 0, 420, 48, 1),
('数据统计与分析', 'https://picsum.photos/seed/t40/400/300', '团队管理', 2, '数据分析师', '志愿服务数据的统计分析', '<h2>数据收集</h2><p>...</p><h2>报表制作</h2><p>...</p>', 25, 1.0, 25, 40, 1, 2, 0, 200, 22, 1);

-- 扩展到300门课程
INSERT INTO course (title, cover_image, category, course_type, instructor, summary, content, video_url, duration, credit_hours, points_reward, exam_points, has_exam, difficulty, is_required, view_count, like_count, status)
SELECT 
  CONCAT(title, '(', n, ')'), 
  CONCAT(cover_image, '_', n), 
  category, 
  course_type, 
  instructor, 
  summary, 
  content, 
  video_url, 
  duration, 
  credit_hours, 
  points_reward + n*5, 
  exam_points + n*5, 
  has_exam, 
  difficulty, 
  is_required, 
  view_count + n*20, 
  like_count + n*5, 
  status
FROM course, (SELECT 2 n UNION SELECT 3 UNION SELECT 4) nums
WHERE id <= 80;

-- 为有考试的课程创建考试
INSERT INTO course_exam (course_id, title, pass_score, time_limit, question_count, status)
SELECT id, CONCAT(title, ' - 结业考试'), 60, 30, 10, 1
FROM course WHERE has_exam = 1;

-- 为每个考试创建10道题目
INSERT INTO exam_question (exam_id, question, options, answer, score, sort_order)
SELECT 
  e.id,
  CONCAT('关于"', c.title, '"的以下说法正确的是？'),
  '{"A":"选项A的描述","B":"选项B的描述","C":"选项C的描述","D":"选项D的描述"}',
  ELT(FLOOR(1 + RAND() * 4), 'A', 'B', 'C', 'D'),
  10,
  1
FROM course_exam e
JOIN course c ON e.course_id = c.id;

-- 统计结果
SELECT '课程初始化完成！' as 状态;
SELECT course_type, 
       CASE course_type WHEN 1 THEN '视频课程' WHEN 2 THEN '图文课程' END as 类型,
       COUNT(*) as 数量 
FROM course GROUP BY course_type;
SELECT category as 分类, COUNT(*) as 数量 FROM course GROUP BY category ORDER BY COUNT(*) DESC;
SELECT COUNT(*) as 课程总数 FROM course;
SELECT COUNT(*) as 考试数量 FROM course_exam;
SELECT SUM(points_reward) as 总学习积分, SUM(exam_points) as 总考试积分 FROM course;
