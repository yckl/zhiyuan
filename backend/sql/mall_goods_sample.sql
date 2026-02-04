-- 积分商城示例商品数据
-- 功能卡片类
INSERT INTO mall_goods (name, category, cover_image, description, points_price, original_price, stock, sold_count, goods_type, virtual_content, limit_per_user, status, sort_order, create_time, update_time, is_deleted) VALUES
('免审核卡', '功能卡片', 'https://img.icons8.com/color/96/ticket.png', '使用后可跳过活动报名审核，直接通过', 200, 20.00, 100, 0, 1, 'SKIP_REVIEW_CARD', 5, 1, 100, NOW(), NOW(), 0),
('补签卡', '功能卡片', 'https://img.icons8.com/color/96/calendar-plus.png', '可用于补签错过的签到日期', 100, 10.00, 200, 0, 1, 'MAKEUP_CARD', 10, 1, 99, NOW(), NOW(), 0),
('双倍积分卡', '功能卡片', 'https://img.icons8.com/color/96/coins.png', '下次活动获得双倍积分', 300, 30.00, 50, 0, 1, 'DOUBLE_POINTS_CARD', 3, 1, 98, NOW(), NOW(), 0);

-- 装扮道具类
INSERT INTO mall_goods (name, category, cover_image, description, points_price, original_price, stock, sold_count, goods_type, limit_per_user, status, sort_order, create_time, update_time, is_deleted) VALUES
('金色勋章框', '装扮道具', 'https://img.icons8.com/color/96/medal2.png', '个人主页展示金色勋章边框', 500, 50.00, 30, 0, 1, 1, 1, 90, NOW(), NOW(), 0),
('爱心头像框', '装扮道具', 'https://img.icons8.com/color/96/like.png', '粉色爱心头像装饰框', 300, 30.00, 50, 0, 1, 1, 1, 89, NOW(), NOW(), 0),
('志愿者之星徽章', '装扮道具', 'https://img.icons8.com/color/96/star.png', '彰显志愿者身份的专属徽章', 150, 15.00, -1, 0, 1, 1, 1, 88, NOW(), NOW(), 0);

-- 权益兑换类
INSERT INTO mall_goods (name, category, cover_image, description, points_price, original_price, stock, sold_count, goods_type, virtual_content, limit_per_user, status, sort_order, create_time, update_time, is_deleted) VALUES
('优先报名权', '权益兑换', 'https://img.icons8.com/color/96/first.png', '热门活动优先报名资格', 400, 40.00, 20, 0, 1, 'VIP_REGISTER', 2, 1, 80, NOW(), NOW(), 0),
('免费培训课程', '权益兑换', 'https://img.icons8.com/color/96/classroom.png', '兑换一门付费培训课程', 600, 60.00, 10, 0, 1, 'FREE_COURSE', 1, 1, 79, NOW(), NOW(), 0);

-- 学习资源类
INSERT INTO mall_goods (name, category, cover_image, description, points_price, original_price, stock, sold_count, goods_type, virtual_content, limit_per_user, status, sort_order, create_time, update_time, is_deleted) VALUES
('志愿服务手册电子版', '学习资源', 'https://img.icons8.com/color/96/book.png', '志愿者服务规范指南', 50, 5.00, -1, 0, 1, 'EBOOK_001', 1, 1, 70, NOW(), NOW(), 0),
('急救知识视频课程', '学习资源', 'https://img.icons8.com/color/96/video.png', '10节急救知识精选课程', 200, 30.00, -1, 0, 1, 'VIDEO_001', 1, 1, 69, NOW(), NOW(), 0);

-- 实物周边类
INSERT INTO mall_goods (name, category, cover_image, description, points_price, original_price, stock, sold_count, goods_type, limit_per_user, status, sort_order, create_time, update_time, is_deleted) VALUES
('志愿者定制T恤', '实物周边', 'https://img.icons8.com/color/96/t-shirt.png', '志愿者专属定制T恤一件', 800, 80.00, 100, 0, 0, 2, 1, 60, NOW(), NOW(), 0),
('志愿者文化衫', '实物周边', 'https://img.icons8.com/color/96/shirt.png', '志愿者专属文化衫，展现志愿精神', 600, 60.00, 80, 0, 0, 2, 1, 59, NOW(), NOW(), 0),
('志愿者纪念水杯', '实物周边', 'https://img.icons8.com/color/96/cup.png', '保温水杯，印有志愿者公益主题Logo', 400, 40.00, 150, 0, 0, 3, 1, 58, NOW(), NOW(), 0),
('志愿者背包', '实物周边', 'https://img.icons8.com/color/96/backpack.png', '双肩背包，容量大，印有志愿者标识', 1000, 100.00, 50, 0, 0, 1, 1, 57, NOW(), NOW(), 0),
('便携笔记本套装', '实物周边', 'https://img.icons8.com/color/96/notebook.png', 'A5笔记本+签字笔套装', 250, 25.00, 200, 0, 0, 5, 1, 56, NOW(), NOW(), 0),
('志愿者钥匙扣', '实物周边', 'https://img.icons8.com/color/96/key.png', '精美金属钥匙扣纪念品', 100, 10.00, 300, 0, 0, 10, 1, 55, NOW(), NOW(), 0);
