-- =====================================================
-- 活动数据初始化 - 包含不同状态
-- 报名中、进行中、已结束 三种状态均匀分布
-- =====================================================

USE `volunteer_system`;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE experience;
TRUNCATE TABLE activity_registration;
TRUNCATE TABLE activity;
SET FOREIGN_KEY_CHECKS = 1;

-- 查看组织
SELECT id, org_name FROM organizer ORDER BY id;

-- 为每个组织生成30个活动（10个报名中 + 10个进行中 + 10个已结束）
INSERT INTO activity (title, category_id, organizer_id, cover_image, summary, content, location,
    start_time, end_time, register_start, register_end, deadline,
    max_participants, current_participants, min_participants, service_hours, points_reward,
    requirements, contact_info, status, audit_status, view_count, like_count, collect_count,
    is_featured, create_time, update_time)
SELECT 
    CONCAT(
        ELT((nums.n % 30) + 1,
            '智慧助老培训','社区环境美化','爱心支教','健康知识讲座','法律咨询服务',
            '科普进校园','文化墙绘制','敬老院慰问','图书整理服务','交通安全宣传',
            '急救知识培训','垃圾分类宣传','节能环保宣传','防诈骗普及','心理健康辅导',
            '红色基地参观','消防演练指导','禁毒宣传','献血志愿服务','关爱留守儿童',
            '社区义诊','技能培训班','读书分享会','文艺演出服务','体育赛事服务',
            '植树造林','河流保护','食品安全宣传','网络安全培训','创业指导'),
        '-', LEFT(o.org_name, 6), '-第', LPAD(nums.n, 3, '0'), '期'
    ) as title,
    (nums.n % 5) + 1 as category_id,
    o.user_id as organizer_id,
    CONCAT('https://picsum.photos/seed/', o.id, '_', nums.n, '/800/400') as cover_image,
    CONCAT(o.org_name, '组织的志愿服务活动，欢迎参与！') as summary,
    '<p>活动详情见现场安排。请准时到场，服从安排。</p>' as content,
    ELT((nums.n % 10) + 1, '阳光社区','翠园社区','和谐社区','育才小学','第一中学','市博物馆','养老院','社区中心','市民广场','科技馆') as location,
    
    -- 根据n值分配不同时间状态
    -- n=1-10: 报名中（未来开始）
    -- n=11-20: 进行中（现在进行）
    -- n=21-30: 已结束（过去）
    CASE 
        WHEN nums.n <= 10 THEN DATE_ADD(NOW(), INTERVAL (nums.n + 5) DAY)  -- 5-15天后开始
        WHEN nums.n <= 20 THEN DATE_SUB(NOW(), INTERVAL (nums.n - 10) HOUR)  -- 1-10小时前开始
        ELSE DATE_SUB(NOW(), INTERVAL (nums.n - 10) DAY)  -- 11-20天前结束
    END as start_time,
    
    CASE 
        WHEN nums.n <= 10 THEN DATE_ADD(NOW(), INTERVAL (nums.n + 5) DAY) + INTERVAL 4 HOUR
        WHEN nums.n <= 20 THEN DATE_ADD(NOW(), INTERVAL (24 - nums.n + 10) HOUR)  -- 还有几小时结束
        ELSE DATE_SUB(NOW(), INTERVAL (nums.n - 14) DAY)  -- 已结束
    END as end_time,
    
    -- 报名时间
    CASE 
        WHEN nums.n <= 10 THEN DATE_SUB(NOW(), INTERVAL (5 - nums.n % 5) DAY)  -- 已开始报名
        WHEN nums.n <= 20 THEN DATE_SUB(NOW(), INTERVAL (nums.n) DAY)
        ELSE DATE_SUB(NOW(), INTERVAL (nums.n + 20) DAY)
    END as register_start,
    
    CASE 
        WHEN nums.n <= 10 THEN DATE_ADD(NOW(), INTERVAL (nums.n + 3) DAY)  -- 报名截止在未来
        WHEN nums.n <= 20 THEN DATE_SUB(NOW(), INTERVAL (nums.n - 15) DAY)
        ELSE DATE_SUB(NOW(), INTERVAL (nums.n - 5) DAY)
    END as register_end,
    
    CASE 
        WHEN nums.n <= 10 THEN DATE_ADD(NOW(), INTERVAL (nums.n + 4) DAY)
        WHEN nums.n <= 20 THEN DATE_SUB(NOW(), INTERVAL 1 DAY)
        ELSE DATE_SUB(NOW(), INTERVAL (nums.n - 8) DAY)
    END as deadline,
    
    25 as max_participants, 
    CASE WHEN nums.n <= 10 THEN nums.n ELSE 15 + (nums.n % 10) END as current_participants,
    5 as min_participants, 
    3 as service_hours, 
    30 as points_reward,
    '身体健康，服从安排' as requirements,
    CONCAT(o.contact_person, ' ', o.contact_phone) as contact_info,
    
    -- 活动状态: 1-草稿 2-报名中 3-进行中 4-已结束
    CASE 
        WHEN nums.n <= 10 THEN 2  -- 报名中
        WHEN nums.n <= 20 THEN 3  -- 进行中
        ELSE 4  -- 已结束
    END as status,
    
    1 as audit_status,
    100 + nums.n * 5 as view_count, 
    20 + nums.n as like_count, 
    10 + nums.n as collect_count,
    IF(nums.n % 7 = 0, 1, 0) as is_featured,
    DATE_SUB(NOW(), INTERVAL 30 DAY) as create_time,
    NOW() as update_time
