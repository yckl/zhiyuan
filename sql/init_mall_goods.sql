-- =====================================================
-- 积分商城商品初始化 (300个商品)
-- 分类：功能卡片、装扮道具、权益兑换、学习资源、实物周边
-- =====================================================

USE `volunteer_system`;

TRUNCATE TABLE mall_goods;

-- ========== 功能卡片类 (goods_type=1) ==========
INSERT INTO mall_goods (name, category, cover_image, description, points_price, stock, sold_count, goods_type, virtual_content, status, create_time) VALUES
('经验加成卡(10%)', '功能卡片', 'https://picsum.photos/seed/v1/200', '活动经验提升10%，持续7天', 20, -1, 0, 1, 'EXP_BOOST_10', 1, NOW()),
('经验加成卡(20%)', '功能卡片', 'https://picsum.photos/seed/v2/200', '活动经验提升20%，持续7天', 35, -1, 0, 1, 'EXP_BOOST_20', 1, NOW()),
('经验加成卡(50%)', '功能卡片', 'https://picsum.photos/seed/v3/200', '活动经验提升50%，持续3天', 50, -1, 0, 1, 'EXP_BOOST_50', 1, NOW()),
('积分翻倍卡', '功能卡片', 'https://picsum.photos/seed/v4/200', '下次活动积分翻倍', 60, -1, 0, 1, 'POINTS_DOUBLE', 1, NOW()),
('签到补签卡', '功能卡片', 'https://picsum.photos/seed/v5/200', '补签一天签到', 30, -1, 0, 1, 'SIGNIN_REPAIR', 1, NOW()),
('幸运加成卡', '功能卡片', 'https://picsum.photos/seed/v6/200', '抽奖概率提升10%', 40, -1, 0, 1, 'LUCK_BOOST', 1, NOW()),
('VIP体验卡(7天)', '功能卡片', 'https://picsum.photos/seed/v7/200', 'VIP特权体验7天', 80, -1, 0, 1, 'VIP_7DAY', 1, NOW()),
('VIP体验卡(30天)', '功能卡片', 'https://picsum.photos/seed/v8/200', 'VIP特权体验30天', 200, -1, 0, 1, 'VIP_30DAY', 1, NOW()),
('抽奖次数+1', '功能卡片', 'https://picsum.photos/seed/v9/200', '增加1次抽奖机会', 25, -1, 0, 1, 'LOTTERY_CHANCE', 1, NOW()),
('抽奖次数+5', '功能卡片', 'https://picsum.photos/seed/v10/200', '增加5次抽奖机会', 100, -1, 0, 1, 'LOTTERY_CHANCE_5', 1, NOW());

-- ========== 装扮道具类 (goods_type=1) ==========
INSERT INTO mall_goods (name, category, cover_image, description, points_price, stock, sold_count, goods_type, virtual_content, status, create_time) VALUES
('专属头像框-青春', '装扮道具', 'https://picsum.photos/seed/v11/200', '限定头像框，展示青春活力', 50, -1, 0, 1, 'FRAME_YOUTH', 1, NOW()),
('专属头像框-热血', '装扮道具', 'https://picsum.photos/seed/v12/200', '限定头像框，彰显热血精神', 50, -1, 0, 1, 'FRAME_PASSION', 1, NOW()),
('专属头像框-奉献', '装扮道具', 'https://picsum.photos/seed/v13/200', '限定头像框，代表奉献精神', 50, -1, 0, 1, 'FRAME_DEVOTION', 1, NOW()),
('专属勋章-初心', '装扮道具', 'https://picsum.photos/seed/v14/200', '个人主页展示勋章', 40, -1, 0, 1, 'BADGE_BEGINNER', 1, NOW()),
('专属勋章-坚守', '装扮道具', 'https://picsum.photos/seed/v15/200', '个人主页展示勋章', 60, -1, 0, 1, 'BADGE_PERSIST', 1, NOW()),
('专属勋章-先锋', '装扮道具', 'https://picsum.photos/seed/v16/200', '个人主页展示勋章', 80, -1, 0, 1, 'BADGE_PIONEER', 1, NOW()),
('昵称改色卡-红', '装扮道具', 'https://picsum.photos/seed/v17/200', '昵称显示为红色7天', 30, -1, 0, 1, 'NAME_RED', 1, NOW()),
('昵称改色卡-蓝', '装扮道具', 'https://picsum.photos/seed/v18/200', '昵称显示为蓝色7天', 30, -1, 0, 1, 'NAME_BLUE', 1, NOW()),
('昵称改色卡-金', '装扮道具', 'https://picsum.photos/seed/v19/200', '昵称显示为金色7天', 50, -1, 0, 1, 'NAME_GOLD', 1, NOW()),
('个性签名卡', '装扮道具', 'https://picsum.photos/seed/v20/200', '解锁自定义个性签名', 40, -1, 0, 1, 'SIGNATURE', 1, NOW());

