-- 修复 notice 表问题
-- 步骤1：先添加缺失的列，步骤2：再插入数据

USE `volunteer_system`;

-- ============================================
-- 步骤1：添加缺失的列（如果报错说列已存在，忽略即可）
-- ============================================

-- 添加 type 列
ALTER TABLE `notice` ADD COLUMN `type` VARCHAR(30) DEFAULT 'NOTICE' COMMENT '类型' AFTER `content`;

-- 添加 status 列
ALTER TABLE `notice` ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布' AFTER `type`;

-- 添加 is_top 列
ALTER TABLE `notice` ADD COLUMN `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '置顶' AFTER `status`;

-- 添加 publish_time 列
ALTER TABLE `notice` ADD COLUMN `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间' AFTER `is_top`;

-- 添加 view_count 列
ALTER TABLE `notice` ADD COLUMN `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数' AFTER `publish_time`;

-- 添加 creator_id 列
ALTER TABLE `notice` ADD COLUMN `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `view_count`;

-- 添加 is_deleted 列
ALTER TABLE `notice` ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除' AFTER `update_time`;

-- ============================================
-- 步骤2：插入公告数据（不使用 creator_id，让其为 NULL）
-- ============================================

-- 清空旧数据
DELETE FROM `notice`;

-- 插入新数据
INSERT INTO `notice` (`title`, `content`, `type`, `status`, `is_top`, `publish_time`, `view_count`, `create_time`, `update_time`, `is_deleted`) VALUES 
(
    '【系统升级】志愿者服务平台 V2.0 正式上线公告', 
    '<p>各位同学：</p><p>经过技术团队的努力，校园志愿者系统 V2.0 版本于今日正式上线！</p><p><b>本次更新重点：</b><br>1. 新增【积分商城】功能，志愿积分可兑换实物好礼；<br>2. 优化【心得广场】发布体验，支持多图上传；<br>3. 修复了部分安卓机型签到定位不准的问题。</p><p>欢迎大家体验！</p>', 
    'SYSTEM', 1, 1, NOW(), 156, NOW(), NOW(), 0
),
(
    '关于本周五凌晨系统停机维护的通知', 
    '<p>为提升服务器稳定性，我们将于 <b>本周五凌晨 02:00 - 05:00</b> 进行停机维护。</p><p>维护期间将无法登录系统、无法进行签到/签退操作。请各位志愿者提前安排好服务时间。</p>', 
    'SYSTEM', 1, 1, NOW(), 89, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0
),
(
    '【新功能】"每日签到"赚积分功能上线啦！', 
    '<p>动动手指，积分到手！</p><p>即日起，每日登录系统并在首页点击"签到"，即可获得 <b>5 积分</b> 奖励。连续签到 7 天更可获得额外奖励包！</p>', 
    'NOTICE', 1, 0, NOW(), 234, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 0
),
(
    '🔥【重磅】2025年度"校园十大杰出志愿者"评选启动', 
    '<p>寻找身边的榜样！</p><p>年度评选活动正式拉开帷幕。凡服务时长超过 50 小时、无违规记录的注册志愿者均可报名。</p><p><b>报名截止时间：</b>本月底。<br><b>奖励：</b>荣誉证书 + 定制奖杯 + 1000 积分。</p>', 
    'ACTIVITY', 1, 1, NOW(), 567, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 0
),
(
    '急募！城市马拉松赛会志愿者（需50人）', 
    '<p>本市马拉松将于下周日开跑，现急需补充一批赛道引导志愿者。</p><p><b>要求：</b><br>1. 身体健康，体能充沛；<br>2. 能服从早起安排（凌晨4点集合）。</p><p><b>福利：</b>提供赛事官方服装、证书及早餐。</p>', 
    'ACTIVITY', 1, 0, NOW(), 312, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW(), 0
),
(
    '暑期"三下乡"社会实践志愿服务队招募公告', 
    '<p>这个夏天，一起去偏远山区支教吧！</p><p>我们将组建 5 支队伍前往云南、贵州等地开展为期 15 天的支教助学活动。名额有限，欲报从速。</p>', 
    'ACTIVITY', 1, 0, NOW(), 198, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), 0
),
(
    '关于调整志愿服务时长认定标准的通知', 
    '<p>根据上级团委要求，自下月起，志愿服务时长认定规则调整如下：</p><p>1. 每日服务时长上限为 8 小时；<br>2. 往返路程时间不再计入服务时长；<br>3. 必须通过系统定位签到签退，补签需提交证明。</p>', 
    'NOTICE', 1, 0, NOW(), 445, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW(), 0
),
(
    '喜报！我校荣获"省志愿服务先进集体"称号', 
    '<p>在大家的共同努力下，我校青年志愿者协会荣获省级表彰！这是属于每一位"红马甲"的荣誉！感谢你们的辛勤付出！</p>', 
    'NOTICE', 1, 0, NOW(), 678, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), 0
);

SELECT CONCAT('成功插入公告数据，共 ', (SELECT COUNT(*) FROM notice), ' 条') as result;
