package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.NotificationService;
import com.volunteer.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 组织者通知管理控制器
 */
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "组织者端通知与催签管理")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 群发广播通知 (增强版 - 支持 targetGroup)
     */
    @PostMapping("/broadcast")
    @Operation(summary = "群发广播通知", description = "根据目标群体群发消息")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<String> broadcast(@RequestBody Map<String, Object> body) {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }

        Long activityId = Long.valueOf(body.get("activityId").toString());
        String targetGroup = body.get("targetGroup") != null ? body.get("targetGroup").toString() : "approved";
        String title = body.get("title") != null ? body.get("title").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;

        if (content == null || content.isBlank()) {
            return Result.error("通知内容不能为空");
        }

        return notificationService.broadcastWithGroup(activityId, targetGroup, title, content, organizerUserId);
    }

    /**
     * 一键催促签到
     */
    @PostMapping("/nudge")
    @Operation(summary = "一键催促签到", description = "给已通过但未签到的志愿者发送提醒")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<Integer> nudge(@RequestParam Long activityId) {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }
        return notificationService.nudge(activityId, organizerUserId);
    }

    /**
     * 获取通知历史统计
     */
    @GetMapping("/stats")
    @Operation(summary = "通知历史统计", description = "获取组织者发出通知的已读率等统计数据")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<Map<String, Object>> getStats() {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }
        return notificationService.getHistoryStats(organizerUserId);
    }

    /**
     * 获取通知发送历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "获取通知历史", description = "分页获取组织者发送的通知记录")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<com.volunteer.vo.NotificationHistoryVO>> getHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long organizerUserId = SecurityUtils.getUserId();
        if (organizerUserId == null) {
            return Result.unauthorized("请先登录");
        }
        return notificationService.getHistory(organizerUserId, page, size);
    }
}
