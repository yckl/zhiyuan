-- ================================================
-- 为志愿者生成丰富的个人资料数据
-- 包括：手机号、邮箱、个人简介、技能特长
-- ================================================

-- 1. 更新 sys_user 表的手机号（每个人唯一）
UPDATE sys_user 
SET phone = CONCAT('138', LPAD(id * 7 + 10000000, 8, '0'))
WHERE phone IS NULL OR phone = '';

-- 2. 更新 sys_user 表的邮箱（每个人唯一）
UPDATE sys_user 
SET email = CONCAT(username, '@campus.edu.cn')
WHERE email IS NULL OR email = '';

-- 3. 更新 volunteer 表的个人简介（多样化内容）
UPDATE volunteer SET bio = CASE
    WHEN MOD(id, 20) = 0 THEN '热爱公益事业，希望用自己的行动为社会贡献一份力量。已参与多次社区服务，获得优秀志愿者称号。'
    WHEN MOD(id, 20) = 1 THEN '计算机专业学生，擅长技术支持和网络维护。喜欢参与科技类志愿活动，帮助老人学习使用智能手机。'
    WHEN MOD(id, 20) = 2 THEN '医学院学生，热心于健康科普和义诊活动。曾多次参与社区健康讲座，帮助居民了解常见疾病预防知识。'
    WHEN MOD(id, 20) = 3 THEN '外语专业，英语和日语流利。经常参与国际交流活动，担任翻译志愿者，帮助外国友人了解中国文化。'
    WHEN MOD(id, 20) = 4 THEN '艺术学院学生，擅长绘画和手工。热衷于为山区儿童开展美术支教活动，用艺术点亮孩子们的梦想。'
    WHEN MOD(id, 20) = 5 THEN '法学专业学生，关注弱势群体权益保护。参与法律援助志愿服务，为社区居民提供免费法律咨询。'
    WHEN MOD(id, 20) = 6 THEN '教育学院学生，有丰富的家教经验。积极参与留守儿童帮扶项目，陪伴孩子们学习和成长。'
    WHEN MOD(id, 20) = 7 THEN '音乐专业学生，擅长钢琴和声乐。定期前往养老院开展文艺表演，用音乐温暖老人们的心灵。'
    WHEN MOD(id, 20) = 8 THEN '体育学院学生，热爱运动。担任社区运动会志愿者，组织青少年体育活动，传播健康生活理念。'
    WHEN MOD(id, 20) = 9 THEN '新闻传播专业，擅长摄影和视频剪辑。记录志愿活动的精彩瞬间，传播正能量。'
    WHEN MOD(id, 20) = 10 THEN '环境科学专业学生，关注生态环保。积极参与河流清洁、垃圾分类宣传等环保志愿活动。'
    WHEN MOD(id, 20) = 11 THEN '心理学专业学生，了解心理咨询基础知识。参与心理健康热线志愿服务，为需要帮助的人提供心理支持。'
    WHEN MOD(id, 20) = 12 THEN '土木工程专业学生，动手能力强。参与农村危房改造、社区基础设施修缮等志愿活动。'
    WHEN MOD(id, 20) = 13 THEN '化学专业学生，喜欢科普教育。走进中小学开展趣味化学实验，激发孩子们对科学的兴趣。'
    WHEN MOD(id, 20) = 14 THEN '经济学专业学生，关注扶贫助农。参与电商扶贫项目，帮助农户推广农产品。'
    WHEN MOD(id, 20) = 15 THEN '护理专业学生，有急救证书。参与马拉松、大型活动的医疗保障志愿服务。'
    WHEN MOD(id, 20) = 16 THEN '建筑学专业学生，热爱古建筑保护。参与历史文化遗址保护宣传和志愿讲解活动。'
    WHEN MOD(id, 20) = 17 THEN '汉语言文学专业，写作能力强。担任志愿者活动的宣传报道工作，撰写感人的志愿故事。'
    WHEN MOD(id, 20) = 18 THEN '社会工作专业学生，善于沟通协调。参与社区治理志愿服务，帮助解决邻里纠纷。'
    ELSE '热爱志愿服务，乐于助人。希望通过自己的努力，让这个世界变得更加美好！'
END
WHERE bio IS NULL OR bio = '';

-- 4. 更新 volunteer 表的技能特长（多样化内容）
UPDATE volunteer SET skills = CASE
    WHEN MOD(id, 15) = 0 THEN '计算机操作,Office办公软件,网络维护,数据分析'
    WHEN MOD(id, 15) = 1 THEN '英语六级,日语N2,商务翻译,跨文化交流'
    WHEN MOD(id, 15) = 2 THEN '急救技能,健康科普,血压测量,心肺复苏'
    WHEN MOD(id, 15) = 3 THEN '钢琴十级,声乐,吉他弹唱,音乐教学'
    WHEN MOD(id, 15) = 4 THEN '素描,水彩画,手工制作,书法'
    WHEN MOD(id, 15) = 5 THEN '篮球,足球,游泳,体育教学'
    WHEN MOD(id, 15) = 6 THEN '摄影,视频剪辑,Photoshop,新媒体运营'
    WHEN MOD(id, 15) = 7 THEN '演讲主持,活动策划,团队管理,PPT制作'
    WHEN MOD(id, 15) = 8 THEN '心理咨询,情绪疏导,倾听陪伴,危机干预'
    WHEN MOD(id, 15) = 9 THEN '法律知识,合同审查,法律咨询,调解纠纷'
    WHEN MOD(id, 15) = 10 THEN '厨艺,烘焙,营养搭配,食品安全'
    WHEN MOD(id, 15) = 11 THEN '驾驶技术,车辆维护,安全驾驶,应急处理'
    WHEN MOD(id, 15) = 12 THEN '编程开发,Python,Java,网站建设'
    WHEN MOD(id, 15) = 13 THEN '财务知识,记账报税,理财规划,经济分析'
    ELSE '沟通协调,团队协作,时间管理,文档写作'
END
WHERE skills IS NULL OR skills = '';

-- 5. 更新兴趣标签（JSON格式）
UPDATE volunteer SET interest_tags = CASE
    WHEN MOD(id, 10) = 0 THEN '["社区服务","文化活动","环境保护"]'
    WHEN MOD(id, 10) = 1 THEN '["科技助老","教育帮扶","公益讲座"]'
    WHEN MOD(id, 10) = 2 THEN '["医疗健康","心理援助","关爱老人"]'
    WHEN MOD(id, 10) = 3 THEN '["支教助学","儿童关怀","艺术培养"]'
    WHEN MOD(id, 10) = 4 THEN '["体育运动","健康生活","全民健身"]'
    WHEN MOD(id, 10) = 5 THEN '["环境保护","垃圾分类","生态文明"]'
    WHEN MOD(id, 10) = 6 THEN '["文化传承","历史保护","非遗推广"]'
    WHEN MOD(id, 10) = 7 THEN '["应急救援","安全保障","防灾减灾"]'
    WHEN MOD(id, 10) = 8 THEN '["扶贫助困","农村发展","乡村振兴"]'
    ELSE '["综合服务","活动策划","志愿管理"]'
END
WHERE interest_tags IS NULL OR interest_tags = '';

-- 6. 验证更新结果
SELECT v.id, v.name, v.bio, v.skills, v.interest_tags, u.phone, u.email
FROM volunteer v
LEFT JOIN sys_user u ON v.user_id = u.id
WHERE v.is_deleted = 0
LIMIT 20;
