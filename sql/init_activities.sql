-- =====================================================
-- 活动参与记录和心得数据初始化脚本
-- 包含：活动、报名记录、心得分享
-- =====================================================

USE `volunteer_system`;

-- =====================================================
-- 1. 创建活动数据 (每个组织2-3个活动，共25个活动)
-- =====================================================

DELETE FROM activity WHERE 1=1;

INSERT INTO activity (title, category_id, organizer_id, cover_image, summary, content, location, start_time, end_time, register_start, register_end, deadline, max_participants, current_participants, min_participants, service_hours, points_reward, requirements, contact_info, status, audit_status, view_count, like_count, collect_count, is_featured, create_time, update_time) VALUES
-- 计算机学院活动
('智慧助老：手机使用培训志愿服务', 1, 1, 'https://picsum.photos/seed/act1/800/400', 
'教老年人使用智能手机，帮助他们融入数字时代', 
'<p>本次活动将前往社区老年活动中心，教授老年人使用智能手机的基本操作，包括：</p><ul><li>微信视频通话</li><li>健康码使用</li><li>网上预约挂号</li><li>防诈骗知识</li></ul><p>请志愿者提前准备手机操作演示内容。</p>',
'阳光社区老年活动中心', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 32 DAY),
20, 18, 5, 4.0, 40, '耐心细致，有基本手机操作知识', '张老师 13800000001', 4, 1, 256, 45, 23, 1, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW()),

('校园网络安全宣传周', 1, 1, 'https://picsum.photos/seed/act2/800/400',
'普及网络安全知识，提高同学们的防范意识',
'<p>配合国家网络安全宣传周活动，在校园内开展网络安全知识宣传：</p><ol><li>设置宣传展板</li><li>发放安全手册</li><li>现场答疑解惑</li><li>安全知识问答</li></ol>',
'学校主干道、图书馆门口', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY) + INTERVAL 6 HOUR,
DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY),
30, 28, 10, 6.0, 60, '了解基本网络安全知识', '李老师 13800000002', 4, 1, 189, 32, 18, 0, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),

-- 电子工程学院活动
('社区电子产品义务维修', 2, 2, 'https://picsum.photos/seed/act3/800/400',
'为社区居民免费维修小家电，传递爱心',
'<p>带上你的工具箱，为居民们修理：</p><ul><li>电风扇、台灯等小家电</li><li>收音机、遥控器等电子产品</li><li>简单电路故障排查</li></ul><p>专业老师现场指导，既是志愿服务也是实践学习。</p>',
'翠园社区服务中心', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY) + INTERVAL 5 HOUR,
DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY),
15, 15, 5, 5.0, 50, '有一定电子维修基础', '王工 13800000003', 4, 1, 178, 56, 34, 1, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW()),

('青少年科技创新体验营', 2, 2, 'https://picsum.photos/seed/act4/800/400',
'带领中小学生体验科技魅力，激发创新热情',
'<p>面向周边中小学生开展科技体验活动：</p><ul><li>简单电路搭建</li><li>机器人编程入门</li><li>3D打印演示</li><li>VR技术体验</li></ul>',
'学校电子实验中心', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY),
25, 22, 8, 4.0, 40, '热爱科技，善于与孩子沟通', '陈老师 13800000004', 4, 1, 234, 67, 45, 1, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),

-- 机械工程学院活动
('爱心自行车维修站', 3, 3, 'https://picsum.photos/seed/act5/800/400',
'为同学们免费维修自行车，倡导绿色出行',
'<p>校园内设立自行车维修服务点：</p><ul><li>链条调整</li><li>刹车检修</li><li>轮胎更换</li><li>车灯安装</li></ul><p>材料由学院提供，请自带维修工具。</p>',
'学生宿舍区广场', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY),
12, 12, 4, 4.0, 35, '会基本自行车维修', '杨老师 13800000005', 4, 1, 145, 28, 15, 0, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW()),

-- 经济管理学院活动
('公益助农直播带货', 4, 4, 'https://picsum.photos/seed/act6/800/400',
'帮助偏远地区农户销售农产品，助力乡村振兴',
'<p>与农村合作社合作，通过直播帮助农户销售：</p><ul><li>高山蜂蜜</li><li>有机蔬菜</li><li>土鸡蛋</li><li>手工豆腐</li></ul><p>需要提前学习直播技巧和产品知识。</p>',
'学校直播间、农村合作社', DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY) + INTERVAL 6 HOUR,
DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY),
10, 10, 3, 6.0, 80, '形象良好，表达能力强', '赵老师 13800000006', 4, 1, 456, 123, 89, 1, DATE_SUB(NOW(), INTERVAL 33 DAY), NOW()),

