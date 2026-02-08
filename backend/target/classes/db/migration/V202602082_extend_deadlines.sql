-- 延长所有已过期活动的报名截止日期（延长7天）
-- 适用于测试环境，确保活动可以继续报名

-- 更新报名截止时间已过的活动
UPDATE activity 
SET register_end = DATE_ADD(NOW(), INTERVAL 7 DAY)
WHERE register_end IS NOT NULL 
  AND register_end < NOW() 
  AND status = 2;  -- 仅更新"已发布/招募中"状态的活动

-- 同时更新活动开始时间（如果也已过期）
UPDATE activity 
SET start_time = DATE_ADD(NOW(), INTERVAL 8 DAY),
    end_time = DATE_ADD(NOW(), INTERVAL 9 DAY)
WHERE start_time IS NOT NULL 
  AND start_time < NOW() 
  AND status = 2;

-- 查看更新结果
SELECT id, title, register_end, start_time, status 
FROM activity 
WHERE status = 2 
ORDER BY id DESC 
LIMIT 10;
