-- =====================================================
-- 大规模用户数据初始化脚本
-- 10个学院，每学院30名志愿者 = 300名志愿者
-- 10个组织，每组织4人 = 40名组织者
-- 密码统一为: 123456
-- =====================================================

USE `volunteer_system`;

-- BCrypt加密后的123456密码
SET @password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

-- =====================================================
-- 1. 清空旧数据
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE volunteer;
TRUNCATE TABLE organizer;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 2. 管理员账户
-- =====================================================
INSERT INTO sys_user (username, password, role, status, avatar, create_time, update_time) VALUES
('admin', @password, 'ADMIN', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=admin', NOW(), NOW());

-- =====================================================
-- 3. 组织者账户 (10个组织，每个4人 = 40人)
-- =====================================================

-- 创建临时数字表
DROP TEMPORARY TABLE IF EXISTS temp_numbers;
CREATE TEMPORARY TABLE temp_numbers (n INT);
INSERT INTO temp_numbers VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),
(11),(12),(13),(14),(15),(16),(17),(18),(19),(20),
(21),(22),(23),(24),(25),(26),(27),(28),(29),(30),
(31),(32),(33),(34),(35),(36),(37),(38),(39),(40);

-- 插入40个组织者用户
INSERT INTO sys_user (username, password, role, status, avatar, create_time, update_time)
SELECT 
    CONCAT('T2024', LPAD(n, 3, '0')),
    @password,
    'ORGANIZER',
    1,
    CONCAT('https://api.dicebear.com/7.x/bottts/svg?seed=org', n),
    NOW(),
    NOW()
FROM temp_numbers WHERE n <= 40;

-- 创建10个组织（每组织的第一个人作为负责人）
INSERT INTO organizer (user_id, org_name, org_type, contact_person, contact_phone, contact_email, description, verified, create_time, update_time) VALUES
((SELECT id FROM sys_user WHERE username='T2024001'), '计算机学院青年志愿者协会', '学生组织', '张明辉', '13800001001', 'cs_youth@university.edu', '计算机学院青协，开展IT支教、智慧助老、科技普及等志愿服务', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024005'), '电子工程学院志愿服务中心', '学生组织', '李华强', '13800001002', 'ee_volunteer@university.edu', '电子工程学院志愿中心，专注电子维修、科技创新体验等服务', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024009'), '机械工程学院青年志愿者协会', '学生组织', '王建国', '13800001003', 'me_youth@university.edu', '机械学院青协，开展社区维修、支教帮扶等活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024013'), '经济管理学院学生志愿服务队', '学生组织', '赵丽华', '13800001004', 'em_volunteer@university.edu', '经管院志愿队，组织公益直播、财务知识普及等活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024017'), '外国语学院青年志愿者协会', '学生组织', '陈伟东', '13800001005', 'fl_youth@university.edu', '外院青协，提供翻译志愿服务和跨文化交流活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024021'), '法学院志愿服务队', '学生组织', '刘芳菲', '13800001006', 'law_volunteer@university.edu', '法学院志愿队，开展法律援助和普法宣传活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024025'), '艺术学院文化志愿团', '学生组织', '杨静雅', '13800001007', 'art_culture@university.edu', '艺术学院文化团，传播艺术文化，美化社区环境', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024029'), '医学院健康志愿服务队', '学生组织', '周健康', '13800001008', 'med_health@university.edu', '医学院健康队，提供健康义诊和急救培训服务', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024033'), '理学院科普志愿团', '学生组织', '吴明理', '13800001009', 'sci_popular@university.edu', '理学院科普团，开展科普讲座和青少年科技教育', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024037'), '建筑学院社区服务队', '学生组织', '郑红建', '13800001010', 'arch_community@university.edu', '建筑学院社区队，参与社区规划和环境改善服务', 1, NOW(), NOW());

-- =====================================================
-- 4. 志愿者账户 (10个学院，每学院30人 = 300人)
-- =====================================================

-- 扩展数字表到300
DROP TEMPORARY TABLE IF EXISTS temp_numbers;
CREATE TEMPORARY TABLE temp_numbers (n INT);
INSERT INTO temp_numbers SELECT a.n + (b.n - 1) * 10 + (c.n - 1) * 100
FROM (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) a,
     (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) b,
     (SELECT 1 n UNION SELECT 2 UNION SELECT 3) c
WHERE a.n + (b.n - 1) * 10 + (c.n - 1) * 100 <= 300;

-- 插入300个志愿者用户
INSERT INTO sys_user (username, password, role, status, avatar, create_time, update_time)
SELECT 
    CONCAT('2024', LPAD(n, 6, '0')),
    @password,
    'VOLUNTEER',
    1,
    CONCAT('https://api.dicebear.com/7.x/notionists/svg?seed=stu_', n),
    NOW(),
    NOW()