('财务知识进社区', 4, 4, 'https://picsum.photos/seed/act7/800/400',
'为社区居民普及理财防骗知识',
'<p>针对社区居民特别是老年人群体：</p><ol><li>识别非法集资</li><li>防范电信诈骗</li><li>合理投资理财</li><li>医保社保政策解读</li></ol>',
'和谐社区大礼堂', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 3 HOUR,
DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY),
20, 18, 6, 3.0, 30, '熟悉基本财务知识', '刘老师 13800000007', 4, 1, 167, 34, 22, 0, DATE_SUB(NOW(), INTERVAL 27 DAY), NOW()),

-- 外国语学院活动
('国际友人城市导览', 5, 5, 'https://picsum.photos/seed/act8/800/400',
'为来华留学生和外国游客提供城市导览服务',
'<p>在市博物馆、旅游景点为外国友人提供：</p><ul><li>英语导览讲解</li><li>中国文化介绍</li><li>生活服务咨询</li><li>语言交流帮助</li></ul>',
'市博物馆、老城区', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 5 HOUR,
DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY),
15, 14, 5, 5.0, 50, '英语流利，了解本地文化', '周老师 13800000008', 4, 1, 289, 78, 56, 1, DATE_SUB(NOW(), INTERVAL 23 DAY), NOW()),

('小学英语角志愿服务', 5, 5, 'https://picsum.photos/seed/act9/800/400',
'为小学生提供趣味英语学习辅导',
'<p>每周六下午前往阳光小学开展英语角活动：</p><ul><li>英语歌曲教学</li><li>英语小游戏</li><li>英语绘本阅读</li><li>口语对话练习</li></ul>',
'阳光小学', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 3 HOUR,
DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY),
20, 18, 8, 3.0, 30, '英语发音标准，喜欢孩子', '吴老师 13800000009', 4, 1, 198, 45, 32, 0, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),

-- 法学院活动
('法律援助咨询日', 5, 6, 'https://picsum.photos/seed/act10/800/400',
'为社区居民提供免费法律咨询服务',
'<p>在社区设立法律咨询点：</p><ul><li>劳动纠纷咨询</li><li>婚姻家庭问题</li><li>房产合同纠纷</li><li>消费维权指导</li></ul><p>由专业老师带队，现场指导。</p>',
'民生社区服务大厅', DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 32 DAY), DATE_SUB(NOW(), INTERVAL 27 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY),
12, 12, 4, 4.0, 50, '法学专业大二以上', '郑老师 13800000010', 4, 1, 234, 56, 34, 1, DATE_SUB(NOW(), INTERVAL 37 DAY), NOW()),

('模拟法庭进校园', 5, 6, 'https://picsum.photos/seed/act11/800/400',
'为中学生演示法庭审判流程，普及法律知识',
'<p>前往中学开展模拟法庭活动：</p><ol><li>法庭角色扮演</li><li>案件审理流程</li><li>法律知识问答</li><li>职业规划交流</li></ol>',
'第一中学报告厅', DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY) + INTERVAL 3 HOUR,
DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY),
25, 24, 10, 3.0, 35, '表达能力强，形象良好', '何老师 13800000011', 4, 1, 178, 43, 28, 0, DATE_SUB(NOW(), INTERVAL 31 DAY), NOW()),

-- 艺术学院活动
('社区文化墙绘制', 3, 7, 'https://picsum.photos/seed/act12/800/400',
'为老旧社区绘制主题文化墙，美化环境',
'<p>将破旧的社区围墙变成美丽的文化墙：</p><ul><li>设计墙绘方案</li><li>准备绑料</li><li>现场绑制</li><li>后期维护指导</li></ul>',
'新华社区', DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY) + INTERVAL 8 HOUR,
DATE_SUB(NOW(), INTERVAL 38 DAY), DATE_SUB(NOW(), INTERVAL 33 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY),
15, 15, 6, 8.0, 80, '美术专业，有绘画基础', '林老师 13800000012', 4, 1, 345, 89, 67, 1, DATE_SUB(NOW(), INTERVAL 43 DAY), NOW()),

('儿童艺术启蒙课堂', 3, 7, 'https://picsum.photos/seed/act13/800/400',
'为社区儿童提供免费艺术启蒙课程',
'<p>每周末为社区儿童开设艺术课：</p><ul><li>简笔画入门</li><li>彩泥手工</li><li>剪纸艺术</li><li>音乐律动</li></ul>',
'社区儿童活动中心', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 3 HOUR,
DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY),
12, 12, 4, 3.0, 30, '有艺术特长，喜欢孩子', '孙老师 13800000013', 4, 1, 167, 45, 32, 0, DATE_SUB(NOW(), INTERVAL 21 DAY), NOW()),

