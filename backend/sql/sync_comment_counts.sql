-- ================================================
-- 同步心得评论计数 & 生成测试评论数据
-- ================================================

-- 1. 首先同步所有心得的评论计数（根据实际评论数更新）
UPDATE experience e
SET comment_count = (
    SELECT COUNT(*) 
    FROM comment c 
    WHERE c.target_type = 'EXPERIENCE' 
      AND c.target_id = e.id 
      AND c.is_deleted = 0
      AND c.parent_id = 0  -- 只计算顶级评论
);

-- 2. 查看当前心得列表
SELECT id, title, comment_count, like_count FROM experience WHERE is_deleted = 0 LIMIT 20;

-- 3. 查看当前评论数据
SELECT * FROM comment WHERE target_type = 'EXPERIENCE' AND is_deleted = 0 LIMIT 20;

-- ================================================
-- 如果需要生成测试评论数据，请执行以下SQL
-- ================================================

-- 为每个心得生成3-5条测试评论
INSERT INTO comment (user_id, target_type, target_id, parent_id, content, like_count, status, create_time, update_time, is_deleted)
SELECT 
    FLOOR(1 + RAND() * 10) AS user_id,  -- 随机用户ID 1-10
    'EXPERIENCE' AS target_type,
    e.id AS target_id,
    0 AS parent_id,
    CASE FLOOR(RAND() * 10)
        WHEN 0 THEN '写得真好，非常有感触！'
        WHEN 1 THEN '很棒的心得分享，学习了！'
        WHEN 2 THEN '同感，志愿服务确实能让人成长很多。'
        WHEN 3 THEN '感谢分享，期待更多这样的内容。'
        WHEN 4 THEN '这次活动我也参加了，感受相同！'
        WHEN 5 THEN '点赞支持，希望更多人看到。'
        WHEN 6 THEN '写得太好了，收藏起来慢慢看。'
        WHEN 7 THEN '作为新手志愿者受益匪浅！'
        WHEN 8 THEN '加油！继续传递正能量！'
        ELSE '好文章，已分享给朋友们。'
    END AS content,
    FLOOR(RAND() * 20) AS like_count,
    1 AS status,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY) AS create_time,
    NOW() AS update_time,
    0 AS is_deleted
FROM experience e
WHERE e.is_deleted = 0
LIMIT 50;

-- 再生成一些回复评论
INSERT INTO comment (user_id, target_type, target_id, parent_id, reply_to_user_id, content, like_count, status, create_time, update_time, is_deleted)
SELECT 
    FLOOR(1 + RAND() * 10) AS user_id,
    c.target_type,
    c.target_id,
    c.id AS parent_id,
    c.user_id AS reply_to_user_id,
    CASE FLOOR(RAND() * 5)
        WHEN 0 THEN '说得对！'
        WHEN 1 THEN '同意这个观点。'
        WHEN 2 THEN '我也这么认为！'
        WHEN 3 THEN '赞一个！'
        ELSE '回复得好！'
    END AS content,
    FLOOR(RAND() * 10) AS like_count,
    1 AS status,
    DATE_ADD(c.create_time, INTERVAL FLOOR(RAND() * 5) HOUR) AS create_time,
    NOW() AS update_time,
    0 AS is_deleted
FROM comment c
WHERE c.target_type = 'EXPERIENCE' 
  AND c.parent_id = 0 
  AND c.is_deleted = 0
LIMIT 30;

-- 4. 最后再次同步评论计数
UPDATE experience e
SET comment_count = (
    SELECT COUNT(*) 
    FROM comment c 
    WHERE c.target_type = 'EXPERIENCE' 
      AND c.target_id = e.id 
      AND c.is_deleted = 0
      AND c.parent_id = 0
);

-- 5. 验证结果
SELECT e.id, e.title, e.comment_count,
    (SELECT COUNT(*) FROM comment c WHERE c.target_type = 'EXPERIENCE' AND c.target_id = e.id AND c.is_deleted = 0 AND c.parent_id = 0) AS actual_count
FROM experience e
WHERE e.is_deleted = 0
LIMIT 20;