-- ========== 权益兑换类 (goods_type=1) ==========
INSERT INTO mall_goods (name, category, cover_image, description, points_price, stock, sold_count, goods_type, virtual_content, status, create_time) VALUES
('腾讯视频VIP周卡', '权益兑换', 'https://picsum.photos/seed/v21/200', '腾讯视频7天VIP', 80, 100, 0, 1, 'TENCENT_VIDEO_7D', 1, NOW()),
('爱奇艺VIP周卡', '权益兑换', 'https://picsum.photos/seed/v22/200', '爱奇艺7天VIP', 80, 100, 0, 1, 'IQIYI_7D', 1, NOW()),
('B站大会员周卡', '权益兑换', 'https://picsum.photos/seed/v23/200', 'B站7天大会员', 60, 100, 0, 1, 'BILIBILI_7D', 1, NOW()),
('网易云音乐VIP周卡', '权益兑换', 'https://picsum.photos/seed/v24/200', '网易云7天黑胶VIP', 50, 100, 0, 1, 'NETEASE_7D', 1, NOW()),
('QQ音乐VIP周卡', '权益兑换', 'https://picsum.photos/seed/v25/200', 'QQ音乐7天绿钻', 50, 100, 0, 1, 'QQMUSIC_7D', 1, NOW()),
('饿了么5元红包', '权益兑换', 'https://picsum.photos/seed/v26/200', '饿了么满20减5', 30, 200, 0, 1, 'ELEME_5', 1, NOW()),
('美团5元红包', '权益兑换', 'https://picsum.photos/seed/v27/200', '美团满20减5', 30, 200, 0, 1, 'MEITUAN_5', 1, NOW()),
('肯德基10元代金券', '权益兑换', 'https://picsum.photos/seed/v28/200', 'KFC满30可用', 60, 100, 0, 1, 'KFC_10', 1, NOW()),
('星巴克15元券', '权益兑换', 'https://picsum.photos/seed/v29/200', '星巴克满50可用', 90, 50, 0, 1, 'STARBUCKS_15', 1, NOW()),
('话费充值5元', '权益兑换', 'https://picsum.photos/seed/v30/200', '全网通用话费', 55, 100, 0, 1, 'PHONE_5', 1, NOW());

-- ========== 学习资源类 (goods_type=1) ==========
INSERT INTO mall_goods (name, category, cover_image, description, points_price, stock, sold_count, goods_type, virtual_content, status, create_time) VALUES
('Python入门课程', '学习资源', 'https://picsum.photos/seed/v31/200', 'Python编程入门视频课', 80, 100, 0, 1, 'COURSE_PYTHON', 1, NOW()),
('Java基础课程', '学习资源', 'https://picsum.photos/seed/v32/200', 'Java编程基础教程', 80, 100, 0, 1, 'COURSE_JAVA', 1, NOW()),
('前端开发入门', '学习资源', 'https://picsum.photos/seed/v33/200', 'HTML/CSS/JS入门', 60, 100, 0, 1, 'COURSE_FRONTEND', 1, NOW()),
('办公软件教程', '学习资源', 'https://picsum.photos/seed/v34/200', 'Word/Excel/PPT全套', 40, 100, 0, 1, 'COURSE_OFFICE', 1, NOW()),
('四级词汇电子书', '学习资源', 'https://picsum.photos/seed/v35/200', 'CET4核心词汇', 30, -1, 0, 1, 'EBOOK_CET4', 1, NOW()),
('六级词汇电子书', '学习资源', 'https://picsum.photos/seed/v36/200', 'CET6核心词汇', 30, -1, 0, 1, 'EBOOK_CET6', 1, NOW()),
('考研资料包', '学习资源', 'https://picsum.photos/seed/v37/200', '考研公共课资料', 80, -1, 0, 1, 'EBOOK_KAOYAN', 1, NOW()),
('简历模板50套', '学习资源', 'https://picsum.photos/seed/v38/200', '精美简历模板', 25, -1, 0, 1, 'TEMPLATE_RESUME', 1, NOW()),
('PPT模板100套', '学习资源', 'https://picsum.photos/seed/v39/200', '商务PPT模板', 35, -1, 0, 1, 'TEMPLATE_PPT', 1, NOW()),
('PS入门教程', '学习资源', 'https://picsum.photos/seed/v40/200', 'Photoshop基础', 50, 100, 0, 1, 'COURSE_PS', 1, NOW());