FROM temp_numbers;

-- 姓氏和名字列表
SET @surnames = '王,李,张,刘,陈,杨,赵,黄,周,吴,徐,孙,胡,朱,高,林,何,郭,马,罗,梁,宋,郑,谢,韩,唐,冯,于,董,萧';
SET @male_names = '伟,强,磊,涛,鹏,军,建,国,勇,杰,斌,辉,超,波,飞,浩,亮,宇,明,峰,雷,刚,志,龙,威';
SET @female_names = '芳,娟,敏,静,丽,艳,娜,霞,燕,玲,婷,萍,慧,莉,琳,颖,雪,倩,欣,蕾,洁,璇,悦,薇,梦';

-- 插入志愿者详情
INSERT INTO volunteer (user_id, name, gender, student_no, college, major, class_name, grade, total_hours, total_points, available_points, level, create_time, update_time)
SELECT 
    u.id,
    CONCAT(
        SUBSTRING_INDEX(SUBSTRING_INDEX(@surnames, ',', FLOOR(RAND()*30)+1), ',', -1),
        IF(n % 2 = 0, 
           SUBSTRING_INDEX(SUBSTRING_INDEX(@male_names, ',', FLOOR(RAND()*25)+1), ',', -1),
           SUBSTRING_INDEX(SUBSTRING_INDEX(@female_names, ',', FLOOR(RAND()*25)+1), ',', -1))
    ),
    n % 2,
    CONCAT('2024', LPAD(n, 6, '0')),
    CASE 
        WHEN n <= 30 THEN '计算机学院'
        WHEN n <= 60 THEN '电子工程学院'
        WHEN n <= 90 THEN '机械工程学院'
        WHEN n <= 120 THEN '经济管理学院'
        WHEN n <= 150 THEN '外国语学院'
        WHEN n <= 180 THEN '法学院'
        WHEN n <= 210 THEN '艺术学院'
        WHEN n <= 240 THEN '医学院'
        WHEN n <= 270 THEN '理学院'
        ELSE '建筑学院'
    END,
    CASE 
        WHEN n <= 30 THEN ELT((n-1)%5+1, '软件工程', '计算机科学', '人工智能', '数据科学', '网络工程')
        WHEN n <= 60 THEN ELT((n-31)%5+1, '电子信息', '通信工程', '微电子', '自动化', '光电信息')
        WHEN n <= 90 THEN ELT((n-61)%5+1, '机械设计', '机械电子', '车辆工程', '工业设计', '智能制造')
        WHEN n <= 120 THEN ELT((n-91)%5+1, '工商管理', '会计学', '金融学', '市场营销', '人力资源')
        WHEN n <= 150 THEN ELT((n-121)%5+1, '英语', '日语', '翻译', '德语', '法语')
        WHEN n <= 180 THEN ELT((n-151)%5+1, '法学', '知识产权', '国际法', '民商法', '刑法学')
        WHEN n <= 210 THEN ELT((n-181)%5+1, '美术学', '视觉传达', '音乐学', '舞蹈学', '艺术设计')
        WHEN n <= 240 THEN ELT((n-211)%5+1, '临床医学', '护理学', '药学', '口腔医学', '中医学')
        WHEN n <= 270 THEN ELT((n-241)%5+1, '数学', '物理学', '化学', '生物学', '统计学')
        ELSE ELT((n-271)%5+1, '建筑学', '城乡规划', '风景园林', '土木工程', '环境设计')
    END,
    CONCAT('24', LPAD(((n-1)%6)+1, 2, '0'), '班'),
    '2024',
    FLOOR(RAND()*200)+10,
    FLOOR(RAND()*1000)+100,
    FLOOR(RAND()*500)+50,
    FLOOR(RAND()*5)+1,
    NOW(),
    NOW()
FROM temp_numbers t
JOIN sys_user u ON u.username = CONCAT('2024', LPAD(t.n, 6, '0'));

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_numbers;

-- =====================================================
-- 5. 显示统计结果
-- =====================================================
SELECT '用户数据初始化完成！' as message;
SELECT 
    (SELECT COUNT(*) FROM sys_user WHERE role = 'ADMIN') as 管理员数量,
    (SELECT COUNT(*) FROM sys_user WHERE role = 'ORGANIZER') as 组织者数量,
    (SELECT COUNT(*) FROM organizer) as 组织数量,
    (SELECT COUNT(*) FROM sys_user WHERE role = 'VOLUNTEER') as 志愿者数量;
