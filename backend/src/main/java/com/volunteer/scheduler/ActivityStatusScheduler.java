package com.volunteer.scheduler;

import com.volunteer.mapper.ActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 活动状态自动流转调度器
 * 定时检查并更新活动状态：
 * - 已发布 + 开始时间已过 -> 进行中
 * - 进行中 + 结束时间已过 -> 已结束
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusScheduler {

    private final ActivityMapper activityMapper;

    /**
     * 每5分钟执行一次活动状态自动流转
     * 将已到开始时间的活动设为"进行中"，已过结束时间的活动设为"已结束"
     */
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行
    public void updateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("开始活动状态自动流转检查, 当前时间: {}", now);

        try {
            // 将已到开始时间的已发布活动设为"进行中"
            int toOngoing = activityMapper.batchUpdateToOngoing(now);
            if (toOngoing > 0) {
                log.info("已将 {} 个活动状态更新为'进行中'", toOngoing);
            }

            // 将已过结束时间的进行中活动设为"已结束"
            int toEnded = activityMapper.batchUpdateToEnded(now);
            if (toEnded > 0) {
                log.info("已将 {} 个活动状态更新为'已结束'", toEnded);
            }
        } catch (Exception e) {
            log.error("活动状态自动流转失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 手动触发活动状态更新（供管理员调用）
     * 
     * @return 更新的活动数量
     */
    public int[] triggerStatusUpdate() {
        LocalDateTime now = LocalDateTime.now();
        int toOngoing = activityMapper.batchUpdateToOngoing(now);
        int toEnded = activityMapper.batchUpdateToEnded(now);
        return new int[] { toOngoing, toEnded };
    }
}