-- 医学院活动
('社区健康义诊', 1, 8, 'https://picsum.photos/seed/act14/800/400',
'为社区居民提供免费健康检测和咨询',
'<p>在社区开展健康义诊：</p><ul><li>血压血糖测量</li><li>健康咨询</li><li>用药指导</li><li>健康知识宣传</li></ul><p>由附属医院医生带队指导。</p>',
'阳光社区广场', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY),
20, 20, 8, 4.0, 50, '医学相关专业', '钱老师 13800000014', 4, 1, 456, 112, 78, 1, DATE_SUB(NOW(), INTERVAL 29 DAY), NOW()),

('急救知识培训进企业', 1, 8, 'https://picsum.photos/seed/act15/800/400',
'为企业员工培训心肺复苏等急救技能',
'<p>前往企业开展急救培训：</p><ol><li>CPR心肺复苏</li><li>AED使用方法</li><li>海姆立克急救法</li><li>外伤包扎</li></ol>',
'科技园区企业', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY),
15, 14, 5, 4.0, 45, '通过急救员认证', '冯老师 13800000015', 4, 1, 234, 67, 45, 0, DATE_SUB(NOW(), INTERVAL 24 DAY), NOW()),

-- 理学院活动
('科普进小学', 2, 9, 'https://picsum.photos/seed/act16/800/400',
'为小学生展示有趣的科学实验，激发科学兴趣',
'<p>带着有趣的科学实验走进小学：</p><ul><li>神奇的化学变色反应</li><li>有趣的物理小实验</li><li>数学魔术</li><li>天文知识普及</li></ul>',
'育才小学', DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY) + INTERVAL 3 HOUR,
DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY),
18, 16, 6, 3.0, 35, '理科专业，动手能力强', '魏老师 13800000016', 4, 1, 289, 78, 56, 1, DATE_SUB(NOW(), INTERVAL 26 DAY), NOW()),

('天文观测夜活动', 2, 9, 'https://picsum.photos/seed/act17/800/400',
'组织社区居民观测星空，普及天文知识',
'<p>在学校天文台开放日：</p><ul><li>专业望远镜观测</li><li>星空讲解</li><li>天文科普讲座</li><li>观星指导</li></ul>',
'学校天文观测台', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 4 HOUR,
DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY),
30, 28, 10, 4.0, 40, '了解基本天文知识', '邱老师 13800000017', 4, 1, 567, 145, 98, 1, DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),

-- 建筑学院活动
('古建筑保护宣传', 3, 10, 'https://picsum.photos/seed/act18/800/400',
'宣传古建筑保护知识，倡导文化遗产保护',
'<p>在老城区开展古建保护宣传：</p><ul><li>古建知识讲解</li><li>保护意识宣传</li><li>现场测绘记录</li><li>文创产品分发</li></ul>',
'历史文化街区', DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY) + INTERVAL 5 HOUR,
DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY),
15, 14, 5, 5.0, 50, '建筑相关专业，了解本地历史', '秦老师 13800000018', 4, 1, 234, 56, 34, 0, DATE_SUB(NOW(), INTERVAL 34 DAY), NOW()),

('社区适老化改造调研', 4, 10, 'https://picsum.photos/seed/act19/800/400',
'调研老旧小区适老化改造需求，提供设计建议',
'<p>深入老旧社区开展调研：</p><ol><li>现场踏勘测量</li><li>居民需求访谈</li><li>问题分析汇总</li><li>提出改造建议</li></ol>',
'老旧小区', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 6 HOUR,
DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY),
12, 11, 4, 6.0, 60, '建筑、规划专业', '尤老师 13800000019', 4, 1, 145, 34, 22, 0, DATE_SUB(NOW(), INTERVAL 19 DAY), NOW()),

-- 额外热门活动
('2026年城市马拉松志愿服务', 5, 1, 'https://picsum.photos/seed/act20/800/400',
'为城市马拉松提供赛事服务，传递运动精神',
'<p>马拉松赛事志愿服务：</p><ul><li>路线引导</li><li>补给站服务</li><li>计时芯片回收</li><li>奖牌发放</li></ul><p>早起集合，请做好准备！</p>',
'城市主干道', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR,
DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY),
100, 98, 50, 8.0, 100, '身体健康，能早起', '组委会 13800000020', 4, 1, 1234, 345, 234, 1, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW());

-- =====================================================
-- 2. 创建活动报名记录 (每个志愿者参与2-5个活动)
-- =====================================================

DELETE FROM activity_registration WHERE 1=1;

-- 为每个志愿者创建2-5条报名记录 (状态3=已完成)
INSERT INTO activity_registration (activity_id, volunteer_id, status, sign_in_time, sign_out_time, actual_hours, actual_points, create_time, update_time)
SELECT 
    a.id as activity_id,
    v.id as volunteer_id,
    3 as status,
    a.start_time as sign_in_time,
    a.end_time as sign_out_time,
    a.service_hours as actual_hours,
    a.points_reward as actual_points,
    a.create_time,
    NOW()