-- ========== 实物周边 (goods_type=0) ==========
INSERT INTO mall_goods (name, category, cover_image, description, points_price, stock, sold_count, goods_type, status, create_time) VALUES
('志愿者定制笔记本', '实物周边', 'https://picsum.photos/seed/p1/200', 'A5精装笔记本', 30, 100, 0, 0, 1, NOW()),
('志愿者定制钢笔', '实物周边', 'https://picsum.photos/seed/p2/200', '金属钢笔礼盒', 50, 80, 0, 0, 1, NOW()),
('志愿者定制书签套装', '实物周边', 'https://picsum.photos/seed/p3/200', '金属书签5枚', 25, 150, 0, 0, 1, NOW()),
('便利贴套装', '实物周边', 'https://picsum.photos/seed/p4/200', '彩色便利贴10本', 15, 200, 0, 0, 1, NOW()),
('荧光笔套装', '实物周边', 'https://picsum.photos/seed/p5/200', '6色荧光笔', 18, 150, 0, 0, 1, NOW()),
('中性笔10支装', '实物周边', 'https://picsum.photos/seed/p6/200', '0.5mm黑色', 12, 300, 0, 0, 1, NOW()),
('计算器', '实物周边', 'https://picsum.photos/seed/p7/200', '科学计算器', 45, 80, 0, 0, 1, NOW()),
('志愿者定制水杯', '实物周边', 'https://picsum.photos/seed/p8/200', '不锈钢保温杯500ml', 60, 80, 0, 0, 1, NOW()),
('志愿者定制雨伞', '实物周边', 'https://picsum.photos/seed/p9/200', '折叠晴雨两用伞', 45, 100, 0, 0, 1, NOW()),
('志愿者定制帆布包', '实物周边', 'https://picsum.photos/seed/p10/200', '环保帆布袋', 35, 120, 0, 0, 1, NOW()),
('志愿者定制T恤', '实物周边', 'https://picsum.photos/seed/p11/200', '纯棉圆领T恤', 80, 60, 0, 0, 1, NOW()),
('志愿者定制帽子', '实物周边', 'https://picsum.photos/seed/p12/200', '棒球帽', 40, 80, 0, 0, 1, NOW()),
('手机支架', '实物周边', 'https://picsum.photos/seed/p13/200', '桌面手机支架', 18, 150, 0, 0, 1, NOW()),
('充电宝5000mAh', '实物周边', 'https://picsum.photos/seed/p14/200', '便携移动电源', 80, 50, 0, 0, 1, NOW()),
('数据线三合一', '实物周边', 'https://picsum.photos/seed/p15/200', '苹果安卓Type-C', 25, 150, 0, 0, 1, NOW()),
('志愿者徽章', '实物周边', 'https://picsum.photos/seed/p16/200', '金属胸针徽章', 15, 200, 0, 0, 1, NOW()),
('志愿者钥匙扣', '实物周边', 'https://picsum.photos/seed/p17/200', '创意钥匙扣', 12, 200, 0, 0, 1, NOW()),
('志愿者马克杯', '实物周边', 'https://picsum.photos/seed/p18/200', '陶瓷马克杯', 35, 80, 0, 0, 1, NOW()),
('蓝牙耳机', '实物周边', 'https://picsum.photos/seed/p19/200', '入耳式蓝牙耳机', 150, 30, 0, 0, 1, NOW()),
('坚果礼盒', '实物周边', 'https://picsum.photos/seed/p20/200', '混合坚果500g', 60, 50, 0, 0, 1, NOW());

-- 扩展商品
INSERT INTO mall_goods (name, category, cover_image, description, points_price, stock, sold_count, goods_type, status, create_time)
SELECT CONCAT(name, '款式', n), category, CONCAT(cover_image, '_', n), description, points_price + n*2, stock, 0, goods_type, 1, NOW()
FROM mall_goods, (SELECT 2 n UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) nums
WHERE id <= 60;

-- ========== 初始化抽奖奖品 ==========
TRUNCATE TABLE lottery_prize;
INSERT INTO lottery_prize (name, image, prize_type, prize_value, probability, daily_limit, total_limit, sort_order, status, create_time) VALUES
('谢谢参与', 'https://picsum.photos/seed/lp1/100', 0, 0, 0.3000, 0, 0, 1, 1, NOW()),
('10积分', 'https://picsum.photos/seed/lp2/100', 0, 10, 0.2500, 100, 0, 2, 1, NOW()),
('签到补签卡', 'https://picsum.photos/seed/lp3/100', 2, 0, 0.1500, 50, 0, 3, 1, NOW()),
('50积分', 'https://picsum.photos/seed/lp4/100', 0, 50, 0.1000, 30, 0, 4, 1, NOW()),
('经验加成卡', 'https://picsum.photos/seed/lp5/100', 2, 0, 0.1000, 30, 0, 5, 1, NOW()),
('100积分', 'https://picsum.photos/seed/lp6/100', 0, 100, 0.0500, 10, 0, 6, 1, NOW()),
('VIP体验卡', 'https://picsum.photos/seed/lp7/100', 2, 0, 0.0300, 5, 0, 7, 1, NOW()),
('谢谢参与', 'https://picsum.photos/seed/lp8/100', 0, 0, 0.0200, 0, 0, 8, 1, NOW());

-- 统计
SELECT '商品和奖品初始化完成！' as 状态;
SELECT category as 分类, COUNT(*) as 数量 FROM mall_goods GROUP BY category ORDER BY COUNT(*) DESC;
SELECT COUNT(*) as 商品总数 FROM mall_goods;
SELECT COUNT(*) as 奖品数量 FROM lottery_prize;