FROM organizer o
CROSS JOIN (
    SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
) nums;

-- 为已结束的活动生成报名记录
INSERT INTO activity_registration (activity_id, volunteer_id, status, sign_in_time, sign_out_time, actual_hours, actual_points, create_time, update_time)
SELECT a.id, v.id, 3, a.start_time, a.end_time, a.service_hours, a.points_reward, a.create_time, NOW()
FROM volunteer v
JOIN activity a ON a.status = 4  -- 只为已结束活动生成
WHERE v.id % 5 = a.id % 5  -- 随机匹配
AND a.id % 3 = 0;

-- 为已结束的活动生成心得
INSERT INTO experience (volunteer_id, activity_id, title, content, status, view_count, like_count, comment_count, collect_count, is_featured, create_time, update_time)
SELECT ar.volunteer_id, ar.activity_id, CONCAT('【', LEFT(a.title, 20), '】心得'),
    '<p>参加活动收获满满！感谢组织者的精心安排！</p>',
    1, 50 + (ar.id % 200), 10 + (ar.id % 40), 2 + (ar.id % 15), 5 + (ar.id % 25),
    IF(ar.id % 10 = 0, 1, 0), DATE_ADD(a.end_time, INTERVAL 1 DAY), NOW()
FROM activity_registration ar 
JOIN activity a ON a.id = ar.activity_id
WHERE ar.status = 3 AND ar.id % 3 = 0;

-- 验证结果
SELECT '活动生成完成！' as 步骤;
SELECT 
    CASE status 
        WHEN 2 THEN '报名中' 
        WHEN 3 THEN '进行中' 
        WHEN 4 THEN '已结束' 
        ELSE '其他' 
    END as 状态, 
    COUNT(*) as 数量 
FROM activity 
GROUP BY status;

SELECT o.org_name, COUNT(a.id) as 活动数量 
FROM organizer o 
LEFT JOIN activity a ON a.organizer_id = o.id 
GROUP BY o.id, o.org_name 
ORDER BY o.id;

SELECT COUNT(*) as 活动总数 FROM activity;
SELECT COUNT(*) as 报名记录数 FROM activity_registration;
SELECT COUNT(*) as 心得数量 FROM experience;
