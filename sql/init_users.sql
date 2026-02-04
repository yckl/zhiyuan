-- =====================================================
-- 用户数据初始化脚本
-- 包含：志愿者、组织者、管理员
-- 密码统一为: 123456 (BCrypt加密)
-- =====================================================

USE `volunteer_system`;

-- BCrypt加密后的123456密码
SET @password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

-- =====================================================
-- 1. 清空旧数据
-- =====================================================
DELETE FROM volunteer WHERE 1=1;
DELETE FROM organizer WHERE 1=1;
DELETE FROM sys_user WHERE 1=1;

-- =====================================================
-- 2. 管理员账户 (1个)
-- =====================================================
INSERT INTO sys_user (username, password, role, status, avatar, create_time, update_time) VALUES
('admin', @password, 'ADMIN', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=admin', NOW(), NOW());

-- =====================================================
-- 3. 组织者账户 (10个组织，每个4人 = 40人)
-- 工号格式：T2024001 ~ T2024040
-- =====================================================

-- 3.1 先创建组织者的系统用户
INSERT INTO sys_user (username, password, role, status, avatar, create_time, update_time) VALUES
-- 计算机学院团委 (4人)
('T2024001', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org1', NOW(), NOW()),
('T2024002', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org2', NOW(), NOW()),
('T2024003', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org3', NOW(), NOW()),
('T2024004', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org4', NOW(), NOW()),
-- 电子工程学院志愿服务中心 (4人)
('T2024005', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org5', NOW(), NOW()),
('T2024006', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org6', NOW(), NOW()),
('T2024007', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org7', NOW(), NOW()),
('T2024008', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org8', NOW(), NOW()),
-- 机械工程学院青年志愿者协会 (4人)
('T2024009', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org9', NOW(), NOW()),
('T2024010', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org10', NOW(), NOW()),
('T2024011', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org11', NOW(), NOW()),
('T2024012', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org12', NOW(), NOW()),
-- 经济管理学院学生会 (4人)
('T2024013', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org13', NOW(), NOW()),
('T2024014', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org14', NOW(), NOW()),
('T2024015', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org15', NOW(), NOW()),
('T2024016', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org16', NOW(), NOW()),
-- 外国语学院青协 (4人)
('T2024017', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org17', NOW(), NOW()),
('T2024018', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org18', NOW(), NOW()),
('T2024019', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org19', NOW(), NOW()),
('T2024020', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org20', NOW(), NOW()),
-- 法学院志愿服务队 (4人)
('T2024021', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org21', NOW(), NOW()),
('T2024022', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org22', NOW(), NOW()),
('T2024023', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org23', NOW(), NOW()),
('T2024024', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org24', NOW(), NOW()),
-- 艺术学院文化志愿团 (4人)
('T2024025', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org25', NOW(), NOW()),
('T2024026', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org26', NOW(), NOW()),
('T2024027', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org27', NOW(), NOW()),
('T2024028', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org28', NOW(), NOW()),
-- 医学院健康志愿服务队 (4人)
('T2024029', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org29', NOW(), NOW()),
('T2024030', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org30', NOW(), NOW()),
('T2024031', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org31', NOW(), NOW()),
('T2024032', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org32', NOW(), NOW()),
-- 理学院科普志愿团 (4人)
('T2024033', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org33', NOW(), NOW()),
('T2024034', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org34', NOW(), NOW()),
('T2024035', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org35', NOW(), NOW()),
('T2024036', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org36', NOW(), NOW()),
-- 建筑学院社区服务队 (4人)
('T2024037', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org37', NOW(), NOW()),
('T2024038', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org38', NOW(), NOW()),
('T2024039', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org39', NOW(), NOW()),
('T2024040', @password, 'ORGANIZER', 1, 'https://api.dicebear.com/7.x/bottts/svg?seed=org40', NOW(), NOW());

-- 3.2 创建10个组织
INSERT INTO organizer (user_id, org_name, org_type, contact_person, contact_phone, contact_email, description, verified, create_time, update_time) VALUES
((SELECT id FROM sys_user WHERE username='T2024001'), '计算机学院团委', '学生组织', '张明老师', '13800000001', 'cs_tw@university.edu', '计算机学院团委志愿服务部，负责组织各类IT志愿服务活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024005'), '电子工程学院志愿服务中心', '学生组织', '李华老师', '13800000002', 'ee_volunteer@university.edu', '电子工程学院志愿服务中心，专注科技普及和电子维修志愿服务', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024009'), '机械工程学院青年志愿者协会', '学生组织', '王强老师', '13800000003', 'me_youth@university.edu', '机械学院青协，开展社区维修、支教等志愿活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024013'), '经济管理学院学生会', '学生组织', '赵丽老师', '13800000004', 'em_student@university.edu', '经管院学生会志愿服务部，组织公益募捐和社区帮扶活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024017'), '外国语学院青协', '学生组织', '陈伟老师', '13800000005', 'fl_youth@university.edu', '外院青协，提供翻译志愿服务和语言文化传播', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024021'), '法学院志愿服务队', '学生组织', '刘芳老师', '13800000006', 'law_volunteer@university.edu', '法学院志愿者团队，开展法律援助和普法宣传活动', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024025'), '艺术学院文化志愿团', '学生组织', '杨静老师', '13800000007', 'art_culture@university.edu', '艺术学院文化志愿团，传播艺术文化，服务社区文化建设', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024029'), '医学院健康志愿服务队', '学生组织', '周健老师', '13800000008', 'med_health@university.edu', '医学院健康志愿队，提供健康咨询和急救知识培训', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024033'), '理学院科普志愿团', '学生组织', '吴明老师', '13800000009', 'sci_popular@university.edu', '理学院科普志愿团，开展科普讲座和青少年科技教育', 1, NOW(), NOW()),
((SELECT id FROM sys_user WHERE username='T2024037'), '建筑学院社区服务队', '学生组织', '郑红老师', '13800000010', 'arch_community@university.edu', '建筑学院社区服务队，参与社区规划和环境改善志愿服务', 1, NOW(), NOW());

-- =====================================================
-- 4. 志愿者账户 (10个学院，每学院10人 = 100人)
-- 学号格式：2024001001 ~ 2024001100
-- =====================================================

-- 4.1 创建志愿者的系统用户
INSERT INTO sys_user (username, password, role, status, avatar, create_time, update_time)
SELECT 
    CONCAT('2024001', LPAD(n, 3, '0')) as username,
    @password,
    'VOLUNTEER',
    1,
    CONCAT('https://api.dicebear.com/7.x/notionists/svg?seed=stu_', n) as avatar,
    NOW(),
    NOW()
FROM (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
    UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
    UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
    UNION SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION SELECT 45
    UNION SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION SELECT 50
    UNION SELECT 51 UNION SELECT 52 UNION SELECT 53 UNION SELECT 54 UNION SELECT 55
    UNION SELECT 56 UNION SELECT 57 UNION SELECT 58 UNION SELECT 59 UNION SELECT 60
    UNION SELECT 61 UNION SELECT 62 UNION SELECT 63 UNION SELECT 64 UNION SELECT 65
    UNION SELECT 66 UNION SELECT 67 UNION SELECT 68 UNION SELECT 69 UNION SELECT 70
    UNION SELECT 71 UNION SELECT 72 UNION SELECT 73 UNION SELECT 74 UNION SELECT 75
    UNION SELECT 76 UNION SELECT 77 UNION SELECT 78 UNION SELECT 79 UNION SELECT 80
    UNION SELECT 81 UNION SELECT 82 UNION SELECT 83 UNION SELECT 84 UNION SELECT 85
    UNION SELECT 86 UNION SELECT 87 UNION SELECT 88 UNION SELECT 89 UNION SELECT 90
    UNION SELECT 91 UNION SELECT 92 UNION SELECT 93 UNION SELECT 94 UNION SELECT 95
    UNION SELECT 96 UNION SELECT 97 UNION SELECT 98 UNION SELECT 99 UNION SELECT 100
) numbers;

-- 4.2 创建志愿者详情记录
-- 学院分配：1-10计算机，11-20电子，21-30机械，31-40经管，41-50外语，51-60法学，61-70艺术，71-80医学，81-90理学，91-100建筑
INSERT INTO volunteer (user_id, name, gender, student_no, college, major, class_name, grade, total_hours, total_points, available_points, level, create_time, update_time)
SELECT 
    u.id,
    CASE 
        WHEN n % 2 = 0 THEN ELT(FLOOR(RAND()*10)+1, '王伟', '李强', '张明', '刘洋', '陈杰', '杨帆', '赵鹏', '黄磊', '周宇', '吴涛')
        ELSE ELT(FLOOR(RAND()*10)+1, '王芳', '李娜', '张丽', '刘婷', '陈静', '杨雪', '赵敏', '黄欣', '周霞', '吴倩')
    END as name,
    n % 2 as gender,
    CONCAT('2024001', LPAD(n, 3, '0')) as student_no,
    CASE 
        WHEN n <= 10 THEN '计算机学院'
        WHEN n <= 20 THEN '电子工程学院'
        WHEN n <= 30 THEN '机械工程学院'
        WHEN n <= 40 THEN '经济管理学院'
        WHEN n <= 50 THEN '外国语学院'
        WHEN n <= 60 THEN '法学院'
        WHEN n <= 70 THEN '艺术学院'
        WHEN n <= 80 THEN '医学院'
        WHEN n <= 90 THEN '理学院'
        ELSE '建筑学院'
    END as college,
    CASE 
        WHEN n <= 10 THEN ELT((n-1)%3+1, '软件工程', '计算机科学', '人工智能')
        WHEN n <= 20 THEN ELT((n-1)%3+1, '电子信息', '通信工程', '微电子')
        WHEN n <= 30 THEN ELT((n-1)%3+1, '机械设计', '自动化', '车辆工程')
        WHEN n <= 40 THEN ELT((n-1)%3+1, '工商管理', '会计学', '金融学')
        WHEN n <= 50 THEN ELT((n-1)%3+1, '英语', '日语', '翻译')
        WHEN n <= 60 THEN ELT((n-1)%3+1, '法学', '知识产权', '国际法')
        WHEN n <= 70 THEN ELT((n-1)%3+1, '美术学', '设计学', '音乐学')
        WHEN n <= 80 THEN ELT((n-1)%3+1, '临床医学', '护理学', '药学')
        WHEN n <= 90 THEN ELT((n-1)%3+1, '数学', '物理学', '化学')
        ELSE ELT((n-1)%3+1, '建筑学', '城乡规划', '风景园林')
    END as major,
    CONCAT('24', LPAD(((n-1)%10)+1, 2, '0'), '班') as class_name,
    '2024' as grade,
    FLOOR(RAND()*100) as total_hours,
    FLOOR(RAND()*500) as total_points,
    FLOOR(RAND()*300) as available_points,
    FLOOR(RAND()*5)+1 as level,
    NOW(),
    NOW()
FROM (
    SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
    UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
    UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
    UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
    UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
    UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
    UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
    UNION SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION SELECT 45
    UNION SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION SELECT 50
    UNION SELECT 51 UNION SELECT 52 UNION SELECT 53 UNION SELECT 54 UNION SELECT 55
    UNION SELECT 56 UNION SELECT 57 UNION SELECT 58 UNION SELECT 59 UNION SELECT 60
    UNION SELECT 61 UNION SELECT 62 UNION SELECT 63 UNION SELECT 64 UNION SELECT 65
    UNION SELECT 66 UNION SELECT 67 UNION SELECT 68 UNION SELECT 69 UNION SELECT 70
    UNION SELECT 71 UNION SELECT 72 UNION SELECT 73 UNION SELECT 74 UNION SELECT 75
    UNION SELECT 76 UNION SELECT 77 UNION SELECT 78 UNION SELECT 79 UNION SELECT 80
    UNION SELECT 81 UNION SELECT 82 UNION SELECT 83 UNION SELECT 84 UNION SELECT 85
    UNION SELECT 86 UNION SELECT 87 UNION SELECT 88 UNION SELECT 89 UNION SELECT 90
    UNION SELECT 91 UNION SELECT 92 UNION SELECT 93 UNION SELECT 94 UNION SELECT 95
    UNION SELECT 96 UNION SELECT 97 UNION SELECT 98 UNION SELECT 99 UNION SELECT 100
) numbers
JOIN sys_user u ON u.username = CONCAT('2024001', LPAD(n, 3, '0'));

-- =====================================================
-- 5. 显示统计结果
-- =====================================================
SELECT '用户数据初始化完成！' as message;
SELECT 
    (SELECT COUNT(*) FROM sys_user WHERE role = 'ADMIN') as 管理员数量,
    (SELECT COUNT(*) FROM sys_user WHERE role = 'ORGANIZER') as 组织者用户数量,
    (SELECT COUNT(*) FROM organizer) as 组织数量,
    (SELECT COUNT(*) FROM sys_user WHERE role = 'VOLUNTEER') as 志愿者数量;

SELECT '
===========================================
账号信息汇总：
===========================================
管理员：admin / 123456

组织者（每组4人，前2个是老师，后2个是学生干部）：
- 计算机学院团委：T2024001~T2024004
- 电子工程学院志愿服务中心：T2024005~T2024008
- 机械工程学院青年志愿者协会：T2024009~T2024012
- 经济管理学院学生会：T2024013~T2024016
- 外国语学院青协：T2024017~T2024020
- 法学院志愿服务队：T2024021~T2024024
- 艺术学院文化志愿团：T2024025~T2024028
- 医学院健康志愿服务队：T2024029~T2024032
- 理学院科普志愿团：T2024033~T2024036
- 建筑学院社区服务队：T2024037~T2024040

志愿者（每学院10人）：
- 计算机学院：2024001001~2024001010
- 电子工程学院：2024001011~2024001020
- 机械工程学院：2024001021~2024001030
- 经济管理学院：2024001031~2024001040
- 外国语学院：2024001041~2024001050
- 法学院：2024001051~2024001060
- 艺术学院：2024001061~2024001070
- 医学院：2024001071~2024001080
- 理学院：2024001081~2024001090
- 建筑学院：2024001091~2024001100

所有密码均为：123456
===========================================
' as 账号信息;
