-- 修复活动表中的主办方ID映射问题
-- 之前错误地使用了 organizer.id，正确的应该是 organizer.user_id

USE `volunteer_system`;

UPDATE activity a 
JOIN organizer o ON a.organizer_id = o.id 
SET a.organizer_id = o.user_id;

-- 验证结果
SELECT a.title, a.organizer_id as '当前organizer_id(应该等于user_id)', o.org_name 
FROM activity a 
JOIN organizer o ON a.organizer_id = o.user_id 
LIMIT 10;
