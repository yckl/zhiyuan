-- 插入默认轮播图测试数据
-- 使用免费的Unsplash图片作为示例

INSERT INTO banner (title, image, link, sort_order, status, create_time, update_time, is_deleted) VALUES
('欢迎加入校园志愿者', 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=1200&h=400&fit=crop', '/activity', 100, 1, NOW(), NOW(), 0),
('爱心传递 温暖校园', 'https://images.unsplash.com/photo-1469571486292-0ba58a3f068b?w=1200&h=400&fit=crop', '/activity', 90, 1, NOW(), NOW(), 0),
('积分商城开放', 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1200&h=400&fit=crop', '/mall', 80, 1, NOW(), NOW(), 0);

-- 验证数据
SELECT * FROM banner WHERE is_deleted = 0 ORDER BY sort_order DESC;