FROM volunteer v
CROSS JOIN activity a
WHERE 
    -- 让志愿者参与对应学院的活动 + 一些跨学院活动
    (
        -- 计算机学院学生参与计算机活动
        (v.college = '计算机学院' AND a.id IN (1, 2, 20))
        OR (v.college = '电子工程学院' AND a.id IN (3, 4, 20))
        OR (v.college = '机械工程学院' AND a.id IN (5, 12, 20))
        OR (v.college = '经济管理学院' AND a.id IN (6, 7, 20))
        OR (v.college = '外国语学院' AND a.id IN (8, 9, 20))
        OR (v.college = '法学院' AND a.id IN (10, 11, 20))
        OR (v.college = '艺术学院' AND a.id IN (12, 13, 20))
        OR (v.college = '医学院' AND a.id IN (14, 15, 20))
        OR (v.college = '理学院' AND a.id IN (16, 17, 20))
        OR (v.college = '建筑学院' AND a.id IN (18, 19, 20))
    )
ORDER BY v.id, a.id;

-- =====================================================
-- 3. 创建心得数据 (每个参与过活动的志愿者写1-2篇心得)
-- =====================================================

DELETE FROM experience WHERE 1=1;

INSERT INTO experience (volunteer_id, activity_id, title, content, status, view_count, like_count, comment_count, collect_count, is_featured, create_time, update_time)
SELECT 
    v.id as volunteer_id,
    a.id as activity_id,
    CONCAT('【', a.title, '】志愿心得') as title,
    CONCAT(
        '<p>参加"', a.title, '"活动让我收获满满！</p>',
        '<p>活动当天，我们早早来到', a.location, '，虽然天气', 
        ELT(FLOOR(RAND()*4)+1, '有点炎热', '略微寒冷', '十分宜人', '下着小雨'),
        '，但大家的热情丝毫不减。</p>',
        '<p>在活动过程中，我深刻体会到了志愿服务的意义。看到', 
        ELT(FLOOR(RAND()*4)+1, '老人们开心的笑容', '孩子们好奇的眼神', '居民们感激的话语', '服务对象满意的反馈'),
        '，我觉得所有的付出都是值得的。</p>',
        '<p>这次活动让我学到了很多。首先，', 
        ELT(FLOOR(RAND()*4)+1, '团队协作的重要性', '沟通技巧的提升', '专业知识的实践', '服务意识的培养'),
        '；其次，', 
        ELT(FLOOR(RAND()*4)+1, '对社会责任有了更深的认识', '锻炼了自己的组织能力', '交到了很多志同道合的朋友', '获得了宝贵的实践经验'),
        '。</p>',
        '<p>感谢学校和老师们提供这次机会，我会继续参与志愿服务，用自己的行动传递温暖和爱心！💪</p>',
        '<p style="text-align:right">——', v.name, '</p>'
    ) as content,
    1 as status,
    FLOOR(RAND()*500)+50 as view_count,
    FLOOR(RAND()*100)+10 as like_count,
    FLOOR(RAND()*30)+2 as comment_count,
    FLOOR(RAND()*50)+5 as collect_count,
    IF(RAND() > 0.8, 1, 0) as is_featured,
    DATE_ADD(a.end_time, INTERVAL FLOOR(RAND()*3)+1 DAY) as create_time,
    NOW() as update_time
FROM volunteer v
JOIN activity_registration ar ON ar.volunteer_id = v.id
JOIN activity a ON a.id = ar.activity_id
WHERE ar.status = 3
GROUP BY v.id, a.id
HAVING RAND() < 0.6; -- 60%的参与记录会写心得

-- =====================================================
-- 4. 显示统计结果
-- =====================================================
SELECT '活动和心得数据初始化完成！' as message;
SELECT 
    (SELECT COUNT(*) FROM activity) as 活动数量,
    (SELECT COUNT(*) FROM activity_registration) as 报名记录数,
    (SELECT COUNT(*) FROM activity_registration WHERE status = 3) as 已完成数,
    (SELECT COUNT(*) FROM experience) as 心得数量;

SELECT '
===========================================
数据汇总：
===========================================
活动：20个（每个学院2-3个 + 大型活动）
报名记录：约200-300条（每人2-3个活动）
心得分享：约100-150篇（部分志愿者撰写）

现在每个志愿者都有：
✓ 参与活动记录
✓ 志愿时长统计
✓ 获得积分记录
✓ 发表的心得（部分）

可在个人中心查看完整的志愿服务历史！
===========================================
' as 数据说明;
