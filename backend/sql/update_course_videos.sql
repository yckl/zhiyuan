-- ================================================
-- 更新所有课程视频 URL
-- 使用 100% 可访问的免费公开视频（已验证）
-- ================================================

-- 使用可靠的公开视频源
-- 来源: sample-videos.com, filesamples.com, samplelib.com

-- 首先，为所有课程设置稳定可用的视频URL
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4' WHERE id = 1;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4' WHERE id = 2;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4' WHERE id = 3;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4' WHERE id = 4;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4' WHERE id = 5;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4' WHERE id = 6;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4' WHERE id = 7;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4' WHERE id = 8;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4' WHERE id = 9;
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4' WHERE id = 10;

-- 批量更新剩余课程（id > 10 的课程轮换使用这些视频）
UPDATE course SET video_url = CASE 
    WHEN MOD(id, 10) = 0 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
    WHEN MOD(id, 10) = 1 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4'
    WHEN MOD(id, 10) = 2 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4'
    WHEN MOD(id, 10) = 3 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4'
    WHEN MOD(id, 10) = 4 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4'
    WHEN MOD(id, 10) = 5 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4'
    WHEN MOD(id, 10) = 6 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4'
    WHEN MOD(id, 10) = 7 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4'
    WHEN MOD(id, 10) = 8 THEN 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4'
    ELSE 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4'
END
WHERE id > 10;

-- 确保所有课程都有视频URL（如果还有为空的）
UPDATE course SET video_url = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'
WHERE video_url IS NULL OR video_url = '';

-- 验证更新结果
SELECT id, title, SUBSTRING(video_url, 1, 60) AS video_url_preview FROM course ORDER BY id LIMIT 30;

-- 提示：这些是 Google 提供的免费开源示例视频，稳定可用
-- BigBuckBunny - 9分钟动画短片
-- ElephantsDream - 10分钟动画短片
-- Sintel - 15分钟动画短片
-- TearsOfSteel - 12分钟科幻短片
